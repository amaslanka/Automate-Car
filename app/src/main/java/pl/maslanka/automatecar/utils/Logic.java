package pl.maslanka.automatecar.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;
import pl.maslanka.automatecar.helperobjectsandinterfaces.PairObject;

/**
 * Created by Artur on 09.11.2016.
 */

public class Logic implements Constants.PREF_KEYS, Constants.FILE_NAMES {

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private static Set blankAppsToLaunch;


    public BluetoothAdapter getMBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public Set<BluetoothDevice> getPairedDevices() {
        return pairedDevices;
    }

    public Logic() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Set<BluetoothDevice> queryBluetoothDevices() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.d("Bluetooth devices", pairedDevices.toString());
        return pairedDevices;
    }

    public List<String[]> getBluetoothDevicesArrays() {

        List<String[]> resultList = new ArrayList<>();
        BluetoothDevice[] bluetoothDevicesArray = new BluetoothDevice[queryBluetoothDevices().size()];
        String[] bluetoothDeviceNamesAndAddresses = new String[bluetoothDevicesArray.length];
        String[] bluetoothDeviceAddresses = new String[bluetoothDevicesArray.length];

        queryBluetoothDevices().toArray(bluetoothDevicesArray);

        for (int i=0; i<bluetoothDevicesArray.length; i++) {
            bluetoothDeviceNamesAndAddresses[i] = bluetoothDevicesArray[i].getName() +"\n"
                    + bluetoothDevicesArray[i].getAddress();
            bluetoothDeviceAddresses[i] = bluetoothDevicesArray[i].getAddress();
            Log.d("bluetoothDevicesNamesAn", bluetoothDeviceNamesAndAddresses[i]);
            Log.d("bluetoothDevicesAddr", bluetoothDeviceAddresses[i]);
        }

        resultList.add(bluetoothDeviceNamesAndAddresses);
        resultList.add(bluetoothDeviceAddresses);

        return resultList;
    }

    public static List<ApplicationInfo> getListOfInstalledApps(Activity activity) {
        final PackageManager pm = activity.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<ApplicationInfo> installedApps = new ArrayList<>();

        for(ApplicationInfo app : packages) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                installedApps.add(app);
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else if(app.packageName.equals(activity.getPackageName())) {
                //Discard this one
            } else {
                installedApps.add(app);
            }
        }

        Collections.sort(installedApps, new ApplicationInfo.DisplayNameComparator(pm));

        return installedApps;
    }

    public static Set getSharedPrefAppList(Context context) {
        blankAppsToLaunch = new HashSet<>();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        Set result = prefs.getStringSet(KEYS_APPS_TO_LAUNCH, null);
        return result == null ? blankAppsToLaunch : result;

    }

    public static void setSharedPrefAppList(Context context, Set<String> appName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(KEYS_APPS_TO_LAUNCH, appName);
        editor.apply();

        Set set  = preferences.getStringSet(KEYS_APPS_TO_LAUNCH, null);
        if (set != null)
            Log.d("Saved apps", set.toString());
    }

    public static void saveToInternalStorage(Activity activity, LinkedList<PairObject<String,
            String>> appList) {

        ContextWrapper cw = new ContextWrapper(activity);
        File directory = cw.getDir(PATH, Context.MODE_PRIVATE);
        File myPath = new File(directory, FILE_NAME);

        FileOutputStream fos;
        ObjectOutputStream out;

        try {
            fos = new FileOutputStream(myPath);
            out = new ObjectOutputStream(fos);
            out.writeObject(appList);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LinkedList<PairObject<String, String>> readList(Activity activity) {

        LinkedList<PairObject<String, String>> appList = new LinkedList<>();
        ContextWrapper cw = new ContextWrapper(activity);
        File directory = cw.getDir(PATH, Context.MODE_PRIVATE);
        File myPath = new File(directory, FILE_NAME);

        FileInputStream fis;
        ObjectInputStream in;

        try {
            fis = new FileInputStream(myPath);
            in = new ObjectInputStream(fis);
            appList = (LinkedList<PairObject<String, String>>) in.readObject();
            in.close();
        } catch (IOException ex) {
            Log.d("readList", "File not found!");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        if (appList != null) {
            return appList;
        } else {
            appList = new LinkedList<>();
            return appList;
        }

    }
}
