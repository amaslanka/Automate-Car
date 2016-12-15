package pl.maslanka.automatecar.utils;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
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
        boolean checkIfInPocket = Logic.getSharedPrefBoolean(context, KEY_CHECK_IF_IN_POCKET, CHECK_IF_IN_POCKET_DEFAULT_VALUE);

        if (checkIfInPocket) {
            bindNewService(context, ProximitySensorService.class, mConnection, startId);
        } else if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(PROXIMITY_CHECK_COMPLETED, startId);
        }

    }

    public static void startForcingAutoRotation(Context context, ServiceConnection mConnection, int startId) {
        Log.d(LOG_TAG, "starting: forceAutoRotationService");
        boolean forceAutoRotation = Logic.getSharedPrefBoolean(context, KEY_FORCE_AUTO_ROTATION, FORCE_AUTO_ROTATION_DEFAULT_VALUE);


        if (forceAutoRotation) {
            bindNewService(context, ForceAutoRotationService.class, mConnection, startId);
        } else if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(FORCE_ROTATION_COMPLETED, startId);
        }

    }

    public static void showConnectedPopup(Context context, int startId) {

        boolean showCancelDialog = Logic.getSharedPrefBoolean(
                context, KEY_SHOW_CANCEL_DIALOG, SHOW_CANCEL_DIALOG_DEFAULT_VALUE);
        int dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_DIALOG_TIMEOUT, Integer.toString(DIALOG_TIMEOUT_DEFAULT_VALUE)));
        boolean actionDialogTimeout = Logic.getSharedPrefBoolean(
                context, KEY_ACTION_DIALOG_TIMEOUT, ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);

        if (showCancelDialog) {
            Intent popup = new Intent(context, PopupConnectedActivity.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtra(START_ID, startId);
            popup.putExtra(KEY_DIALOG_TIMEOUT, dialogTimeout);
            popup.putExtra(KEY_ACTION_DIALOG_TIMEOUT, actionDialogTimeout);
            context.startActivity(popup);
        } else if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(POPUP_CONNECTED_FINISH_CONTINUE, startId);
        }
    }

    public static void launchApps(Context context, int startId) {

        LinkedList<PairObject<String, String>> appList = Logic.readList(context);
        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES, Integer.toString(SLEEP_TIMES_DEFAULT_VALUE)));

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

                Intent launchIntent =
                        context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());

                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    Thread.sleep(sleepTimes*1000);
                } else {
                    Log.e("AppLaunchingError", "Package " + app.getPackageName() + " not found!");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(LAUNCH_APPS_COMPLETED, startId);
        }

    }


    public static void playMusic(Context context, int startId) {

        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES, Integer.toString(SLEEP_TIMES_DEFAULT_VALUE)));

        LinkedList<PairObject<String, String>> appList = Logic.readList(context);
        boolean playMusic = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC, PLAY_MUSIC_DEFAULT_VALUE);
        boolean playMusicOnA2dp = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC_ON_A2DP, PLAY_MUSIC_ON_A2DP_DEFAULT_VALUE);
        String musicPlayer = Logic.getSharedPrefString(context, KEY_SELECT_MUSIC_PLAYER, null);
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            if (playMusic) {
                if (musicPlayer != null) {

                    List<ActivityInfo> musicPlayersActivityInfo = Logic.getListOfMediaBroadcastReceivers(context);
                    ActivityInfo musicPlayerInfo = new ActivityInfo();

                    for (ActivityInfo activityInfo: musicPlayersActivityInfo) {
                        if (activityInfo.packageName.equals(musicPlayer)) {
                            musicPlayerInfo = activityInfo;
                        }
                    }

                    ComponentName component = new ComponentName(musicPlayerInfo.packageName, musicPlayerInfo.name);

                    Log.d(LOG_TAG, "broadcastName: " + musicPlayerInfo.name);
//
//                    Log.e("if1", Boolean.toString(!appList.contains(new PairObject<>(musicPlayerInfo.loadLabel(context.getPackageManager()).toString(), musicPlayerInfo.packageName))));
//                    Log.e("if2", Boolean.toString(!AppBroadcastReceiver.startWithProximityFarPerformed));


//                    if (!appList.contains(new PairObject<>(musicPlayerInfo.loadLabel(context.getPackageManager()).toString(), musicPlayerInfo.packageName)) ||
//                            !AppBroadcastReceiver.startWithProximityFarPerformed) {
//                        Intent launchIntent =
//                                context.getPackageManager().getLaunchIntentForPackage(musicPlayerInfo.packageName);
//                        if (launchIntent != null) {
//                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(launchIntent);
//                            Thread.sleep(sleepTimes*1000);
//                        } else {
//                            Log.e("MusicPlayerLaunchError", "Package " + musicPlayerInfo.packageName + " not found!");
//                        }
//                    }


                    if (playMusicOnA2dp) {

                        waitForA2dp(manager);

                        if (manager.isBluetoothA2dpOn()) {
                            Log.d(LOG_TAG, "Bluetooth A2DP is turned on!");
                            sendOrderedPlayButtonEvent(context, component, KeyEvent.ACTION_DOWN);
                            Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
                            sendOrderedPlayButtonEvent(context, component, KeyEvent.ACTION_UP);
                        }

                    } else {
                        sendOrderedPlayButtonEvent(context, component, KeyEvent.ACTION_DOWN);
                        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
                        sendOrderedPlayButtonEvent(context, component, KeyEvent.ACTION_UP);
                    }

                } else {
                    Log.d(LOG_TAG, "musicPlayer: no Music Player defined");
                    if (playMusicOnA2dp) {

                        waitForA2dp(manager);

                        if (manager.isBluetoothA2dpOn()) {
                            Log.d(LOG_TAG, "Bluetooth A2DP is turned on!");
                            sendPlayButtonEvent(context, KeyEvent.ACTION_DOWN);
                            Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
                            sendPlayButtonEvent(context, KeyEvent.ACTION_UP);
                        }

                    } else {
                        sendPlayButtonEvent(context, KeyEvent.ACTION_DOWN);
                        Thread.sleep(Constants.SLEEP_BETWEEN_BUTTON_PRESS);
                        sendPlayButtonEvent(context, KeyEvent.ACTION_UP);
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

    public static void dismissLockScreen(Context context) {

        boolean dismissLockScreen = Logic.getSharedPrefBoolean(context,
                KEY_DISMISS_LOCK_SCREEN, DISMISS_LOCK_SCREEN_DEFAULT_VALUE);


        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);

        //Dismiss lock screen only when accessibility service provides info that it is on top
        //Check also whether device is locked or other system ui event is shown
        try {

            if (Logic.getCarConnectedProcessState() != CarConnectedProcessState.NOT_STARTED
                    && Logic.getCurrentForegroundAppPackage().equals(Constants.SYSTEM_UI_PACKAGE_NAME)
                    && keyguardManager.inKeyguardRestrictedInputMode()
                    && dismissLockScreen) {

                    Log.d(LOG_TAG, "Dismiss lock screen key event sent!");
                    Process process1 = Runtime.getRuntime().exec(Constants.DISMISS_LOCK_SCREEN_SU_COMMAND);
                    Process process2 = Runtime.getRuntime().exec(Constants.DISMISS_LOCK_SCREEN_SU_COMMAND_ALTERNATIVE);

                }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Device not rooted! Dismiss lock screen action cannot be done!");
            e.printStackTrace();
        }



    }

    private static void sendOrderedPlayButtonEvent(Context context, ComponentName component, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(component);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(intent, null);
    }

    private static void sendPlayButtonEvent(Context context, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendBroadcast(intent);
    }

    private static void bindNewService(Context context, Class<?> cls, ServiceConnection mConnection, int startId) {
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
            Log.d(LOG_TAG, "isBluetoothA2dpOn?: " + Boolean.toString(manager.isBluetoothA2dpOn()));
            Thread.sleep(SLEEP_BETWEEN_CHECK);
            counter++;
        }
    }
}
