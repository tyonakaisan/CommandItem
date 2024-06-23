package github.tyonakaisan.commanditem.util;

import github.tyonakaisan.commanditem.CommandItem;
import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class PlaceholderParser {

    private PlaceholderParser() {}

    private static final Component DEFAULT_COMPONENT = Component.empty()
            // .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false);

    public static String plainText(final Player player, final String string) {
        return PlainTextComponentSerializer.plainText()
                .serialize(miniMessage(player).deserialize(placeholderApi(player, string)));
    }

    public static Component component(final Player player, final String string) {
        return DEFAULT_COMPONENT.append(miniMessage(player).deserialize(placeholderApi(player, string)));
    }

    public static double calculate(final Player player, final String expression) {
        var exp = new ExpressionBuilder(plainText(player, expression)).build();
        return exp.evaluate();
    }

    private static String placeholderApi(final Player player, final String string) {
        return CommandItem.papiLoaded() && PlaceholderAPI.containsPlaceholders(string)
                ? PlaceholderAPI.setPlaceholders(player, string)
                : string;
    }

    private static MiniMessage miniMessage(final Player player) {
        var tagResolver = TagResolver.builder();

        if (CommandItem.miniPlaceholdersLoaded()) {
            tagResolver.resolver(MiniPlaceholders.getGlobalPlaceholders());
            tagResolver.resolver(MiniPlaceholders.getAudiencePlaceholders(player));
        }

        tagResolver.tag("player", Tag.inserting(player.displayName()));
        tagResolver.resolver(TagResolver.standard());

        return MiniMessage.builder()
                .tags(tagResolver.build())
                .build();
    }
}
