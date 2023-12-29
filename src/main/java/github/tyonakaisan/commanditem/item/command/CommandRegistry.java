package github.tyonakaisan.commanditem.item.command;

import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.item.Registry;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Iterator;
import java.util.Set;

@Singleton
@DefaultQualifier(NonNull.class)
public class CommandRegistry implements Registry<Key, CustomCommand> {
    @Override
    public @NonNull CustomCommand register(@NonNull Key key, @NonNull CustomCommand value) {
        return null;
    }

    @Override
    public @Nullable CustomCommand get(@NonNull Key key) {
        return null;
    }

    @Override
    public @Nullable Key key(@NonNull CustomCommand value) {
        return null;
    }

    @Override
    public @NonNull Set<Key> keySet() {
        return null;
    }

    @Override
    public @NonNull Set<CustomCommand> valueSet() {
        return null;
    }

    @Override
    public @NonNull Iterator<CustomCommand> iterator() {
        return null;
    }
}
