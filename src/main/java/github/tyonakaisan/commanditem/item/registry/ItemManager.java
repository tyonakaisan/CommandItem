package github.tyonakaisan.commanditem.item.registry;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.item.Item;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Instant;
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
        if (itemStack == null || item == null) {
            return false;
        }

        final var usageCounts = this.getUsagesOrDefault(itemStack, 0);
        final var max = item.attributes().maxUses(player);
        return max > -1 && usageCounts > max;
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

    public Key getId(final ItemStack itemStack) {
        final var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        final var keys = pdc.getOrDefault(NamespacedKeyUtils.idKey(), PersistentDataType.STRING, "command_item:empty").split(":", 2);
        final var namespace = keys[0];
        final var value = keys[1];

        return Key.key(namespace, value);
    }

    public int getUsagesOrDefault(final ItemStack itemStack, final int defaultValue) {
        if (!itemStack.hasItemMeta()) {
            return defaultValue;
        }

        final var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(NamespacedKeyUtils.usageKey())) {
            return defaultValue;
        }

        return pdc.getOrDefault(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, defaultValue);
    }
}
