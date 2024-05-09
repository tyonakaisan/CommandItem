package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.util.ItemBuilder;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Optional;

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

    public void setPlayerHandItem(final Player player, final ItemStack itemStack, final @Nullable EquipmentSlot equipmentSlot, final Action.Item action) {
        if (equipmentSlot == null) {
            return;
        }

        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(this.updateCounts(itemStack, action, player));
        } else {
            player.getInventory().setItemInOffHand(this.updateCounts(itemStack, action, player));
        }
    }

    private ItemStack updateCounts(final ItemStack itemStack, final Action.Item action, final Player player) {
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
            return cloneItemStack.getAmount() == 1
                    ? new ItemStack(Material.AIR)
                    : ItemBuilder.of(this.itemRegistry.toItemStack(item, player))
                    .amount(cloneItemStack.getAmount() - 1)
                    .build();
        } else {
            return cloneItemStack;
        }
    }
}
