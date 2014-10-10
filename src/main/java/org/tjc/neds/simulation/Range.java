package org.tjc.neds.simulation;

/**
 *
 * @author Thomas
 * @param <T>
 */
public class Range<T> {

    private final T low;
    private final T high;

    public Range(T low, T hi) {
        this.low = low;
        this.high = hi;
    }

    public T getLow() {
        return low;
    }

    public T getHigh() {
        return high;
    }

    @Override
    public String toString() {
        return "range[" + low + "," + high + "]";
    }
}
