package pl.maslanka.automatecar.prefconnected;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.provider.DocumentsContract;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.services.HelperAccessibilityService;
import pl.maslanka.automatecar.userinputfilter.EditTextIntegerPreference;
import pl.maslanka.automatecar.userinputfilter.InputFilterMinMax;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.utils.RootUtil;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarConnectedFragment extends com.github.machinarius.preferencefragment.PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES,
        Constants.APP_CREATOR_FRAGMENT {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private List<String[]> bluetoothDevicesArray;
    private String[] bluetoothDeviceNamesAndAddresses;
    private String[] bluetoothDeviceAddresses;

    private MultiSelectListPreference selectBluetoothDevices;
    private CheckBoxPreference checkNfcTag;
    private CheckBoxPreference checkWirelessPowerSupply;

    private CheckBoxPreference showCancelDialog;
    private EditTextIntegerPreference dialogTimeout;
    private CheckBoxPreference actionDialogTimeout;

    private Preference appsToLaunch;
    private EditTextIntegerPreference sleepTimes;

    private CheckBoxPreference playMusic;
    private Preference selectMusicPlayer;

    private CheckBoxPreference forceAutoRotation;
    private Preference rotationExcludedApps;

    private CheckBoxPreference checkIfInPocket;
    private CheckBoxPreference dismissLockScreen;
    private CheckBoxPreference showNavi;

    private ProgressDialog dialog;
    private MusicPlayerListCreator musicPlayerListCreator;
    private AlertDialog musicPlayerList;
    private RotationExcludedAppsListCreator rotationExcludedAppsListCreator;
    private AlertDialog rotationExcludedAppsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_car_connected);

        setRetainInstance(true);

        findPreferences();
        setPreferencesFeatures();

    }

    protected void findPreferences() {
        selectBluetoothDevices = (MultiSelectListPreference) findPreference(KEY_SELECT_BLUETOOTH_DEVICES);
        checkNfcTag = (CheckBoxPreference) findPreference(KEY_CHECK_NFC_TAG);
        checkWirelessPowerSupply = (CheckBoxPreference) findPreference(KEY_CHECK_WIRELESS_POWER_SUPPLY);
        showCancelDialog = (CheckBoxPreference) findPreference(KEY_SHOW_CANCEL_DIALOG);
        dialogTimeout = (EditTextIntegerPreference) findPreference(KEY_DIALOG_TIMEOUT);
        actionDialogTimeout = (CheckBoxPreference) findPreference(KEY_ACTION_DIALOG_TIMEOUT);
        appsToLaunch = findPreference(KEY_APPS_TO_LAUNCH);
        sleepTimes = (EditTextIntegerPreference) findPreference(KEY_SLEEP_TIMES);
        playMusic = (CheckBoxPreference) findPreference(KEY_PLAY_MUSIC);
        selectMusicPlayer = findPreference(KEY_SELECT_MUSIC_PLAYER);
        forceAutoRotation = (CheckBoxPreference) findPreference(KEY_FORCE_AUTO_ROTATION);
        rotationExcludedApps = findPreference(KEY_ROTATION_EXCLUDED_APPS);
        dismissLockScreen = (CheckBoxPreference) findPreference(KEY_DISMISS_LOCK_SCREEN);
        checkIfInPocket = (CheckBoxPreference) findPreference(KEY_CHECK_IF_IN_POCKET);
        showNavi = (CheckBoxPreference) findPreference(KEY_SHOW_NAVI);
    }

    protected void setPreferencesFeatures() {

        selectBluetoothDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    android.content.Intent enableIntent = new android.content.Intent(
                            android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableIntent);
                    selectBluetoothDevices.getDialog().dismiss();
                }
                return false;
            }
        });

        checkNfcTag.setEnabled(false);

        setDialogTimeoutFeatures();

        appsToLaunch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent appsToLaunch = new Intent(getActivity(), AppsToLaunch.class);
                getActivity().startActivity(appsToLaunch);
                return false;
            }
        });

        setSleepTimesFeatures();

        setMusicPlayerSummary(Logic.getSharedPrefString(getActivity(), KEY_SELECT_MUSIC_PLAYER, null));

        selectMusicPlayer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                musicPlayerListCreator = new MusicPlayerListCreator(PrefsCarConnectedFragment.this);
                musicPlayerListCreator.execute(getActivity());
                return false;
            }
        });

        forceAutoRotation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isNowChecked = Boolean.parseBoolean(newValue.toString());
                HelperAccessibilityService.setForceAutoRotation(isNowChecked);
                return true;
            }
        });

        rotationExcludedApps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                rotationExcludedAppsListCreator = new RotationExcludedAppsListCreator(PrefsCarConnectedFragment.this);
                rotationExcludedAppsListCreator.execute(getActivity());
                return false;
            }
        });

        dismissLockScreen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isNowChecked = Boolean.parseBoolean(newValue.toString());
                boolean isRooted = RootUtil.isDeviceRooted();
                Log.d(LOG_TAG, "isRooted" + Boolean.toString(isRooted));
                if (isNowChecked) {
                    return isRooted;
                }

                return true;
            }
        });

    }


    protected void refreshBluetoothDevicesList() {
        bluetoothDevicesArray = Logic.getBluetoothDevicesArrays();
        bluetoothDeviceNamesAndAddresses = bluetoothDevicesArray.get(0);
        bluetoothDeviceAddresses = bluetoothDevicesArray.get(1);
        selectBluetoothDevices.setEntries(bluetoothDeviceNamesAndAddresses);
        selectBluetoothDevices.setEntryValues(bluetoothDeviceAddresses);
    }

    protected void setMusicPlayerSummary(String summary) {
        PackageManager pm = getActivity().getPackageManager();
        if (summary == null) {
            selectMusicPlayer.setSummary(getString(R.string.no_player_selected));
        } else {
            try {
                selectMusicPlayer.setSummary(pm.getApplicationLabel(pm.getApplicationInfo(summary, 0)));
            } catch (PackageManager.NameNotFoundException e) {
                selectMusicPlayer.setSummary(summary);
            }
        }
    }


    protected void setDialogTimeoutFeatures() {
        dialogTimeout.getEditText().setFilters(new InputFilter[]{
                new InputFilterMinMax(DIALOG_DURATION_MIN_VALUE, DIALOG_DURATION_MAX_VALUE)});
        dialogTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                    android.preference.Preference preference, Object newValue) {
                if (newValue.toString().trim().equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.duration_time_null_alert),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
        dialogTimeout.setDialogMessage(
                String.format(getString(R.string.max_min_seconds_dialog_message),
                        DIALOG_DURATION_MIN_VALUE, DIALOG_DURATION_MAX_VALUE));
    }

    protected void setSleepTimesFeatures() {
        sleepTimes.getEditText().setFilters(new InputFilter[]{
                new InputFilterMinMax(SLEEP_TIMES_MIN_VALUE, SLEEP_TIMES_MAX_VALUE)});
        sleepTimes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                    android.preference.Preference preference, Object newValue) {
                if (newValue.toString().trim().equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.sleep_time_null_alert),
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });
        sleepTimes.setDialogMessage(
                String.format(getString(R.string.max_min_seconds_dialog_message),
                        SLEEP_TIMES_MIN_VALUE, SLEEP_TIMES_MAX_VALUE));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Fragment: onResume");
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


    public void showProgressDialog() {
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getResources().getString(R.string.loading_app_list));
        dialog.setMessage(getResources().getString(R.string.loading_app_list));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismissProgressDialog() {
        if (dialog != null)
            if (dialog.isShowing())
                dialog.dismiss();
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public void showMusicPlayerList (AlertDialog musicPlayerList) {
        this.musicPlayerList = musicPlayerList;
        this.musicPlayerList.show();
    }

    public void dismissMusicPlayerList () {
        if (musicPlayerList != null)
            if (musicPlayerList.isShowing())
                musicPlayerList.dismiss();
    }

    public AlertDialog getMusicPlayerList() {
        return musicPlayerList;
    }

    public MusicPlayerListCreator getMusicPlayerListCreator() {
        return musicPlayerListCreator;
    }

    public AsyncTask.Status getMusicPlayerListCreatorStatus() {
        return musicPlayerListCreator.getStatus();
    }



    public void showRotationExcludedAppsList (AlertDialog rotationExcludedAppsList) {
        this.rotationExcludedAppsList = rotationExcludedAppsList;
        this.rotationExcludedAppsList.show();
    }

    public AlertDialog getRotationExcludedAppsList() {
        return rotationExcludedAppsList;
    }

    public RotationExcludedAppsListCreator getRotationExcludedAppsListCreator() {
        return rotationExcludedAppsListCreator;
    }

    public AsyncTask.Status getRotationExcludedAppsListCreatorStatus() {
        return rotationExcludedAppsListCreator.getStatus();
    }


}
