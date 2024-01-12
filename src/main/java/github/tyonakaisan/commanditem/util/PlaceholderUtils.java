package github.tyonakaisan.commanditem.util;

import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false);

    public static Component defaultComponent() {
        return DEFAULT_COMPONENT;
    }

    public static String getPlainText(Player player, String string) {
        return PlainTextComponentSerializer.plainText()
                .serialize(miniMessage(player)
                        .deserialize(papiParserString(player, string)));
    }

    public static Component getComponent(Player player, String string) {
        return defaultComponent().append(miniMessage(player).deserialize(papiParserString(player, string)));
    }

    public static double calculate(Player player, String expression) {
        var exp = new ExpressionBuilder(getPlainText(player, expression)).build();
        return exp.evaluate();
    }

    private static String papiParserString(Player player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

    private static MiniMessage miniMessage(Player player) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .tag("player", Tag.inserting(player.displayName()))
                        .resolver(MiniPlaceholders.getGlobalPlaceholders())
                        .resolver(MiniPlaceholders.getAudiencePlaceholders(player))
                        .resolver(TagResolver.standard())
                        .build())
                .build();
    }
}
