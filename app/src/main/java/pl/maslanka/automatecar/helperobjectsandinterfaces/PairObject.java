package pl.maslanka.automatecar.helperobjectsandinterfaces;

import java.io.Serializable;

/**
 * Created by Artur on 17.11.2016.
 */

public class PairObject<F, S> implements Serializable {
    private final F name;
    private final S packageName;


    public PairObject(F name, S packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public F getName() {
        return name;
    }

    public S getPackageName() {
        return packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairObject)) return false;

        PairObject<?, ?> that = (PairObject<?, ?>) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        return getPackageName() != null ? getPackageName().equals(that.getPackageName()) : that.getPackageName() == null;

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getPackageName() != null ? getPackageName().hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "name= " + name + "; packageName= " + packageName;
    }
}
