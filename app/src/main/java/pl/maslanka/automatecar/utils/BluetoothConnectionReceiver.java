package pl.maslanka.automatecar.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.services.CarConnectedService;

/**
 * Created by Artur on 21.11.2016.
 */

public class BluetoothConnectionReceiver extends BroadcastReceiver implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS {


    public BluetoothConnectionReceiver(){
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
                        !Logic.isMyServiceRunning(CarConnectedService.class, context)) {

                    Intent carConnectedService = new Intent(context, CarConnectedService.class);
                    carConnectedService.setAction(POPUP_ACTION);
                    context.startService(carConnectedService);
                }
                break;

            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                //Do something with bluetooth device disconnection
                break;

            case CONTINUE_ACTION:
                Log.d("onReceive", "continue action");
                Intent carConnectedService = new Intent(context, CarConnectedService.class);
                carConnectedService.setAction(CONTINUE_ACTION);
                context.startService(carConnectedService);
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
}
