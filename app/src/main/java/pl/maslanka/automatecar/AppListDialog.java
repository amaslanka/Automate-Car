package pl.maslanka.automatecar;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Artur on 09.11.2016.
 */

public class AppListDialog extends android.support.v4.app.DialogFragment {

    private PackageManager pm;
    private List<ApplicationInfo> installedApps;
    private Map<String, Boolean> selectedApps;
    private List<Drawable> appIcons;
    private List<String> appNames;
    private List<String> appsFromPrefs;
    private SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pm = getActivity().getPackageManager();
        installedApps = MainActivity.logic.getListOfInstalledApps();
        selectedApps = new HashMap<>();
        appIcons = new ArrayList<>();
        appNames = new ArrayList<>();
        appsFromPrefs = new ArrayList<>();

        appsFromPrefs.addAll(Logic.getSharedPrefAppList(getActivity()));

        Log.d("appsFromPrefs", appsFromPrefs.toString());

        for (int i = 0; i < installedApps.size(); i++) {
            appIcons.add(pm.getApplicationIcon(installedApps.get(i)));
            appNames.add(pm.getApplicationLabel(installedApps.get(i)).toString());
            for (int j=0; j < appsFromPrefs.size(); i++) {
                if (appNames.get(i).equals(appsFromPrefs.get(j))) {
                    selectedApps.put(appNames.get(i), true);
                } else {
                    selectedApps.put(appNames.get(i), false);
                }
            }
        }

        final ArrayAdapterWithIcon adapter = new ArrayAdapterWithIcon(appNames, appIcons, getActivity());

        adapter.checkedTextViews = new CheckedTextView[appNames.size()];
        for (int i=0; i<adapter.checkedTextViews.length; i++) {
            adapter.checkedTextViews[i] = new CheckedTextView(getActivity());
            adapter.checkedTextViews[i].setChecked();
        }

        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setTitle("Select Apps")
                .setAdapter(adapter, null)
                .setPositiveButton(getResources().getString(android.R.string.ok), null)
                .setNegativeButton(getResources().getString(android.R.string.cancel), null)
                .create();

        builder.getListView().setAdapter(adapter);
        builder.getListView().setItemsCanFocus(false);
        builder.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        builder.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Toast.makeText(getActivity(), appNames.get(position).toString(), Toast.LENGTH_SHORT).show();

                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.checked_text_view);
                boolean isChecked = checkedTextView.isChecked();

                checkedTextView.setChecked(!isChecked);
                adapter.checkedTextViews[position].setChecked(!isChecked);

            }
        });


        return builder;
    }
}


