package github.tyonakaisan.commanditem.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public interface CommandItemCommand {
    ArgumentBuilder<CommandSourceStack, ?> init();
}
