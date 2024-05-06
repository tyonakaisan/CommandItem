package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Attributes(
        Key key,
        boolean stackable,
        boolean placeable,
        String maxUses,
        String coolTime,
        String pickCommands
) {
    public static Attributes defaultCreate(final Key key) {
        return new Attributes(key, true, true, "0", "0", "0");
    }

    public int maxUses(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.maxUses);
    }

    public int coolTime(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.coolTime);
    }
}
