package pl.maslanka.automatecar.prefconnected;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.AppAdapterItem;
import pl.maslanka.automatecar.prefconnected.adapters.ItemAdapter;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.AppObject;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 15.11.2016.
 */

public class AppsToLaunch extends AppCompatActivity implements Constants.SELECT_APPS_FRAGMENT,
        Constants.PREF_KEYS, Constants.FILE_NAMES {

    private ArrayList<AppAdapterItem> mItemArray;
    private DragListView mDragListView;
    private ArrayList<String> appPackages;
    private LinkedList<AppObject> appList;
    private ItemAdapter listAdapter;

    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final String KEY_APP_LIST_WAS_SHOWING = "app_list_was_showing";
    private static final String KEY_ACTIVITY_LIST_WAS_SHOWING = "activity_list_was_showing";
    private static final String KEY_ACTIVITY_LIST_PACKAGE_NAME = "activity_list_package_name";
    private SelectAppsFragment selectAppsFragment;
    private boolean appListWasShowing;
    private boolean activityListWasShowing;
    private String activityListPackageName;
    private Bundle savedInstance;
    private FloatingActionButton fab;
    private ProgressDialog progressDialog;
    private AlertDialog activityList;
    private ActivityListCreator activityListCreator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apps_to_launch);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItemArray = new ArrayList<>();

        final PackageManager pm = getPackageManager();

        ArrayList<ActivityInfo> list = getAllRunningActivities(getApplicationContext());
        if (list != null) {
            for (ActivityInfo activityInfo: list) {
                Log.d(LOG_TAG, "activity: " + activityInfo.name + ", exported: " + activityInfo.exported + ", label: " + activityInfo.loadLabel(pm));
            }
        }

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

    public static ArrayList<ActivityInfo> getAllRunningActivities(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    "com.google.android.apps.maps", PackageManager.GET_ACTIVITIES);

            return new ArrayList<>(Arrays.asList(pi.activities));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (selectAppsFragment != null && selectAppsFragment.getAppList() != null) {
                savedInstanceState.putBoolean(KEY_APP_LIST_WAS_SHOWING,
                        selectAppsFragment.getAppList().isShowing());
        }
        if (activityList != null) {
            savedInstanceState.putBoolean(KEY_ACTIVITY_LIST_WAS_SHOWING,
                    activityList.isShowing());
        }
        savedInstanceState.putString(KEY_ACTIVITY_LIST_PACKAGE_NAME, activityListPackageName);
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void buildAndRefreshView() {

        mDragListView = (DragListView) findViewById(R.id.drag_list_view);
        appPackages = new ArrayList<>();
        appPackages.addAll(Logic.getSharedPrefStringSet(this, KEY_APPS_TO_LAUNCH_IN_CAR));
        appList = Logic.readList(this, APPS_TO_LAUNCH);

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
                            .add(android.R.id.content, selectAppsFragment, TAG_SELECT_APPS_FRAGMENT_IN_CAR)
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
                    .findFragmentByTag(TAG_SELECT_APPS_FRAGMENT_IN_CAR);

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
                    new AppObject(mItemArray.get(i).getName(), mItemArray.get(i).getPackageName(), mItemArray.get(i).getActivityName());

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
                
                

                Drawable icon = null;
                String activityLabel;
                String activitySimpleName = null;
                if (!TextUtils.isEmpty(appList.get(i).getActivityName())) {
                    String activityName = appList.get(i).getActivityName();
                    ActivityInfo activityInfo = getPackageManager().getActivityInfo(
                            new ComponentName(appList.get(i).getPackageName(), activityName), 0);
                    icon = activityInfo.loadIcon(getPackageManager());
                    activityLabel = activityInfo.loadLabel(getPackageManager()).toString();
                    if (!TextUtils.isEmpty(activityLabel)) {
                        activitySimpleName = activityLabel;
                    } else {
                        int lastDotIndex = activityName.lastIndexOf(".");
                        if (lastDotIndex + 1 < activityName.length()) {
                            activitySimpleName = activityName.substring(lastDotIndex + 1);
                        } else {
                            activitySimpleName = activityName;
                        }
                    }
                }
                if (icon == null) {
                    icon = getPackageManager().getApplicationIcon(appList.get(i).getPackageName());
                }

                if (activitySimpleName == null) {
                    activitySimpleName = appList.get(i).getActivityName();
                }

                AppAdapterItem appItem = new AppAdapterItem(newIndex,
                        appList.get(i).getName(), appList.get(i).getPackageName(), activitySimpleName,
                        icon);

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

    public AppObject getAppListElement(int position) {
        return appList.get(position);
    }

    public void setAppObjectActivity(String packageName, String activityLabel, String activityName, Drawable drawable) {
        if (!TextUtils.isEmpty(packageName)) {
            if (!TextUtils.isEmpty(activityName)) {
                for (AppObject appObject: appList) {
                    if (packageName.equals(appObject.getPackageName())) {
                        appObject.setActivityName(activityName);
                    }
                }
                for (AppAdapterItem appAdapterItem: mItemArray) {
                    if (packageName.equals(appAdapterItem.getPackageName())) {
                        String activitySimpleName;
                        if (!TextUtils.isEmpty(activityLabel)) {
                            activitySimpleName = activityLabel;
                        } else {
                            int lastDotIndex = activityName.lastIndexOf(".");
                            if (lastDotIndex + 1 < activityName.length()) {
                                activitySimpleName = activityName.substring(lastDotIndex + 1);
                            } else {
                                activitySimpleName = activityName;
                            }
                        }
                        appAdapterItem.setActivityName(activitySimpleName);
                        appAdapterItem.setDrawable(drawable);
                    }
                }
            } else {
                for (AppObject appObject: appList) {
                    if (packageName.equals(appObject.getPackageName())) {
                        appObject.setActivityName("");
                    }
                }
                for (AppAdapterItem appAdapterItem: mItemArray) {
                    if (packageName.equals(appAdapterItem.getPackageName())) {
                        appAdapterItem.setActivityName("");
                        try {
                            appAdapterItem.setDrawable(getPackageManager().getApplicationIcon(packageName));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            notifyAdapterDataHasChanged();
        }
    }

    public void deleteApp(int position) {
       /*
       * equals method checks only name and package name, so there is no point
       * of creating new AppAdapterItem with Index and Icon, because it is only
       * for deletion purposes.
       * */
        AppAdapterItem appItem = new AppAdapterItem(null,
                appList.get(position).getName(),
                appList.get(position).getPackageName(),
                appList.get(position).getActivityName(),
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
                AppsToLaunch.this, new HashSet<>(appPackages), KEY_APPS_TO_LAUNCH_IN_CAR);
        Logic.saveListToInternalStorage(AppsToLaunch.this, appList, APPS_TO_LAUNCH);
    }

    protected void dismissDialogs() {
        try {
            if (selectAppsFragment != null &&
                    selectAppsFragment.getProgressDialog() != null &&
                    selectAppsFragment.getProgressDialog().isShowing())
                selectAppsFragment.getProgressDialog().dismiss();

            if (selectAppsFragment != null &&
                    selectAppsFragment.getAppList() != null &&
                    selectAppsFragment.getAppList().isShowing())
                selectAppsFragment.getAppList().dismiss();

            if (activityList != null && activityList.isShowing())
                activityList.dismiss();

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

            activityListWasShowing = savedInstance.getBoolean(KEY_ACTIVITY_LIST_WAS_SHOWING);
            activityListPackageName = savedInstance.getString(KEY_ACTIVITY_LIST_PACKAGE_NAME);

            if (activityListWasShowing && !TextUtils.isEmpty(activityListPackageName))
                createActivityList(activityListPackageName);

        } catch (NullPointerException ex) {
            Log.v(LOG_TAG, "No AppList or ProgressDialog - no need to show it");
        }
    }

    public void createActivityList(String packageName) {
        activityListPackageName = packageName;
        activityListCreator = new ActivityListCreator(this);
        activityListCreator.execute(packageName);
    }

    public void showNewProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.loading_activities));
        progressDialog.setMessage(getResources().getString(R.string.loading_activities));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }


    public void showActivityList (AlertDialog activityList) {
        this.activityList = activityList;
        this.activityList.show();
    }

    public void showProgressDialog() {
        if (progressDialog != null)
            progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null)
            if (progressDialog.isShowing())
                progressDialog.dismiss();
    }


}
