package pl.maslanka.automatecar.services;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.helperobjectsandinterfaces.PairObject;
import pl.maslanka.automatecar.userinputfilter.EditTextIntegerPreference;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.utils.PopupActivity;

/**
 * Created by Artur on 22.11.2016.
 */

public class CarConnectedService extends Service implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS {

    private boolean disableLockScreen;
    private boolean forceAutoRotation;
    private boolean checkIfInPocket;
    private boolean checkWirelessPowerSupply;
    private boolean checkNfcTag;
    private boolean showCancelDialog;
    private int dialogTimeout;
    private boolean actionDialogTimeout;
    private LinkedList<PairObject<String, String>> appList;
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
                    stopSelf(msg.arg1);
                    break;
                default:
                    stopSelf(msg.arg1);
            }

            Log.d("StopID", Integer.toString(msg.arg1));

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
        Log.d("Service", "started");
        Log.d("StartID", Integer.toString(startId));

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
        Log.d("Service", "stopped");
    }


    protected void getPreferencesData() {
        disableLockScreen = Logic.getSharedPrefBoolean(this, KEY_DISABLE_LOCK_SCREEN);
        forceAutoRotation = Logic.getSharedPrefBoolean(this, KEY_FORCE_AUTO_ROTATION);
        checkIfInPocket = Logic.getSharedPrefBoolean(this, KEY_CHECK_IF_IN_POCKET);
        checkWirelessPowerSupply = Logic.getSharedPrefBoolean(this, KEY_CHECK_WIRELESS_POWER_SUPPLY);
        checkNfcTag = Logic.getSharedPrefBoolean(this, KEY_CHECK_NFC_TAG);
        showCancelDialog = Logic.getSharedPrefBoolean(this, KEY_SHOW_CANCEL_DIALOG);
        dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(this, KEY_DIALOG_TIMEOUT));
        actionDialogTimeout = Logic.getSharedPrefBoolean(this, KEY_ACTION_DIALOG_TIMEOUT);
        appList = Logic.readList(this);
        showNavi = Logic.getSharedPrefBoolean(this, KEY_SHOW_NAVI);
    }


    protected void showPopup() {
        showCancelDialog = Logic.getSharedPrefBoolean(
                CarConnectedService.this, KEY_SHOW_CANCEL_DIALOG);
        dialogTimeout = Integer.parseInt(Logic.getSharedPrefString(
                CarConnectedService.this, KEY_DIALOG_TIMEOUT));
        actionDialogTimeout = Logic.getSharedPrefBoolean(
                CarConnectedService.this, KEY_ACTION_DIALOG_TIMEOUT);
        if (showCancelDialog) {
            Log.d("popUp", "showDialog");
            Intent popup = new Intent(getBaseContext(), PopupActivity.class);
            popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            popup.putExtra(KEY_DIALOG_TIMEOUT, dialogTimeout);
            popup.putExtra(KEY_ACTION_DIALOG_TIMEOUT, actionDialogTimeout);
            startActivity(popup);
        }
    }

    protected void launchApps() {
        appList = Logic.readList(CarConnectedService.this);

        Log.d("continue", "launchApps");
        try {
            Thread.sleep(1000);
            for (PairObject<String, String> app: appList) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
                if (launchIntent != null) {
                    startActivity(launchIntent);
                    Thread.sleep(8000);
                } else {
                    Log.e("AppLaunchingError", "Package " + app.getPackageName() + " not found!");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}


