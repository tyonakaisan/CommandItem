package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

/*
ItemMetaを変える場合は必ずcloneすること
 */
@DefaultQualifier(NonNull.class)
@Singleton
public final class Convert {

    private final ItemRegistry itemRegistry;

    @Inject
    public Convert(
            final ItemRegistry itemRegistry
    ) {
        this.itemRegistry = itemRegistry;
    }

    public static Item defaultItem(final Key key, final ItemStack itemStack) {
        return new Item(getDisplayName(itemStack), getLore(itemStack), itemStack, defaultAttributes(key), Map.of(Action.Item.LEFT_CLICK, List.of(defaultCommand())));
    }

    public static Attributes defaultAttributes(final Key key) {
        return new Attributes(key, true, true, false, "1", "0", Map.of(Action.Item.LEFT_CLICK, "1"));
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
        if (displayName == null) {
            // Return empty if skullMeta or itemName is present
            if (itemMeta.hasItemName() || itemMeta instanceof SkullMeta || isWrittenBook(itemStack)) {
                return "";
            }
            return getTranslationKey(itemStack);
        }

        return MiniMessage.miniMessage().serialize(displayName);
    }

    private static boolean isWrittenBook(final ItemStack itemStack) {
        return itemStack.getItemMeta() instanceof final BookMeta bookMeta && bookMeta.hasTitle();
    }

    private static String getRarityColor(final ItemStack itemStack) {
        final var itemMeta = itemStack.getItemMeta();
        return !itemMeta.hasRarity() || itemMeta.getRarity().equals(ItemRarity.COMMON)
                ? ""
                : String.format("<%s>", itemMeta.getRarity().color());
    }

    private static String getTranslationKey(final ItemStack itemStack) {
        final var itemType = itemStack.getType();
        return itemType.getItemTranslationKey() == null
                ? ""
                : String.format("%s<lang:%s>", getRarityColor(itemStack), itemType.getItemTranslationKey());
    }

    public void setPlayerHandItem(final ItemStack itemStack, final Player player, final Action.Item action, final @Nullable EquipmentSlot equipmentSlot) {
        if (equipmentSlot == null) {
            return;
        }

        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(this.updateCounts(itemStack, player, action));
        } else {
            player.getInventory().setItemInOffHand(this.updateCounts(itemStack, player, action));
        }
    }

    private ItemStack updateCounts(final ItemStack itemStack, final Player player, final Action.Item action) {
        final var cloneItemStack = itemStack.clone();
        final @Nullable Item item = this.itemRegistry.toItem(cloneItemStack);

        if (item == null || item.attributes().maxUses(player) <= -1 || !item.commands().containsKey(action)) {
            return itemStack;
        }

        // return usage counts
        int oldCounts = Optional.of(cloneItemStack)
                .filter(ItemStack::hasItemMeta)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getPersistentDataContainer)
                .filter(pdc -> pdc.has(NamespacedKeyUtils.usageKey()))
                .map(pdc -> pdc.get(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER))
                .orElse(Integer.MAX_VALUE);

        var newCounts = oldCounts + 1;

        cloneItemStack.editMeta(meta -> meta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, newCounts));

        if (newCounts >= item.attributes().maxUses(player)) {
            var reduceAmountItem = this.itemRegistry.toItemStack(item, player);
            reduceAmountItem.setAmount(cloneItemStack.getAmount() - 1);

            return cloneItemStack.getAmount() == 1
                    ? new ItemStack(Material.AIR)
                    : reduceAmountItem;
        } else {
            return cloneItemStack;
        }
    }

    public WeightedRandom<Command> weightedCommands(final Item item, final Player player, final Action.Item action) {
        var weightedRandom = new WeightedRandom<Command>();
        item.commands().get(action).forEach(command -> weightedRandom.add(command, command.runWeight(player)));
        return weightedRandom;
    }

    public int pickCommands(final Item item, final Player player, final Action.Item action) {
        @Nullable String picks = item.attributes().pickCommands().get(action);
        return picks != null
                ? (int) PlaceholderUtils.calculate(player, picks)
                : 0;
    }
}
