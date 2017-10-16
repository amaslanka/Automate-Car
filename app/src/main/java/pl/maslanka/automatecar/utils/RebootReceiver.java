package pl.maslanka.automatecar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.services.MainService;

/**
 * Created by Artur on 15.10.2017.
 */

public class RebootReceiver extends BroadcastReceiver {

    public static final String TAG = "RebootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean shouldMainServiceBeStarted = Logic.getSharedPrefBoolean(context, Constants.PREF_KEYS.KEY_MAIN_SERVICE_STARTED, false);

        Log.d(TAG, "reboot completed, starting main service? " + shouldMainServiceBeStarted);

        if (shouldMainServiceBeStarted) {
            Intent startIntent = new Intent(context, MainService.class);
            startIntent.setAction(Constants.ACTION.START_FOREGROUND_ACTION);
            context.startService(startIntent);
            Toast.makeText(context, context.getString(R.string.service_started), Toast.LENGTH_SHORT).show();
        }

    }

}
