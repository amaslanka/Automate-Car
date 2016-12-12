package pl.maslanka.automatecar.services;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Set;

import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.callbackmessages.MessageForceAutoRotation;
import pl.maslanka.automatecar.utils.AppBroadcastReceiver;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 07.12.2016.
 */

public class FAutoRotationAccessibilityService extends AccessibilityService implements
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES {

    private static final String LOG_TAG = FAutoRotationAccessibilityService.class.getSimpleName();
    private static Set<String> rotationExcludedApps;
    private boolean forceAutoRotation;

    public static void setRotationExcludedApps(Set<String> newRotationExcludedApps) {
        rotationExcludedApps = newRotationExcludedApps;
        Log.d(LOG_TAG, "Set new rotation excluded apps list: " + rotationExcludedApps.toString());
    }

    @Override
    protected void onServiceConnected() {

        setRotationExcludedApps(Logic.getSharedPrefStringSet(this, KEY_ROTATION_EXCLUDED_APPS));

        forceAutoRotation = Logic.getSharedPrefBoolean(this, KEY_FORCE_AUTO_ROTATION,
                FORCE_AUTO_ROTATION_DEFAULT_VALUE);

        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(event == null) {
            Log.d(LOG_TAG, "Null event");
            return;
        }

        try {

            Log.d(LOG_TAG, event.getPackageName().toString());

            if (forceAutoRotation && AppBroadcastReceiver.isInCarAlreadyPerformed()) {
                if (rotationExcludedApps.contains(event.getPackageName().toString())) {

                    if (Logic.isMyServiceRunning(ForceAutoRotationService.class, this)) {

                        Log.d(LOG_TAG, "Accessibility service will stop forcing rotation " +
                                "app package from received event equals the one from excluded apps. " +
                                "Service has been running before.");

                        Intent autoRotation = new Intent(this, ForceAutoRotationService.class);
                        stopService(autoRotation);
                    }

                } else  {

                    if (!Logic.isMyServiceRunning(ForceAutoRotationService.class, this)) {

                        Log.d(LOG_TAG, "Accessibility service will start forcing rotation " +
                                "app package from received event equals the one from excluded apps. " +
                                "Service has not been running before.");

                        Intent autoRotation = new Intent(this, ForceAutoRotationService.class);
                        bindService(autoRotation, mConnection, BIND_AUTO_CREATE);
                        startService(autoRotation);
                    }

                }
            }

        } catch (Exception e) {
            Log.d(LOG_TAG, "unable to get package name");
        }

    }

    @Override
    public void onInterrupt() {

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            if (className.getClassName().equals(ForceAutoRotationService.class.getName())) {
                Log.d(LOG_TAG, "Service " + className.getClass().getSimpleName()
                        + " connected - posting message");
                EventBus.getDefault().post(new MessageForceAutoRotation(FAutoRotationAccessibilityService.this));
                FAutoRotationAccessibilityService.this.unbindService(mConnection);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
