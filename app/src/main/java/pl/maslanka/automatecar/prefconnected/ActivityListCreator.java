package pl.maslanka.automatecar.prefconnected;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pl.maslanka.automatecar.R;
import pl.maslanka.automatecar.helpers.Constants;
import pl.maslanka.automatecar.prefconnected.adapters.ActivityAdapterWithIcon;
import pl.maslanka.automatecar.utils.Logic;

/**
 * Created by Artur on 25.11.2016.
 */

public class ActivityListCreator extends AsyncTask<String, Void, Void> implements Constants.PREF_KEYS{

    private final String LOG_TAG = this.getClass().getSimpleName();

    private AppsToLaunch appsToLaunch;
    private List<ActivityInfo> activityList;
    private List<Drawable> activityIcons;
    private List<String> activityLabels;
    private List<String> activityNames;
    private List<String> appPackages;
    private ActivityAdapterWithIcon adapter;
    private AlertDialog.Builder builder;
    private AlertDialog activityListDialog;
    private PackageManager pm;
    private String packageName;


    public ActivityListCreator(AppsToLaunch appsToLaunch) {
        this.appsToLaunch = appsToLaunch;
    }

    @Override
    protected void onPreExecute() {
        appsToLaunch.showNewProgressDialog();
    }


    @Override
    protected Void doInBackground(String... params) {

        try {
            packageName = params[0];
            pm = appsToLaunch.getPackageManager();
            activityList = Logic.getAllActivities(appsToLaunch.getApplicationContext(), packageName);
            activityIcons = new ArrayList<>();
            activityLabels = new ArrayList<>();
            activityNames = new ArrayList<>();
            appPackages = new ArrayList<>();

            createAppListData(appsToLaunch);

            adapter = new ActivityAdapterWithIcon(activityLabels, activityNames, activityIcons, appsToLaunch);


            builder = new AlertDialog.Builder(appsToLaunch)
                    .setTitle(appsToLaunch.getString(R.string.select_activity))
                    .setAdapter(adapter, null);

        } catch (NullPointerException ex) {
            Log.e(LOG_TAG, "Error: Fragment not attached to an activity - task cancelled");
            this.cancel(true);
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

        activityListDialog = builder.create();

        activityListDialog.getListView().setAdapter(adapter);
        activityListDialog.getListView().setItemsCanFocus(false);
        activityListDialog.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        activityListDialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                appsToLaunch.setAppObjectActivity(appPackages.get(position), activityLabels.get(position),
                        activityNames.get(position), activityIcons.get(position));
                appsToLaunch.dismissDialogs();
            }
        });

        appsToLaunch.showActivityList(activityListDialog);
        appsToLaunch.dismissProgressDialog();
    }

    private void createAppListData(Context context) {
        int activityCount = 0;
        activityLabels.add(context.getResources().getString(R.string.clear_selection));
        activityIcons.add(ResourcesCompat.getDrawable(context.getApplicationContext().getResources(),
                R.drawable.close_circle, null));
        activityNames.add(null);
        appPackages.add(packageName);
        for (int i = 0; i < activityList.size(); i++) {
            String activityLabel = activityList.get(i).loadLabel(pm).toString();
            final PackageManager pm = context.getApplicationContext().getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(activityList.get(i).packageName, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : null);
            if (TextUtils.isEmpty(applicationName) || !applicationName.equals(activityLabel)) {
                activityCount++;
                activityLabels.add(activityLabel);
                activityIcons.add(activityList.get(i).loadIcon(pm));
                activityNames.add(activityList.get(i).name);
                appPackages.add(activityList.get(i).packageName);
            }
        }
        if (activityCount == 0) {
            activityLabels.clear();
            activityIcons.clear();
            activityNames.clear();
            appPackages.clear();
            activityLabels.add(context.getResources().getString(R.string.no_activities_found));
            activityIcons.add(ResourcesCompat.getDrawable(context.getApplicationContext().getResources(),
                    R.drawable.close_circle, null));
            activityNames.add(null);
            appPackages.add(packageName);
        }
    }

}
