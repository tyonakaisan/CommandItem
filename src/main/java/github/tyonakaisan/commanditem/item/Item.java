package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Item(
        String displayName,
        List<String> lore,
        ItemStack itemStack,
        Attributes attributes,
        Map<Action.Item, List<Command>> commands
) {
    public Component displayName(final Player player) {
        return this.displayName.isEmpty()
                ? Component.empty()
                : PlaceholderUtils.getComponent(player, this.displayName);
    }

    public List<Component> lore(final Player player) {
        return this.lore.isEmpty()
                ? List.of()
                : this.lore.stream()
                .map(text -> PlaceholderUtils.getComponent(player, text))
                .toList();
    }
}
