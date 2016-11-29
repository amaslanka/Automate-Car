package pl.maslanka.automatecar.connectedpref;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.userinputfilter.EditTextIntegerPreference;
import pl.maslanka.automatecar.userinputfilter.InputFilterMinMax;
import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarConnectedFragment extends com.github.machinarius.preferencefragment.PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES,
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
    private CheckBoxPreference showCancelDialog;
    private EditTextIntegerPreference dialogTimeout;
    private CheckBoxPreference actionDialogTimeout;
    private Preference appsToLaunch;
    private EditTextIntegerPreference sleepTimes;
    private CheckBoxPreference maxVolume;
    private CheckBoxPreference playMusic;
    private Preference chooseMusicPlayer;
    private CheckBoxPreference showNavi;

    private MusicPlayerListCreator musicPlayerListCreator;
    private ProgressDialog dialog;
    private AlertDialog musicPlayerList;

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
        disableLockScreen = (CheckBoxPreference) findPreference(KEY_DISABLE_LOCK_SCREEN);
        forceAutoRotation = (CheckBoxPreference) findPreference(KEY_FORCE_AUTO_ROTATION);
        checkIfInPocket = (CheckBoxPreference) findPreference(KEY_CHECK_IF_IN_POCKET);
        checkWirelessPowerSupply = (CheckBoxPreference) findPreference(KEY_CHECK_WIRELESS_POWER_SUPPLY);
        checkNfcTag = (CheckBoxPreference) findPreference(KEY_CHECK_NFC_TAG);
        showCancelDialog = (CheckBoxPreference) findPreference(KEY_SHOW_CANCEL_DIALOG);
        dialogTimeout = (EditTextIntegerPreference) findPreference(KEY_DIALOG_TIMEOUT);
        actionDialogTimeout = (CheckBoxPreference) findPreference(KEY_ACTION_DIALOG_TIMEOUT);
        appsToLaunch = findPreference(KEY_APPS_TO_LAUNCH);
        sleepTimes = (EditTextIntegerPreference) findPreference(KEY_SLEEP_TIMES);
        maxVolume = (CheckBoxPreference) findPreference(KEY_MAX_VOLUME);
        playMusic = (CheckBoxPreference) findPreference(KEY_PLAY_MUSIC);
        chooseMusicPlayer = findPreference(KEY_CHOOSE_MUSIC_PLAYER);
        showNavi = (CheckBoxPreference) findPreference(KEY_SHOW_NAVI);
    }

    protected void setPreferencesFeatures() {

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

        chooseMusicPlayer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                musicPlayerListCreator = new MusicPlayerListCreator(PrefsCarConnectedFragment.this);
                musicPlayerListCreator.execute(getActivity());
                return false;
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
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public void showMusicPlayerList (AlertDialog musicPlayerList) {
        this.musicPlayerList = musicPlayerList;
        this.musicPlayerList.show();
    }

    public void dismissMusicPlayerList () {
        if (musicPlayerList != null) {
            if (musicPlayerList.isShowing()) {
                musicPlayerList.dismiss();
            }
        }
    }

    public AlertDialog getMusicPlayerList() {
        return musicPlayerList;
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public MusicPlayerListCreator getMusicPlayerListCreator() {
        return musicPlayerListCreator;
    }

    public AsyncTask.Status getMusicPlayerListCreatorStatus() {
        return musicPlayerListCreator.getStatus();
    }



}
