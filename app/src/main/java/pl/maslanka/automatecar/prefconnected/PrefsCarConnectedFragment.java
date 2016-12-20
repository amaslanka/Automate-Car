package pl.maslanka.automatecar.prefconnected;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.prefother.MediaVolumeLevelPref;
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
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES {

    public static final int PERMISSIONS_REQUEST_PHONE_STATE = 3697;
    private final String LOG_TAG = this.getClass().getSimpleName();

    private List<String[]> bluetoothDevicesArray;
    private String[] bluetoothDeviceNamesAndAddresses;
    private String[] bluetoothDeviceAddresses;

    private MultiSelectListPreference triggerType;
    private MultiSelectListPreference selectBluetoothDevices;
    private Preference selectNfcTags;

    private CheckBoxPreference showCancelDialog;
    private EditTextIntegerPreference dialogTimeout;
    private CheckBoxPreference actionDialogTimeout;

    private CheckBoxPreference forceAutoRotation;
    private Preference rotationExcludedApps;

    private CheckBoxPreference changeWifiState;
    private SwitchPreference wifiEnable;

    private CheckBoxPreference changeMobileDataState;
    private SwitchPreference mobileDataEnable;

    private Preference appsToLaunch;
    private EditTextIntegerPreference sleepTimes;

    private CheckBoxPreference playMusic;
    private Preference selectMusicPlayer;
    private CheckBoxPreference playMusicOnA2dp;
    private CheckBoxPreference setMediaVolume;
    private MediaVolumeLevelPref mediaVolumeLevel;

    private CheckBoxPreference checkIfInPocket;
    private CheckBoxPreference showNavi;

    private ProgressDialog progressDialog;
    private MusicPlayerListCreator musicPlayerListCreator;
    private AlertDialog musicPlayerList;
    private RotationExcludedAppsListCreator rotationExcludedAppsListCreator;
    private AlertDialog rotationExcludedAppsList;
    private boolean triggerTypeDialogWasShowing;

    public CheckBoxPreference getChangeMobileDataState() {
        return changeMobileDataState;
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
        triggerType = (MultiSelectListPreference) findPreference(KEY_TRIGGER_TYPE_IN_CAR);
        selectBluetoothDevices = (MultiSelectListPreference) findPreference(KEY_BLUETOOTH_DEVICES_ADDRESSES_IN_CAR);
        selectNfcTags = findPreference(KEY_NFC_TAGS_IN_CAR);

        showCancelDialog = (CheckBoxPreference) findPreference(KEY_SHOW_CANCEL_DIALOG_IN_CAR);
        dialogTimeout = (EditTextIntegerPreference) findPreference(KEY_DIALOG_TIMEOUT_IN_CAR);
        actionDialogTimeout = (CheckBoxPreference) findPreference(KEY_ACTION_DIALOG_TIMEOUT_IN_CAR);

        forceAutoRotation = (CheckBoxPreference) findPreference(KEY_FORCE_AUTO_ROTATION_IN_CAR);
        rotationExcludedApps = findPreference(KEY_ROTATION_EXCLUDED_APPS_IN_CAR);

        changeWifiState = (CheckBoxPreference) findPreference(KEY_CHANGE_WIFI_STATE_IN_CAR);
        wifiEnable = (SwitchPreference) findPreference(KEY_WIFI_ENABLE_IN_CAR);

        changeMobileDataState = (CheckBoxPreference) findPreference(KEY_CHANGE_MOBILE_DATA_STATE_IN_CAR);
        mobileDataEnable = (SwitchPreference) findPreference(KEY_MOBILE_DATA_ENABLE_IN_CAR);

        appsToLaunch = findPreference(KEY_APPS_TO_LAUNCH_IN_CAR);
        sleepTimes = (EditTextIntegerPreference) findPreference(KEY_SLEEP_TIMES_IN_CAR);

        playMusic = (CheckBoxPreference) findPreference(KEY_PLAY_MUSIC_IN_CAR);
        selectMusicPlayer = findPreference(KEY_SELECT_MUSIC_PLAYER_IN_CAR);
        playMusicOnA2dp = (CheckBoxPreference) findPreference(KEY_PLAY_MUSIC_ON_A2DP_IN_CAR);
        setMediaVolume = (CheckBoxPreference) findPreference(KEY_SET_MEDIA_VOLUME_IN_CAR);
        mediaVolumeLevel = (MediaVolumeLevelPref) findPreference(KEY_MEDIA_VOLUME_LEVEL_IN_CAR);

        checkIfInPocket = (CheckBoxPreference) findPreference(KEY_CHECK_IF_IN_POCKET_IN_CAR);
        showNavi = (CheckBoxPreference) findPreference(KEY_SHOW_NAVI_IN_CAR);
    }

    protected void setPreferencesFeatures() {

        setSelectBluetoothDevicesFeatures();

        setSelectNfcTagsFeatures();

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

        setMusicPlayerSummary(Logic.getSharedPrefString(getActivity(), KEY_SELECT_MUSIC_PLAYER_IN_CAR, null));

        selectMusicPlayer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startMusicPlayerListCreator();
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
                startRotationExcludedAppsListCreator();
                return false;
            }
        });

        setWifiEnableText();
        setChangeMobileDataStateFeatures();
        setMobileDataEnableText();


    }

    protected void refreshBluetoothDevicesList() {
        bluetoothDevicesArray = Logic.getBluetoothDevicesArrays();
        bluetoothDeviceNamesAndAddresses = bluetoothDevicesArray.get(0);
        bluetoothDeviceAddresses = bluetoothDevicesArray.get(1);
        selectBluetoothDevices.setEntries(bluetoothDeviceNamesAndAddresses);
        selectBluetoothDevices.setEntryValues(bluetoothDeviceAddresses);
    }

    protected void setSelectBluetoothDevicesFeatures() {
        boolean isEnabled = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_IN_CAR)
                .contains(Constants.TRIGGER_BLUETOOTH);

        selectBluetoothDevices.setEnabled(isEnabled);

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
    }

    protected void setSelectNfcTagsFeatures() {
        boolean isEnabled = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_IN_CAR)
                .contains(Constants.TRIGGER_NFC);

        selectNfcTags.setEnabled(isEnabled);
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

    protected void startMusicPlayerListCreator() {
        musicPlayerListCreator = new MusicPlayerListCreator(PrefsCarConnectedFragment.this);
        musicPlayerListCreator.execute(getActivity());
    }

    protected void startRotationExcludedAppsListCreator() {
        rotationExcludedAppsListCreator = new RotationExcludedAppsListCreator(PrefsCarConnectedFragment.this);
        rotationExcludedAppsListCreator.execute(getActivity());
    }


    protected void setDialogTimeoutFeatures() {
        dialogTimeout.getEditText().setFilters(new InputFilter[]{
                new InputFilterMinMax(DIALOG_DURATION_IN_CAR_MIN_VALUE, DIALOG_DURATION_IN_CAR_MAX_VALUE)});
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
                        DIALOG_DURATION_IN_CAR_MIN_VALUE, DIALOG_DURATION_IN_CAR_MAX_VALUE));
    }

    protected void setSleepTimesFeatures() {
        sleepTimes.getEditText().setFilters(new InputFilter[]{
                new InputFilterMinMax(SLEEP_TIMES_IN_CAR_MIN_VALUE, SLEEP_TIMES_IN_CAR_MAX_VALUE)});
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
                        SLEEP_TIMES_IN_CAR_MIN_VALUE, SLEEP_TIMES_IN_CAR_MAX_VALUE));
    }

    protected void setWifiEnableText() {
        boolean isEnabled = Logic.getSharedPrefBoolean(getActivity(), KEY_WIFI_ENABLE_IN_CAR,
                WIFI_ENABLE_IN_CAR_DEFAULT_VALUE);

        if (isEnabled)
            wifiEnable.setTitle(getString(R.string.wifi_on));
        else
            wifiEnable.setTitle(getString(R.string.wifi_off));

    }

    protected void setChangeMobileDataStateFeatures() {
        boolean isDeviceRooted = RootUtil.isDeviceRooted();

        if (!isDeviceRooted) {
            changeMobileDataState.setEnabled(false);
            changeMobileDataState.setChecked(false);
        } else {
            changeMobileDataState.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isNowChecked = Boolean.parseBoolean(newValue.toString());
                    if (!isNowChecked) {
                        return true;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        boolean askRootPermissions = RootUtil.askRootPermissions();
                        Log.d(LOG_TAG, "askRootPermissions" + Boolean.toString(askRootPermissions));

                        boolean isReadPhoneStateGranted = ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_PHONE_STATE)
                                == PackageManager.PERMISSION_GRANTED;

                        Log.d(LOG_TAG, "isReadPhoneStateGranted" + Boolean.toString(isReadPhoneStateGranted));

                        if (isReadPhoneStateGranted && askRootPermissions) {
                            return true;
                        } else if (!isReadPhoneStateGranted) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    PERMISSIONS_REQUEST_PHONE_STATE);
                            return false;
                        }

                        return false;
                    }

                    return true;

                }
            });
        }

    }

    protected void setMobileDataEnableText() {
        boolean isEnabled = Logic.getSharedPrefBoolean(getActivity(), KEY_MOBILE_DATA_ENABLE_IN_CAR,
                KEY_MOBILE_DATA_ENABLE_IN_CAR_DEFAULT_VALUE);

        if (isEnabled)
            mobileDataEnable.setTitle(getString(R.string.mobile_data_on));
        else
            mobileDataEnable.setTitle(getString(R.string.mobile_data_off));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        switch (key) {
            case KEY_TRIGGER_TYPE_IN_CAR:
                boolean isTriggerBluetooth = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_IN_CAR)
                        .contains(Constants.TRIGGER_BLUETOOTH);
                boolean isTriggerNfc = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_IN_CAR)
                        .contains(Constants.TRIGGER_NFC);

                selectBluetoothDevices.setEnabled(isTriggerBluetooth);
                selectNfcTags.setEnabled(isTriggerNfc);

                break;
            case KEY_WIFI_ENABLE_IN_CAR:
                setWifiEnableText();

                break;
            case KEY_MOBILE_DATA_ENABLE_IN_CAR:
                setMobileDataEnableText();
                break;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    public void showNewProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getResources().getString(R.string.loading_app_list));
        progressDialog.setMessage(getResources().getString(R.string.loading_app_list));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
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

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }


    public void showMusicPlayerList (AlertDialog musicPlayerList) {
        this.musicPlayerList = musicPlayerList;
        this.musicPlayerList.show();
    }

    public void showMusicPlayerList () {
        if (musicPlayerList != null)
            musicPlayerList.show();
    }

    public void dismissMusicPlayerList () {
        if (musicPlayerList != null)
            if (musicPlayerList.isShowing())
                musicPlayerList.dismiss();
    }

    public AlertDialog getMusicPlayerList() {
        return musicPlayerList;
    }

    public AsyncTask.Status getMusicPlayerListCreatorStatus() {
        return musicPlayerListCreator != null ? musicPlayerListCreator.getStatus() : AsyncTask.Status.PENDING;
    }


    public void showRotationExcludedAppsList (AlertDialog rotationExcludedAppsList) {
        this.rotationExcludedAppsList = rotationExcludedAppsList;
        this.rotationExcludedAppsList.show();
    }

    public void showRotationExcludedAppsList () {
        if (rotationExcludedAppsList != null)
            rotationExcludedAppsList.show();
    }

    public void dismissRotationExcludedAppsList() {
        if (rotationExcludedAppsList != null)
            if (rotationExcludedAppsList.isShowing())
                rotationExcludedAppsList.dismiss();
    }

    public AlertDialog getRotationExcludedAppsList() {
        return rotationExcludedAppsList;
    }

    public AsyncTask.Status getRotationExcludedAppsListCreatorStatus() {
        return rotationExcludedAppsListCreator != null ?
                rotationExcludedAppsListCreator.getStatus() : AsyncTask.Status.PENDING;
    }

}
