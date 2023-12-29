package github.tyonakaisan.commanditem.item;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.CommandExecutor;
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

    public void setPlayerHandItem(Player player, @Nullable EquipmentSlot equipmentSlot, ItemStack item, ActionUtils.ItemAction itemAction) {
        if (equipmentSlot == null) return;
        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(reduceUsageCounts(item, itemAction));
        } else {
            player.getInventory().setItemInOffHand(reduceUsageCounts(item, itemAction));
        }
    }

    private ItemStack reduceUsageCounts(ItemStack itemStack, ActionUtils.ItemAction itemAction) {
        var cloneItem = itemStack.clone();
        var pdc = cloneItem.getItemMeta().getPersistentDataContainer();
        var commandsItem = this.toCommandsItem(cloneItem);

        // 重ねれるアイテムの場合、カウント減らしたあとに分けると両方へってしまう（？）
        // 例：2こ(残り3)->一回使用&分ける->1こ(残り2)&1こ(残り2)
        if (pdc.has(NamespacedKeyUtils.usageKey())
                && (commandsItem.byPlayerCommands().containsKey(itemAction) || commandsItem.byConsoleCommands().containsKey(itemAction))) {
            int counts = Objects.requireNonNull(pdc.get(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER)) - 1;

            if (counts <= -1) {
                return cloneItem;
            }

            cloneItem.editMeta(meta -> meta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, counts));

            if (counts == 0) {
                return cloneItem.getAmount() == 1 ? new ItemStack(Material.AIR) : ItemBuilder.of(this.toItemStack(this.toCommandsItem(cloneItem)))
                        .amount(cloneItem.getAmount() - 1)
                        .build();
            }
        }
        return cloneItem;
    }

    public ItemStack toItemStack(CommandsItem commandsItem) {
        var itemStack = commandsItem.itemStack();

        itemStack.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.idKey(), PersistentDataType.STRING, commandsItem.key().value());
            itemMeta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, commandsItem.maxUses());
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
        List<String> byPlayerCommands = item.byPlayerCommands().get(itemAction) != null ?
                Lists.newArrayList(item.byPlayerCommands().get(itemAction)) : Collections.emptyList();

        List<String> byConsoleCommands = item.byConsoleCommands().get(itemAction) != null ?
                Lists.newArrayList(item.byConsoleCommands().get(itemAction)) : Collections.emptyList();

        byPlayerCommands.forEach(command -> CommandExecutor.executeByPlayer(command, player));
        byConsoleCommands.forEach(command -> CommandExecutor.executeByConsole(command, player));
    }
}
