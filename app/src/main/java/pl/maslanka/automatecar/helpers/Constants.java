package pl.maslanka.automatecar.helpers;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.utils.MyApplication;

/**
 * Created by Artur on 08.11.2016.
 */

public class Constants {

    public static final String DISMISS_LOCK_SCREEN_SU_COMMAND = "su -c input keyevent USER";
    public static final String DISMISS_LOCK_SCREEN_SU_COMMAND_ALTERNATIVE = "su -c input keyevent 82";
    public static final String INTENT_EXTRA_RESULT_CODE = "intent_extra_result_code";
    public static final String SYSTEM_UI_PACKAGE_NAME = "com.android.systemui";
    public static final String TRIGGER_BLUETOOTH = MyApplication.getAppContext().getResources().getStringArray(R.array.triggers_entryValues)[0];
    public static final String TRIGGER_NFC = MyApplication.getAppContext().getResources().getStringArray(R.array.triggers_entryValues)[1];
    public static final int SLEEP_BETWEEN_BUTTON_PRESS = 500;
    public static final int WAIT_FOR_MUSIC_PLAY = 2000;

    public interface FILE_NAMES {
        String PATH = "appList";
        String FILE_NAME = "appList.obj";
    }

    public interface SELECT_APPS_FRAGMENT {
        String TAG_SELECT_APPS_FRAGMENT = "select_apps_fragment";
    }

    public interface POPUP_CONNECTED_FRAGMENT {
        String TAG_POPUP_CONNECTED_FRAGMENT = "popup_connected_fragment";
    }

    public interface DEFAULT_VALUES {
        int START_ID_NO_VALUE = -1;
        boolean FIRST_RUN_DEFAULT_VALUE = true;

        int DIALOG_DURATION_IN_CAR_MAX_VALUE = 600;
        int DIALOG_DURATION_IN_CAR_MIN_VALUE = 1;
        int SLEEP_TIMES_IN_CAR_MIN_VALUE = 1;
        int SLEEP_TIMES_IN_CAR_MAX_VALUE = 20;

        boolean SHOW_CANCEL_DIALOG_IN_CAR_DEFAULT_VALUE = true;
        int DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE = 10;
        boolean ACTION_DIALOG_TIMEOUT_IN_CAR_DEFAULT_VALUE = true;
        boolean FORCE_AUTO_ROTATION_IN_CAR_DEFAULT_VALUE = false;
        boolean CHANGE_WIFI_STATE_IN_CAR_DEFAULT_VALUE = false;
        boolean WIFI_ENABLE_IN_CAR_DEFAULT_VALUE = false;
        boolean KEY_CHANGE_MOBILE_DATA_STATE_IN_CAR_DEFAULT_VALUE = false;
        boolean KEY_MOBILE_DATA_ENABLE_IN_CAR_DEFAULT_VALUE = false;
        int SLEEP_TIMES_IN_CAR_DEFAULT_VALUE = 8;
        boolean PLAY_MUSIC_IN_CAR_DEFAULT_VALUE = false;
        boolean PLAY_MUSIC_ON_A2DP_IN_CAR_DEFAULT_VALUE = false;
        boolean SET_MEDIA_VOLUME_IN_CAR_DEFAULT_VALUE = false;
        int MEDIA_VOLUME_LEVEL_IN_CAR_DEFAULT_VALUE = 50;
        boolean CHECK_IF_IN_POCKET_IN_CAR_DEFAULT_VALUE = false;
        boolean SHOW_NAVI_IN_CAR_DEFAULT_VALUE =  true;

        boolean DISMISS_LOCK_SCREEN_IN_CAR_DEFAULT_VALUE = false;
        boolean NFC_DOCK_TRIGGER_DEFAULT_VALUE = false;
        boolean WIRELESS_LOADING_DOCK_TRIGGER_DEFAULT_VALUE = false;


    }

    public interface PREF_KEYS {
        String KEY_FIRST_RUN = "first_run";

        String KEY_TRIGGER_TYPE_IN_CAR = "trigger_type_in_car";
        String KEY_BLUETOOTH_DEVICES_ADDRESSES_IN_CAR = "bluetooth_devices_addresses_in_car";
        String KEY_NFC_TAGS_IN_CAR = "nfc_tags_in_car";
        String KEY_SHOW_CANCEL_DIALOG_IN_CAR = "show_cancel_dialog_in_car";
        String KEY_DIALOG_TIMEOUT_IN_CAR = "dialog_timeout_in_car";
        String KEY_ACTION_DIALOG_TIMEOUT_IN_CAR = "action_dialog_timeout_in_car";
        String KEY_FORCE_AUTO_ROTATION_IN_CAR = "force_auto_rotation_in_car";
        String KEY_ROTATION_EXCLUDED_APPS_IN_CAR = "rotation_excluded_apps_in_car";
        String KEY_CHANGE_WIFI_STATE_IN_CAR = "change_wifi_state_in_car";
        String KEY_WIFI_ENABLE_IN_CAR = "wifi_enable_in_car";
        String KEY_CHANGE_MOBILE_DATA_STATE_IN_CAR = "change_mobile_data_state_in_car";
        String KEY_MOBILE_DATA_ENABLE_IN_CAR = "mobile_data_enable_in_car";
        String KEY_APPS_TO_LAUNCH_IN_CAR = "apps_to_launch_in_car";
        String KEY_SLEEP_TIMES_IN_CAR = "sleep_times_in_car";
        String KEY_PLAY_MUSIC_IN_CAR = "play_music_in_car";
        String KEY_SELECT_MUSIC_PLAYER_IN_CAR = "select_music_player_in_car";
        String KEY_PLAY_MUSIC_ON_A2DP_IN_CAR = "play_music_on_a2dp_in_car";
        String KEY_SET_MEDIA_VOLUME_IN_CAR = "set_media_volume_in_car";
        String KEY_MEDIA_VOLUME_LEVEL_IN_CAR = "media_volume_level_in_car";
        String KEY_CHECK_IF_IN_POCKET_IN_CAR = "check_if_in_pocket_in_car";
        String KEY_SHOW_NAVI_IN_CAR = "show_navi_in_car";

        String KEY_DISMISS_LOCK_SCREEN = "dismiss_lock_screen";
        String KEY_NFC_DOCK_TRIGGER = "nfc_dock_trigger";
        String KEY_WIRELESS_LOADING_DOCK_TRIGGER = "wireless_loading_dock_trigger";

    }


    public interface ACTION {
       String MAIN_ACTION = "com.maslanka.automatecar.action.main";
       String START_FOREGROUND_ACTION = "com.maslanka.automatecar.startforeground";
       String STOP_FOREGROUND_ACTION = "com.maslanka.automatecar..stopforeground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface BROADCAST_NOTIFICATIONS {
        String PROXIMITY_CHECK_ACTION = "com.maslanka.automatecar.proximity.check.action";
        String FORCE_ROTATION_ACTION = "com.maslanka.automatecar.force.rotation.action";
        String POPUP_CONNECTED_ACTION = "com.maslanka.automatecar.popup.connected.action";
        String CONTINUE_CONNECTED_ACTION = "com.maslanka.automatecar.continue.connected.action";
        String DISCONTINUE_CONNECTED_ACTION = "com.maslanka.automatecar.discontinue.action";
        String CHANGE_WIFI_STATE_ACTION = "com.maslanka.automatecar.change.wifi.state.action";
        String CHANGE_MOBILE_DATA_STATE_ACTION = "com.maslanka.automatecar.change.mobile.data.state.action";
        String SET_MEDIA_VOLUME_ACTION = "com.maslanka.automatecar.set.media.volume.action";
        String PLAY_MUSIC_ACTION = "com.maslanka.automatecar.play.music.action";
        String SHOW_NAVI_ACTION = "com.maslanka.automatecar.show.navi.action";
    }

    public interface CALLBACK_ACTIONS {
        String START_ID = "com.maslanka.automatecar.start.id";
        String PROXIMITY_CHECK_COMPLETED = "com.maslanka.automatecar.proximity.check.completed";
        String FORCE_ROTATION_COMPLETED = "com.maslanka.automatecar.force.rotation.completed";
        String POPUP_CONNECTED_FINISH_CONTINUE = "com.maslanka.automatecar.popup.connected.finish.continue";
        String POPUP_CONNECTED_FINISH_DISCONTINUE = "com.maslanka.automatecar.popup.connected.finish.discontinue";
        String LAUNCH_APPS_COMPLETED = "com.maslanka.automatecar.launch.apps.completed";
        String CHANGE_WIFI_STATE_COMPLETED = "com.maslanka.automatecar.change.wifi.state.completed";
        String CHANGE_MOBILE_DATA_STATE_COMPLETED = "com.maslanka.automatecar.change.mobile.data.state.completed";
        String SET_MEDIA_VOLUME_COMPLETED = "com.maslanka.automatecar.set.media.volume.completed";
        String PLAY_MUSIC_COMPLETED = "com.maslanka.automatecar.play.music.completed";
        String SHOW_NAVI_COMPLETED = "com.maslanka.automatecar.show.navi.completed";
    }
}
