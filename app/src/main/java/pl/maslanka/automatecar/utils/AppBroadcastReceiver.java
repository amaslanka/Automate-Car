package pl.maslanka.automatecar.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pl.maslanka.automatecar.helpers.CarConnectedProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.services.CarConnectedService;
import pl.maslanka.automatecar.services.ForceAutoRotationService;

/**
 * Created by Artur on 21.11.2016.
 */

public class AppBroadcastReceiver extends android.content.BroadcastReceiver implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS, Constants.DEFAULT_VALUES {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private Intent intent;


    public AppBroadcastReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent){
        final String action = intent.getAction();
        switch (action) {

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                BluetoothDevice bluetoothDeviceConnected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(LOG_TAG, "bluetoothDevCon: " + bluetoothDeviceConnected.getAddress());
                Log.d(LOG_TAG, "SharedPrefDevice: " + Logic
                        .getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).toString());

                if (Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).contains(bluetoothDeviceConnected.getAddress()) &&
                        Logic.getCarConnectedProcessState() == CarConnectedProcessState.NOT_STARTED) {
                    startServiceWithAction(context, PROXIMITY_CHECK_ACTION, CarConnectedService.class);
                }

                break;

            case FORCE_ROTATION_ACTION:
                Log.d(LOG_TAG, "force rotation action");
                startServiceWithAction(context, FORCE_ROTATION_ACTION, CarConnectedService.class);
                break;

            case POPUP_CONNECTED_ACTION:
                Log.d(LOG_TAG, "popup connected action");
                startServiceWithAction(context, POPUP_CONNECTED_ACTION, CarConnectedService.class);
                break;

            case CONTINUE_CONNECTED_ACTION:
                Log.d(LOG_TAG, "continue action");
                startServiceWithAction(context, CONTINUE_CONNECTED_ACTION, CarConnectedService.class);
                break;

            case DISCONTINUE_CONNECTED_ACTION:
                Log.d(LOG_TAG, "discontinue action");
                startServiceWithAction(context, DISCONTINUE_CONNECTED_ACTION, CarConnectedService.class);
                break;

            case PLAY_MUSIC_ACTION:
                Log.d(LOG_TAG, "play music action");
                startServiceWithAction(context, PLAY_MUSIC_ACTION, CarConnectedService.class);
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:

                BluetoothDevice bluetoothDeviceDisconnected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(LOG_TAG, "bluetoothDevDisconnected: " + bluetoothDeviceDisconnected.getAddress());
                Log.d(LOG_TAG, "SharedPrefDevice: " + Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).toString());

                if (Logic.getSharedPrefStringSet(context,
                        KEY_SELECT_BLUETOOTH_DEVICES).contains(bluetoothDeviceDisconnected.getAddress()) &&
                        Logic.getCarConnectedProcessState() == CarConnectedProcessState.COMPLETED &&
                        !Logic.isMyServiceRunning(CarConnectedService.class, context)) {
                    restoreDefaultValues();
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

            case Intent.ACTION_SCREEN_ON:
                Log.d(LOG_TAG, "screen on action");
                Actions.dismissLockScreen(MyApplication.getAppContext());
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

    private void restoreDefaultValues() {
        Logic.setCarConnectedProcessState(CarConnectedProcessState.NOT_STARTED);
        Logic.setProximityState(ProximityState.NOT_TESTED);
        Logic.setStartWithProximityFarPerformed(false);
    }
}

