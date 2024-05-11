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
public final class PlaceholderUtils {

    private PlaceholderUtils() {
        throw new AssertionError("Utility class");
    }

    private static final Component DEFAULT_COMPONENT = Component.empty()
            // .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false);

    public static Component defaultComponent() {
        return DEFAULT_COMPONENT;
    }

    public static String getPlainText(final Player player, final String string) {
        return PlainTextComponentSerializer.plainText()
                .serialize(miniMessage(player)
                        .deserialize(papiParserString(player, string)));
    }

    public static Component getComponent(final Player player, final String string) {
        return DEFAULT_COMPONENT.append(miniMessage(player).deserialize(papiParserString(player, string)));
    }

    public static double calculate(final Player player, final String expression) {
        var exp = new ExpressionBuilder(getPlainText(player, expression)).build();
        return exp.evaluate();
    }

    private static String papiParserString(final Player player, final String string) {
        return CommandItem.papiLoaded() ? PlaceholderAPI.setPlaceholders(player, string) : string;
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
