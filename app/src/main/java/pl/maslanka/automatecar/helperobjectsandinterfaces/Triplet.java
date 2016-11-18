package pl.maslanka.automatecar.helperobjectsandinterfaces;

/**
 * Created by Artur on 15.11.2016.
 */

public class Triplet<T, U, V> {

    private T first;
    private U second;
    private V third;

    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public V getThird() { return third; }

    public void setThird(V third) {
        this.third = third;
    }
}
