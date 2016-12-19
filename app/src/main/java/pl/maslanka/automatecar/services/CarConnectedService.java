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

import java.util.LinkedList;

import pl.maslanka.automatecar.callbackmessages.MessageProximitySensor;
import pl.maslanka.automatecar.connected.PopupConnectedActivity;
import pl.maslanka.automatecar.callbackmessages.MessageForceAutoRotation;
import pl.maslanka.automatecar.callbackmessages.MessagePopupConnected;
import pl.maslanka.automatecar.helpers.CallbackService;
import pl.maslanka.automatecar.helpers.CarConnectedProcessState;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.utils.Actions;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.utils.MyApplication;

/**
 * Created by Artur on 22.11.2016.
 */

public class CarConnectedService extends CallbackService
        implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS, Constants.DEFAULT_VALUES,
        Constants.CALLBACK_ACTIONS, Application.ActivityLifecycleCallbacks {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private boolean forceAutoRotation;
    private boolean checkIfInPocket;
    private boolean showCancelDialog;
    private int dialogTimeout;
    private boolean actionDialogTimeout;
    private LinkedList<PairObject<String, String>> appList;
    private int sleepTimes;
    private boolean maxVolume;
    private boolean playMusic;
    private String musicPlayer;
    private boolean playMusicOnA2dp;
    private boolean dismissLockScreen;
    private boolean showNavi;
    private String action;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Override
    public void callback(String action, int startId) {
        Log.d(LOG_TAG, "Received callback - " + action);
        if (startId != START_ID_NO_VALUE) {
            switch (action) {
                case PROXIMITY_CHECK_COMPLETED:
                    sendBroadcastAction(FORCE_ROTATION_ACTION);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case FORCE_ROTATION_COMPLETED:
                    if (Logic.getProximityState() != ProximityState.NEAR) {
                        sendBroadcastAction(POPUP_CONNECTED_ACTION);
                        Logic.setStartWithProximityFarPerformed(true);
                    } else {
                        sendBroadcastAction(PLAY_MUSIC_ACTION);
                        Logic.setStartWithProximityFarPerformed(false);
                    }
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case POPUP_CONNECTED_FINISH_CONTINUE:
                    sendBroadcastAction(CONTINUE_CONNECTED_ACTION);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case POPUP_CONNECTED_FINISH_DISCONTINUE:
                    sendBroadcastAction(DISCONTINUE_CONNECTED_ACTION);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case LAUNCH_APPS_COMPLETED:
                    sendBroadcastAction(PLAY_MUSIC_ACTION);
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    stopSelf(startId);
                    break;
                case PLAY_MUSIC_COMPLETED:
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(startId));
                    Logic.setCarConnectedProcessState(CarConnectedProcessState.COMPLETED);
                    stopSelf(startId);
                    break;
            }
        } else {
            Log.d(LOG_TAG, "stopped! (stoppedSelf)");
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
                case PROXIMITY_CHECK_ACTION:
                    Logic.setCarConnectedProcessState(CarConnectedProcessState.PERFORMING);
                    Actions.proximityCheck(CarConnectedService.this, mConnection, msg.arg1);
                    break;
                case FORCE_ROTATION_ACTION:
                    Actions.startForcingAutoRotation(CarConnectedService.this, mConnection, msg.arg1);
                    break;
                case POPUP_CONNECTED_ACTION:
                    Actions.showConnectedPopup(CarConnectedService.this, msg.arg1);
                    break;
                case DISCONTINUE_CONNECTED_ACTION:
                    stopRunningService(CarConnectedService.this, ForceAutoRotationService.class);
                    restoreDefaultValues();
                    Log.d(LOG_TAG, "stopped! StopID: " + Integer.toString(msg.arg1));
                    stopSelf(msg.arg1);
                    break;
                case CONTINUE_CONNECTED_ACTION:
                    Actions.launchApps(CarConnectedService.this, msg.arg1);
                    break;
                case PLAY_MUSIC_ACTION:
                    Actions.playMusic(CarConnectedService.this, msg.arg1);
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
        mServiceHandler = new ServiceHandler(mServiceLooper);
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

    @Override
    public void onDestroy() {

    }


    protected void getPreferencesData() {
        forceAutoRotation = Logic.getSharedPrefBoolean(this, KEY_FORCE_AUTO_ROTATION_IN_CAR, FORCE_AUTO_ROTATION_IN_CAR_DEFAULT_VALUE);
        checkIfInPocket = Logic.getSharedPrefBoolean(this, KEY_CHECK_IF_IN_POCKET_IN_CAR, CHECK_IF_IN_POCKET_IN_CAR_DEFAULT_VALUE);
        showCancelDialog = Logic.getSharedPrefBoolean(this, KEY_SHOW_CANCEL_DIALOG_IN_CAR, SHOW_CANCEL_DIALOG_IN_CAR_DEFAULT_VALUE);
        dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(this, KEY_DIALOG_TIMEOUT_IN_CAR, Integer.toString(DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE)));
        actionDialogTimeout = Logic.getSharedPrefBoolean(this, KEY_ACTION_DIALOG_TIMEOUT_IN_CAR, ACTION_DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE);
        appList = Logic.readList(this);
        sleepTimes = Integer.parseInt(Logic.getSharedPrefString(this, KEY_SLEEP_TIMES_IN_CAR, Integer.toString(SLEEP_TIMES_IN_CAR_DEFAULT_VALUE)));
        playMusic = Logic.getSharedPrefBoolean(this, KEY_PLAY_MUSIC_IN_CAR, PLAY_MUSIC_IN_CAR_DEFAULT_VALUE);
        musicPlayer = Logic.getSharedPrefString(this, KEY_SELECT_MUSIC_PLAYER_IN_CAR, null);
        playMusicOnA2dp = Logic.getSharedPrefBoolean(this, KEY_PLAY_MUSIC_ON_A2DP_IN_CAR, PLAY_MUSIC_ON_A2DP_IN_CAR_DEFAULT_VALUE);
        dismissLockScreen = Logic.getSharedPrefBoolean(this, KEY_DISMISS_LOCK_SCREEN, DISMISS_LOCK_SCREEN_IN_CAR_DEFAULT_VALUE);
        showNavi = Logic.getSharedPrefBoolean(this, KEY_SHOW_NAVI_IN_CAR, SHOW_NAVI_IN_CAR_DEFAULT_VALUE);
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

                EventBus.getDefault().post(new MessageForceAutoRotation(CarConnectedService.this));
                CarConnectedService.this.unbindService(mConnection);

            } else if (className.getClassName().equals(ProximitySensorService.class.getName())) {

                Log.d(LOG_TAG, "Service " + className.getClass().getSimpleName()
                        + " connected - posting message");

                EventBus.getDefault().post(new MessageProximitySensor(CarConnectedService.this));
                CarConnectedService.this.unbindService(mConnection);

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
        if (activity instanceof PopupConnectedActivity) {
            Log.d(LOG_TAG, "Activity " + activity.getClass().getSimpleName() + " started. " +
                    "This service will post message to this activity.");
            EventBus.getDefault().post(new MessagePopupConnected(CarConnectedService.this));
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
        Logic.setCarConnectedProcessState(CarConnectedProcessState.NOT_STARTED);
        Logic.setProximityState(ProximityState.NOT_TESTED);
        Logic.setStartWithProximityFarPerformed(false);
    }

}


