package pl.maslanka.automatecar.connectedpref;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private SwitchPreference showCancelDialog;
    private EditTextIntegerPreference dialogTimeout;
    private CheckBoxPreference actionDialogTimeout;
    private Preference appsToLaunch;
    private CheckBoxPreference showNavi;



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
                Intent appsToLaunch = new Intent(getActivity(), AppsToLaunch.class);
                getActivity().startActivity(appsToLaunch);
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
                String.format(getString(R.string.duration_time_dialog_message),
                        DIALOG_DURATION_MIN_VALUE, DIALOG_DURATION_MAX_VALUE));
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


}
