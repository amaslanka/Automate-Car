package pl.maslanka.automatecar.connectedpref;

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

import pl.maslanka.automatecar.MainActivity;
import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.connectedpref.adapters.ArrayAdapterWithIcon;
import pl.maslanka.automatecar.helperobjectsandinterfaces.PairObject;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 15.11.2016.
 */

public class SelectAppsFragment extends Fragment {

    private PackageManager pm;
    private List<ApplicationInfo> installedApps;
    private Map<String, Boolean> selectedApps;
    private List<String> appNames;
    private List<Drawable> appIcons;
    private List<String> appPackages;
    private Set<String> appsFromPrefs;
    private Set<String> appsFromPrefsBackup;
    private LinkedList<PairObject<String, String>> appsToSave;
    private ArrayAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog appList;
    private ProgressDialog dialog;
    private AppListCreator appListCreator;

    public ProgressDialog getDialog() {
        return dialog;
    }

    public AlertDialog getAppList() {
        return appList;
    }

    public AppListCreator getAppListCreator() {
        return appListCreator;
    }

    public AsyncTask.Status getAppListCreatorStatus() {
        return appListCreator.getStatus();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Log.d("Fragment", "onCreate");
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

            final Activity activity = params[0];
            selectedApps = new HashMap<>();
            appNames = new ArrayList<>();
            appIcons = new ArrayList<>();
            appPackages = new ArrayList<>();
            appsFromPrefs = new HashSet<>();
            appsFromPrefsBackup = new HashSet<>();

            installedApps = Logic.getListOfInstalledApps(activity);
            appsToSave = Logic.readList(activity);

            appsFromPrefs.addAll(Logic.getSharedPrefAppList(activity));
            appsFromPrefsBackup.addAll(appsFromPrefs);

            createAppListData();
            checkForUninstalledApps(activity);

            adapter = new ArrayAdapterWithIcon(appNames, appIcons, activity);

            setCheckStateToCheckBoxes();


            builder = new AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.select_apps))
                    .setAdapter(adapter, null)
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("appsFromPrefs", appsFromPrefs.toString());
                            Log.d("appsToSave", appsToSave.toString());
                            Log.d("appsToSave", "print");
                            for (PairObject<String, String> pair: appsToSave) {
                                Log.d("element", pair.toString());
                            }
                            Logic.setSharedPrefAppList(activity, appsFromPrefs);
                            Logic.saveToInternalStorage(activity, appsToSave);

                            ((AppsToLaunch)getActivity()).buildAndRefreshView();
                        }
                    })
                    .setNegativeButton(getResources().getString(android.R.string.cancel), null);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i("Method executed", "onCancelled()!");
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

            dialog.dismiss();
        }

        void createAndShowDialog() {
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getResources().getString(R.string.loading_app_list));
            dialog.setMessage(getResources().getString(R.string.loading_app_list));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
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

            Logic.setSharedPrefAppList(activity, appsFromPrefs);
        }

        void setCheckStateToCheckBoxes() {
            adapter.setCheckedTextViews(new CheckedTextView[appPackages.size()]);

            for (int i=0; i < adapter.getCheckedTextViews().length; i++) {
                if (getActivity() != null) {
                    adapter.getCheckedTextViews()[i] = new CheckedTextView(getActivity());

                    if (selectedApps.get(appPackages.get(i)) != null) {
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
