package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.util.CommandExecutor;
import github.tyonakaisan.commanditem.util.ItemBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Singleton
@DefaultQualifier(NonNull.class)
public final class Convert {
    private final Path dataDirectory;
    private final CommandItem commandItem;
    private final CommandItemRegistry commandItemRegistry;

    public static final String NAMESPACE = "command_item";
    public static final NamespacedKey commandItemKey = new NamespacedKey(NAMESPACE, "item_id");
    public static final NamespacedKey commandItemUsageCountsKey = new NamespacedKey(NAMESPACE, "counts");
    public static final NamespacedKey commandItemUnStackableKey = new NamespacedKey(NAMESPACE, "item_uuid");

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
        return pdc.has(commandItemKey);
    }

    public ItemStack reduceUsageCounts(ItemStack itemStack, String action) {
        var item = itemStack.clone();
        var pdc = item.getItemMeta().getPersistentDataContainer();
        var itemRecord = this.toCommandItemRecord(item);

        if(pdc.has(commandItemUsageCountsKey)
                && (itemRecord.byPlayerCommands().containsKey(action) || itemRecord.byConsoleCommands().containsKey(action))) {
            int counts = Objects.requireNonNull(pdc.get(commandItemUsageCountsKey, PersistentDataType.INTEGER)) - 1;

            item.editMeta(meta -> meta.getPersistentDataContainer().set(commandItemUsageCountsKey, PersistentDataType.INTEGER, counts));

            if (counts == 0) {
                if (item.getAmount() == 1) return new ItemStack(Material.AIR);

                return ItemBuilder.of(this.toItemStack(this.toCommandItemRecord(item)))
                        .amount(item.getAmount() - 1)
                        .build();
            }
        }
        return item;
    }

    public void setPlayerHandItem(Player player, @Nullable EquipmentSlot equipmentSlot, ItemStack item, String action) {
        if (equipmentSlot == null) return;
        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(reduceUsageCounts(item, action));
        } else {
            player.getInventory().setItemInOffHand(reduceUsageCounts(item, action));
        }
    }

    public ItemStack toItemStack(ItemRecord itemRecord) {
        var displayName = itemRecord.displayName();
        var item = itemRecord.item().clone();

        var loreList = new ArrayList<>(itemRecord.lore());

        item.editMeta(meta -> {
            meta.displayName(Component.empty()
                    .decoration(TextDecoration.ITALIC, false)
                    .append(MiniMessage.miniMessage().deserialize(displayName))
            );

            meta.lore(loreList.stream()
                    .map(lore -> Component.empty()
                            .color(NamedTextColor.WHITE)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(MiniMessage.miniMessage().deserialize(lore)))
                    .toList());
            //pdc
            meta.getPersistentDataContainer().set(commandItemKey, PersistentDataType.STRING, itemRecord.key().value());
            meta.getPersistentDataContainer().set(commandItemUsageCountsKey, PersistentDataType.INTEGER, itemRecord.usageCounts());
            if (itemRecord.unStackable()) {
                meta.getPersistentDataContainer().set(commandItemUnStackableKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            }
        });

        return item;
    }

    public ItemRecord toCommandItemRecord(ItemStack itemStack) {
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();

        ItemRecord itemRecord;
        var itemValue = Objects.requireNonNull(pdc.get(commandItemKey, PersistentDataType.STRING));
        itemRecord = Objects.requireNonNull(this.commandItemRegistry.get(Key.key(NAMESPACE, itemValue)));

        return itemRecord;
    }

    public void executeCommand(ItemRecord itemRecord, Player player, String action) {
        var byPlayerCommands = new ArrayList<String>();
        var byConsoleCommands = new ArrayList<String>();

        if (itemRecord.byPlayerCommands().get(action) != null) byPlayerCommands = (ArrayList<String>) itemRecord.byPlayerCommands().get(action);
        if (itemRecord.byConsoleCommands().get(action) != null) byConsoleCommands = (ArrayList<String>) itemRecord.byConsoleCommands().get(action);

        byPlayerCommands.forEach(command -> CommandExecutor.executeByPlayer(command, player));
        byConsoleCommands.forEach(command -> CommandExecutor.executeByConsole(command, player));
    }
}
