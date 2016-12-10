package pl.maslanka.automatecar.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

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

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.connected.DeviceAdminLock;
import pl.maslanka.automatecar.helpers.ActivityForResult;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.PairObject;
import pl.maslanka.automatecar.services.FAutoRotationAccessibilityService;

/**
 * Created by Artur on 09.11.2016.
 */

public class Logic implements Constants.PREF_KEYS, Constants.FILE_NAMES {

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

    public static List<ApplicationInfo> getListOfAllInstalledApps(Activity activity) {
        final PackageManager pm = activity.getPackageManager();
        List<ApplicationInfo> allInstalledApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        Collections.sort(allInstalledApps, new ApplicationInfo.DisplayNameComparator(pm));

        return allInstalledApps;
    }

    public static List<ApplicationInfo> getListOfUserInstalledApps(Activity activity) {
        final PackageManager pm = activity.getPackageManager();
        List<ApplicationInfo> allInstalledApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<ApplicationInfo> userInstalledApps = new ArrayList<>();

        for(ApplicationInfo app : allInstalledApps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                userInstalledApps.add(app);
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else if(app.packageName.equals(activity.getPackageName())) {
                //Discard this one
            } else {
                userInstalledApps.add(app);
            }
        }

        Collections.sort(userInstalledApps, new ApplicationInfo.DisplayNameComparator(pm));

        return userInstalledApps;
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

    public static void setSharedPrefBoolean(Context context, boolean value, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(key, value);
        editor.apply();
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

    public static boolean testDeviceAdminPermission(Activity activity) {
        DevicePolicyManager mDPM = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final ComponentName compName = new ComponentName(activity, DeviceAdminLock.class);

        return mDPM != null && mDPM.isAdminActive(compName);
    }

    @SuppressLint("NewApi")
    public static boolean testSystemOverlayPermission(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(activity);
        } else {
            return true;
        }
    }

    public static boolean testAccessibilityPermission(Activity activity) {
        AccessibilityManager mDPM = (AccessibilityManager) activity
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        String runningAccessibilityAppPackage;
        String myAccessibilityAppPackage = activity.getApplicationContext().getPackageName();

        List<AccessibilityServiceInfo> runningServices = mDPM
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);

        for (AccessibilityServiceInfo service : runningServices) {
            runningAccessibilityAppPackage =  service.getResolveInfo().serviceInfo.packageName;
            if (myAccessibilityAppPackage.equals(runningAccessibilityAppPackage)) {
                return true;
            }
        }

        return false;
    }

}
