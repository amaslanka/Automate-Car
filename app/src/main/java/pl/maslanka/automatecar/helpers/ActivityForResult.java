package pl.maslanka.automatecar.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Artur on 02.12.2016.
 */

public class ActivityForResult extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int resultCode = intent.getIntExtra(Constants.INTENT_EXTRA_RESULT_CODE, RESULT_CANCELED);

        setResult(resultCode);
        finish();

    }
}
