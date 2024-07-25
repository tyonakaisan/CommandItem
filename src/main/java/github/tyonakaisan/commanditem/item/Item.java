package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import github.tyonakaisan.commanditem.util.PlaceholderParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Item(
        @Comment("Override original item name")
        String displayName,
        @Comment("Override original item lore")
        List<String> lore,
        @Comment("Not readable & editable")
        ItemStack rawItemStack,
        Attributes attributes,
        Map<Action.Item, List<Command>> commands
) {
    public Component displayName(final Player player) {
        return this.displayName.isEmpty()
                ? Component.empty()
                : PlaceholderParser.component(player, this.displayName);
    }

    public List<Component> lore(final Player player) {
        return this.lore.isEmpty()
                ? List.of()
                : this.lore.stream()
                .map(text -> PlaceholderParser.component(player, text))
                .toList();
    }

    public ItemStack asItemStack(final Player player) {
        final var itemStack = this.rawItemStack.clone();

        itemStack.editMeta(itemMeta -> {
            final var newDisplayName = this.displayName(player);
            if (!newDisplayName.equals(Component.empty())) {
                itemMeta.displayName(newDisplayName);
            }

            final var newLore = this.lore(player);
            if (!newLore.isEmpty()) {
                itemMeta.lore(newLore);
            }

            final var pdc = itemMeta.getPersistentDataContainer();
            pdc.set(NamespacedKeyUtils.idKey(), PersistentDataType.STRING, this.attributes().key().asString());
            pdc.set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, 0);
            if (!this.attributes().stackable()) {
                pdc.set(NamespacedKeyUtils.uuidKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
                pdc.set(NamespacedKeyUtils.timestampKey(), PersistentDataType.LONG, Instant.now().toEpochMilli());
            }
        });
        return itemStack;
    }

    /**
     * Return parsed display name and lore applied item stack.
     * No special data attached.
     * <p>Use {@link Item#rawItemStack()} for the raw item stack.
     *
     * @param player the player
     * @return the itemStack
     */
    public ItemStack asSimple(final Player player) {
        final var itemStack = this.rawItemStack.clone();

        itemStack.editMeta(itemMeta -> {
            final var newDisplayName = this.displayName(player);
            if (!newDisplayName.equals(Component.empty())) {
                itemMeta.displayName(newDisplayName);
            }

            final var newLore = this.lore(player);
            if (!newLore.isEmpty()) {
                itemMeta.lore(newLore);
            }
        });
        return itemStack;
    }

    /**
     * Returns an item stack with display name and lore applied with no special data attached.
     * <p>Use {@link Item#rawItemStack()} for the raw item stack.
     *
     * @return the itemStack
     */
    public ItemStack asSimple() {
        final var itemStack = this.rawItemStack.clone();

        itemStack.editMeta(itemMeta -> {
            final var newDisplayName = this.displayName;
            if (!newDisplayName.isEmpty()) {
                itemMeta.displayName(MiniMessage.miniMessage().deserialize(newDisplayName));
            }

            final var newLore = this.lore;
            if (!newLore.isEmpty()) {
                itemMeta.lore(
                        newLore.stream()
                                .map(MiniMessage.miniMessage()::deserialize)
                                .toList());
            }
        });
        return itemStack;
    }
}
