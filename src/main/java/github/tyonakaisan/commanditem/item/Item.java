package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.PlaceholderParser;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Item(
        @Comment("Rewrite if not empty")
        String displayName,
        @Comment("Rewrite if not empty")
        List<String> lore,
        @Comment("Not readable & editable")
        ItemStack rawItemStack,
        Attributes attributes,
        Map<Action.Item, List<Command>> commands
) {
    public Component displayName(final Player player) {
        return this.displayName.isEmpty()
                ? Component.empty()
                : PlaceholderParser.component(player, this.displayName);
    }

    public List<Component> lore(final Player player) {
        return this.lore.isEmpty()
                ? List.of()
                : this.lore.stream()
                .map(text -> PlaceholderParser.component(player, text))
                .toList();
    }
}
