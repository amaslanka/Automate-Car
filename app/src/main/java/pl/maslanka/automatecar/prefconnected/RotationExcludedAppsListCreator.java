package pl.maslanka.automatecar.prefconnected;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.prefconnected.adapters.ArrayAdapterWithIcon;
import pl.maslanka.automatecar.services.HelperAccessibilityService;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 25.11.2016.
 */

public class RotationExcludedAppsListCreator extends AsyncTask<Activity, Void, Void> implements Constants.PREF_KEYS{

    private PreferenceFragment prefsCarConnectedFragment;


    private PackageManager pm;
    private List<ApplicationInfo> installedApps;
    private Map<String, Boolean> selectedApps;
    private List<String> appNames;
    private List<Drawable> appIcons;
    private List<String> appPackages;
    private Set<String> appsFromPrefs;
    private ArrayAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog appList;


    public RotationExcludedAppsListCreator(PreferenceFragment prefsCarConnectedFragment) {
        this.prefsCarConnectedFragment = prefsCarConnectedFragment;
    }

    @Override
    protected void onPreExecute() {
        ((PrefsCarConnectedFragment) prefsCarConnectedFragment).showProgressDialog();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(Activity... params) {

        final Activity activity = params[0];

        try {

            pm = activity.getPackageManager();
            selectedApps = new HashMap<>();
            appNames = new ArrayList<>();
            appIcons = new ArrayList<>();
            appPackages = new ArrayList<>();
            appsFromPrefs = new HashSet<>();

            installedApps = Logic.getListOfAllInstalledApps(activity);

            appsFromPrefs.addAll(Logic.getSharedPrefStringSet(activity, KEY_ROTATION_EXCLUDED_APPS_IN_CAR));

            createAppListData();
            checkForUninstalledApps(activity);

            adapter = new ArrayAdapterWithIcon(appNames, appPackages, appIcons, activity);

            setCheckStateToCheckBoxes(activity);

            builder = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.exclude_from_forcing_auto_rotation))
                    .setAdapter(adapter, null)
                    .setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("appsFromPrefs", appsFromPrefs.toString());
                            Logic.setSharedPrefStringSet(activity, appsFromPrefs, KEY_ROTATION_EXCLUDED_APPS_IN_CAR);
                            HelperAccessibilityService.setRotationExcludedApps(appsFromPrefs);
                        }
                    })
                    .setNegativeButton(activity.getResources().getString(android.R.string.cancel), null);
        }  catch (IllegalStateException ex) {
            Log.e("Error", "Fragment not attached to an activity - task cancelled");
            this.cancel(true);
        }


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
                } else {
                    appsFromPrefs.add(appPackages.get(position));
                }

            }
        });

        ((PrefsCarConnectedFragment) prefsCarConnectedFragment).showRotationExcludedAppsList(appList);

        ((PrefsCarConnectedFragment) prefsCarConnectedFragment).dismissProgressDialog();
    }


    private void createAppListData() {
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


    private void checkForUninstalledApps(Activity activity) {
        List<String> uninstalledApps = new ArrayList<>();
        for (String appFromPrefs: appsFromPrefs) {
            if (!appPackages.contains(appFromPrefs))
                uninstalledApps.add(appFromPrefs);
        }

        for (String uninstalledApp: uninstalledApps) {
            appsFromPrefs.remove(uninstalledApp);
        }

        Logic.setSharedPrefStringSet(activity, appsFromPrefs, KEY_ROTATION_EXCLUDED_APPS_IN_CAR);
    }

    private void setCheckStateToCheckBoxes(Activity activity) {
        adapter.setCheckedTextViews(new CheckedTextView[appPackages.size()]);

        for (int i=0; i < adapter.getCheckedTextViews().length; i++) {
            if (activity != null) {
                adapter.getCheckedTextViews()[i] = new CheckedTextView(activity);

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
