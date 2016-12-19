package pl.maslanka.automatecar.prefother;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import pl.maslanka.automatecar.prefconnected.PrefsCarConnectedFragment;

/**
 * Created by Artur on 18.12.2016.
 */

public class PrefsOther extends AppCompatActivity {

    private AppCompatDelegate mDelegate;
    private PrefsOtherFragment prefsOtherFragment;

    private static final String TAG_PREFS_OTHER_FRAGMENT = "prefs_other_fragment";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            prefsOtherFragment = new PrefsOtherFragment();
            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, prefsOtherFragment, TAG_PREFS_OTHER_FRAGMENT).commit();
        } else {
            prefsOtherFragment = (PrefsOtherFragment) getSupportFragmentManager().findFragmentByTag(TAG_PREFS_OTHER_FRAGMENT);
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

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }
}
