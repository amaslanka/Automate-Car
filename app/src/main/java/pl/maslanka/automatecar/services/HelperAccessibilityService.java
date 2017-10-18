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

import pl.maslanka.automatecar.helpers.ConnectingProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.callbackmessages.MessageForceAutoRotation;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 07.12.2016.
 */

public class HelperAccessibilityService extends AccessibilityService implements
        Constants.PREF_KEYS, Constants.DEFAULT_VALUES {

    private static final String LOG_TAG = HelperAccessibilityService.class.getSimpleName();
    private static Set<String> rotationExcludedApps;
    private static boolean forceAutoRotation;

    public static void setRotationExcludedApps(Set<String> newRotationExcludedApps) {
        rotationExcludedApps = newRotationExcludedApps;
        Log.d(LOG_TAG, "Set new rotation excluded apps list: " + rotationExcludedApps.toString());
    }

    public static void setForceAutoRotation(boolean forceAutoRotation) {
        HelperAccessibilityService.forceAutoRotation = forceAutoRotation;
    }

    @Override
    protected void onServiceConnected() {

        Log.e(LOG_TAG, "onServiceConnected");

        setRotationExcludedApps(Logic.getSharedPrefStringSet(this, KEY_ROTATION_EXCLUDED_APPS_IN_CAR));

        setForceAutoRotation(Logic.getSharedPrefBoolean(this, KEY_FORCE_AUTO_ROTATION_IN_CAR,
                FORCE_AUTO_ROTATION_IN_CAR_DEFAULT_VALUE));

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

            Logic.setCurrentForegroundAppPackage(event.getPackageName().toString());

            if (forceAutoRotation
                    && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                    && Logic.getCarConnectedProcessState() == ConnectingProcessState.COMPLETED
                    && Logic.getCarDisconnectedProcessState() != ConnectingProcessState.PERFORMING) {
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
                EventBus.getDefault().post(new MessageForceAutoRotation(HelperAccessibilityService.this));
                HelperAccessibilityService.this.unbindService(mConnection);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
