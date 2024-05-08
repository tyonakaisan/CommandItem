package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import io.papermc.paper.inventory.ItemRarity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Collections;
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
    public static Item defaultConvert(final Key key, final ItemStack itemStack) {
        @Nullable Component metaDisplayName = itemStack.getItemMeta().displayName();
        var color = itemStack.getRarity() == ItemRarity.COMMON
                ? ""
                : String.format("<%s>", itemStack.getRarity().getColor());
        var itemTranslationKey = itemStack.getType().getItemTranslationKey() == null
                ? ""
                : String.format("%s<lang:%s>", color, itemStack.getType().getItemTranslationKey());

        var displayName = metaDisplayName == null
                ? itemTranslationKey
                : MiniMessage.miniMessage().serialize(metaDisplayName);

        final @Nullable List<Component> lore = itemStack.getItemMeta().lore();
        final List<String> copyLore = lore == null
                ? Collections.emptyList()
                : lore.stream()
                .map(text -> MiniMessage.miniMessage().serialize(text))
                .toList();

        return new Item(displayName, copyLore, itemStack, Attributes.defaultCreate(key), Map.of(Action.Item.LEFT_CLICK, List.of(Command.defaultCreate())));
    }

    public Component displayName(final Player player) {
        return PlaceholderUtils.getComponent(player, this.displayName);
    }

    public List<Component> lore(final Player player) {
        return this.lore.stream()
                .map(text -> PlaceholderUtils.getComponent(player, text))
                .toList();
    }

    public WeightedRandom<Command> weightedCommands(final Action.Item action, final Player player) {
        var weightedRandom = new WeightedRandom<Command>();
        this.commands.get(action).forEach(command -> weightedRandom.add(command, command.runWeight(player)));
        return weightedRandom;
    }

    public void runRandomCommands(final Player player, final Action.Item action) {
        if (this.commands.getOrDefault(action, List.of()).isEmpty()) {
            return;
        }

        var max = this.attributes.pickCommands(player, action);
        var weightedCommands = this.weightedCommands(action, player);
        if (weightedCommands.size() == 0 || max == 0 || this.attributes.maxUses(player) == 0) {
            return;
        }

        if (max <= -1) {
            this.commands.get(action).forEach(command -> command.repeatCommands(player, command.isConsole()));
        } else {
            for (int i = 0; i < max; i++) {
                var command = weightedCommands.select();
                command.repeatCommands(player, command.isConsole());
            }
        }
    }
}
