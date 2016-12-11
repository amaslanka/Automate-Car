package pl.maslanka.automatecar.utils;

import android.app.Application;
import android.content.Context;

import pl.maslanka.automatecar.services.CarConnectedService;

/**
 * Created by Artur on 11.12.2016.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        // Simply add the handler, and that's it! No need to add any code
        // to every activity. Everything is contained in MyLifecycleHandler
        // with just a few lines of code. Now *that's* nice.
        super.onCreate();
        MyApplication.context = getApplicationContext();
        registerActivityLifecycleCallbacks(new CarConnectedService());
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
