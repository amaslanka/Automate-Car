package pl.maslanka.automatecar.connected;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * Created by Artur on 22.11.2016.
 */

public class PopupActivityConnected extends AppCompatActivity implements Constants.PREF_KEYS, Constants.DEFAULT_VALUES, Constants.BROADCAST_NOTIFICATIONS {

    public static boolean isInFront = false;
    private AlertDialog alertDialog;
    private int dialogTimeout;
    private boolean actionDialogTimeout;
    private CountDownTimer counter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialogTimeout = getIntent().getIntExtra(KEY_DIALOG_TIMEOUT, DIALOG_TIMEOUT_DEFAULT_VALUE);
        actionDialogTimeout = getIntent().getBooleanExtra(KEY_ACTION_DIALOG_TIMEOUT,
                ACTION_DIALOG_TIMEOUT_DEFAULT_VALUE);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);


        alertDialog = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.car_connected) + " - " + dialogTimeout + "s")
        .setMessage(getString(R.string.car_detected_desc))
        .setIcon(R.drawable.connected_icon)
        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                counter.cancel();
                Intent intent = new Intent();
                intent.setAction(CONTINUE_ACTION);
                sendBroadcast(intent);
                finish();
            }
        })
        .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                counter.cancel();
                finish();
            }
        }).create();

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_BACK) {
                    counter.cancel();
                    finish();
                    return true;
                }
                return false;
            }
        });


        alertDialog.show();

        counter = new CountDownTimer(dialogTimeout*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                alertDialog.setTitle(getString(R.string.car_connected) + " - " + (millisUntilFinished/1000) + "s");
            }

            @Override
            public void onFinish() {
                if (actionDialogTimeout) {
                    Intent intent = new Intent();
                    intent.setAction(CONTINUE_ACTION);
                    sendBroadcast(intent);
                }

                finish();
            }
        }.start();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alertDialog != null) {
            alertDialog.show();
        }
        isInFront = true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }


    @Override
    protected void onPause() {
        super.onPause();
        alertDialog.dismiss();
        isInFront = false;
    }
}