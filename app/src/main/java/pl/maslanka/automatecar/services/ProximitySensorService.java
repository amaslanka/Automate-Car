package pl.maslanka.automatecar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import pl.maslanka.automatecar.callbackmessages.MessageProximitySensor;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 13.12.2016.
 */

public class ProximitySensorService extends Service implements SensorEventListener,
        Constants.DEFAULT_VALUES, Constants.CALLBACK_ACTIONS {


    private final String LOG_TAG = this.getClass().getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    private SensorManager mSensorManager;
    private Sensor mProximity;
    private Context contextToCallback;
    private int carConnectedServiceStartId;

    public class LocalBinder extends Binder {
        public ProximitySensorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ProximitySensorService.this;
        }
    }

    @Subscribe
    public void onMessageProximitySensor(MessageProximitySensor event) {
        Log.d(LOG_TAG, "MessageProximitySensor received");
        this.contextToCallback = event.context;
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");

        EventBus.getDefault().register(this);

        HandlerThread thread = new HandlerThread("ProximitySensorService",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        mSensorManager.unregisterListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        carConnectedServiceStartId = intent.getIntExtra(START_ID, START_ID_NO_VALUE);
        return mBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        Log.d(LOG_TAG, "Proximity sensor distance = " + Float.toString(distance));
        if (distance < 2.5) {
            Logic.setProximityState(ProximityState.NEAR);
        } else {
            Logic.setProximityState(ProximityState.FAR);
        }

        if (contextToCallback instanceof CarConnectedService) {
            ((CarConnectedService) contextToCallback).callback(PROXIMITY_CHECK_COMPLETED,
                    carConnectedServiceStartId);
        }

        stopSelf();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}