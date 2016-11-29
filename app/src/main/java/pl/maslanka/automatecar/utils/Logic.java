package pl.maslanka.automatecar.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;

/**
 * Created by Artur on 09.11.2016.
 */

public class Logic implements Constants.PREF_KEYS, Constants.FILE_NAMES {


    public Logic() {

    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Set<BluetoothDevice> queryBluetoothDevices() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        Log.d("Bluetooth devices", pairedDevices.toString());
        return pairedDevices;
    }

    public static List<String[]> getBluetoothDevicesArrays() {

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

    public static List<ApplicationInfo> getListOfInstalledMusicPlayers (Activity activity) {
        final PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> musicPlayersResolveInfo;
        List<ApplicationInfo> allApps;
        List<String> musicPlayerPackages = new ArrayList<>();
        List<ApplicationInfo> musicPlayers = new ArrayList<>();

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        musicPlayersResolveInfo = pm.queryBroadcastReceivers(mediaButtonIntent,
                PackageManager.GET_RESOLVED_FILTER);

        allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ResolveInfo resolveInfo: musicPlayersResolveInfo) {
            musicPlayerPackages.add(resolveInfo.activityInfo.packageName);
        }

        Log.d("allPackagesSize", Integer.toString(allApps.size()));

        for(ApplicationInfo app : allApps) {
            if (musicPlayerPackages.contains(app.packageName))
                musicPlayers.add(app);
        }

        Collections.sort(musicPlayers, new ApplicationInfo.DisplayNameComparator(pm));

        Log.d("musicPlayersSize", Integer.toString(musicPlayers.size()));

        return musicPlayers;
    }

    public static List<ActivityInfo> getListOfMediaBroadcastReceivers(Context activity) {
        final PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> musicPlayersResolveInfo;
        List<ActivityInfo> musicPlayersActivityInfo = new ArrayList<>();

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        musicPlayersResolveInfo = pm.queryBroadcastReceivers(mediaButtonIntent,
                PackageManager.GET_RESOLVED_FILTER);

        for (ResolveInfo resolveInfo: musicPlayersResolveInfo) {
            musicPlayersActivityInfo.add(resolveInfo.activityInfo);
        }

        return musicPlayersActivityInfo;
    }

    public static void setSharedPrefString(Context context, String string, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, string);
        editor.apply();
    }

    public static void setSharedPrefStringSet(Context context, Set<String> set, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(key, set);
        editor.apply();

        Set readSet  = preferences.getStringSet(key, null);
        if (readSet != null)
            Log.d("Saved SharedPref set", readSet.toString());
    }

    public static boolean getSharedPrefBoolean(Context context, String key, boolean defaultValue) {
        boolean result;
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        result = prefs.getBoolean(key, defaultValue);
        return result;

    }

    public static String getSharedPrefString(Context context, String key, String defaultValue) {
        String result;
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        result = prefs.getString(key, defaultValue);
        return result;

    }

    public static Set<String> getSharedPrefStringSet(Context context, String key) {
        Set<String> blankSet = new HashSet<>();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> result = prefs.getStringSet(key, null);
        return result == null ? blankSet : result;

    }

    public static void saveListToInternalStorage(Context context, LinkedList<PairObject<String,
            String>> appList) {

        ContextWrapper cw = new ContextWrapper(context);
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

    public static LinkedList<PairObject<String, String>> readList(Context context) {

        LinkedList<PairObject<String, String>> appList = new LinkedList<>();
        ContextWrapper cw = new ContextWrapper(context);
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
