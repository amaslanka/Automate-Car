package pl.maslanka.automatecar.utils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import pl.maslanka.automatecar.connected.PopupConnectedActivity;
import pl.maslanka.automatecar.helpers.CarConnectedProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;
import pl.maslanka.automatecar.helpers.TurnScreenOnActivity;
import pl.maslanka.automatecar.services.CarConnectedService;
import pl.maslanka.automatecar.services.ForceAutoRotationService;
import pl.maslanka.automatecar.services.ProximitySensorService;

/**
 * Created by Artur on 10.12.2016.
 */

public class Actions implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS,
        Constants.CALLBACK_ACTIONS, Constants.DEFAULT_VALUES {

    private static final String LOG_TAG = Actions.class.getSimpleName();


    public static void proximityCheck(Context context, ServiceConnection mConnection, int startId) {
        boolean checkIfInPocket = Logic.getSharedPrefBoolean(context,
                KEY_CHECK_IF_IN_POCKET_IN_CAR, CHECK_IF_IN_POCKET_IN_CAR_DEFAULT_VALUE);

        if (checkIfInPocket) {
            bindNewService(context, ProximitySensorService.class, mConnection, startId);
        } else if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(PROXIMITY_CHECK_COMPLETED, startId);
        }

    }

    public static void startForcingAutoRotation(Context context, ServiceConnection mConnection,
                                                int startId) {
        Log.d(LOG_TAG, "starting: forceAutoRotationService");
        boolean forceAutoRotation = Logic.getSharedPrefBoolean(context,
                KEY_FORCE_AUTO_ROTATION_IN_CAR, FORCE_AUTO_ROTATION_IN_CAR_DEFAULT_VALUE);


        if (forceAutoRotation) {
            bindNewService(context, ForceAutoRotationService.class, mConnection, startId);
        } else if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(FORCE_ROTATION_COMPLETED, startId);
        }

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
        } else if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(POPUP_CONNECTED_FINISH_CONTINUE, startId);
        }
    }

    public static void launchApps(Context context, int startId) {

        LinkedList<PairObject<String, String>> appList = Logic.readList(context);
        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES_IN_CAR, Integer.toString(SLEEP_TIMES_IN_CAR_DEFAULT_VALUE)));

        Log.d(LOG_TAG, "continue - launchApps");


        if (appList.size() > 0 && !Logic.isScreenOn(context)) {
            Log.d(LOG_TAG, "screen is turned off - it will be invoked.");
            Intent turnScreenOn = new Intent(context, TurnScreenOnActivity.class);
            turnScreenOn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(turnScreenOn);
        }

        try {
            Thread.sleep(1000);
            for (PairObject<String, String> app: appList) {

                launchIntentFromPackage(context, app.getPackageName());

                Thread.sleep(sleepTimes*1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(LAUNCH_APPS_COMPLETED, startId);
        }

    }

    public static void changeWifiState(Context context, int startId) {

        boolean changeWifiStateInCar = Logic.getSharedPrefBoolean(context,
                KEY_CHANGE_WIFI_STATE_IN_CAR, CHANGE_WIFI_STATE_IN_CAR_DEFAULT_VALUE);
        boolean wifiEnableInCar = Logic.getSharedPrefBoolean(context,
                KEY_WIFI_ENABLE_IN_CAR, WIFI_ENABLE_IN_CAR_DEFAULT_VALUE);

        if (changeWifiStateInCar) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(wifiEnableInCar);
        }

        if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(CHANGE_WIFI_STATE_COMPLETED, startId);
        }
    }

    public static void changeMobileDataState(Context context, int startId) {

        boolean changeMobileDataStateInCar = Logic.getSharedPrefBoolean(context,
                KEY_CHANGE_MOBILE_DATA_STATE_IN_CAR, KEY_CHANGE_MOBILE_DATA_STATE_IN_CAR_DEFAULT_VALUE);
        boolean mobileDataEnableInCar = Logic.getSharedPrefBoolean(context,
                KEY_MOBILE_DATA_ENABLE_IN_CAR, KEY_MOBILE_DATA_ENABLE_IN_CAR_DEFAULT_VALUE);
        int mobileDataEnableInCarAsInt = (mobileDataEnableInCar) ? 1 : 0;

        if (changeMobileDataStateInCar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                int isDataOn = Settings.Global.getInt(context.getContentResolver(), "mobile_data");
                Log.e("isDataOn", Integer.toString(isDataOn));
                Log.e("mobileDataEnableInCar", Integer.toString(mobileDataEnableInCarAsInt));

                if (isDataOn != mobileDataEnableInCarAsInt) {
                    Logic.setMobileDataStateFromLollipop(context, mobileDataEnableInCarAsInt);
                }

            } catch (Settings.SettingNotFoundException e) {
                Log.e(LOG_TAG, "Setting not found!" + e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to change mobile data state!" + e);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to change mobile data state!" + e);
                e.printStackTrace();
            }
        } else if (changeMobileDataStateInCar) {
            boolean isDataOn = Logic.getMobileDataStateBelowLollipop(context);
            Log.e("isDataOn", Boolean.toString(isDataOn));
            Log.e("mobileDataEnableInCar", Boolean.toString(mobileDataEnableInCar));

            if (isDataOn != mobileDataEnableInCar) {
                Logic.setMobileDataStateBelowLollipop(context, mobileDataEnableInCar);
            }
        }

        if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(CHANGE_MOBILE_DATA_STATE_COMPLETED, startId);
        }
    }

    public static void setMediaVolume(Context context, int startId) {

        if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(SET_MEDIA_VOLUME_COMPLETED, startId);
        }
    }


    public static void playMusic(Context context, int startId) {

        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES_IN_CAR, Integer.toString(SLEEP_TIMES_IN_CAR_DEFAULT_VALUE)));
        boolean playMusic = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC_IN_CAR, PLAY_MUSIC_IN_CAR_DEFAULT_VALUE);
        boolean playMusicOnA2dp = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC_ON_A2DP_IN_CAR,
                PLAY_MUSIC_ON_A2DP_IN_CAR_DEFAULT_VALUE);
        String musicPlayer = Logic.getSharedPrefString(context, KEY_SELECT_MUSIC_PLAYER_IN_CAR, null);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            if (playMusic) {
                if (musicPlayer != null) {

                    List<ActivityInfo> musicPlayersActivityInfo =
                            Logic.getListOfMediaBroadcastReceivers(context);
                    ActivityInfo musicPlayerInfo = new ActivityInfo();

                    for (ActivityInfo activityInfo: musicPlayersActivityInfo) {
                        if (activityInfo.packageName.equals(musicPlayer)) {
                            musicPlayerInfo = activityInfo;
                        }
                    }

                    ComponentName component =
                            new ComponentName(musicPlayerInfo.packageName, musicPlayerInfo.name);

                    Log.d(LOG_TAG, "broadcastName: " + musicPlayerInfo.name);

                    if (playMusicOnA2dp) {

                        waitForA2dp(manager);

                        if (manager.isBluetoothA2dpOn()) {
                            Log.d(LOG_TAG, "Bluetooth A2DP is turned on!");

                            startPlayingMusicOnComponent(context, component);
                            Log.v(LOG_TAG, "Waiting for music to play...");
                            Thread.sleep(Constants.WAIT_FOR_MUSIC_PLAY);
                            if (!manager.isMusicActive())
                                retryPlayingMusicOnComponent(context, sleepTimes*1000, component);
                        }

                    } else {
                        startPlayingMusicOnComponent(context, component);

                        Thread.sleep(Constants.WAIT_FOR_MUSIC_PLAY);
                        if (!manager.isMusicActive())
                            retryPlayingMusicOnComponent(context, sleepTimes*1000, component);
                    }

                } else {
                    Log.d(LOG_TAG, "musicPlayer: no Music Player defined");
                    if (playMusicOnA2dp) {

                        waitForA2dp(manager);

                        if (manager.isBluetoothA2dpOn()) {
                            Log.d(LOG_TAG, "Bluetooth A2DP is turned on!");
                            startPlayingMusic(context);
                        }

                    } else {
                        startPlayingMusic(context);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(PLAY_MUSIC_COMPLETED, startId);
        }
    }

    public static void showNavi(Context context, int startId) {

        if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(SHOW_NAVI_COMPLETED, startId);
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

            if (Logic.getCarConnectedProcessState() != CarConnectedProcessState.NOT_STARTED
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

    private static void launchIntentFromPackage(Context context, String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        } else {
            Log.e("AppLaunchingError", "Package " + packageName + " not found!");
        }
    }

    private static void startPlayingMusicOnComponent(Context context, ComponentName component)
            throws InterruptedException {
        sendOrderedPlayButtonEvent(context, component, KeyEvent.ACTION_DOWN);
        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
        sendOrderedPlayButtonEvent(context, component, KeyEvent.ACTION_UP);
    }

    private static void retryPlayingMusicOnComponent(Context context, int sleepAfterComponentLaunch,
                                                     ComponentName component) throws InterruptedException {
        Log.d(LOG_TAG, "Music still not active! Launching music player to retry to play music.");
        launchIntentFromPackage(context, component.getPackageName());
        Thread.sleep(sleepAfterComponentLaunch);
        startPlayingMusicOnComponent(context, component);
    }

    private static void startPlayingMusic(Context context) throws InterruptedException {
        sendPlayButtonEvent(context, KeyEvent.ACTION_DOWN);
        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
        sendPlayButtonEvent(context, KeyEvent.ACTION_UP);
    }

    private static void sendOrderedPlayButtonEvent(Context context, ComponentName component, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(intent, null);
    }

    private static void sendPlayButtonEvent(Context context, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendBroadcast(intent);
    }

    private static void bindNewService(Context context, Class<?> cls, ServiceConnection mConnection,
                                       int startId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(START_ID, startId);
        context.startService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private static void waitForA2dp(AudioManager manager) throws InterruptedException {
        final int MAX_LOOP_NUMBER = 100;
        final int SLEEP_BETWEEN_CHECK = 100;
        int counter = 0;
        while (!manager.isBluetoothA2dpOn() && counter < MAX_LOOP_NUMBER) {
            Log.d(LOG_TAG, "Is A2DP profile on?: " + Boolean.toString(manager.isBluetoothA2dpOn()));
            Thread.sleep(SLEEP_BETWEEN_CHECK);
            counter++;
        }
    }


}
