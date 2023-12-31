package github.tyonakaisan.commanditem.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CommandPlaceholder {

    private CommandPlaceholder() {
        throw new IllegalStateException("Utility class");
    }
}
