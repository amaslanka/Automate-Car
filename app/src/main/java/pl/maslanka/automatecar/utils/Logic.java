package pl.maslanka.automatecar.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pl.maslanka.automatecar.helpers.ConnectingProcessState;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.helpers.AppObject;
import pl.maslanka.automatecar.helpers.ProximityState;
import pl.maslanka.automatecar.receivers.AppBroadcastReceiver;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Artur on 09.11.2016.
 */

public class Logic implements Constants.PREF_KEYS, Constants.FILE_NAMES {

    private static final String LOG_TAG = Logic.class.getSimpleName();

    private static ProximityState proximityState = ProximityState.NOT_TESTED;
    private static ConnectingProcessState carConnectedProcessState = ConnectingProcessState.NOT_STARTED;
    private static ConnectingProcessState carDisconnectedProcessState = ConnectingProcessState.NOT_STARTED;
    private static boolean startWithProximityFarPerformed;
    private static String currentForegroundAppPackage = "";

    public static ProximityState getProximityState() {
        return proximityState;
    }

    public static void setProximityState(ProximityState proximityState) {
        Logic.proximityState = proximityState;
    }

    public static ConnectingProcessState getCarConnectedProcessState() {
        return carConnectedProcessState;
    }

    public static void setCarConnectedProcessState(ConnectingProcessState carConnectedProcessState) {
        Logic.carConnectedProcessState = carConnectedProcessState;
    }

    public static ConnectingProcessState getCarDisconnectedProcessState() {
        return carDisconnectedProcessState;
    }

    public static void setCarDisconnectedProcessState(ConnectingProcessState carDisconnectedProcessState) {
        Logic.carDisconnectedProcessState = carDisconnectedProcessState;
    }

    public static String getCurrentForegroundAppPackage() {
        return currentForegroundAppPackage;
    }

    public static void setCurrentForegroundAppPackage(String currentForegroundAppPackage) {
        Logic.currentForegroundAppPackage = currentForegroundAppPackage;
    }

    public static boolean isStartWithProximityFarPerformed() {
        return startWithProximityFarPerformed;
    }

    public static void setStartWithProximityFarPerformed(boolean startWithProximityFarPerformed) {
        Logic.startWithProximityFarPerformed = startWithProximityFarPerformed;
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

    private static Set<BluetoothDevice> queryBluetoothDevices() {
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
        Intent mediaButtonIntent;

        mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        musicPlayersResolveInfo = pm.queryBroadcastReceivers(mediaButtonIntent,
                PackageManager.GET_RESOLVED_FILTER);

        allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ResolveInfo resolveInfo: musicPlayersResolveInfo) {
            musicPlayerPackages.add(resolveInfo.activityInfo.packageName);
        }

        for(ApplicationInfo app : allApps) {
            if (musicPlayerPackages.contains(app.packageName))
                musicPlayers.add(app);
        }

        Collections.sort(musicPlayers, new ApplicationInfo.DisplayNameComparator(pm));

        Log.d(LOG_TAG, "musicPlayersSize: " + Integer.toString(musicPlayers.size()));

        return musicPlayers;
    }

    public static ArrayList<ActivityInfo> getAllActivities(Context context, String packageName) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);
            ArrayList<ActivityInfo> ai = new ArrayList<>(Arrays.asList(pi.activities));

            for (int i = 0; i < ai.size(); i++) {
                if (!ai.get(i).exported) {
                    ai.remove(i);
                    i--;
                }
            }

            return ai;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ActivityInfo> getListOfMediaBroadcastReceivers(Context activity) {
        final PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> musicPlayersResolveInfo;
        List<ActivityInfo> musicPlayersActivityInfo = new ArrayList<>();
        Intent mediaButtonIntent;

        mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
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

    public static void setSharedPrefInt(Context context, int integer, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(key, integer);
        editor.apply();

    }

    public static void setSharedPrefStringSet(Context context, Set<String> set, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(key, set);
        editor.apply();

        Set readSet  = preferences.getStringSet(key, null);
        if (readSet != null)
            Log.d(LOG_TAG, "Saved SharedPref set" + readSet.toString());
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

    public static int getSharedPrefInt(Context context, String key, int defaultValue) {
        int result;
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        result = prefs.getInt(key, defaultValue);
        return result;

    }

    public static Set<String> getSharedPrefStringSet(Context context, String key) {
        Set<String> blankSet = new HashSet<>();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> result = prefs.getStringSet(key, null);
        return result == null ? blankSet : result;

    }

    public static void saveListToInternalStorage(Context context, LinkedList<AppObject> appList, String fileName) {

        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(PATH, Context.MODE_PRIVATE);
        File myPath = new File(directory, fileName);

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

    public static LinkedList<AppObject> readList(Context context, String fileName) {

        LinkedList<AppObject> appList = new LinkedList<>();
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(PATH, Context.MODE_PRIVATE);
        File myPath = new File(directory, fileName);

        FileInputStream fis;
        ObjectInputStream in;

        try {
            fis = new FileInputStream(myPath);
            in = new ObjectInputStream(fis);
            appList = (LinkedList<AppObject>) in.readObject();
            in.close();
        } catch (IOException ex) {
            Log.d("readList", "File not found!");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return appList != null ? appList : new LinkedList<AppObject>();

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

    public static boolean isScreenOn(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    return true;
                }
            }
            return false;
        } else {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            if (powerManager.isScreenOn()){
                return true;
            }
        }

        return false;
    }


    public static void setMobileDataStateBelowLollipop(Context context, boolean mobileDataEnabled) {
        try {
            TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (setMobileDataEnabledMethod != null) {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Error setting mobile data state", ex);
        }
    }

    public static boolean getMobileDataStateBelowLollipop(Context context) {
        try {
            TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (getMobileDataEnabledMethod != null) {
                return (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
            }
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "Error getting mobile data state", ex);
        }

        return false;
    }

    public static void setMobileDataStateFromLollipop(Context context, int state) throws Exception {
        String command = null;
        try {
            String transactionCode = getTransactionCode(context);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager)
                        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    if (transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        int subscriptionId = mSubscriptionManager
                                .getActiveSubscriptionInfoList().get(i).getSubscriptionId();

                        command = "su -c service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        Process process1 = Runtime.getRuntime().exec(command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                if (transactionCode.length() > 0) {
                    command = "su -c service call phone " + transactionCode + " i32 " + state;
                    Process process1 = Runtime.getRuntime().exec(command);
                }
            }
        } catch(Exception e) {
            Log.e(LOG_TAG, "Exception occurred while changing mobile data state!" + "\n" + e);
        }
    }

    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

    @Nullable
    public static List<UsageStats> getForegroundTasksApiAtLeast22(Context context) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long msInMonth = 2592000000L;
            long beginTime = endTime - msInMonth;
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  beginTime, endTime);
            if (appList != null && appList.size() > 0) {
                for (UsageStats usageStats : appList) {
                    Log.e("ForegroundTask", "Current App in foreground is: " + usageStats.getPackageName());
                }
            }
            return appList;
        }

        return null;
    }

    @Nullable
    public static List<ActivityManager.RunningAppProcessInfo> getForegroundTasksApiLowerThan22(Context context) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.getRunningAppProcesses();
        }

        return null;
    }

    public static boolean isAppInactive(Context context, String packageName) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            Log.e(packageName, Boolean.toString(usm.isAppInactive(packageName)));
            return usm.isAppInactive(packageName);
        }
        return false;
    }

    public static void startServiceWithAction(Context context, String action, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        context.startService(intent);
    }

    public static synchronized boolean checkIfBtDeviceConnected(Context context) {
        Set<String> sharedPrefBtDeviceAddresses =
                Logic.getSharedPrefStringSet(context, KEY_BLUETOOTH_DEVICES_ADDRESSES_IN_CAR);

        for (String device: sharedPrefBtDeviceAddresses) {
            if (AppBroadcastReceiver.connectedBluetoothDevices.contains(device)) {
                return true;
            }
        }

        return false;
    }


}
