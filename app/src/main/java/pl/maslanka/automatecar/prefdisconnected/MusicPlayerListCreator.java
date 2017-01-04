package pl.maslanka.automatecar.prefdisconnected;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import java.util.ArrayList;
import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.prefdisconnected.adapters.MusicPlayerAdapterWithIcon;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 25.11.2016.
 */

public class MusicPlayerListCreator extends AsyncTask<Activity, Void, Void> implements Constants.PREF_KEYS{

    private final String LOG_TAG = this.getClass().getSimpleName();

    private PreferenceFragment prefsCarDisconnectedFragment;
    private List<ApplicationInfo> installedMusicPlayers;
    private List<Drawable> appIcons;
    private List<String> appNames;
    private List<String> appPackages;
    private MusicPlayerAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog musicPlayerList;
    private PackageManager pm;


    public MusicPlayerListCreator(PreferenceFragment prefsCarDisconnectedFragment) {
        this.prefsCarDisconnectedFragment = prefsCarDisconnectedFragment;
    }

    @Override
    protected void onPreExecute() {
        ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment).showNewProgressDialog();
    }


    @Override
    protected Void doInBackground(Activity... params) {

        Activity activity = params[0];

        try {
            pm = activity.getPackageManager();
            installedMusicPlayers = Logic.getListOfInstalledMusicPlayers(activity);
            appIcons = new ArrayList<>();
            appNames = new ArrayList<>();
            appPackages = new ArrayList<>();

            createAppListData(activity);

            adapter = new MusicPlayerAdapterWithIcon(appNames, appIcons, activity);


            builder = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.select_music_player))
                    .setAdapter(adapter, null);

        } catch (NullPointerException ex) {
            Log.e(LOG_TAG, "Error: Fragment not attached to an activity - task cancelled");
            ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment).dismissProgressDialog();
            this.cancel(true);
            ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment).startMusicPlayerListCreator();
        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i(LOG_TAG, "Method executed - onCancelled()!");
    }

    @Override
    protected void onPostExecute(Void parameter) {

        musicPlayerList = builder.create();

        musicPlayerList.getListView().setAdapter(adapter);
        musicPlayerList.getListView().setItemsCanFocus(false);
        musicPlayerList.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        musicPlayerList.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Logic.setSharedPrefString(prefsCarDisconnectedFragment.getActivity(),
                            appPackages.get(position), KEY_SELECT_MUSIC_PLAYER_OUT_CAR);

                ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment)
                        .setMusicPlayerSummary(appPackages.get(position));
                ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment).dismissMusicPlayerList();
            }
        });

        ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment).showMusicPlayerList(musicPlayerList);

        ((PrefsCarDisconnectedFragment) prefsCarDisconnectedFragment).dismissProgressDialog();
    }

    private void createAppListData(Activity activity) {
        appNames.add(activity.getResources().getString(R.string.clear_selection));
        appIcons.add(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.close_circle, null));
        appPackages.add(null);
        for (int i = 0; i < installedMusicPlayers.size(); i++) {
            appNames.add(pm.getApplicationLabel(installedMusicPlayers.get(i)).toString());
            appIcons.add(pm.getApplicationIcon(installedMusicPlayers.get(i)));
            appPackages.add(installedMusicPlayers.get(i).packageName);
        }
    }

}
