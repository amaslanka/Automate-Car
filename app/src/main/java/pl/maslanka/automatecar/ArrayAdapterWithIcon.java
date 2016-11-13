package pl.maslanka.automatecar;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Artur on 09.11.2016.
 */

public class ArrayAdapterWithIcon extends BaseAdapter {

    private List<String> appNames;
    private List<Drawable> appIcons;
    protected CheckedTextView[] checkedTextViews;
    private Activity activity;
    protected CheckedTextView checkedTextView;
    protected ImageView appIcon;
    protected TextView appName;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ArrayAdapterWithIcon(List<String> appNames, List<Drawable> appIcons, Activity activity) {
        this.appNames = appNames;
        this.appIcons = appIcons;
        this.activity = activity;


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
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_app, parent, false);
        }

        checkedTextView = (CheckedTextView) convertView.findViewById(R.id.checked_text_view);
        appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
        appName = (TextView) convertView.findViewById(R.id.app_name);

        checkedTextView.setChecked(checkedTextViews[position].isChecked());
        appIcon.setImageDrawable(appIcons.get(position));
        appName.setText(appNames.get(position));

        return convertView;
    }

}
