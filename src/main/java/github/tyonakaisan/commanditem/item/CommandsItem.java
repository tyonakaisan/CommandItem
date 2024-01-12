package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.ActionUtils;
import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
        int maxUses,
        boolean stackable,
        boolean placeable,
        int coolTime,
        Map<ActionUtils.ItemAction, List<CustomCommand>> byPlayerCommands,
        Map<ActionUtils.ItemAction, List<CustomCommand>> byConsoleCommands
) {

    static final Component defaultComponent = Component.empty()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false);

    static MiniMessage miniMessage(Player player) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .tag("player", Tag.inserting(player.displayName()))
                        .resolver(MiniPlaceholders.getGlobalPlaceholders())
                        .resolver(MiniPlaceholders.getAudiencePlaceholders(player))
                        .resolver(TagResolver.standard())
                        .build())
                .build();
    }

    public Component displayName(Player player) {
        var papiParser = PlaceholderAPI.setPlaceholders(player, this.displayName);
        return defaultComponent.append(miniMessage(player).deserialize(papiParser));
    }

    public List<Component> lore(Player player) {
        return this.lore.stream().map(text -> {
            var papiParser = PlaceholderAPI.setPlaceholders(player, text);
            return defaultComponent.append(miniMessage(player).deserialize(papiParser));
        }).toList();
    }
}
