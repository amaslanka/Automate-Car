package pl.maslanka.automatecar.connectedpref;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.connectedpref.adapters.ItemAdapter;
import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.helperobjectsandinterfaces.PairObject;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.helperobjectsandinterfaces.Triplet;

/**
 * Created by Artur on 15.11.2016.
 */

public class AppsToLaunch extends AppCompatActivity implements Constants.SELECT_APPS_FRAGMENT {

    private ArrayList<Triplet<Long, String, Drawable>> mItemArray;
    private DragListView mDragListView;
    private ArrayList<String> appPackages;
    private LinkedList<PairObject<String, String>> appList;
    private ItemAdapter listAdapter;

    private static final String KEY_APP_LIST_WAS_SHOWING = "app_list_was_showing";
    private static final String KEY_FRAGMENT_WAS_SHOWING = "fragment_was_showing";
    private SelectAppsFragment selectAppsFragment;
    private boolean appListWasShowing;
    private boolean fragmentWasShowing;
    private Bundle savedInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drag_drop_list_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            savedInstance = savedInstanceState;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_apps_to_launch, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.add:
                if (selectAppsFragment == null) {
                    Logic.saveToInternalStorage(this, appList);
                    selectAppsFragment = new SelectAppsFragment();
                    getSupportFragmentManager().beginTransaction().add(android.R.id.content, selectAppsFragment, TAG_SELECT_APPS_FRAGMENT).commit();
                    return true;
                } else if (selectAppsFragment.getAppListCreatorStatus() == AsyncTask.Status.FINISHED) {
                    Logic.saveToInternalStorage(this, appList);
                    selectAppsFragment.startAppListCreator();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logic.saveToInternalStorage(this, appList);
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
        showDialogs();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selectAppsFragment != null) {
            if (selectAppsFragment.getAppList() != null) {
                savedInstanceState.putBoolean(KEY_APP_LIST_WAS_SHOWING, selectAppsFragment.getAppList().isShowing());
                savedInstanceState.putBoolean(KEY_FRAGMENT_WAS_SHOWING, selectAppsFragment.isVisible());
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void buildAndRefreshView() {

        mItemArray = new ArrayList<>();
        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        appPackages = new ArrayList<>();
        appList = Logic.readList(this);

        getFragment();

        mDragListView.setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                if (fromPosition != toPosition) {
                    PairObject<String, String> appInFromPosition = appList.get(fromPosition);

                    if (fromPosition > toPosition) {
                        appList.removeLastOccurrence(appInFromPosition);
                    } else {
                        appList.removeFirstOccurrence(appInFromPosition);
                    }

                    appList.add(toPosition, appInFromPosition);

                    for (PairObject<String, String> pair: appList) {
                        Log.d("element", pair.toString());
                    }
                }
            }
        });


        appPackages.addAll(Logic.getSharedPrefAppList(this));

        for (int i = 0; i < appList.size(); i++) {
            try {
                mItemArray.add(new Triplet<>(Long.valueOf(i),
                        appList.get(i).getName(),
                        getPackageManager().getApplicationIcon(appList.get(i).getPackageName())));
            } catch (PackageManager.NameNotFoundException e) {
                appPackages.remove(appList.get(i).getPackageName());
                appList.remove(i);
                i--;
            }
        }


        listAdapter = new ItemAdapter(this, mItemArray, R.layout.list_item, R.id.image, false);

        mDragListView.setLayoutManager(new LinearLayoutManager(this));
        mDragListView.setAdapter(listAdapter, true);
        mDragListView.setCanDragHorizontally(false);

    }


    protected void dismissDialogs() {
        if (selectAppsFragment != null) {
            if (selectAppsFragment.getAppList() != null)
                if (selectAppsFragment.getAppList().isShowing())
                    selectAppsFragment.getAppList().dismiss();

            if (selectAppsFragment.getDialog() != null)
                if (selectAppsFragment.getDialog().isShowing())
                    selectAppsFragment.getDialog().dismiss();

        }
    }

    protected void showDialogs() {
        if (selectAppsFragment != null) {
            if (selectAppsFragment.getAppListCreator() != null) {
                if (selectAppsFragment.getAppListCreatorStatus() == AsyncTask.Status.RUNNING)
                    selectAppsFragment.getDialog().show();

                if ((appListWasShowing && selectAppsFragment.getAppList() != null) || (fragmentWasShowing && selectAppsFragment.getAppList() != null))

                    selectAppsFragment.getAppList().show();
            }
        }
    }

    protected void getFragment() {
        if(savedInstance != null) {
            selectAppsFragment = (SelectAppsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SELECT_APPS_FRAGMENT);
            if (selectAppsFragment != null) {
                appListWasShowing = savedInstance.getBoolean(KEY_APP_LIST_WAS_SHOWING);
                fragmentWasShowing = savedInstance.getBoolean(KEY_FRAGMENT_WAS_SHOWING);
            }
        }
    }




}
