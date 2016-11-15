package pl.maslanka.automatecar;

/**
 * Created by Artur on 08.11.2016.
 */

public class Constants {

    interface APP_CREATOR_FRAGMENT {
        String TAG_APP_CREATOR_FRAGMENT = "app_creator_fragment";
    }

    interface DIALOG_DURATION_MAX_MIN_VALUES {
        int DIALOG_DURATION_MAX_VALUE = 600;
        int DIALOG_DURATION_MIN_VALUE = 1;
    }

    interface PREF_KEYS {
        String KEY_SELECT_BLUETOOTH_DEVICES = "select_bluetooth_devices";
        String KEY_DISABLE_LOCK_SCREEN = "disable_lock_screen";
        String KEY_FORCE_AUTO_ROTATION = "force_auto_rotation";
        String KEY_CHECK_IF_IN_POCKET = "check_if_in_pocket";
        String KEY_CHECK_WIRELESS_POWER_SUPPLY = "check_wireless_power_supply";
        String KEY_CHECK_NFC_TAG = "check_nfc_tag";
        String KEY_SHOW_CANCEL_DIALOG = "show_cancel_dialog";
        String KEY_DIALOG_TIMEOUT = "dialog_timeout";
        String KEY_ACTION_DIALOG_TIMEOUT = "action_dialog_timeout";
        String KEYS_APPS_TO_LAUNCH = "apps_to_launch";
        String KEY_SHOW_NAVI = "show_navi";
    }


    interface ACTION {
       String MAIN_ACTION = "com.maslanka.automatecar.action.main";
       String STARTFOREGROUND_ACTION = "com.maslanka.automatecar.startforeground";
       String STOPFOREGROUND_ACTION = "com.maslanka.automatecar..stopforeground";
    }

    interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
