package github.tyonakaisan.commanditem.item;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.ItemBuilder;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Singleton
@DefaultQualifier(NonNull.class)
public final class Convert {

    private final Path dataDirectory;
    private final CommandItem commandItem;
    private final CommandItemRegistry commandItemRegistry;
    private final Map<UUID, Long> internalCoolTime = new HashMap<>();

    @Inject
    Convert(
            final Path dataDirectory,
            final CommandItem commandItem,
            final CommandItemRegistry commandItemRegistry
    ) {
        this.dataDirectory = dataDirectory;
        this.commandItem = commandItem;
        this.commandItemRegistry = commandItemRegistry;

        try {
            this.commandItemRegistry.reloadItemConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCommandItem(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return pdc.has(NamespacedKeyUtils.idKey());
    }

    public boolean isMaxUsesExceeded(ItemStack itemStack) {
        if (!this.isCommandItem(itemStack)) return false;
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        int currentCount = pdc.getOrDefault(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, 0);
        if (this.toCommandsItem(itemStack).maxUses() <= -1) {
            return false;
        }
        return currentCount > this.toCommandsItem(itemStack).maxUses();
    }

    public void setPlayerHandItem(Player player, @Nullable EquipmentSlot equipmentSlot, ItemStack item, ActionUtils.ItemAction itemAction) {
        if (equipmentSlot == null) return;
        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(updateCounts(item, itemAction, player));
        } else {
            player.getInventory().setItemInOffHand(updateCounts(item, itemAction, player));
        }
    }

    // 2回callされる対策　ﾕﾙｾﾅｲ…
    public boolean checkInternalCoolTime(UUID uuid) {
        this.internalCoolTime.computeIfAbsent(uuid, v -> System.currentTimeMillis());
        if (this.internalCoolTime.containsKey(uuid)) {
            if (this.internalCoolTime.get(uuid) == -1L) {
                return true;
            } else {
                long timeElapsed = System.currentTimeMillis() - this.internalCoolTime.get(uuid);

                if (timeElapsed >= 3L) {
                    this.internalCoolTime.put(uuid, System.currentTimeMillis());
                    return true;
                }
            }
        } else {
            this.internalCoolTime.put(uuid, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public void initInternalCoolTime(UUID uuid) {
        this.internalCoolTime.computeIfAbsent(uuid, k -> System.currentTimeMillis());
    }

    private ItemStack updateCounts(ItemStack itemStack, ActionUtils.ItemAction itemAction, Player player) {
        var cloneItem = itemStack.clone();
        var pdc = cloneItem.getItemMeta().getPersistentDataContainer();
        var commandsItem = this.toCommandsItem(cloneItem);

        // 重ねれるアイテムの場合、カウント減らしたあとに分けると両方へってしまう（？）
        // 例：2こ(残り3)->一回使用&分ける->1こ(残り2)&1こ(残り2)
        if (commandsItem.maxUses() <= -1) {
            return cloneItem;
        }

        if (pdc.has(NamespacedKeyUtils.usageKey())
                && (commandsItem.byPlayerCommands().containsKey(itemAction) || commandsItem.byConsoleCommands().containsKey(itemAction))) {
            int counts = Objects.requireNonNull(pdc.get(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER)) + 1;

            cloneItem.editMeta(meta -> meta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, counts));

            if (counts >= commandsItem.maxUses()) {
                return cloneItem.getAmount() == 1 ? new ItemStack(Material.AIR) : ItemBuilder.of(this.toItemStack(this.toCommandsItem(cloneItem), player))
                        .amount(cloneItem.getAmount() - 1)
                        .build();
            }
        }
        return cloneItem;
    }

    public ItemStack toItemStack(CommandsItem commandsItem, Player player) {
        var itemStack = commandsItem.itemStack();

        itemStack.editMeta(itemMeta -> {
            itemMeta.displayName(commandsItem.displayName(player));
            itemMeta.lore(commandsItem.lore(player));

            itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.idKey(), PersistentDataType.STRING, commandsItem.key().value());
            itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, 0);
            if (!commandsItem.stackable()) {
                itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.uuidKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
            }
        });
        return itemStack;
    }

    public CommandsItem toCommandsItem(ItemStack itemStack) {
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        @Subst("key")
        var keyValue = NamespacedKeyUtils.namespace();
        @Subst("value")
        var value = Objects.requireNonNull(pdc.get(NamespacedKeyUtils.idKey(), PersistentDataType.STRING));
        return Objects.requireNonNull(this.commandItemRegistry.get(Key.key(keyValue, value)));
    }

    public void executeCommand(CommandsItem item, Player player, ActionUtils.ItemAction itemAction) {
        List<CustomCommand> byPlayerCommands = item.byPlayerCommands().get(itemAction) != null ?
                Lists.newArrayList(item.byPlayerCommands().get(itemAction)) : Collections.emptyList();

        List<CustomCommand> byConsoleCommands = item.byConsoleCommands().get(itemAction) != null ?
                Lists.newArrayList(item.byConsoleCommands().get(itemAction)) : Collections.emptyList();

        byPlayerCommands.forEach(customCommand -> customCommand.repeatCommands(player, customCommand, this.commandItem, false));

        byConsoleCommands.forEach(customCommand -> customCommand.repeatCommands(player, customCommand, this.commandItem, true));
    }
}
