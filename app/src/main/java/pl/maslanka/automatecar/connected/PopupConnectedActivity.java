package pl.maslanka.automatecar.connected;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;

/**
 * Created by Artur on 22.11.2016.
 */

public class PopupConnectedActivity extends AppCompatActivity implements Constants.PREF_KEYS, Constants.DEFAULT_VALUES, Constants.BROADCAST_NOTIFICATIONS, Constants.POPUP_CONNECTED_FRAGMENT {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final String KEY_POPUP_WAS_SHOWING = "popup_was_showing";

    public static boolean isInFront = false;
    public static int dialogTimeout;
    public static boolean actionDialogTimeout;
    public static boolean popupConnectedActive = false;

    private PopupConnectedFragment popupFragment;
    private boolean popupWasShowing;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        turnScreenOn();

            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

            dialogTimeout = getIntent().getIntExtra(KEY_DIALOG_TIMEOUT, DIALOG_TIMEOUT_DEFAULT_VALUE);
            actionDialogTimeout = getIntent().getBooleanExtra(KEY_ACTION_DIALOG_TIMEOUT,
                    ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);


            if(savedInstanceState == null && popupFragment == null) {
                popupFragment = new PopupConnectedFragment();
                // Display the fragment as the main content.
                getSupportFragmentManager().beginTransaction().add(android.R.id.content, popupFragment, TAG_POPUP_CONNECTED_FRAGMENT).commit();
            } else {
                popupFragment = (PopupConnectedFragment) getSupportFragmentManager().findFragmentByTag(TAG_POPUP_CONNECTED_FRAGMENT);
                popupWasShowing = savedInstanceState.getBoolean(KEY_POPUP_WAS_SHOWING);
                Log.d(LOG_TAG, "popupWasShowing - " + Boolean.toString(popupWasShowing));
            }

    }


    @Override
    protected void onResume() {
        super.onResume();
        isInFront = true;
        if (popupFragment != null)
            if (popupFragment.getAlertDialog() != null)
                if (popupWasShowing)
                    popupFragment.getAlertDialog().show();


    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }


    @Override
    protected void onPause() {

        super.onPause();
        isInFront = false;
        if (popupFragment != null) {
            if (popupFragment.getAlertDialog() != null) {
                popupWasShowing = popupFragment.getAlertDialog().isShowing();
                if (popupWasShowing)
                    popupFragment.getAlertDialog().dismiss();
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (popupFragment.getAlertDialog() != null)
            savedInstanceState.putBoolean(KEY_POPUP_WAS_SHOWING, popupWasShowing);

        super.onSaveInstanceState(savedInstanceState);
    }


    private void turnScreenOn() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
}
//
//package pl.maslanka.automatecar.connected;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.os.PersistableBundle;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.Window;
//import android.view.WindowManager;
//
//import pl.maslanka.automatecar.R;
//import pl.maslanka.automatecar.helpers.Constants;
//
//import static android.view.KeyEvent.KEYCODE_BACK;
//
///**
// * Created by Artur on 22.11.2016.
// */
//
//public class PopupConnectedActivity extends AppCompatActivity implements Constants.PREF_KEYS, Constants.DEFAULT_VALUES, Constants.BROADCAST_NOTIFICATIONS {
//
//    public static boolean isInFront = false;
//    private AlertDialog alertDialog;
//    private int dialogTimeout;
//    private int timeoutLeft;
//    private boolean actionDialogTimeout;
//    private CountDownTimer counter;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
//
//        if (savedInstanceState == null) {
//            dialogTimeout = getIntent().getIntExtra(KEY_DIALOG_TIMEOUT, DIALOG_TIMEOUT_DEFAULT_VALUE);
//            actionDialogTimeout = getIntent().getBooleanExtra(KEY_ACTION_DIALOG_TIMEOUT,
//                    ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);
//
//            alertDialog = new AlertDialog.Builder(this)
//                    .setTitle(getString(R.string.car_connected) + " - " + dialogTimeout + "s")
//                    .setMessage(getString(R.string.car_detected_desc))
//                    .setIcon(R.drawable.connected_icon)
//                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            counter.cancel();
//                            sendBroadcastWithAction(CONTINUE_ACTION);
//                            alertDialog.dismiss();
//                            alertDialog = null;
//                            finish();
//                        }
//                    })
//                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            counter.cancel();
//                            sendBroadcastWithAction(DISCONTINUE_ACTION);
//                            alertDialog.dismiss();
//                            alertDialog = null;
//                            finish();
//                        }
//                    }).create();
//
//            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//
//            alertDialog.setCanceledOnTouchOutside(false);
//            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    counter.cancel();
//                    sendBroadcastWithAction(DISCONTINUE_ACTION);
//                    alertDialog.dismiss();
//                    alertDialog = null;
//                    finish();
//                }
//            });
//            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                @Override
//                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                    if (keyCode == KEYCODE_BACK) {
//                        counter.cancel();
//                        sendBroadcastWithAction(DISCONTINUE_ACTION);
//                        alertDialog.dismiss();
//                        alertDialog = null;
//                        finish();
//                        return true;
//                    }
//                    return false;
//                }
//            });
//
//
//            alertDialog.show();
//
//            counter = new CountDownTimer(dialogTimeout*1000, 1000) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    alertDialog.setTitle(getString(R.string.car_connected) + " - " + (millisUntilFinished/1000) + "s");
//                    timeoutLeft = (int) millisUntilFinished/1000;
//                }
//
//                @Override
//                public void onFinish() {
//                    Log.d("timeoutLeft", Integer.toString(timeoutLeft));
//                    if (actionDialogTimeout && timeoutLeft == 1) {
//                        sendBroadcastWithAction(CONTINUE_ACTION);
//                    } else {
//                        sendBroadcastWithAction(DISCONTINUE_ACTION);
//                    }
//                    alertDialog.dismiss();
//                    alertDialog = null;
//                    finish();
//                }
//            }.start();
//
//        }
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        turnScreenOn();
//        if (alertDialog != null) {
//            alertDialog.show();
//        }
//        isInFront = true;
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
//    }
//
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (alertDialog != null) {
//            alertDialog.dismiss();
//        }
//        isInFront = false;
//    }
//
//    protected void sendBroadcastWithAction (String action) {
//        Intent intent = new Intent();
//        intent.setAction(action);
//        sendBroadcast(intent);
//    }
//
//    private void turnScreenOn() {
//        Window window = this.getWindow();
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//    }
//
//}