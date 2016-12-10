package pl.maslanka.automatecar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;

/**
 * Created by Artur on 01.12.2016.
 */

public class ForceAutoRotationService extends Service implements Constants.BROADCAST_NOTIFICATIONS {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private LinearLayout orientationChanger;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = null;


    public void startHandlerThread(){
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");

        startHandlerThread();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                enableAutoRotation(ForceAutoRotationService.this);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                disableAutoRotation(ForceAutoRotationService.this);
            }
        });
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");

        return null;
    }

    protected void enableAutoRotation(Context context) {
        try {
            orientationChanger = new LinearLayout(context);
            // Using TYPE_SYSTEM_OVERLAY is crucial to make window appear on top
            // Need the permission android.permission.SYSTEM_ALERT_WINDOW
            WindowManager.LayoutParams orientationLayout = new WindowManager
                    .LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.RGBA_8888);
            orientationLayout.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;

            WindowManager wm = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
            wm.addView(orientationChanger, orientationLayout);
            orientationChanger.setVisibility(View.VISIBLE);

            Log.d(LOG_TAG, "enabled auto rotation");

        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "No needed permissions granted! Auto rotation will not work.");
            Toast.makeText(context, getString(R.string.no_system_overlay_permission), Toast.LENGTH_SHORT).show();
        }

    }

    protected void disableAutoRotation(Context context) {
        try {
            if (orientationChanger != null) {
                orientationChanger.setVisibility(View.GONE);
                Log.d(LOG_TAG, "disabled auto rotation");
            }
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "No needed permissions granted! Auto rotation will not work.");
            Toast.makeText(context, getString(R.string.no_system_overlay_permission), Toast.LENGTH_LONG).show();
        }

    }

    protected void sendBroadcastAction(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }


}
