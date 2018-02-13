package pl.maslanka.automatecar.prefconnected.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pl.maslanka.automatecar.R;

/**
 * Created by Artur on 09.11.2016.
 */

public class ActivityAdapterWithIcon extends BaseAdapter {

    private List<String> activityLabels;
    private List<String> activityNames;
    private List<Drawable> activityIcons;
    private Activity activity;
    private ImageView appIcon;
    private TextView appLabel;
    private TextView appName;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ActivityAdapterWithIcon(List<String> activityLabels, List<String> activityNames, List<Drawable> activityIcons, Activity activity) {
        this.activityLabels = activityLabels;
        this.activityNames = activityNames;
        this.activityIcons = activityIcons;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return activityLabels.size();
    }

    @Override
    public String getItem(int position) {
        return activityLabels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_list_view_item, parent, false);
        }

        appIcon = (ImageView) convertView.findViewById(R.id.icon);
        appLabel = (TextView) convertView.findViewById(R.id.label);
        appName = (TextView) convertView.findViewById(R.id.description);

        appIcon.setImageDrawable(activityIcons.get(position));
        appLabel.setText(activityLabels.get(position));
        if (TextUtils.isEmpty(activityNames.get(position))) {
            appName.setVisibility(View.GONE);
        } else {
            appName.setVisibility(View.VISIBLE);
        }
        appName.setText(activityNames.get(position));

        return convertView;
    }

}
