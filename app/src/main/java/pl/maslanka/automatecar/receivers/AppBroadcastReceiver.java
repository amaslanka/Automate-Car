package pl.maslanka.automatecar.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.maslanka.automatecar.helpers.ConnectingProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.services.CarConnectedService;
import pl.maslanka.automatecar.services.CarDisconnectedService;
import pl.maslanka.automatecar.services.ForceAutoRotationService;
import pl.maslanka.automatecar.utils.Actions;
import pl.maslanka.automatecar.utils.Logic;
import pl.maslanka.automatecar.utils.MyApplication;

import static pl.maslanka.automatecar.utils.Logic.startServiceWithAction;

/**
 * Created by Artur on 21.11.2016.
 */

public class AppBroadcastReceiver extends android.content.BroadcastReceiver implements Constants.PREF_KEYS, Constants.BROADCAST_NOTIFICATIONS, Constants.DEFAULT_VALUES {

    private final String LOG_TAG = this.getClass().getSimpleName();
    public static final Set<String> connectedBluetoothDevices = new HashSet<>();


    public AppBroadcastReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent){
        final String action = intent.getAction();
        switch (action) {

            case BluetoothDevice.ACTION_ACL_CONNECTED:
                BluetoothDevice bluetoothDeviceConnected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Set triggerType = Logic.getSharedPrefStringSet(context, KEY_TRIGGER_TYPE_IN_CAR);

                Set<String> sharedPrefBtDeviceAddresses =
                        Logic.getSharedPrefStringSet(context, KEY_BLUETOOTH_DEVICES_ADDRESSES_IN_CAR);

                Log.d(LOG_TAG, "bluetoothDevCon: " + bluetoothDeviceConnected.getAddress());
                Log.d(LOG_TAG, "SharedPrefDevice: " + sharedPrefBtDeviceAddresses.toString());

                if (sharedPrefBtDeviceAddresses.contains(bluetoothDeviceConnected.getAddress())) {
                    connectedBluetoothDevices.add(bluetoothDeviceConnected.getAddress());
                }

                if (triggerType.contains(Constants.TRIGGER_BLUETOOTH)
                        && sharedPrefBtDeviceAddresses.contains(bluetoothDeviceConnected.getAddress())
                        && Logic.getCarConnectedProcessState() == ConnectingProcessState.NOT_STARTED) {

                    if (Logic.getCarDisconnectedProcessState() == ConnectingProcessState.PERFORMING) {
                        CarDisconnectedService.setCanceled(true);
                    }

                    startServiceWithAction(context, PROXIMITY_CHECK_ACTION, CarConnectedService.class);
                }

                break;


            case BluetoothDevice.ACTION_ACL_DISCONNECTED:

                BluetoothDevice bluetoothDeviceDisconnected = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Set triggerTypeDisconnected = Logic.getSharedPrefStringSet(context, KEY_TRIGGER_TYPE_OUT_CAR);

                Set<String> sharedPrefBtDeviceAddressesOut =
                        Logic.getSharedPrefStringSet(context, KEY_BLUETOOTH_DEVICES_ADDRESSES_OUT_CAR);

                Log.d(LOG_TAG, "bluetoothDevDisconnected: " + bluetoothDeviceDisconnected.getAddress());
                Log.d(LOG_TAG, "SharedPrefDevice: " + sharedPrefBtDeviceAddressesOut.toString());
                Log.d(LOG_TAG, "gic.getCarConnectedProcessState() " + Logic.getCarConnectedProcessState() );
                Log.d(LOG_TAG, "triggerTypeDisconnected: " + triggerTypeDisconnected);

                connectedBluetoothDevices.remove(bluetoothDeviceDisconnected.getAddress());


                if (triggerTypeDisconnected.contains(Constants.TRIGGER_BLUETOOTH)
                        && sharedPrefBtDeviceAddressesOut.contains(bluetoothDeviceDisconnected.getAddress())
                        && Logic.getCarDisconnectedProcessState() == ConnectingProcessState.NOT_STARTED) {

                    if (Logic.getCarConnectedProcessState() == ConnectingProcessState.PERFORMING) {
                        // stop connecting process and start disconnecting
                        CarConnectedService.setCanceled(true);
                        startServiceWithAction(context, WAIT_FOR_RECONNECTION_ACTION, CarDisconnectedService.class);
                    } else if (Logic.getCarConnectedProcessState() == ConnectingProcessState.COMPLETED) {
                        startServiceWithAction(context, WAIT_FOR_RECONNECTION_ACTION, CarDisconnectedService.class);
                    }
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

    protected void sendBroadcastAction(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }

    public static void restoreDefaultValues() {
        Logic.setCarConnectedProcessState(ConnectingProcessState.NOT_STARTED);
        Logic.setCarDisconnectedProcessState(ConnectingProcessState.NOT_STARTED);
        Logic.setProximityState(ProximityState.NOT_TESTED);
        Logic.setStartWithProximityFarPerformed(false);
    }
}

