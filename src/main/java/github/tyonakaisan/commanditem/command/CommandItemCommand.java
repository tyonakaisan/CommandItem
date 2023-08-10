package github.tyonakaisan.commanditem.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface CommandItemCommand {

    void init();
}
