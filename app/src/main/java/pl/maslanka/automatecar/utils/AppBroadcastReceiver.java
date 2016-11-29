package pl.maslanka.automatecar.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pl.maslanka.automatecar.connected.PopupActivityConnected;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.services.CarConnectedService;

/**
 * Created by Artur on 21.11.2016.
 */

public class AppBroadcastReceiver extends android.content.BroadcastReceiver implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS {

    private Intent carConnectedService;

    public AppBroadcastReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent){
        final String action = intent.getAction();
        switch (action) {

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d("bluetoothDev", bluetoothDevice.getAddress());
                Log.d("SharedPrefDevice", Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES).toString());

                if (Logic.getSharedPrefStringSet(context, KEY_SELECT_BLUETOOTH_DEVICES)
                        .contains(bluetoothDevice.getAddress()) &&
                        !Logic.isMyServiceRunning(CarConnectedService.class, context) && !PopupActivityConnected.isInFront) {

                    startServiceWithAction(context, POPUP_ACTION);
                }
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                //Do something with bluetooth device disconnection
                break;

            case CONTINUE_ACTION:
                Log.d("onReceive", "continue action");
               startServiceWithAction(context, CONTINUE_ACTION);
                break;

            case PLAY_MUSIC_ACTION:
                Log.d("onReceive", "play music action");
                startServiceWithAction(context, PLAY_MUSIC_ACTION);
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

    protected void startServiceWithAction(Context context, String action) {
        carConnectedService = new Intent(context, CarConnectedService.class);
        carConnectedService.setAction(action);
        context.startService(carConnectedService);
    }
}

