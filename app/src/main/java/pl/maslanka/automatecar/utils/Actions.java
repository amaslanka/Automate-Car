package pl.maslanka.automatecar.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.KeyEvent;

import java.util.LinkedList;
import java.util.List;

import pl.maslanka.automatecar.connected.PopupConnectedActivity;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;
import pl.maslanka.automatecar.services.CarConnectedService;
import pl.maslanka.automatecar.services.ForceAutoRotationService;

/**
 * Created by Artur on 10.12.2016.
 */

public class Actions implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS,
        Constants.CALLBACK_ACTIONS, Constants.DEFAULT_VALUES {

    private static final String LOG_TAG = Actions.class.getSimpleName();

    public static void startForcingAutoRotation(Context context, ServiceConnection mConnection) {
        Log.d(LOG_TAG, "starting: forceAutoRotationService");
        boolean forceAutoRotation = Logic.getSharedPrefBoolean(context, KEY_FORCE_AUTO_ROTATION, FORCE_AUTO_ROTATION_DEFAULT_VALUE);

        if (forceAutoRotation) {
            bindNewService(context, ForceAutoRotationService.class, mConnection);

        } else if (context instanceof CarConnectedService) {
            ((CarConnectedService) context).callback(FORCE_ROTATION_COMPLETED);
        }

    }

    public static void showConnectedPopup(Context context) {

        boolean showCancelDialog = Logic.getSharedPrefBoolean(
                context, KEY_SHOW_CANCEL_DIALOG, SHOW_CANCEL_DIALOG_DEFAULT_VALUE);
        int dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_DIALOG_TIMEOUT, Integer.toString(DIALOG_TIMEOUT_DEFAULT_VALUE)));
        boolean actionDialogTimeout = Logic.getSharedPrefBoolean(
                context, KEY_ACTION_DIALOG_TIMEOUT, ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);

        if (showCancelDialog) {
            Intent popup = new Intent(context, PopupConnectedActivity.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtra(KEY_DIALOG_TIMEOUT, dialogTimeout);
            popup.putExtra(KEY_ACTION_DIALOG_TIMEOUT, actionDialogTimeout);
            context.startActivity(popup);
        } else if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(POPUP_FINISH_CONTINUE);
        }
    }

    public static void launchApps(Context context) {

        LinkedList<PairObject<String, String>> appList = Logic.readList(context);
        int sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                context, KEY_SLEEP_TIMES, Integer.toString(SLEEP_TIMES_DEFAULT_VALUE)));

        Log.d(LOG_TAG, "continue - launchApps");
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
            ((CarConnectedService) context).callback(LAUNCH_APPS_COMPLETED);
        }

    }


    public static void playMusic(Context context) {

        boolean playMusic = Logic.getSharedPrefBoolean(context, KEY_PLAY_MUSIC, PLAY_MUSIC_DEFAULT_VALUE);
        String musicPlayer = Logic.getSharedPrefString(context, KEY_SELECT_MUSIC_PLAYER, null);

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

                sendOrderedPlayBroadcast(context, component, KeyEvent.ACTION_DOWN);
                sendOrderedPlayBroadcast(context, component, KeyEvent.ACTION_UP);

            } else {
                Log.d(LOG_TAG, "musicPlayer: no Music Player defined");
                sendPlayBroadcast(context, KeyEvent.ACTION_DOWN);
                sendPlayBroadcast(context, KeyEvent.ACTION_UP);
            }
        }

        if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(PLAY_MUSIC_COMPLETED);
        }
    }

    public static void dismissLockScreen(Context context) {


        if (context instanceof CarConnectedService){
            ((CarConnectedService) context).callback(DISMISS_LOCK_SCREEN_COMPLETED);
        }
    }

    private static void sendOrderedPlayBroadcast(Context context, ComponentName component, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(component);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendOrderedBroadcast(intent, null);
    }

    private static void sendPlayBroadcast(Context context, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        context.sendBroadcast(intent);
    }

    private static void bindNewService(Context context, Class<?> cls, ServiceConnection mConnection) {
        Intent intent = new Intent(context, cls);
        context.startService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

}
