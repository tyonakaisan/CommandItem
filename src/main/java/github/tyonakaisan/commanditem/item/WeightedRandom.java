package github.tyonakaisan.commanditem.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@DefaultQualifier(NonNull.class)
public final class WeightedRandom<T> {
    private final NavigableMap<Double, T> map = new TreeMap<>();
    private double total = 0;

    public WeightedRandom<T> add(final T result, final double weight) {
        if (weight <= 0) {
            return this;
        }
        this.total += weight;
        this.map.put(this.total, result);
        return this;
    }

    public T select() {
        var value = ThreadLocalRandom.current().nextDouble() * this.total;
        return this.map.higherEntry(value).getValue();
    }

    public int size() {
        return this.map.size();
    }
}
