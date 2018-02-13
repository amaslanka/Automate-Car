package pl.maslanka.automatecar.helpers;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Artur on 17.11.2016.
 */

public class AppObject implements Serializable {
    private final String name;
    private final String packageName;
    @Nullable private String activityName;


    public AppObject(String name, String packageName, String activityName) {
        this.name = name;
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    @Nullable
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(@Nullable String activityName) {
        this.activityName = activityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppObject appObject = (AppObject) o;

        if (name != null ? !name.equals(appObject.name) : appObject.name != null) return false;
        if (packageName != null ? !packageName.equals(appObject.packageName) : appObject.packageName != null)
            return false;
        return activityName != null ? activityName.equals(appObject.activityName) : appObject.activityName == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + (activityName != null ? activityName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AppObject{" +
                "name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                ", activityName='" + activityName + '\'' +
                '}';
    }
}
