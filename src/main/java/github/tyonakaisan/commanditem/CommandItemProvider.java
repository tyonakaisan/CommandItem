package github.tyonakaisan.commanditem;

import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CommandItemProvider {

    private static @Nullable CommandItem instance;

    private CommandItemProvider() {

    }

    static void register(final CommandItem instance) {
        CommandItemProvider.instance = instance;
    }

    public static CommandItem instance() {
        if (instance == null) {
            throw new IllegalStateException("CommandItem not initialized!");
        }

        return instance;
    }
}
