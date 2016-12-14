package pl.maslanka.automatecar.helpers;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Artur on 13.12.2016.
 */

public class TurnScreenOnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnScreenOn();
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        CountDownTimer counter = new CountDownTimer(100, 50) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() throws NullPointerException {
                finish();
            }
        }.start();
    }

    private void turnScreenOn() {
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

}
