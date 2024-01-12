package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
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
        String displayName,
        List<String> lore,
        Key key,
        String maxUses,
        boolean stackable,
        boolean placeable,
        String coolTime,
        Map<ActionUtils.ItemAction, List<CustomCommand>> byPlayerCommands,
        Map<ActionUtils.ItemAction, List<CustomCommand>> byConsoleCommands
) {

    public Component displayName(Player player) {
        return PlaceholderUtils.getComponent(player, this.displayName);
    }

    public List<Component> lore(Player player) {
        return this.lore.stream()
                .map(text -> PlaceholderUtils.getComponent(player, text))
                .toList();
    }

    public int maxUses(Player player) {
        return (int) PlaceholderUtils.calculate(player, this.maxUses);
    }

    public int coolTime(Player player) {
        return (int) PlaceholderUtils.calculate(player, this.coolTime);
    }
}
