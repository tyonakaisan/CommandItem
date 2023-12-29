package github.tyonakaisan.commanditem.item.command;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public record CustomCommand(
        List<String> commands
) {
}
