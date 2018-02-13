package pl.maslanka.automatecar.helpers;

import android.graphics.drawable.Drawable;

/**
 * Created by Artur on 15.11.2016.
 */

public class AppAdapterItem {

    private Long index;
    private String name;
    private String packageName;
    private String activityName;
    private Drawable drawable;

    public AppAdapterItem(Long index, String name, String packageName, String activityName, Drawable drawable) {
        this.index = index;
        this.name = name;
        this.packageName = packageName;
        this.activityName = activityName;
        this.drawable = drawable;
    }

    public Long getIndex() { return index; }
    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public String getActivityName() {
        return activityName;
    }
    public Drawable getDrawable() { return drawable; }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppAdapterItem that = (AppAdapterItem) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
            return false;
        return activityName != null ? activityName.equals(that.activityName) : that.activityName == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + (activityName != null ? activityName.hashCode() : 0);
        return result;
    }
}
