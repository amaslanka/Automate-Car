package pl.maslanka.automatecar.connected;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.callbackmessages.MessagePopupConnected;

/**
 * Created by Artur on 22.11.2016.
 */

public class PopupConnectedActivity extends AppCompatActivity
        implements Constants.PREF_KEYS, Constants.DEFAULT_VALUES,
        Constants.BROADCAST_NOTIFICATIONS, Constants.POPUP_CONNECTED_FRAGMENT,
        Constants.CALLBACK_ACTIONS {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final String KEY_POPUP_WAS_SHOWING = "popup_was_showing";

    public static boolean isInFront = false;
    public static int carConnectedServiceStartId;
    public static int dialogTimeout;
    public static boolean actionDialogTimeout;

    private Context contextToCallback;
    private PopupConnectedFragment popupFragment;
    private boolean popupWasShowing;

    public Context getContextToCallback() {
        return contextToCallback;
    }

    @Subscribe
    public void onMessagePopupConnected(MessagePopupConnected event) {
        Log.d(LOG_TAG, "MessagePopupConnected received");
        this.contextToCallback = event.context;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        turnScreenOn();

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

        carConnectedServiceStartId = getIntent().getIntExtra(START_ID, START_ID_NO_VALUE);
        dialogTimeout = getIntent().getIntExtra(KEY_DIALOG_TIMEOUT_IN_CAR, DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE);
        actionDialogTimeout = getIntent().getBooleanExtra(KEY_ACTION_DIALOG_TIMEOUT_IN_CAR,
                ACTION_DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE);


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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInFront = true;
        if (popupFragment.getAlertDialog() != null && popupWasShowing)
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

        if (popupFragment.getAlertDialog() != null) {
            popupWasShowing = popupFragment.getAlertDialog().isShowing();

            if (popupWasShowing)
                popupFragment.getAlertDialog().dismiss();
        }

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
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
