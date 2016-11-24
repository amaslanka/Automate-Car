package pl.maslanka.automatecar.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import pl.maslanka.automatecar.MainActivity;
import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.utils.BluetoothConnectionReceiver;


public class MainService extends Service implements Constants.BROADCAST_NOTIFICATIONS{
    private static final String LOG_TAG = "MainService";
    private Notification notification;
    private IntentFilter bluetoothIntentFilter;
    private final BluetoothConnectionReceiver mReceiver = new BluetoothConnectionReceiver();


    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        bluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothIntentFilter.addAction(CONTINUE_ACTION);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.START_FOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setTicker(getString(R.string.app_name))
                    .setContentText(getString(R.string.service_running))
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);

            registerReceiver(mReceiver, bluetoothIntentFilter);

        } else if (intent.getAction().equals(
                Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
            unregisterReceiver(mReceiver);
        }
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }


}