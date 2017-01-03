package pl.maslanka.automatecar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.callbackmessages.MessageForceAutoRotation;

/**
 * Created by Artur on 01.12.2016.
 */

public class ForceAutoRotationService extends Service implements Constants.BROADCAST_NOTIFICATIONS,
        Constants.CALLBACK_ACTIONS, Constants.DEFAULT_VALUES {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    private LinearLayout orientationChanger;
    private Context contextToCallback;
    private int carConnectedServiceStartId;

    public class LocalBinder extends Binder {
        public ForceAutoRotationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ForceAutoRotationService.this;
        }
    }

    @Subscribe
    public void onMessageContext(MessageForceAutoRotation event) {
        Log.d(LOG_TAG, "MessageForceAutoRotation received");
        this.contextToCallback = event.context;

        enableAutoRotation();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        EventBus.getDefault().register(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        disableAutoRotation();
        EventBus.getDefault().unregister(this);
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        carConnectedServiceStartId = intent.getIntExtra(START_ID, START_ID_NO_VALUE);
        return mBinder;
    }

    public void enableAutoRotation() {
        try {
            orientationChanger = new LinearLayout(this);
            // Using TYPE_SYSTEM_OVERLAY is crucial to make window appear on top
            // Need the permission android.permission.SYSTEM_ALERT_WINDOW
            WindowManager.LayoutParams orientationLayout = new WindowManager
                    .LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.RGBA_8888);
            orientationLayout.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

            WindowManager wm = (WindowManager) this.getSystemService(Service.WINDOW_SERVICE);
            wm.addView(orientationChanger, orientationLayout);
            orientationChanger.setVisibility(View.VISIBLE);

            if (contextToCallback instanceof CarConnectedService) {
                ((CarConnectedService) contextToCallback).callback(FORCE_ROTATION_COMPLETED, carConnectedServiceStartId);
            }


        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "No needed permissions granted! Auto rotation will not work.");
            Toast.makeText(this, getString(R.string.no_system_overlay_permission), Toast.LENGTH_SHORT).show();
            if (contextToCallback instanceof CarConnectedService) {
                ((CarConnectedService) contextToCallback).callback(FORCE_ROTATION_COMPLETED, carConnectedServiceStartId);
            }
        }

    }

    public void disableAutoRotation() {
        try {
            if (orientationChanger != null) {
                orientationChanger.setVisibility(View.GONE);
                Log.d(LOG_TAG, "disabled auto rotation");
            }
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "No needed permissions granted! Auto rotation will not work.");
            Toast.makeText(this, getString(R.string.no_system_overlay_permission), Toast.LENGTH_LONG).show();
        }

    }


}
