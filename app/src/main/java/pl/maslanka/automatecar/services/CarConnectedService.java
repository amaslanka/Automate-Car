package pl.maslanka.automatecar.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import java.util.LinkedList;
import java.util.List;

import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.connected.PopupActivityConnected;

/**
 * Created by Artur on 22.11.2016.
 */

public class CarConnectedService extends Service implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS, Constants.DEFAULT_VALUES {

    private boolean disableLockScreen;
    private boolean forceAutoRotation;
    private boolean checkIfInPocket;
    private boolean checkWirelessPowerSupply;
    private boolean checkNfcTag;
    private boolean showCancelDialog;
    private int dialogTimeout;
    private boolean actionDialogTimeout;
    private LinkedList<PairObject<String, String>> appList;
    private int sleepTimes;
    private boolean maxVolume;
    private boolean playMusic;
    private String musicPlayer;
    private boolean showNavi;
    private String action;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (action) {
                case POPUP_ACTION:
                    showPopup();
                    stopSelf(msg.arg1);
                    break;
                case CONTINUE_ACTION:
                    launchApps();
                    sendBroadcastAction(PLAY_MUSIC_ACTION);
                    stopSelf(msg.arg1);
                    break;
                case PLAY_MUSIC_ACTION:
                    playMusic();
                    stopSelf(msg.arg1);
                    break;
                default:
                    stopSelf(msg.arg1);
            }

            Log.d("CarConnectedService", "stopped! StopID: " + Integer.toString(msg.arg1));

        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CarConnectedService", "started! StartID: " + Integer.toString(startId));

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
        disableLockScreen = Logic.getSharedPrefBoolean(this, KEY_DISABLE_LOCK_SCREEN, DISABLE_LOCK_SCREEN_DEFAULT_VALUE);
        forceAutoRotation = Logic.getSharedPrefBoolean(this, KEY_FORCE_AUTO_ROTATION, FORCE_AUTO_ROTATION_DEFAULT_VALUE);
        checkIfInPocket = Logic.getSharedPrefBoolean(this, KEY_CHECK_IF_IN_POCKET, CHECK_IF_IN_POCKET_DEFAULT_VALUE);
        checkWirelessPowerSupply = Logic.getSharedPrefBoolean(this, KEY_CHECK_WIRELESS_POWER_SUPPLY, CHECK_WIRELESS_POWER_SUPPLY_DEFAULT_VALUE);
        checkNfcTag = Logic.getSharedPrefBoolean(this, KEY_CHECK_NFC_TAG, CHECK_NFC_TAG_DEFAULT_VALUE);
        showCancelDialog = Logic.getSharedPrefBoolean(this, KEY_SHOW_CANCEL_DIALOG, SHOW_CANCEL_DIALOG_DEFAULT_VALUE);
        dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(this, KEY_DIALOG_TIMEOUT, Integer.toString(DIALOG_TIMEOUT_DEFAULT_VALUE)));
        actionDialogTimeout = Logic.getSharedPrefBoolean(this, KEY_ACTION_DIALOG_TIMEOUT, ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);
        appList = Logic.readList(this);
        sleepTimes = Integer.parseInt(Logic.getSharedPrefString(this, KEY_SLEEP_TIMES, Integer.toString(SLEEP_TIMES_DEFAULT_VALUE)));
        maxVolume = Logic.getSharedPrefBoolean(this, KEY_MAX_VOLUME, MAX_VOLUME_DEFAULT_VALUE);
        playMusic = Logic.getSharedPrefBoolean(this, KEY_PLAY_MUSIC, PLAY_MUSIC_DEFAULT_VALUE);
        musicPlayer = Logic.getSharedPrefString(this, KEY_CHOOSE_MUSIC_PLAYER, null);
        showNavi = Logic.getSharedPrefBoolean(this, KEY_SHOW_NAVI, SHOW_NAVI_DEFAULT_VALUE);
    }

    protected void sendBroadcastAction(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }


    protected void showPopup() {
        showCancelDialog = Logic.getSharedPrefBoolean(
                CarConnectedService.this, KEY_SHOW_CANCEL_DIALOG, SHOW_CANCEL_DIALOG_DEFAULT_VALUE);
        dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(
                CarConnectedService.this, KEY_DIALOG_TIMEOUT, Integer.toString(DIALOG_TIMEOUT_DEFAULT_VALUE)));
        actionDialogTimeout = Logic.getSharedPrefBoolean(
                CarConnectedService.this, KEY_ACTION_DIALOG_TIMEOUT, ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);
        if (showCancelDialog) {
            Log.d("popUp", "showDialog");
            Intent popup = new Intent(getBaseContext(), PopupActivityConnected.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtra(KEY_DIALOG_TIMEOUT, dialogTimeout);
            popup.putExtra(KEY_ACTION_DIALOG_TIMEOUT, actionDialogTimeout);
            startActivity(popup);
        }
    }

    protected void launchApps() {
        appList = Logic.readList(CarConnectedService.this);
        sleepTimes = Integer.parseInt(Logic.getSharedPrefString(
                CarConnectedService.this, KEY_SLEEP_TIMES, Integer.toString(SLEEP_TIMES_DEFAULT_VALUE)));

        Log.d("continue", "launchApps");
        try {
            Thread.sleep(1000);
            for (PairObject<String, String> app: appList) {

                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(app.getPackageName());

                if (launchIntent != null) {
                    startActivity(launchIntent);
                    Thread.sleep(sleepTimes*1000);
                } else {
                    Log.e("AppLaunchingError", "Package " + app.getPackageName() + " not found!");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void playMusic() {
        playMusic = Logic.getSharedPrefBoolean(this, KEY_PLAY_MUSIC, PLAY_MUSIC_DEFAULT_VALUE);
        musicPlayer = Logic.getSharedPrefString(this, KEY_CHOOSE_MUSIC_PLAYER, null);

        if (playMusic) {
            if (musicPlayer != null) {

                List<ActivityInfo> musicPlayersActivityInfo = Logic.getListOfMediaBroadcastReceivers(this);
                ActivityInfo musicPlayerInfo = new ActivityInfo();

                for (ActivityInfo activityInfo: musicPlayersActivityInfo) {
                    if (activityInfo.packageName.equals(musicPlayer)) {
                        musicPlayerInfo = activityInfo;
                    }
                }

                ComponentName component = new ComponentName(musicPlayerInfo.packageName, musicPlayerInfo.name);

                Log.d("broadcastName", musicPlayerInfo.name);

                sendOrderedPlayBroadcast(component, KeyEvent.ACTION_DOWN);
                sendOrderedPlayBroadcast(component, KeyEvent.ACTION_UP);

            } else {
                Log.d("musicPlayer", "noMusicPlayerDefined");
                sendPlayBroadcast(KeyEvent.ACTION_DOWN);
                sendPlayBroadcast(KeyEvent.ACTION_UP);
            }
        }
    }

    protected void sendOrderedPlayBroadcast(ComponentName component, int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(component);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        sendOrderedBroadcast(intent, null);
    }

    protected void sendPlayBroadcast(int action) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(action, KeyEvent.KEYCODE_MEDIA_PLAY));
        sendBroadcast(intent);
    }


}


