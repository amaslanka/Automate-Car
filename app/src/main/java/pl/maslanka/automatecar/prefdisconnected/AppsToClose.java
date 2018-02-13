package pl.maslanka.automatecar.prefdisconnected;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.AppAdapterItem;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.AppObject;
import pl.maslanka.automatecar.prefdisconnected.adapters.ItemAdapter;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 15.11.2016.
 */

public class AppsToClose extends AppCompatActivity implements Constants.SELECT_APPS_FRAGMENT,
        Constants.PREF_KEYS, Constants.FILE_NAMES {

    private ArrayList<AppAdapterItem> mItemArray;
    private DragListView mDragListView;
    private ArrayList<String> appPackages;
    private LinkedList<AppObject> appList;
    private ItemAdapter listAdapter;

    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final String KEY_APP_LIST_WAS_SHOWING = "app_list_was_showing";
    private SelectAppsFragment selectAppsFragment;
    private boolean appListWasShowing;
    private Bundle savedInstance;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apps_to_launch);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItemArray = new ArrayList<>();

        if (savedInstanceState != null)
            savedInstance = savedInstanceState;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLists();
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissDialogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildAndRefreshView();

        if (listAdapter == null)
            createAndSetNewAdapter();
        else
            notifyAdapterDataHasChanged();

        showDialogs();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selectAppsFragment != null && selectAppsFragment.getAppList() != null) {
                savedInstanceState.putBoolean(KEY_APP_LIST_WAS_SHOWING,
                        selectAppsFragment.getAppList().isShowing());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void buildAndRefreshView() {

        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        appPackages = new ArrayList<>();
        appPackages.addAll(Logic.getSharedPrefStringSet(this, KEY_APPS_TO_CLOSE));
        appList = Logic.readList(this, APPS_TO_CLOSE);

        setFloatingActionButton();
        getFragment();
        createDragListView();

    }

    protected void setFloatingActionButton() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectAppsFragment == null) {
                    saveLists();
                    selectAppsFragment = new SelectAppsFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(android.R.id.content, selectAppsFragment, TAG_SELECT_APPS_FRAGMENT_OUT_CAR)
                            .commit();
                } else if (selectAppsFragment.getAppListCreatorStatus() == AsyncTask.Status.FINISHED) {
                    saveLists();
                    selectAppsFragment.startAppListCreator();
                }
            }
        });
    }

    protected void getFragment() {
        try {

            selectAppsFragment = (SelectAppsFragment) getSupportFragmentManager()
                    .findFragmentByTag(TAG_SELECT_APPS_FRAGMENT_OUT_CAR);

            appListWasShowing = savedInstance.getBoolean(KEY_APP_LIST_WAS_SHOWING);

        } catch (NullPointerException ex) {
            Log.v(LOG_TAG, "No SelectAppsFragment yet!");
        }
    }

    protected void createDragListView() {
        mDragListView.setVerticalScrollBarEnabled(true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    AppObject appInFromPosition = appList.get(fromPosition);

                    if (fromPosition > toPosition) {
                        appList.removeLastOccurrence(appInFromPosition);
                    } else {
                        appList.removeFirstOccurrence(appInFromPosition);
                    }

                    appList.add(toPosition, appInFromPosition);

                }
            }
        });

        checkForAnyDeletedApps();
        addNewAppsToItemArray();

    }

    protected void checkForAnyDeletedApps() {
        for (int i = 0; i < mItemArray.size(); i++) {
            AppObject appListItem =
                    new AppObject(mItemArray.get(i).getName(), mItemArray.get(i).getPackageName(), null);

            if (!appList.contains(appListItem)) {
                mItemArray.remove(i);
                i--;
            }
            notifyAdapterDataHasChanged();
        }
    }

    protected void addNewAppsToItemArray() {
        for (int i = 0; i < appList.size(); i++) {
            try {
                long newIndex;

                if (mItemArray.size() > 0)
                    newIndex = getNewMaxIndex();
                else
                    newIndex = i;


                AppAdapterItem appItem = new AppAdapterItem(newIndex,
                        appList.get(i).getName(), appList.get(i).getPackageName(), null,
                        getPackageManager().getApplicationIcon(appList.get(i).getPackageName()));

                if (!mItemArray.contains(appItem)) {
                    mItemArray.add(appItem);
                }

            } catch (PackageManager.NameNotFoundException e) {
                deleteApp(i);
                i--;
            }
        }
        notifyAdapterDataHasChanged();
    }

    protected long getNewMaxIndex() {
        return Collections.max(mItemArray,
                new Comparator<AppAdapterItem>() {
            @Override
            public int compare(AppAdapterItem o1,
                               AppAdapterItem o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        }).getIndex() + 1;
    }

    protected void createAndSetNewAdapter() {
        listAdapter = new ItemAdapter(this, mItemArray, R.layout.apps_to_launch_item, R.id.image, false);

        mDragListView.setLayoutManager(new LinearLayoutManager(this));
        mDragListView.setAdapter(listAdapter, true);
    }

    public void deleteApp(int position) {
       /*
       * equals method checks only name and package name, so there is no point
       * of creating new AppAdapterItem with Index and Icon, because it is only
       * for deletion purposes.
       * */
        AppAdapterItem appItem = new AppAdapterItem(null,
                appList.get(position).getName(), appList.get(position).getPackageName(), null,
                null);

        appPackages.remove(appList.get(position).getPackageName());
        appList.remove(position);
        mItemArray.remove(appItem);

        notifyAdapterDataHasChanged();

    }

    public void notifyAdapterDataHasChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listAdapter != null)
                    listAdapter.notifyDataSetChanged();
            }
        });
    }

    protected void saveLists() {
        Logic.setSharedPrefStringSet(
                AppsToClose.this, new HashSet<>(appPackages), KEY_APPS_TO_CLOSE);
        Logic.saveListToInternalStorage(AppsToClose.this, appList, APPS_TO_CLOSE);
    }

    protected void dismissDialogs() {
        try {
            if (selectAppsFragment.getProgressDialog().isShowing())
                selectAppsFragment.getProgressDialog().dismiss();

            if (selectAppsFragment.getAppList().isShowing())
                selectAppsFragment.getAppList().dismiss();

        } catch (NullPointerException ex) {
            Log.v(LOG_TAG, "No AppList or ProgressDialog - no need to dismiss it");
        }
    }

    protected void showDialogs() {
        try {
            if (selectAppsFragment.getAppListCreatorStatus() == AsyncTask.Status.RUNNING)
                selectAppsFragment.getProgressDialog().show();

            if (appListWasShowing)
                selectAppsFragment.getAppList().show();

        } catch (NullPointerException ex) {
            Log.v(LOG_TAG, "No AppList or ProgressDialog - no need to show it");
        }
    }

}
