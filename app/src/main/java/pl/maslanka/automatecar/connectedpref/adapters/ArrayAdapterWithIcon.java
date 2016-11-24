package pl.maslanka.automatecar.connectedpref.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pl.maslanka.automatecar.R;

/**
 * Created by Artur on 09.11.2016.
 */

public class ArrayAdapterWithIcon extends BaseAdapter {

    private List<String> appNames;
    private List<Drawable> appIcons;
    private CheckedTextView[] checkedTextViews;
    private Activity activity;
    private CheckedTextView checkedTextView;
    private ImageView appIcon;
    private TextView appName;


    public CheckedTextView[] getCheckedTextViews() {
        return checkedTextViews;
    }

    public void setCheckedTextViews(CheckedTextView[] checkedTextViews) {
        this.checkedTextViews = checkedTextViews;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public ArrayAdapterWithIcon(List<String> appNames, List<Drawable> appIcons, Activity activity) {
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
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.app_list_view_item, parent, false);
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
