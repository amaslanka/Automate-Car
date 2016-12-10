package pl.maslanka.automatecar.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pl.maslanka.automatecar.connected.PopupConnectedActivity;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.services.CarConnectedService;
import pl.maslanka.automatecar.services.ForceAutoRotationService;

/**
 * Created by Artur on 21.11.2016.
 */

public class AppBroadcastReceiver extends android.content.BroadcastReceiver implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private Intent intent;
    private static boolean inCarAlreadyPerformed;

    public static boolean isInCarAlreadyPerformed() {
        return inCarAlreadyPerformed;
    }

    public AppBroadcastReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent){
        final String action = intent.getAction();
        switch (action) {

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                BluetoothDevice bluetoothDeviceConnected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(LOG_TAG, "bluetoothDevCon: " + bluetoothDeviceConnected.getAddress());
                Log.d(LOG_TAG, "SharedPrefDevice: " + Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).toString());

                if (Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).contains(bluetoothDeviceConnected.getAddress()) &&
                        !inCarAlreadyPerformed) {
                    inCarAlreadyPerformed = true;
                    startServiceWithAction(context, FORCE_ROTATION_ACTION, CarConnectedService.class);
                }

                break;

            case FORCE_ROTATION_COMPLETED:
                Log.d(LOG_TAG, "force rotation complete received");
                sendBroadcastAction(context, POPUP_ACTION);
                break;


            case POPUP_ACTION:
                Log.d(LOG_TAG, "popup action");
                startServiceWithAction(context, POPUP_ACTION, CarConnectedService.class);
                break;

            case CONTINUE_ACTION:
                Log.d(LOG_TAG, "continue action");
                startServiceWithAction(context, CONTINUE_ACTION, CarConnectedService.class);
                break;

            case DISCONTINUE_ACTION:
                Log.d(LOG_TAG, "discontinue action");
                startServiceWithAction(context, DISCONTINUE_ACTION, CarConnectedService.class);
                inCarAlreadyPerformed = false;
                break;

            case PLAY_MUSIC_ACTION:
                Log.d(LOG_TAG, "play music action");
                startServiceWithAction(context, PLAY_MUSIC_ACTION, CarConnectedService.class);
                break;

            case DISABLE_LOCK_SCREEN_ACTION:
                Log.d(LOG_TAG, "disable lock screen action");
                startServiceWithAction(context, DISABLE_LOCK_SCREEN_ACTION, CarConnectedService.class);
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                BluetoothDevice bluetoothDeviceDisconnected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(LOG_TAG, "bluetoothDevDisconnected: " + bluetoothDeviceDisconnected.getAddress());
                Log.d(LOG_TAG, "SharedPrefDevice: " + Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).toString());

                if (Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).contains(bluetoothDeviceDisconnected.getAddress()) &&
                        inCarAlreadyPerformed && !Logic.isMyServiceRunning(CarConnectedService.class, context) &&
                        !PopupConnectedActivity.isInFront) {
                    inCarAlreadyPerformed = false;
                    context.stopService(new Intent(context, ForceAutoRotationService.class));
                }

                break;

            case BluetoothAdapter.ACTION_STATE_CHANGED:
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, "Bluetooth off", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(context, "Bluetooth on", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    protected void startServiceWithAction(Context context, String action, Class<?> cls) {
        intent = new Intent(context, cls);
        intent.setAction(action);
        context.startService(intent);
    }

    protected void sendBroadcastAction(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }
}

