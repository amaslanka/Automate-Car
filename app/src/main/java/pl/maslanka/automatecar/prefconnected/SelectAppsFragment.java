package pl.maslanka.automatecar.prefconnected;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.prefconnected.adapters.ArrayAdapterWithIcon;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 15.11.2016.
 */

public class SelectAppsFragment extends Fragment implements Constants.PREF_KEYS {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private PackageManager pm;
    private List<ApplicationInfo> installedApps;
    private Map<String, Boolean> selectedApps;
    private List<String> appNames;
    private List<Drawable> appIcons;
    private List<String> appPackages;
    private Set<String> appsFromPrefs;
    private LinkedList<PairObject<String, String>> appsToSave;
    private ArrayAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog appList;
    private ProgressDialog progressDialog;
    private AppListCreator appListCreator;

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public AlertDialog getAppList() {
        return appList;
    }

    public AsyncTask.Status getAppListCreatorStatus() {
        return appListCreator.getStatus();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Log.v(LOG_TAG, "Fragment - onCreate");
        pm = getActivity().getPackageManager();

        if (savedInstanceState == null) {
            startAppListCreator();
        }
    }

    public void startAppListCreator() {
        appListCreator = new AppListCreator();
        appListCreator.execute(getActivity());
    }


    private class AppListCreator extends AsyncTask<Activity, Void, Void> {

        @Override
        protected void onPreExecute() {
           createAndShowDialog();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Activity... params) {

            try {

                final Activity activity = params[0];
                selectedApps = new HashMap<>();
                appNames = new ArrayList<>();
                appIcons = new ArrayList<>();
                appPackages = new ArrayList<>();
                appsFromPrefs = new HashSet<>();

                installedApps = Logic.getListOfUserInstalledApps(activity);
                appsToSave = Logic.readList(activity);

                appsFromPrefs.addAll(Logic.getSharedPrefStringSet(activity, KEY_APPS_TO_LAUNCH_IN_CAR));

                createAppListData();
                checkForUninstalledApps(activity);

                adapter = new ArrayAdapterWithIcon(appNames, appPackages, appIcons, activity);

                setCheckStateToCheckBoxes();

                builder = new AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.select_apps))
                        .setAdapter(adapter, null)
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.v(LOG_TAG, "appsFromPrefs: " + appsFromPrefs.toString());
                                Log.v(LOG_TAG, "appsToSave - print: ");
                                for (PairObject<String, String> pair: appsToSave) {
                                    Log.v(LOG_TAG, "element: " + pair.toString());
                                }
                                Logic.setSharedPrefStringSet(activity, appsFromPrefs, KEY_APPS_TO_LAUNCH_IN_CAR);
                                Logic.saveListToInternalStorage(activity, appsToSave);

                                ((AppsToLaunch) getActivity()).buildAndRefreshView();
                                ((AppsToLaunch) getActivity()).notifyAdapterDataHasChanged();
                            }
                        })
                        .setNegativeButton(getResources().getString(android.R.string.cancel), null);
            } catch (IllegalStateException ex) {
                Log.e(LOG_TAG, "Error: Fragment not attached to an activity - task cancelled");
                progressDialog.dismiss();
                this.cancel(true);
                startAppListCreator();
            }


            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(LOG_TAG, "Method executed - onCancelled()!");
        }

        @Override
        protected void onPostExecute(Void parameter) {

            appList = builder.create();

            appList.getListView().setAdapter(adapter);
            appList.getListView().setItemsCanFocus(false);
            appList.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            appList.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.checked_text_view);
                    boolean wasChecked = checkedTextView.isChecked();

                    /*
                    Set CheckedTextView check-state & update view in adapter
                    */
                    checkedTextView.setChecked(!wasChecked);
                    adapter.getCheckedTextViews()[position].setChecked(!wasChecked);

                    /*
                    Add or remove app from the "appsFromPrefs" set (depending on it has been checked or unchecked), which will be passed as an argument to save in shared prefs
                    */
                    if (wasChecked) {
                        appsFromPrefs.remove(appPackages.get(position));
                        appsToSave.remove(new PairObject<>(appNames.get(position), appPackages.get(position)));
                    } else {
                        appsFromPrefs.add(appPackages.get(position));
                        appsToSave.add(new PairObject<>(appNames.get(position), appPackages.get(position)));
                    }

                }
            });

            appList.show();

            progressDialog.dismiss();
        }

        void createAndShowDialog() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(getResources().getString(R.string.loading_app_list));
            progressDialog.setMessage(getResources().getString(R.string.loading_app_list));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        void createAppListData() {
            for (int i = 0; i < installedApps.size(); i++) {
                appIcons.add(pm.getApplicationIcon(installedApps.get(i)));
                appNames.add(pm.getApplicationLabel(installedApps.get(i)).toString());
                appPackages.add(installedApps.get(i).packageName);

                // Check which boxes should be checked
                if (appsFromPrefs.contains(appPackages.get(i))) {
                    selectedApps.put(appPackages.get(i), true);
                } else {
                    selectedApps.put(appPackages.get(i), false);
                }

            }
        }


        void checkForUninstalledApps(Activity activity) {
            List<String> uninstalledApps = new ArrayList<>();
            for (String appFromPrefs: appsFromPrefs) {
                if (!appPackages.contains(appFromPrefs))
                    uninstalledApps.add(appFromPrefs);
            }

            for (String uninstalledApp: uninstalledApps) {
                appsFromPrefs.remove(uninstalledApp);
            }

            Logic.setSharedPrefStringSet(activity, appsFromPrefs, KEY_APPS_TO_LAUNCH_IN_CAR);
        }

        void setCheckStateToCheckBoxes() {
            adapter.setCheckedTextViews(new CheckedTextView[appPackages.size()]);

            for (int i=0; i < adapter.getCheckedTextViews().length; i++) {
                if (getActivity() != null) {
                    adapter.getCheckedTextViews()[i] = new CheckedTextView(getActivity());

                    if (selectedApps.containsKey(appPackages.get(i))) {
                        adapter.getCheckedTextViews()[i].setChecked(selectedApps.get(appPackages.get(i)));
                    } else {
                        adapter.getCheckedTextViews()[i].setChecked(false);
                    }
                } else {
                    break;
                }
            }
        }


    }
}
