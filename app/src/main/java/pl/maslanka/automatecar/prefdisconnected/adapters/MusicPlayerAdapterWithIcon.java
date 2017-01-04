package pl.maslanka.automatecar.prefdisconnected.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pl.maslanka.automatecar.R;

/**
 * Created by Artur on 25.11.2016.
 */

public class MusicPlayerAdapterWithIcon extends BaseAdapter {

    private List<String> appNames;
    private List<Drawable> appIcons;
    private Activity activity;
    private ImageView appIcon;
    private TextView appName;


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public MusicPlayerAdapterWithIcon(List<String> appNames, List<Drawable> appIcons, Activity activity) {
        this.appNames = appNames;
        this.activity = activity;
        this.appIcons = appIcons;

    }

    @Override
    public int getCount() {
        return appNames.size();
    }

    @Override
    public String getItem(int position) {
        return appNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.music_player_list_view_item, parent, false);
        }

        appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
        appName = (TextView) convertView.findViewById(R.id.app_name);

        appIcon.setImageDrawable(appIcons.get(position));
        appName.setText(appNames.get(position));


        return convertView;
    }

}

