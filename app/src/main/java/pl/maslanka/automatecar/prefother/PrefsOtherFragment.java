package pl.maslanka.automatecar.prefother;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.Log;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.utils.RootUtil;

/**
 * Created by Artur on 18.12.2016.
 */

public class PrefsOtherFragment extends com.github.machinarius.preferencefragment.PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES{

    private final String LOG_TAG = this.getClass().getSimpleName();

    private CheckBoxPreference dismissLockScreen;

    private CheckBoxPreference nfcDockTrigger;
    private CheckBoxPreference wirelessLoadingDockTrigger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_other);

        setRetainInstance(true);

        findPreferences();
        setPreferencesFeatures();

    }

    protected void findPreferences() {
        dismissLockScreen = (CheckBoxPreference) findPreference(KEY_DISMISS_LOCK_SCREEN);
        nfcDockTrigger = (CheckBoxPreference) findPreference(KEY_NFC_DOCK_TRIGGER);
        wirelessLoadingDockTrigger = (CheckBoxPreference) findPreference(KEY_WIRELESS_LOADING_DOCK_TRIGGER);

    }

    protected void setPreferencesFeatures() {

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


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Fragment: onResume");
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
