package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.ActionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public record CustomCommand(
        ActionUtils.CommandAction action,
        List<String> commands,
        int repeat,
        int period,
        int delay
) {
}
