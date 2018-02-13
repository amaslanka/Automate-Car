package pl.maslanka.automatecar.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import pl.maslanka.automatecar.connected.PopupConnectedActivity;
import pl.maslanka.automatecar.disconnected.PopupDisconnectedActivity;
import pl.maslanka.automatecar.helpers.CallbackService;
import pl.maslanka.automatecar.helpers.ConnectingProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.AppObject;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.helpers.TurnScreenOnActivity;
import pl.maslanka.automatecar.services.CarConnectedService;
import pl.maslanka.automatecar.services.CarDisconnectedService;
import pl.maslanka.automatecar.services.ForceAutoRotationService;
import pl.maslanka.automatecar.services.ProximitySensorService;

/**
 * Created by Artur on 10.12.2016.
 */

public class Actions implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS,
        Constants.CALLBACK_ACTIONS, Constants.DEFAULT_VALUES, Constants.FILE_NAMES {

    private static final String LOG_TAG = Actions.class.getSimpleName();
    private static final String A2DP_IDENTIFIER_NAME = "bluetooth_a2dp_audio_route_name";


    public static void proximityCheck(Context context, Class<?> callingClass, ServiceConnection mConnection, int startId) {

        boolean checkIfInPocket = false;

        if (callingClass != null && context != null) {
            if (callingClass.getName().equals(CarConnectedService.class.getName())) {
                checkIfInPocket = Logic.getSharedPrefBoolean(context,
                        KEY_CHECK_IF_IN_POCKET_IN_CAR, CHECK_IF_IN_POCKET_IN_CAR_DEFAULT_VALUE);
            } else if (callingClass.getName().equals(CarDisconnectedService.class.getName())) {
                checkIfInPocket = Logic.getSharedPrefBoolean(context,
                        KEY_CHECK_IF_IN_POCKET_OUT_CAR, CHECK_IF_IN_POCKET_OUT_CAR_DEFAULT_VALUE);
            }
        }

        if (checkIfInPocket) {
            bindNewService(context, ProximitySensorService.class, mConnection, startId);
        } else if (context instanceof CallbackService) {
            ((CallbackService) context).callback(PROXIMITY_CHECK_COMPLETED, startId);
        }

    }

    public static void startForcingAutoRotation(Context context, ServiceConnection mConnection,
                                                int startId) {

        boolean forceAutoRotation = Logic.getSharedPrefBoolean(context,
                KEY_FORCE_AUTO_ROTATION_IN_CAR, FORCE_AUTO_ROTATION_IN_CAR_DEFAULT_VALUE);

        if (forceAutoRotation) {
            bindNewService(context, ForceAutoRotationService.class, mConnection, startId);
        } else if (context instanceof CallbackService) {
            ((CallbackService) context).callback(FORCE_ROTATION_COMPLETED, startId);
        }

    }

    public static void stopForcingAutoRotation(Context context, int startId) {

        context.stopService(new Intent(context, ForceAutoRotationService.class));

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(STOP_FORCING_ROTATION_COMPLETED, startId);
        }
    }

    private static void bindNewService(Context context, Class<?> cls, ServiceConnection mConnection,
                                       int startId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(START_ID, startId);
        context.startService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public static void showConnectedPopup(Context context, int startId) {

        boolean showCancelDialog = Logic.getSharedPrefBoolean(
                context, KEY_SHOW_CANCEL_DIALOG_IN_CAR, SHOW_CANCEL_DIALOG_IN_CAR_DEFAULT_VALUE);
        int dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_DIALOG_TIMEOUT_IN_CAR, Integer.toString(DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE)));
        boolean actionDialogTimeout = Logic.getSharedPrefBoolean(
                context, KEY_ACTION_DIALOG_TIMEOUT_IN_CAR, ACTION_DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE);

        if (showCancelDialog) {
            Intent popup = new Intent(context, PopupConnectedActivity.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtra(START_ID, startId);
            popup.putExtra(KEY_DIALOG_TIMEOUT_IN_CAR, dialogTimeout);
            popup.putExtra(KEY_ACTION_DIALOG_TIMEOUT_IN_CAR, actionDialogTimeout);
            context.startActivity(popup);
        } else if (context instanceof CallbackService){
            ((CallbackService) context).callback(POPUP_CONNECTED_FINISH_CONTINUE, startId);
        }
    }

    public static void launchApps(Context context, int startId) {

        LinkedList<AppObject> appList = Logic.readList(context, APPS_TO_LAUNCH);
        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES_IN_CAR, Integer.toString(SLEEP_TIMES_IN_CAR_DEFAULT_VALUE)));

        Log.d(LOG_TAG, "continue - launchApps");


        if (appList.size() > 0 && !Logic.isScreenOn(context)) {
            Log.d(LOG_TAG, "screen is turned off - will be invoked.");
            Intent turnScreenOn = new Intent(context, TurnScreenOnActivity.class);
            turnScreenOn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(turnScreenOn);
        }

        try {
            Thread.sleep(1000);
            for (AppObject app: appList) {

                if (!TextUtils.isEmpty(app.getActivityName())) {
                    Log.d(LOG_TAG, "LAUNCH ACTIVITY: packageName: " + app.getPackageName() + " activity: " + app.getActivityName());
                    launchActivity(context, app.getPackageName(), app.getActivityName());
                } else {
                    Log.d(LOG_TAG, "launchIntentFromPackage ACTIVITY: packageName: " + app.getPackageName() + " activity: " + app.getActivityName());
                    launchIntentFromPackage(context, app.getPackageName());
                }

                Thread.sleep(sleepTimes*1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (context instanceof CallbackService){
            ((CallbackService) context).callback(LAUNCH_APPS_COMPLETED, startId);
        }

    }

    public static void changeWifiState(Context context, Class<?> callingClass, int startId) {

        boolean changeWifiState = false;
        boolean wifiEnable = false;

        if (callingClass != null && context != null) {
            if (callingClass.getName().equals(CarConnectedService.class.getName())) {
                changeWifiState = Logic.getSharedPrefBoolean(context,
                        KEY_CHANGE_WIFI_STATE_IN_CAR, CHANGE_WIFI_STATE_IN_CAR_DEFAULT_VALUE);
                wifiEnable = Logic.getSharedPrefBoolean(context,
                        KEY_WIFI_ENABLE_IN_CAR, WIFI_ENABLE_IN_CAR_DEFAULT_VALUE);
            } else if (callingClass.getName().equals(CarDisconnectedService.class.getName())) {
                changeWifiState = Logic.getSharedPrefBoolean(context,
                        KEY_CHANGE_WIFI_STATE_OUT_CAR, CHANGE_WIFI_STATE_OUT_CAR_DEFAULT_VALUE);
                wifiEnable = Logic.getSharedPrefBoolean(context,
                        KEY_WIFI_ENABLE_OUT_CAR, WIFI_ENABLE_OUT_CAR_DEFAULT_VALUE);
            }
        }

        if (changeWifiState) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(wifiEnable);
        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(CHANGE_WIFI_STATE_COMPLETED, startId);
        }
    }

    public static void changeMobileDataState(Context context, Class<?> callingClass, int startId) {

        boolean changeMobileDataState = false;
        boolean mobileDataEnable = false;

        if (callingClass != null && context != null) {
            if (callingClass.getName().equals(CarConnectedService.class.getName())) {
                changeMobileDataState = Logic.getSharedPrefBoolean(context,
                        KEY_CHANGE_MOBILE_DATA_STATE_IN_CAR, CHANGE_MOBILE_DATA_STATE_IN_CAR_DEFAULT_VALUE);
                mobileDataEnable = Logic.getSharedPrefBoolean(context,
                        KEY_MOBILE_DATA_ENABLE_IN_CAR, MOBILE_DATA_ENABLE_IN_CAR_DEFAULT_VALUE);
            } else if (callingClass.getName().equals(CarDisconnectedService.class.getName())) {
                changeMobileDataState = Logic.getSharedPrefBoolean(context,
                        KEY_CHANGE_MOBILE_DATA_STATE_OUT_CAR, CHANGE_MOBILE_DATA_STATE_OUT_CAR_DEFAULT_VALUE);
                mobileDataEnable = Logic.getSharedPrefBoolean(context,
                        KEY_MOBILE_DATA_ENABLE_OUT_CAR, MOBILE_DATA_ENABLE_OUT_CAR_DEFAULT_VALUE);
            }
        }

        int mobileDataEnableAsInt = (mobileDataEnable) ? 1 : 0;


        if (changeMobileDataState && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                int isDataOn = Settings.Global.getInt(context.getContentResolver(), "mobile_data");
                Log.v(LOG_TAG, "isMobileDataOn: " + Integer.toString(isDataOn));
                Log.v(LOG_TAG, "mobileDataEnablePreference: " + Integer.toString(mobileDataEnableAsInt));

                if (isDataOn != mobileDataEnableAsInt) {
                    Logic.setMobileDataStateFromLollipop(context, mobileDataEnableAsInt);
                }

            } catch (Settings.SettingNotFoundException e) {
                Log.e(LOG_TAG, "Setting not found!" + e);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to change mobile data state!" + e);
                e.printStackTrace();
            }

        } else if (changeMobileDataState) {
            boolean isDataOn = Logic.getMobileDataStateBelowLollipop(context);
            Log.v(LOG_TAG, "isMobileDataOn: " + Boolean.toString(isDataOn));
            Log.v(LOG_TAG, "mobileDataEnablePreference: " + Boolean.toString(mobileDataEnable));

            if (isDataOn != mobileDataEnable) {
                Logic.setMobileDataStateBelowLollipop(context, mobileDataEnable);
            }
        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(CHANGE_MOBILE_DATA_STATE_COMPLETED, startId);
        }
    }

    public static void setMediaVolume(Context context, Class<?> callingClass, int startId) {

        boolean setMediaVolume = false;
        int volumeLevel = 0;

        if (callingClass != null && context != null) {
            if (callingClass.getName().equals(CarConnectedService.class.getName())) {
                setMediaVolume = Logic.getSharedPrefBoolean(context,
                        KEY_SET_MEDIA_VOLUME_IN_CAR, SET_MEDIA_VOLUME_IN_CAR_DEFAULT_VALUE);
                volumeLevel = Integer.parseInt(Logic.getSharedPrefString(context,
                        KEY_MEDIA_VOLUME_LEVEL_IN_CAR, Integer.toString(MEDIA_VOLUME_LEVEL_IN_CAR_DEFAULT_VALUE)));
            } else if (callingClass.getName().equals(CarDisconnectedService.class.getName())) {
                setMediaVolume = Logic.getSharedPrefBoolean(context,
                        KEY_SET_MEDIA_VOLUME_OUT_CAR, SET_MEDIA_VOLUME_OUT_CAR_DEFAULT_VALUE);
                volumeLevel = Integer.parseInt(Logic.getSharedPrefString(context,
                        KEY_MEDIA_VOLUME_LEVEL_OUT_CAR, Integer.toString(MEDIA_VOLUME_LEVEL_OUT_CAR_DEFAULT_VALUE)));
            }
        }

        if (setMediaVolume) {

            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0);

            Log.e(LOG_TAG, "setMediaVolume: " + volumeLevel);

            /*
            Fix for Android 4.3 and higher:
            When system media volume level is set to specified value but NO MEDIA WAS PLAYED, then
            volume levels (system media and bluetooth a2dp media) are not fixed!

            In such situation changing only system media level will not change bluetooth a2dp volume!
            That's why we need to change a2dp volume separately AFTER media stream is routed to
            bluetooth a2dp.
             */

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                    && callingClass.getName().equals(CarConnectedService.class.getName())) {

                waitForBluetoothA2dpRoute(context);

                Log.e(LOG_TAG, "setMediaVolume one more time: " + volumeLevel);
                //Set the volume level once more for bluetooth a2dp media
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0);
            }

        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(SET_MEDIA_VOLUME_COMPLETED, startId);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static void waitForBluetoothA2dpRoute(Context context) {
        final int MAX_LOOP_NUMBER = 100;
        final int SLEEP_BETWEEN_CHECK = 100;
        int counter = 0;

        MediaRouter mediaRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        CharSequence selectedRouteDesc = mediaRouter.
                getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO).getDescription();
        String a2dpRouteSystemDesc = Resources.getSystem().getString(Resources.getSystem()
                .getIdentifier(A2DP_IDENTIFIER_NAME, "string", "android"));

        try {
            while (counter < MAX_LOOP_NUMBER && (selectedRouteDesc == null ||
                    !selectedRouteDesc.toString().equals(a2dpRouteSystemDesc))) {

                Log.v(LOG_TAG, "Waiting for Bluetooth A2DP route... ");

                Thread.sleep(SLEEP_BETWEEN_CHECK);

                counter++;
                selectedRouteDesc = mediaRouter.
                        getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO).getDescription();

            }

            if (counter == MAX_LOOP_NUMBER)
                Log.e(LOG_TAG, "Bluetooth A2DP is still not the selected route!");

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static boolean checkIfA2dp(Context context) {
        final int MAX_LOOP_NUMBER = 50;
        final int SLEEP_BETWEEN_CHECK = 100;
        int counter = 0;

        MediaRouter mediaRouter = (MediaRouter) context.getSystemService(Context.MEDIA_ROUTER_SERVICE);
        CharSequence selectedRouteDesc = mediaRouter.
                getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO).getDescription();
        String a2dpRouteSystemDesc = Resources.getSystem().getString(Resources.getSystem()
                .getIdentifier(A2DP_IDENTIFIER_NAME, "string", "android"));

        try {
            while (counter < MAX_LOOP_NUMBER && (selectedRouteDesc == null ||
                    !selectedRouteDesc.toString().equals(a2dpRouteSystemDesc))) {

                Log.v(LOG_TAG, "Waiting for Bluetooth A2DP route... ");

                Thread.sleep(SLEEP_BETWEEN_CHECK);

                counter++;
                selectedRouteDesc = mediaRouter.
                        getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO).getDescription();

                if (a2dpRouteSystemDesc.equals(selectedRouteDesc)) {
                    return true;
                }

            }

            if (a2dpRouteSystemDesc.equals(selectedRouteDesc)) {
                return true;
            }

            if (counter == MAX_LOOP_NUMBER)
                Log.e(LOG_TAG, "Bluetooth A2DP is still not the selected route!");
            return false;

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public static void playMusic(Context context, int startId) {

        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES_IN_CAR, Integer.toString(SLEEP_TIMES_IN_CAR_DEFAULT_VALUE)));
        boolean playMusic = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC_IN_CAR, PLAY_MUSIC_IN_CAR_DEFAULT_VALUE);
        boolean playMusicOnA2dp = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC_ON_A2DP_IN_CAR,
                PLAY_MUSIC_ON_A2DP_IN_CAR_DEFAULT_VALUE);
        String musicPlayerPackageName = Logic.getSharedPrefString(context, KEY_SELECT_MUSIC_PLAYER_IN_CAR, null);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            if (playMusic && musicPlayerPackageName != null) {

                ActivityInfo musicPlayerInfo = getMusicPlayerInfo(context, musicPlayerPackageName);

                ComponentName component =
                        new ComponentName(musicPlayerInfo.packageName, musicPlayerInfo.name);

                Log.d(LOG_TAG, "broadcastName: " + musicPlayerInfo.name);

                if (playMusicOnA2dp) {
                    waitForA2dp(manager);
                    checkForA2dpAndPlayOnComponentIfTrue(manager, context, component, sleepTimes);
                } else {
                    playOnComponentWithoutCheckingA2dp(context, component, manager, sleepTimes);
                }

            } else if (playMusic) {

                Log.d(LOG_TAG, "musicPlayer: no Music Player defined");

                if (playMusicOnA2dp) {
                    waitForA2dp(manager);
                    checkForA2dpAndPlayIfTrue(manager, context);
                } else {
                    startPlayingMusic(context);
                }

            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }

        if (context instanceof CallbackService){
            ((CallbackService) context).callback(PLAY_MUSIC_COMPLETED, startId);
        }
    }

    private static ActivityInfo getMusicPlayerInfo(Context context, String musicPlayerPackageName) {
        List<ActivityInfo> musicPlayersActivityInfo =
                Logic.getListOfMediaBroadcastReceivers(context);
        ActivityInfo musicPlayerInfo;

        for (ActivityInfo activityInfo: musicPlayersActivityInfo) {
            if (activityInfo.packageName.equals(musicPlayerPackageName)) {
                musicPlayerInfo = activityInfo;
                return musicPlayerInfo;
            }
        }

        return new ActivityInfo();
    }

    private static void waitForA2dp(AudioManager manager) {
        final int MAX_LOOP_NUMBER = 100;
        final int SLEEP_BETWEEN_CHECK = 100;
        int counter = 0;

        try {
            while (counter < MAX_LOOP_NUMBER && !manager.isBluetoothA2dpOn()) {
                Log.v(LOG_TAG, "Waiting for Bluetooth A2DP profile becoming ready...");
                Thread.sleep(SLEEP_BETWEEN_CHECK);
                counter++;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

    private static void checkForA2dpAndPlayOnComponentIfTrue(AudioManager manager, Context context,
                                                             ComponentName component, int sleepTimes) throws InterruptedException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (checkIfA2dp(context)) {
                playOnComponentWithoutCheckingA2dp(context, component, manager, sleepTimes);
            } else {
                Log.e(LOG_TAG, "Bluetooth A2DP is still turned off! Music will not be played!");
            }
        } else {
            if (manager.isBluetoothA2dpOn()) {
                Log.d(LOG_TAG, "Bluetooth A2DP is turned on!");
                playOnComponentWithoutCheckingA2dp(context, component, manager, sleepTimes);
            } else {
                Log.e(LOG_TAG, "Bluetooth A2DP is still turned off! Music will not be played!");
            }
        }
    }

    private static void playOnComponentWithoutCheckingA2dp(Context context, ComponentName component,
                                                           AudioManager manager, int sleepTimes) throws InterruptedException {
        startPlayingMusicOnComponent(context, component);
        Log.v(LOG_TAG, "Waiting for music to play...");
        Thread.sleep(Constants.WAIT_FOR_MUSIC_PLAY);
        if (!manager.isMusicActive())
            retryPlayingMusicOnComponent(context, sleepTimes*1000, component);
    }

    private static void checkForA2dpAndPlayIfTrue(AudioManager manager, Context context) throws InterruptedException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (checkIfA2dp(context)) {
                startPlayingMusic(context);
            } else {
                Log.e(LOG_TAG, "Bluetooth A2DP is still turned off! Music will not be played!");
            }
        } else {
            if (manager.isBluetoothA2dpOn()) {
                Log.d(LOG_TAG, "Bluetooth A2DP is turned on!");
                startPlayingMusic(context);
            } else {
                Log.e(LOG_TAG, "Bluetooth A2DP is still turned off! Music will not be played!");
            }
        }
    }


    private static void startPlayingMusicOnComponent(Context context, ComponentName component)
            throws InterruptedException {
        sendOrderedButtonEvent(context, component, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
        sendOrderedButtonEvent(context, component, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY);
    }

    private static void pausePlayingMusicOnComponent(Context context, ComponentName component)
            throws InterruptedException {
        sendOrderedButtonEvent(context, component, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
        sendOrderedButtonEvent(context, component, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE);
    }

    private static void retryPlayingMusicOnComponent(Context context, int sleepAfterComponentLaunch,
                                                     ComponentName component) throws InterruptedException {
        Log.d(LOG_TAG, "Music still not active! Launching music player to retry to play music.");
        launchIntentFromPackage(context, component.getPackageName());
        Thread.sleep(sleepAfterComponentLaunch);
        startPlayingMusicOnComponent(context, component);
    }

    private static void launchActivity(Context context, String packageName, String activityName) {
        try {
            if (activityName.endsWith("FreeNavCreateShortcutActivity")) {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("google.navigation:/?free=1&mode=d&entry=fnls"));
                context.startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setComponent(new ComponentName(packageName, activityName));
                context.startActivity(intent);
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Cannot start activity: " + packageName + ", " + activityName + ", reason: " + ex.getMessage());
        }
    }

    private static void launchIntentFromPackage(Context context, String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        } else {
            Log.e("AppLaunchingError", "Package " + packageName + " not found!");
        }
    }

    private static void startPlayingMusic(Context context) throws InterruptedException {
        sendButtonEvent(context, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
        sendButtonEvent(context, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY);
    }

    private static void pausePlayingMusic(Context context) throws InterruptedException {
        sendButtonEvent(context, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE);
        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
        sendButtonEvent(context, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE);
    }

    private static void sendOrderedButtonEvent(Context context, ComponentName component, int action, int keyEvent) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, keyEvent));
        context.sendOrderedBroadcast(intent, null);
    }

    private static void sendButtonEvent(Context context, int action, int keyEvent) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, keyEvent));
        context.sendBroadcast(intent);
    }

    public static void showNavi(Context context, int startId) {

        final String NAVI_SERVICE_NAME = "com.google.android.apps.gmm.navigation.service.base.NavigationService";
        boolean showNavi = Logic.getSharedPrefBoolean(context,
                KEY_SHOW_NAVI_IN_CAR, SHOW_NAVI_IN_CAR_DEFAULT_VALUE);

        if (showNavi && Logic.getProximityState() != ProximityState.NEAR) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceInfo.service.getClassName().equals(NAVI_SERVICE_NAME)) {
                    Log.v(LOG_TAG, "Show navigation performing...");
                    launchIntentFromPackage(context, serviceInfo.service.getPackageName());
                }
            }
        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(SHOW_NAVI_COMPLETED, startId);
        }
    }

    public static void dismissLockScreen(Context context) {

        boolean dismissLockScreen = Logic.getSharedPrefBoolean(context,
                KEY_DISMISS_LOCK_SCREEN, DISMISS_LOCK_SCREEN_IN_CAR_DEFAULT_VALUE);


        KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);

        //Dismiss lock screen only when accessibility service provides info that it is on top
        //Check also whether device is locked or other system ui event is shown
        try {

            if (Logic.getCarConnectedProcessState() != ConnectingProcessState.NOT_STARTED
                    && Logic.getCurrentForegroundAppPackage().equals(Constants.SYSTEM_UI_PACKAGE_NAME)
                    && keyguardManager.inKeyguardRestrictedInputMode()
                    && dismissLockScreen) {

                    Log.d(LOG_TAG, "Dismiss lock screen key event sent!");
                    Process process1 = Runtime.getRuntime()
                            .exec(Constants.DISMISS_LOCK_SCREEN_SU_COMMAND);
                    Process process2 = Runtime.getRuntime()
                            .exec(Constants.DISMISS_LOCK_SCREEN_SU_COMMAND_ALTERNATIVE);

                }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Device not rooted! Dismiss lock screen action cannot be done!");
            e.printStackTrace();
        }

    }

    public static void waitForReconnection(Context context, int startId) {
        boolean shouldWait = Logic.getSharedPrefBoolean(context, Constants.PREF_KEYS.KEY_WAIT_FOR_RECONNECTION, WAIT_FOR_RECONNECTION_DEFAULT_VALUE);
        if (shouldWait) {
            int waitTime = Integer.parseInt(Logic.getSharedPrefString(context, KEY_WAIT_TIME, String.valueOf(WAIT_TIME_DEFAULT_VALUE)));
            try {
                Thread.sleep(waitTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(WAIT_FOR_RECONNECTION_COMPLETED, startId);
        }
    }

    public static void showDisconnectedPopup(Context context, int startId) {

        final String NAVI_SERVICE_NAME = "com.google.android.apps.gmm.navigation.service.base.NavigationService";

        boolean showCancelDialog = Logic.getSharedPrefBoolean(
                context, KEY_SHOW_DIALOG_TO_CONFIRM_NAVI_STOP, SHOW_DIALOG_TO_CONFIRM_NAVI_STOP_DEFAULT_VALUE);
        int dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_DIALOG_TIMEOUT_OUT_CAR, Integer.toString(DIALOG_TIMEOUT_OUT_CAR_DEFAULT_VALUE)));
        boolean cancelNaviOnDialogTimeout = Logic.getSharedPrefBoolean(
                context, KEY_CANCEL_NAVI_ON_DIALOG_TIMEOUT, CANCEL_NAVI_ON_DIALOG_TIMEOUT_DEFAULT_VALUE);
        boolean naviServiceWorking = false;

        if (Logic.getProximityState() != ProximityState.NEAR) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceInfo.service.getClassName().equals(NAVI_SERVICE_NAME)) {
                    Log.v(LOG_TAG, "Navigation service is working - showing dialog...");
                    naviServiceWorking = true;
                }
            }
        }

        if (showCancelDialog && naviServiceWorking) {
            Intent popup = new Intent(context, PopupDisconnectedActivity.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtra(START_ID, startId);
            popup.putExtra(KEY_DIALOG_TIMEOUT_OUT_CAR, dialogTimeout);
            popup.putExtra(KEY_CANCEL_NAVI_ON_DIALOG_TIMEOUT, cancelNaviOnDialogTimeout);
            context.startActivity(popup);
        } else if (context instanceof CallbackService){
            ((CallbackService) context).callback(POPUP_DISCONNECTED_FINISH_DISCONTINUE, startId);
        }
    }

    public static void killNavigation(Context context, int startId) {
        String packageName = "com.google.android.apps.maps";

        boolean isDeviceRooted = RootUtil.isDeviceRooted();

        try {
            if (isDeviceRooted) {
                Process suProcess = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                os.writeBytes("adb shell" + "\n");
                os.flush();
                os.writeBytes("am force-stop " + packageName + "\n");
                os.flush();
            } else {
                ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(packageName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(Constants.CALLBACK_ACTIONS.NAVIGATION_CANCELLATION_COMPLETED, startId);
        }
    }

    public static void turnScreenOff(final Context context, int startId) {

        boolean turnScreenOff = Logic.getSharedPrefBoolean(context,
                KEY_TURN_SCREEN_OFF_OUT_CAR, TURN_SCREEN_OFF_OUT_CAR_DEFAULT_VALUE);
        int screenOffTimeout = SCREEN_OFF_TIMEOUT_DEFAULT_VALUE;
        boolean isScreenOn = Logic.isScreenOn(context);

        if (turnScreenOff && isScreenOn) {
            try {
                int systemScreenOffTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
                if (systemScreenOffTimeout < 1000) {
                    //screen timeout was probably set by this app
                    screenOffTimeout = Logic.getSharedPrefInt(context, USER_SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT_DEFAULT_VALUE);
                } else {
                    Logic.setSharedPrefInt(context, systemScreenOffTimeout, USER_SCREEN_OFF_TIMEOUT);
                    screenOffTimeout = systemScreenOffTimeout;
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 100);
            final int screenOffTimeoutFinal = screenOffTimeout;
            BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(LOG_TAG, "Screen off! Restoring screen timeout: " + screenOffTimeoutFinal);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, screenOffTimeoutFinal);
                    context.getApplicationContext().unregisterReceiver(this);
                }
            };
            context.getApplicationContext().registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(Constants.CALLBACK_ACTIONS.TURN_SCREEN_OFF_COMPLETED, startId);
        }
    }

    public static void pauseMusic(Context context, int startId) {

        boolean pauseMusic = Logic.getSharedPrefBoolean(context, KEY_PAUSE_MUSIC, PAUSE_MUSIC_DEFAULT_VALUE);
        String musicPlayerPackageName = Logic.getSharedPrefString(context, KEY_SELECT_MUSIC_PLAYER_OUT_CAR, null);

        try {
            if (pauseMusic && musicPlayerPackageName != null) {

                ActivityInfo musicPlayerInfo = getMusicPlayerInfo(context, musicPlayerPackageName);

                ComponentName component =
                        new ComponentName(musicPlayerInfo.packageName, musicPlayerInfo.name);

                Log.d(LOG_TAG, "broadcastName: " + musicPlayerInfo.name);
                pausePlayingMusicOnComponent(context, component);

            } else if (pauseMusic) {
                pausePlayingMusic(context);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(Constants.CALLBACK_ACTIONS.PAUSE_MUSIC_COMPLETED, startId);
        }
    }

    public static void killApps(Context context, int startId) {

        boolean isDeviceRooted = RootUtil.isDeviceRooted();
        LinkedList<AppObject> appList = Logic.readList(context, APPS_TO_CLOSE);


        if (isDeviceRooted) {
            Log.d(LOG_TAG, "Device rooted");
            try {
                for (AppObject app: appList) {
                    Log.d(LOG_TAG, "app: " + app.getPackageName());
                    Process suProcess = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                    os.writeBytes("adb shell" + "\n");
                    os.flush();
                    os.writeBytes("am force-stop " + app.getPackageName() + "\n");
                    os.flush();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {

            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

            for (AppObject app: appList) {
                am.killBackgroundProcesses(app.getPackageName());
            }

        }


        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(Constants.CALLBACK_ACTIONS.CLOSE_APPS_COMPLETED, startId);
        }

    }

    public static void showHomeScreen(Context context, int startId) {

        boolean showHomeScreen = Logic.getSharedPrefBoolean(context,
                KEY_SHOW_HOME_SCREEN, SHOW_HOME_SCREEN_DEFAULT_VALUE);

        if (showHomeScreen) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startMain);
        }

        if (context instanceof CallbackService) {
            ((CallbackService) context).callback(Constants.CALLBACK_ACTIONS.BACK_TO_HOME_SCREEN_COMPLETED, startId);
        }

    }

}
