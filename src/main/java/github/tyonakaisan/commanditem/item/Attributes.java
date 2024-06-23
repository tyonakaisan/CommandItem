package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.PlaceholderParser;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Attributes(
        Key key,
        @Comment("If false, the time of acquisition is recorded on the item")
        boolean stackable,
        @Comment("Cancel event before executing command")
        boolean eventCancel,
        boolean hideCoolTimeAnnounce,
        String maxUses,
        String coolTime,
        Map<Action.Item, String> pickCommands
) {
    public int maxUses(final Player player) {
        return (int) PlaceholderParser.calculate(player, this.maxUses);
    }

    public int coolTime(final Player player) {
        return (int) PlaceholderParser.calculate(player, this.coolTime);
    }
}
