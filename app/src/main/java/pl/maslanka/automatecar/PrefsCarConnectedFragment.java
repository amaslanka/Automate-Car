package pl.maslanka.automatecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarConnectedFragment extends com.github.machinarius.preferencefragment.PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Constants.PREF_KEYS, Constants.DIALOG_DURATION_MAX_MIN_VALUES,
        Constants.APP_CREATOR_FRAGMENT {

    private List<String[]> bluetoothDevicesArray;
    private String[] bluetoothDeviceNamesAndAddresses;
    private String[] bluetoothDeviceAddresses;
    private MultiSelectListPreference selectBluetoothDevices;
    private CheckBoxPreference disableLockScreen;
    private CheckBoxPreference forceAutoRotation;
    private CheckBoxPreference checkIfInPocket;
    private CheckBoxPreference checkWirelessPowerSupply;
    private CheckBoxPreference checkNfcTag;
    private SwitchPreference showCancelDialog;
    private EditTextIntegerPreference dialogTimeout;
    private CheckBoxPreference actionDialogTimeout;
    private Preference appsToLaunch;
    private CheckBoxPreference showNavi;

    private PackageManager pm;
    private List<ApplicationInfo> installedApps;
    private Map<String, Boolean> selectedApps;
    private List<Drawable> appIcons;
    private List<String> appNames;
    private List<String> appPackages;
    private List<String> appsFromPrefs;
    private Set<String> appsToSave;
    private ArrayAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog appList;
    private ProgressDialog dialog;
    private AppListCreator appListCreator;

    public AlertDialog getAppList() {
        return appList;
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public AppListCreator getAppListCreator() {
        return appListCreator;
    }

    public AsyncTask.Status getAppListCreatorStatus() {
        return appListCreator.getStatus();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_car_connected);

        setRetainInstance(true);

        findPreferences();
        setPreferencesFeatures();
        refreshBluetoothDevicesList();

    }

    protected void findPreferences() {
        selectBluetoothDevices = (MultiSelectListPreference) findPreference(KEY_SELECT_BLUETOOTH_DEVICES);
        disableLockScreen = (CheckBoxPreference) findPreference(KEY_DISABLE_LOCK_SCREEN);
        forceAutoRotation = (CheckBoxPreference) findPreference(KEY_FORCE_AUTO_ROTATION);
        checkIfInPocket = (CheckBoxPreference) findPreference(KEY_CHECK_IF_IN_POCKET);
        checkWirelessPowerSupply = (CheckBoxPreference) findPreference(KEY_CHECK_WIRELESS_POWER_SUPPLY);
        checkNfcTag = (CheckBoxPreference) findPreference(KEY_CHECK_NFC_TAG);
        showCancelDialog = (SwitchPreference) findPreference(KEY_SHOW_CANCEL_DIALOG);
        dialogTimeout = (EditTextIntegerPreference) findPreference(KEY_DIALOG_TIMEOUT);
        actionDialogTimeout = (CheckBoxPreference) findPreference(KEY_ACTION_DIALOG_TIMEOUT);
        appsToLaunch = findPreference(KEYS_APPS_TO_LAUNCH);
        showNavi = (CheckBoxPreference) findPreference(KEY_SHOW_NAVI);
    }

    protected void setPreferencesFeatures() {

        checkNfcTag.setEnabled(false);

        setDialogTimeoutFeatures();

        appsToLaunch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                pm = getActivity().getPackageManager();
                appListCreator = new AppListCreator();
                appListCreator.execute(getActivity());
                return false;
            }
        });

    }


    protected void refreshBluetoothDevicesList() {
        bluetoothDevicesArray = MainActivity.logic.getBluetoothDevicesArrays();
        bluetoothDeviceNamesAndAddresses = bluetoothDevicesArray.get(0);
        bluetoothDeviceAddresses = bluetoothDevicesArray.get(1);
        selectBluetoothDevices.setEntries(bluetoothDeviceNamesAndAddresses);
        selectBluetoothDevices.setEntryValues(bluetoothDeviceAddresses);
    }


    protected void setDialogTimeoutFeatures() {
        dialogTimeout.getEditText().setFilters(new InputFilter[]{ new InputFilterMinMax(DIALOG_DURATION_MIN_VALUE, DIALOG_DURATION_MAX_VALUE)});
        dialogTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                    android.preference.Preference preference, Object newValue) {
                if (newValue.toString().trim().equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.duration_time_null_alert), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
        dialogTimeout.setDialogMessage(String.format(getString(R.string.duration_time_dialog_message), DIALOG_DURATION_MIN_VALUE, DIALOG_DURATION_MAX_VALUE));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("Fragment", "onResume");
        refreshBluetoothDevicesList();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }



    private class AppListCreator extends AsyncTask<Activity, Void, Void> {

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getResources().getString(R.string.loading_app_list));
            dialog.setMessage(getResources().getString(R.string.loading_app_list));
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Void doInBackground(Activity... params) {

            final Activity activity = params[0];

            installedApps = MainActivity.logic.getListOfInstalledApps();
            selectedApps = new HashMap<>();
            appIcons = new ArrayList<>();
            appNames = new ArrayList<>();
            appPackages = new ArrayList<>();
            appsFromPrefs = new ArrayList<>();
            appsToSave = new HashSet<>();

            appsFromPrefs.addAll(Logic.getSharedPrefAppList(activity));
            appsToSave.addAll(appsFromPrefs);

            Log.d("appsFromPrefs", appsFromPrefs.toString());

            createAppListData();
            checkForUninstalledApps(activity);

            adapter = new ArrayAdapterWithIcon(appNames, appIcons, activity);

            setCheckStateToCheckBoxes();

            builder = new AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.select_apps))
                    .setAdapter(adapter, null)
                    .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("appsToSave", appsToSave.toString());
                            Logic.setSharedPrefAppList(activity, appsToSave);
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
                    adapter.checkedTextViews[position].setChecked(!wasChecked);

                /*
                Add or remove app from the "appsToSave" set (depending on it has been checked or unchecked), which will be passed as an argument to save in shared prefs
                */
                    if (wasChecked) {
                        appsToSave.remove(appPackages.get(position));
                    } else {
                        appsToSave.add(appPackages.get(position));
                    }

                }
            });

            appList.show();

            dialog.dismiss();
        }

        void createAppListData() {
            for (int i = 0; i < installedApps.size(); i++) {
                appIcons.add(pm.getApplicationIcon(installedApps.get(i)));
                appNames.add(pm.getApplicationLabel(installedApps.get(i)).toString());
                appPackages.add(installedApps.get(i).packageName);

                if (appsFromPrefs.contains(appPackages.get(i))) {
                    selectedApps.put(appPackages.get(i), true);
                } else {
                    selectedApps.put(appPackages.get(i), false);
                }


            }
        }

        void checkForUninstalledApps(Activity activity) {
            for (int i=0; i < appsFromPrefs.size(); i++) {
                if (!appPackages.contains(appsFromPrefs.get(i))) {
                    String appToRemove = appsFromPrefs.get(i);
                    appsToSave.remove(appToRemove);
                    appsFromPrefs.remove(appToRemove);
                }
            }
            Logic.setSharedPrefAppList(activity, appsToSave);
        }

        void setCheckStateToCheckBoxes() {
            adapter.checkedTextViews = new CheckedTextView[appPackages.size()];

            for (int i=0; i < adapter.checkedTextViews.length; i++) {
                if (getActivity() != null) {
                    adapter.checkedTextViews[i] = new CheckedTextView(getActivity());

                    if (selectedApps.get(appPackages.get(i)) != null) {
                        adapter.checkedTextViews[i].setChecked(selectedApps.get(appPackages.get(i)));
                    } else {
                        adapter.checkedTextViews[i].setChecked(false);
                    }
                } else {
                    break;
                }
            }
        }
    }


}
