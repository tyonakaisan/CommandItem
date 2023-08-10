package github.tyonakaisan.commanditem.item;

import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public record ItemRecord(
        ItemStack item,
        String displayName,
        List<String> lore,
        Key key,
        int usageCounts,
        boolean unStackable,
        boolean canPlace,
        Map<String, List<String>> byPlayerCommands,
        Map<String, List<String>> byConsoleCommands
) {
}
