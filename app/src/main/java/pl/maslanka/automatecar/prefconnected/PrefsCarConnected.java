package pl.maslanka.automatecar.prefconnected;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import pl.maslanka.automatecar.helpers.Constants;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarConnected extends AppCompatActivity implements Constants.APP_CREATOR_FRAGMENT {

    private AppCompatDelegate mDelegate;
    private PrefsCarConnectedFragment prefsCarConnectedFragment;

    private static final String KEY_MUSIC_PLAYER_LIST_WAS_SHOWING = "music_player_list_was_showing";
    private static final String KEY_ROTATION_EXCLUDED_APPS_LIST_WAS_SHOWING =
            "rotation_excluded_apps_list_was_showing";

    private boolean musicPlayerListWasShowing;
    private boolean rotationExcludedAppListWasShowing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            prefsCarConnectedFragment = new PrefsCarConnectedFragment();
            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, prefsCarConnectedFragment, TAG_APP_CREATOR_FRAGMENT).commit();
        } else {
            prefsCarConnectedFragment = (PrefsCarConnectedFragment) getSupportFragmentManager().findFragmentByTag(TAG_APP_CREATOR_FRAGMENT);
            musicPlayerListWasShowing = savedInstanceState.getBoolean(KEY_MUSIC_PLAYER_LIST_WAS_SHOWING);
            rotationExcludedAppListWasShowing = savedInstanceState.getBoolean(KEY_ROTATION_EXCLUDED_APPS_LIST_WAS_SHOWING);
        }


    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }


    public AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (prefsCarConnectedFragment.getMusicPlayerList() != null)
            if (prefsCarConnectedFragment.getMusicPlayerList().isShowing())
                prefsCarConnectedFragment.getMusicPlayerList().dismiss();

        if (prefsCarConnectedFragment.getRotationExcludedAppsList() != null)
            if (prefsCarConnectedFragment.getRotationExcludedAppsList().isShowing())
                prefsCarConnectedFragment.getRotationExcludedAppsList().dismiss();

        if (prefsCarConnectedFragment.getDialog() != null)
            if (prefsCarConnectedFragment.getDialog().isShowing())
                prefsCarConnectedFragment.getDialog().dismiss();


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (prefsCarConnectedFragment != null) {
            if (prefsCarConnectedFragment.getMusicPlayerListCreator() != null) {
                if (prefsCarConnectedFragment.getMusicPlayerListCreatorStatus() == AsyncTask.Status.RUNNING)
                    prefsCarConnectedFragment.getDialog().show();

                if (musicPlayerListWasShowing && prefsCarConnectedFragment.getMusicPlayerList() != null)
                    prefsCarConnectedFragment.getMusicPlayerList().show();
            }

            if (prefsCarConnectedFragment.getRotationExcludedAppsListCreator() != null) {
                if (prefsCarConnectedFragment.getRotationExcludedAppsListCreatorStatus() == AsyncTask.Status.RUNNING)
                    prefsCarConnectedFragment.getDialog().show();

                if (rotationExcludedAppListWasShowing && prefsCarConnectedFragment.getRotationExcludedAppsList() != null)
                    prefsCarConnectedFragment.getRotationExcludedAppsList().show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (prefsCarConnectedFragment.getMusicPlayerList() != null)
            savedInstanceState.putBoolean(KEY_MUSIC_PLAYER_LIST_WAS_SHOWING,
                    prefsCarConnectedFragment.getMusicPlayerList().isShowing());

        if (prefsCarConnectedFragment.getRotationExcludedAppsList() != null)
            savedInstanceState.putBoolean(KEY_ROTATION_EXCLUDED_APPS_LIST_WAS_SHOWING,
                    prefsCarConnectedFragment.getRotationExcludedAppsList().isShowing());

        super.onSaveInstanceState(savedInstanceState);
    }
}
