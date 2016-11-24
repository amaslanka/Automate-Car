package pl.maslanka.automatecar.helperobjectsandinterfaces;

/**
 * Created by Artur on 15.11.2016.
 */

public class QuattroObject<T, U, V, W> {

    private T index;
    private U name;
    private V packageName;
    private W drawable;

    public QuattroObject(T index, U name, V packageName, W drawable) {
        this.index = index;
        this.name = name;
        this.packageName = packageName;
        this.drawable = drawable;
    }

    public T getIndex() { return index; }
    public U getName() { return name; }
    public V getPackageName() { return packageName; }
    public W getDrawable() { return drawable; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuattroObject)) return false;

        QuattroObject<?, ?, ?, ?> that = (QuattroObject<?, ?, ?, ?>) o;

        if (!getName().equals(that.getName())) return false;
        return getPackageName().equals(that.getPackageName());

    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getPackageName().hashCode();
        return result;
    }
}
