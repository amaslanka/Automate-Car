package pl.maslanka.automatecar.services;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import pl.maslanka.automatecar.callbackmessages.MessageForceAutoRotation;
import pl.maslanka.automatecar.callbackmessages.MessagePopupDisconnected;
import pl.maslanka.automatecar.callbackmessages.MessageProximitySensor;
import pl.maslanka.automatecar.disconnected.PopupDisconnectedActivity;
import pl.maslanka.automatecar.helpers.CallbackService;
import pl.maslanka.automatecar.helpers.ConnectingProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.utils.Actions;
import pl.maslanka.automatecar.receivers.AppBroadcastReceiver;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.utils.MyApplication;

import static pl.maslanka.automatecar.utils.Logic.startServiceWithAction;

/**
 * Created by artur on 18.03.17.
 */

public class CarDisconnectedService  extends CallbackService
        implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS, Constants.DEFAULT_VALUES,
        Constants.CALLBACK_ACTIONS, Application.ActivityLifecycleCallbacks {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private String action;
    private Looper mServiceLooper;
    private CarDisconnectedService.ServiceHandler mServiceHandler;
    private static boolean canceled;


    public static boolean isCanceled() {
        return canceled;
    }

    public static void setCanceled(boolean canceled) {
        CarDisconnectedService.canceled = canceled;
    }

    @Override
    public void callback(String action, int startId) {
        Log.d(LOG_TAG, "Received callback - " + action);
        if (startId != START_ID_NO_VALUE && !canceled) {
            switch (action) {
                case WAIT_FOR_RECONNECTION_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            PROXIMITY_CHECK_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case PROXIMITY_CHECK_COMPLETED:
                    if (Logic.getProximityState() != ProximityState.NEAR) {
                        startServiceWithAction(getApplicationContext(),
                                POPUP_DISCONNECTED_ACTION, CarDisconnectedService.class);
                        Logic.setStartWithProximityFarPerformed(true);
                    } else {
                        boolean cancelNaviOnDialogTimeout = Logic.getSharedPrefBoolean(
                                getApplicationContext(), KEY_CANCEL_NAVI_ON_DIALOG_TIMEOUT, CANCEL_NAVI_ON_DIALOG_TIMEOUT_DEFAULT_VALUE);
                        if (cancelNaviOnDialogTimeout) {
                            Logic.startServiceWithAction(MyApplication.getAppContext(),
                                    NAVIGATION_CANCELLATION_ACTION, CarDisconnectedService.class);
                        } else {
                            Logic.startServiceWithAction(MyApplication.getAppContext(),
                                    TURN_SCREEN_OFF_ACTION, CarDisconnectedService.class);
                        }
                        Logic.setStartWithProximityFarPerformed(false);
                    }
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case POPUP_DISCONNECTED_FINISH_CONTINUE:
                    Logic.startServiceWithAction(MyApplication.getAppContext(),
                            NAVIGATION_CANCELLATION_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case POPUP_DISCONNECTED_FINISH_DISCONTINUE:
                    Logic.startServiceWithAction(MyApplication.getAppContext(),
                            TURN_SCREEN_OFF_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case NAVIGATION_CANCELLATION_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            TURN_SCREEN_OFF_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case TURN_SCREEN_OFF_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            PAUSE_MUSIC_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case PAUSE_MUSIC_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            SET_MEDIA_VOLUME_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case SET_MEDIA_VOLUME_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            CHANGE_WIFI_STATE_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case CHANGE_WIFI_STATE_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            CHANGE_MOBILE_DATA_STATE_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case CHANGE_MOBILE_DATA_STATE_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            CLOSE_APPS_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case CLOSE_APPS_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            STOP_FORCING_ROTATION_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case STOP_FORCING_ROTATION_COMPLETED:
                    Logic.startServiceWithAction(getApplicationContext(),
                            BACK_TO_HOME_SCREEN_ACTION, CarDisconnectedService.class);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case BACK_TO_HOME_SCREEN_COMPLETED:
                    AppBroadcastReceiver.restoreDefaultValues();
                    if (Logic.checkIfBtDeviceConnected(getApplicationContext())) {
                        CarConnectedService.setCanceled(false);
                        Logic.startServiceWithAction(getApplicationContext(),
                                PROXIMITY_CHECK_ACTION, CarConnectedService.class);
                    }
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;


            }
        } else {
            Log.e(LOG_TAG, "Process canceled! Service stopped! (stoppedSelf)");
            Logic.setCarDisconnectedProcessState(ConnectingProcessState.NOT_STARTED);
            setCanceled(false);
            if (!Logic.checkIfBtDeviceConnected(getApplicationContext())) {
                CarConnectedService.setCanceled(false);
                Logic.startServiceWithAction(getApplicationContext(),
                        PROXIMITY_CHECK_ACTION, CarDisconnectedService.class);
            }
            stopSelf();
        }

    }

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (action) {
                case WAIT_FOR_RECONNECTION_ACTION:
                    Logic.setCarDisconnectedProcessState(ConnectingProcessState.PERFORMING);
                    Actions.waitForReconnection(CarDisconnectedService.this, msg.arg1);
                    break;
                case PROXIMITY_CHECK_ACTION:
                    Actions.proximityCheck(CarDisconnectedService.this,  CarDisconnectedService.class, mConnection, msg.arg1);
                    break;
                case POPUP_DISCONNECTED_ACTION:
                    Actions.showDisconnectedPopup(CarDisconnectedService.this, msg.arg1);
                    break;
                case NAVIGATION_CANCELLATION_ACTION:
                    Actions.killNavigation(CarDisconnectedService.this, msg.arg1);
                    break;
                case TURN_SCREEN_OFF_ACTION:
                    Actions.turnScreenOff(CarDisconnectedService.this, msg.arg1);
                    break;
                case PAUSE_MUSIC_ACTION:
                    Actions.pauseMusic(CarDisconnectedService.this, msg.arg1);
                    break;
                case SET_MEDIA_VOLUME_ACTION:
                    Actions.setMediaVolume(CarDisconnectedService.this, CarDisconnectedService.class, msg.arg1);
                    break;
                case CHANGE_WIFI_STATE_ACTION:
                    Actions.changeWifiState(CarDisconnectedService.this, CarDisconnectedService.class, msg.arg1);
                    break;
                case CHANGE_MOBILE_DATA_STATE_ACTION:
                    Actions.changeMobileDataState(CarDisconnectedService.this, CarDisconnectedService.class, msg.arg1);
                    break;
                case CLOSE_APPS_ACTION:
                    Actions.killApps(CarDisconnectedService.this, msg.arg1);
                    break;
                case STOP_FORCING_ROTATION_ACTION:
                    Actions.stopForcingAutoRotation(CarDisconnectedService.this, msg.arg1);
                    break;
                case BACK_TO_HOME_SCREEN_ACTION:
                    Actions.showHomeScreen(CarDisconnectedService.this, msg.arg1);
                    break;
                default:
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(msg.arg1));
                    stopSelf(msg.arg1);
            }

        }
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new CarDisconnectedService.ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "started! StartID: " + Integer.toString(startId));

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        action = intent.getAction();
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }


    protected void stopRunningService(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.stopService(intent);
        Log.d(LOG_TAG, "Following service will be stopped - " + cls.getSimpleName());
    }

    protected void sendBroadcastAction(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        MyApplication.getAppContext().sendBroadcast(intent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            if (className.getClassName().equals(ForceAutoRotationService.class.getName())) {

                Log.d(LOG_TAG, "Service " + className.getClass().getSimpleName()
                        + " connected - posting message");

                EventBus.getDefault().post(new MessageForceAutoRotation(CarDisconnectedService.this));
                CarDisconnectedService.this.unbindService(mConnection);

            } else if (className.getClassName().equals(ProximitySensorService.class.getName())) {

                Log.d(LOG_TAG, "Service " + className.getClass().getSimpleName()
                        + " connected - posting message");

                EventBus.getDefault().post(new MessageProximitySensor(CarDisconnectedService.this));
                CarDisconnectedService.this.unbindService(mConnection);

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof PopupDisconnectedActivity) {
            Log.d(LOG_TAG, "Activity " + activity.getClass().getSimpleName() + " started. " +
                    "This service will post message to this activity.");
            EventBus.getDefault().post(new MessagePopupDisconnected(CarDisconnectedService.this));
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private void restoreDefaultValues() {
        Logic.setCarConnectedProcessState(ConnectingProcessState.NOT_STARTED);
        Logic.setProximityState(ProximityState.NOT_TESTED);
        Logic.setStartWithProximityFarPerformed(false);
    }

}
