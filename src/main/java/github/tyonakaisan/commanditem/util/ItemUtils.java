package github.tyonakaisan.commanditem.util;

import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.Attributes;
import github.tyonakaisan.commanditem.item.Command;
import github.tyonakaisan.commanditem.item.Item;
import github.tyonakaisan.commanditem.item.registry.WeightedRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@DefaultQualifier(NonNull.class)
public final class ItemUtils {

    private ItemUtils() {
    }

    public static Item defaultItem(final Key key, final ItemStack itemStack) {
        return new Item(getDisplayName(itemStack), getLore(itemStack), itemStack, defaultAttributes(key), Map.of(Action.Item.LEFT_CLICK, List.of(defaultCommand())));
    }

    public static Attributes defaultAttributes(final Key key) {
        return new Attributes(key, true, false, false, "-1", "0", Map.of(Action.Item.LEFT_CLICK, "-1"));
    }

    public static Command defaultCommand() {
        return new Command(Action.Command.COMMAND, List.of(), true, "1", "1", "0", "1");
    }

    private static List<String> getLore(final ItemStack itemStack) {
        final @Nullable List<Component> lore = itemStack.getItemMeta().lore();
        return lore == null
                ? Collections.emptyList()
                : lore.stream()
                .map(MiniMessage.miniMessage()::serialize)
                .toList();
    }

    private static String getDisplayName(final ItemStack itemStack) {
        final var itemMeta = itemStack.getItemMeta();
        final @Nullable Component displayName = itemMeta.displayName();
        return displayName != null
                ? MiniMessage.miniMessage().serialize(displayName)
                : "";
    }

    public static WeightedRandom<Command> weightedCommands(final Item item, final Player player, final Action.Item action) {
        final var weightedRandom = new WeightedRandom<Command>();
        item.commands().get(action).forEach(command -> weightedRandom.add(command, command.runWeight(player)));
        return weightedRandom;
    }

    public static int picks(final Item item, final Player player, final Action.Item action) {
        final @Nullable String picks = item.attributes().pickCommands().get(action);
        return picks != null
                ? (int) PlaceholderParser.calculate(player, picks)
                : 0;
    }
}
