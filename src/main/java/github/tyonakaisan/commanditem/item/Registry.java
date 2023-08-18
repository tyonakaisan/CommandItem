package github.tyonakaisan.commanditem.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Iterator;
import java.util.Set;

@DefaultQualifier(NonNull.class)
public interface Registry<K, V> {
    @NonNull V register(final @NonNull K key, final @NonNull V value);

    @Nullable V get(final @NonNull K key);

    @Nullable K key(final @NonNull V value);

    @NonNull Set<K> keySet();

    @NonNull Set<V> valueSet();

    @NonNull Iterator<V> iterator();
}
