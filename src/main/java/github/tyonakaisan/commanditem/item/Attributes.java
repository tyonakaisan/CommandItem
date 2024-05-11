package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Map;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Attributes(
        Key key,
        boolean stackable,
        boolean placeable,
        boolean hideCoolTimeAnnounce,
        String maxUses,
        String coolTime,
        Map<Action.Item, String> pickCommands
) {
    public int maxUses(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.maxUses);
    }

    public int coolTime(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.coolTime);
    }
}
