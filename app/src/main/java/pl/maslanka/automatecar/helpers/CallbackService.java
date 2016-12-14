package pl.maslanka.automatecar.helpers;

import android.app.Service;
import android.content.ServiceConnection;

/**
 * Created by Artur on 14.12.2016.
 */

abstract public class CallbackService extends Service {

    public abstract void callback(String action, int startId);

}
