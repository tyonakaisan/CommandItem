package github.tyonakaisan.commanditem.item.registry;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.Item;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// ItemMetaを変える場合は必ずcloneすること
@SuppressWarnings("PatternValidation")
@DefaultQualifier(NonNull.class)
public final class ItemManager {

    private final ItemRegistry itemRegistry;

    @Inject
    public ItemManager(
            final ItemRegistry itemRegistry
    ) {
        this.itemRegistry = itemRegistry;
    }

    public @Nullable Item toItem(final @Nullable ItemStack itemStack) {
        if (!this.isItem(itemStack)) {
            return null;
        }

        return this.itemRegistry.item(this.getId(itemStack));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isItem(final @Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty() || !itemStack.hasItemMeta()) {
            return false;
        }
        final var pdc = itemStack.getItemMeta().getPersistentDataContainer();

        if (!pdc.has(NamespacedKeyUtils.idKey())) {
            return false;
        }

        return this.itemRegistry.keys().contains(this.getId(itemStack));
    }

    public boolean isMaxUsesExceeded(final @Nullable ItemStack itemStack, final Player player) {
        final @Nullable Item item = this.toItem(itemStack);

        if (itemStack != null && item != null) {
            final var usageCounts = this.getUsagesOrDefault(itemStack, 0);
            final var max = item.attributes().maxUses(player);

            return max > -1 && usageCounts > max;
        }

        return false;
    }

    public ItemStack toItemStack(final Item item, final Player player) {
        var itemStack = item.rawItemStack().clone();

        itemStack.editMeta(itemMeta -> {
            final var newDisplayName = item.displayName(player);
            if (!newDisplayName.equals(Component.empty())) {
                itemMeta.displayName(newDisplayName);
            }

            final var newLore = item.lore(player);
            if (!newLore.isEmpty()) {
                itemMeta.lore(newLore);
            }

            final var pdc = itemMeta.getPersistentDataContainer();
            pdc.set(NamespacedKeyUtils.idKey(), PersistentDataType.STRING, item.attributes().key().asString());
            pdc.set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, 0);
            if (!item.attributes().stackable()) {
                pdc.set(NamespacedKeyUtils.uuidKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
                pdc.set(NamespacedKeyUtils.timestampKey(), PersistentDataType.LONG, Instant.now().toEpochMilli());
            }
        });
        return itemStack;
    }

    public void setPlayerHandItem(final ItemStack itemStack, final Player player, final Action.Item action, final @Nullable EquipmentSlot equipmentSlot) {
        if (equipmentSlot == null) {
            return;
        }

        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(this.addUseCountsIfNeeded(itemStack, player, action));
        } else {
            player.getInventory().setItemInOffHand(this.addUseCountsIfNeeded(itemStack, player, action));
        }
    }

    private ItemStack addUseCountsIfNeeded(final ItemStack itemStack, final Player player, final Action.Item action) {
        final var cloneItemStack = itemStack.clone();
        final @Nullable Item item = this.toItem(cloneItemStack);

        if (item == null || item.attributes().maxUses(player) <= -1 || !item.commands().containsKey(action)) {
            return itemStack;
        }

        // return usage counts
        int oldCounts = this.getUsagesOrDefault(cloneItemStack, Integer.MAX_VALUE);

        var newCounts = oldCounts + 1;

        cloneItemStack.editMeta(meta -> meta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, newCounts));

        if (newCounts >= item.attributes().maxUses(player)) {
            var reduceAmountItem = this.toItemStack(item, player);
            reduceAmountItem.setAmount(cloneItemStack.getAmount() - 1);

            return cloneItemStack.getAmount() == 1
                    ? new ItemStack(Material.AIR)
                    : reduceAmountItem;
        } else {
            return cloneItemStack;
        }
    }

    private Key getId(final ItemStack itemStack) {
        final var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        final var keys = pdc.getOrDefault(NamespacedKeyUtils.idKey(), PersistentDataType.STRING,"command_item:empty").split(":", 2);
        final var namespace = keys[0];
        final var value = keys[1];

        return Key.key(namespace, value);
    }

    private int getUsagesOrDefault(final ItemStack itemStack, final int defaultValue) {
        return Optional.of(itemStack)
                .filter(ItemStack::hasItemMeta)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getPersistentDataContainer)
                .filter(pdc -> pdc.has(NamespacedKeyUtils.usageKey()))
                .map(pdc -> pdc.get(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER))
                .orElse(defaultValue);
    }
}
