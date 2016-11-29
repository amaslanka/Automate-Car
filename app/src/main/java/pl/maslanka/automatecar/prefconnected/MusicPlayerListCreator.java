package pl.maslanka.automatecar.prefconnected;

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
import pl.maslanka.automatecar.prefconnected.adapters.MusicPlayerAdapterWithIcon;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 25.11.2016.
 */

public class MusicPlayerListCreator extends AsyncTask<Activity, Void, Void> implements Constants.PREF_KEYS{

    private PreferenceFragment prefsCarConnectedFragment;
    private List<ApplicationInfo> installedMusicPlayers;
    private List<Drawable> appIcons;
    private List<String> appNames;
    private List<String> appPackages;
    private MusicPlayerAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog musicPlayerList;
    private PackageManager pm;


    public MusicPlayerListCreator(PreferenceFragment prefsCarConnectedFragment) {
        this.prefsCarConnectedFragment = prefsCarConnectedFragment;
    }

    @Override
    protected void onPreExecute() {
        ((PrefsCarConnectedFragment) prefsCarConnectedFragment).showProgressDialog();
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

            boolean activityNull = true;
            int counter = 0;
            while (activityNull && counter < 50) {
                counter++;
                try {
                    Thread.sleep(100);
                    activity = prefsCarConnectedFragment.getActivity();
                    if (activity != null) {
                        activityNull = false;
                        doInBackground(activity);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (activityNull) {
                Log.e("Error", "Cannot show music players list - no activity");
            }

        }

        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.i("Method executed", "onCancelled()!");
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
                if (appPackages.get(position) != null)
                    Log.d("clicked", appPackages.get(position));

                Logic.setSharedPrefString(prefsCarConnectedFragment.getActivity(),
                            appPackages.get(position), KEY_CHOOSE_MUSIC_PLAYER);
                ((PrefsCarConnectedFragment) prefsCarConnectedFragment).dismissMusicPlayerList();
            }
        });

        ((PrefsCarConnectedFragment) prefsCarConnectedFragment).showMusicPlayerList(musicPlayerList);

        ((PrefsCarConnectedFragment) prefsCarConnectedFragment).dismissProgressDialog();
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
