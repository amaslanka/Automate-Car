package pl.maslanka.automatecar.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;
import java.util.Set;

import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.utils.AppBroadcastReceiver;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 07.12.2016.
 */

public class FAutoRotationAccessibilityService extends AccessibilityService implements
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private Set<String> rotationExcludedApps;
    private boolean forceAutoRotation;

    @Override
    protected void onServiceConnected() {

        rotationExcludedApps = Logic.getSharedPrefStringSet(this, KEY_ROTATION_EXCLUDED_APPS);

        forceAutoRotation = Logic.getSharedPrefBoolean(this, KEY_FORCE_AUTO_ROTATION,
                FORCE_AUTO_ROTATION_DEFAULT_VALUE);

        rotationExcludedApps = new HashSet<>();

        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


        if(event == null) {
            Log.d(LOG_TAG, "Null event");
            return;
        }


        try {
            boolean shouldStopService = false;

            if (forceAutoRotation && AppBroadcastReceiver.isInCarAlreadyPerformed()) {
                for (String packageName: rotationExcludedApps) {
                    Log.d(LOG_TAG, "if true");
                    if (event.getPackageName().toString().equals(packageName)) {
                        Log.d(LOG_TAG, "app equals");
                        shouldStopService = true;
                    } else {
                        shouldStopService = false;
                    }
                }

                if (shouldStopService) {
                    Intent autoRotation = new Intent(this, ForceAutoRotationService.class);
                    stopService(autoRotation);
                } else {
                    Intent autoRotation = new Intent(this, ForceAutoRotationService.class);
                    startService(autoRotation);
                }

            }


        } catch (Exception e) {
            Log.d(LOG_TAG, "unable to get package name");
        }


    }

    @Override
    public void onInterrupt() {

    }
}
