package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.ActionUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public record CommandsItem(
        ItemStack itemStack,
        Key key,
        int maxUses,
        boolean stackable,
        boolean placeable,
        Map<ActionUtils.ItemAction, List<String>> byPlayerCommands,
        Map<ActionUtils.ItemAction, List<String>> byConsoleCommands
) {
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "";
    }
}
