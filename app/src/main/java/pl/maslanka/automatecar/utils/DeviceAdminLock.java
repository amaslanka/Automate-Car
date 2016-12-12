package pl.maslanka.automatecar.utils;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import pl.maslanka.automatecar.R;

/**
 * Created by Artur on 29.11.2016.
 */

public class DeviceAdminLock extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.message_on_disable_admin_rights_request);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
    }

}