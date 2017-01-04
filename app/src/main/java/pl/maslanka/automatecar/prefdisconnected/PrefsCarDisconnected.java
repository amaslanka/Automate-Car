package pl.maslanka.automatecar.prefdisconnected;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarDisconnected extends AppCompatActivity {

    private AppCompatDelegate mDelegate;
    private PrefsCarDisconnectedFragment prefsCarDisconnectedFragment;

    private static final String KEY_MUSIC_PLAYER_LIST_WAS_SHOWING = "music_player_list_was_showing";
    private static final String TAG_PREFS_CAR_DISCONNECTED_FRAGMENT = "prefs_car_disconnected_fragment";


    private boolean musicPlayerListWasShowing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            prefsCarDisconnectedFragment = new PrefsCarDisconnectedFragment();
            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, prefsCarDisconnectedFragment, TAG_PREFS_CAR_DISCONNECTED_FRAGMENT).commit();
        } else {
            prefsCarDisconnectedFragment = (PrefsCarDisconnectedFragment) getSupportFragmentManager().findFragmentByTag(TAG_PREFS_CAR_DISCONNECTED_FRAGMENT);
            musicPlayerListWasShowing = savedInstanceState.getBoolean(KEY_MUSIC_PLAYER_LIST_WAS_SHOWING);
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

        prefsCarDisconnectedFragment.dismissProgressDialog();
        prefsCarDisconnectedFragment.dismissMusicPlayerList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (prefsCarDisconnectedFragment.getMusicPlayerListCreatorStatus() == AsyncTask.Status.RUNNING)
            prefsCarDisconnectedFragment.showProgressDialog();

        if (musicPlayerListWasShowing)
            prefsCarDisconnectedFragment.showMusicPlayerList();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (prefsCarDisconnectedFragment.getMusicPlayerList() != null)
            savedInstanceState.putBoolean(KEY_MUSIC_PLAYER_LIST_WAS_SHOWING,
                    prefsCarDisconnectedFragment.getMusicPlayerList().isShowing());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PrefsCarDisconnectedFragment.PERMISSIONS_REQUEST_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    prefsCarDisconnectedFragment.getChangeMobileDataState().setChecked(true);
                else
                    prefsCarDisconnectedFragment.getChangeMobileDataState().setChecked(false);

            }

        }
    }
}
