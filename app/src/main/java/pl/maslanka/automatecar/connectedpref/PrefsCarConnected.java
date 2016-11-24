package pl.maslanka.automatecar.connectedpref;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import pl.maslanka.automatecar.helperobjectsandinterfaces.Constants;

/**
 * Created by Artur on 09.11.2016.
 */

public class PrefsCarConnected extends AppCompatActivity implements Constants.APP_CREATOR_FRAGMENT {

    private AppCompatDelegate mDelegate;
    private PrefsCarConnectedFragment prefsCarConnectedFragment;


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
}
