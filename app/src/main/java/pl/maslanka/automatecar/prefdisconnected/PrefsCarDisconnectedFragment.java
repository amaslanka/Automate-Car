package pl.maslanka.automatecar.prefdisconnected;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.Toast;

import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.userinputfilter.EditTextIntegerPreference;
import pl.maslanka.automatecar.userinputfilter.InputFilterMinMax;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.utils.RootUtil;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarDisconnectedFragment extends com.github.machinarius.preferencefragment.PreferenceFragment
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

    private CheckBoxPreference waitForReconnection;
    private EditTextIntegerPreference waitTime;

    private CheckBoxPreference showDialogToConfirmNaviStop;
    private EditTextIntegerPreference dialogTimeout;
    private CheckBoxPreference cancelNaviOnDialogTimeout;

    private CheckBoxPreference pauseMusic;
    private Preference selectMusicPlayer;
    private CheckBoxPreference setMediaVolume;
    private MediaVolumeLevelPref mediaVolumeLevel;

    private CheckBoxPreference changeWifiState;
    private SwitchPreference wifiEnable;

    private CheckBoxPreference changeMobileDataState;
    private SwitchPreference mobileDataEnable;

    private Preference appsToClose;

    private CheckBoxPreference checkIfInPocket;
    private CheckBoxPreference showHomeScreen;

    private ProgressDialog progressDialog;
    private MusicPlayerListCreator musicPlayerListCreator;
    private AlertDialog musicPlayerList;
    private boolean triggerTypeDialogWasShowing;

    public CheckBoxPreference getChangeMobileDataState() {
        return changeMobileDataState;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_car_disconnected);

        setRetainInstance(true);

        findPreferences();
        setPreferencesFeatures();
        refreshBluetoothDevicesList();

    }


    protected void findPreferences() {
        triggerType = (MultiSelectListPreference) findPreference(KEY_TRIGGER_TYPE_OUT_CAR);
        selectBluetoothDevices = (MultiSelectListPreference) findPreference(KEY_BLUETOOTH_DEVICES_ADDRESSES_OUT_CAR);
        selectNfcTags = findPreference(KEY_NFC_TAGS_OUT_CAR);

        waitForReconnection = (CheckBoxPreference) findPreference(KEY_WAIT_FOR_RECONNECTION);
        waitTime = (EditTextIntegerPreference) findPreference(KEY_WAIT_TIME);

        showDialogToConfirmNaviStop = (CheckBoxPreference) findPreference(KEY_SHOW_DIALOG_TO_CONFIRM_NAVI_STOP);
        dialogTimeout = (EditTextIntegerPreference) findPreference(KEY_DIALOG_TIMEOUT_OUT_CAR);
        cancelNaviOnDialogTimeout = (CheckBoxPreference) findPreference(KEY_CANCEL_NAVI_ON_DIALOG_TIMEOUT);

        pauseMusic = (CheckBoxPreference) findPreference(KEY_PAUSE_MUSIC);
        selectMusicPlayer = findPreference(KEY_SELECT_MUSIC_PLAYER_OUT_CAR);
        setMediaVolume = (CheckBoxPreference) findPreference(KEY_SET_MEDIA_VOLUME_OUT_CAR);
        mediaVolumeLevel = (MediaVolumeLevelPref) findPreference(KEY_MEDIA_VOLUME_LEVEL_OUT_CAR);

        changeWifiState = (CheckBoxPreference) findPreference(KEY_CHANGE_WIFI_STATE_OUT_CAR);
        wifiEnable = (SwitchPreference) findPreference(KEY_WIFI_ENABLE_OUT_CAR);

        changeMobileDataState = (CheckBoxPreference) findPreference(KEY_CHANGE_MOBILE_DATA_STATE_OUT_CAR);
        mobileDataEnable = (SwitchPreference) findPreference(KEY_MOBILE_DATA_ENABLE_OUT_CAR);

        appsToClose = findPreference(KEY_APPS_TO_CLOSE);

        checkIfInPocket = (CheckBoxPreference) findPreference(KEY_CHECK_IF_IN_POCKET_OUT_CAR);
        showHomeScreen = (CheckBoxPreference) findPreference(KEY_SHOW_HOME_SCREEN);
    }

    protected void setPreferencesFeatures() {

        setSelectBluetoothDevicesFeatures();

        setSelectNfcTagsFeatures();

        setDialogTimeoutFeatures();

        appsToClose.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent appsToClose = new Intent(getActivity(), AppsToClose.class);
                getActivity().startActivity(appsToClose);
                return false;
            }
        });

        setMusicPlayerSummary(Logic.getSharedPrefString(getActivity(), KEY_SELECT_MUSIC_PLAYER_OUT_CAR, null));

        selectMusicPlayer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startMusicPlayerListCreator();
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
        boolean isEnabled = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_OUT_CAR)
                .contains(Constants.TRIGGER_BLUETOOTH);

        selectBluetoothDevices.setEnabled(isEnabled);

        selectBluetoothDevices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableIntent);
                    selectBluetoothDevices.getDialog().dismiss();
                }
                return false;
            }
        });
    }

    protected void setSelectNfcTagsFeatures() {
        boolean isEnabled = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_OUT_CAR)
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
        musicPlayerListCreator = new MusicPlayerListCreator(PrefsCarDisconnectedFragment.this);
        musicPlayerListCreator.execute(getActivity());
    }



    protected void setDialogTimeoutFeatures() {
        dialogTimeout.getEditText().setFilters(new InputFilter[]{
                new InputFilterMinMax(DIALOG_DURATION_OUT_CAR_MIN_VALUE, DIALOG_DURATION_OUT_CAR_MAX_VALUE)});
        dialogTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(
                    Preference preference, Object newValue) {
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
                        DIALOG_DURATION_OUT_CAR_MIN_VALUE, DIALOG_DURATION_OUT_CAR_MAX_VALUE));
    }

    protected void setWifiEnableText() {
        boolean isEnabled = Logic.getSharedPrefBoolean(getActivity(), KEY_WIFI_ENABLE_OUT_CAR,
                WIFI_ENABLE_OUT_CAR_DEFAULT_VALUE);

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
        boolean isEnabled = Logic.getSharedPrefBoolean(getActivity(), KEY_MOBILE_DATA_ENABLE_OUT_CAR,
                MOBILE_DATA_ENABLE_OUT_CAR_DEFAULT_VALUE);

        if (isEnabled)
            mobileDataEnable.setTitle(getString(R.string.mobile_data_on));
        else
            mobileDataEnable.setTitle(getString(R.string.mobile_data_off));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        switch (key) {
            case KEY_TRIGGER_TYPE_OUT_CAR:
                boolean isTriggerBluetooth = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_OUT_CAR)
                        .contains(Constants.TRIGGER_BLUETOOTH);
                boolean isTriggerNfc = Logic.getSharedPrefStringSet(getActivity(), KEY_TRIGGER_TYPE_OUT_CAR)
                        .contains(Constants.TRIGGER_NFC);

                selectBluetoothDevices.setEnabled(isTriggerBluetooth);
                selectNfcTags.setEnabled(isTriggerNfc);

                break;
            case KEY_WIFI_ENABLE_OUT_CAR:
                setWifiEnableText();

                break;
            case KEY_MOBILE_DATA_ENABLE_OUT_CAR:
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



}
