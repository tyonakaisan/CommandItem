package github.tyonakaisan.commanditem.item;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.ItemBuilder;
import github.tyonakaisan.commanditem.util.NamespaceKeyUtils;
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

import java.time.Duration;
import java.util.*;

@Singleton
@DefaultQualifier(NonNull.class)
public final class Convert {

    private final CommandItem commandItem;
    private final ConfigFactory configFactory;
    private final CommandItemRegistry commandItemRegistry;
    private final ItemCoolTimeManager itemCoolTimeManager;

    private final Map<UUID, Long> internalCoolTime = new HashMap<>();

    @Inject
    public Convert(
            final CommandItem commandItem,
            final ConfigFactory configFactory,
            final CommandItemRegistry commandItemRegistry,
            final ItemCoolTimeManager itemCoolTimeManager
    ) {
        this.commandItem = commandItem;
        this.configFactory = configFactory;
        this.commandItemRegistry = commandItemRegistry;
        this.itemCoolTimeManager = itemCoolTimeManager;
    }

    public boolean isCommandItem(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getItemMeta() == null) return false;
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return pdc.has(NamespaceKeyUtils.idKey());
    }

    public boolean isMaxUsesExceeded(ItemStack itemStack, Player player) {
        if (!this.isCommandItem(itemStack)) return false;
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        int currentCount = pdc.getOrDefault(NamespaceKeyUtils.usageKey(), PersistentDataType.INTEGER, 0);
        if (this.toCommandsItem(itemStack).maxUses(player) <= -1) {
            return false;
        }
        return currentCount > this.toCommandsItem(itemStack).maxUses(player);
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

    public void setPlayerHandItem(Player player, @Nullable EquipmentSlot equipmentSlot, ItemStack item, ActionUtils.ItemAction itemAction) {
        if (equipmentSlot == null) return;
        if (equipmentSlot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(this.updateCounts(item, itemAction, player));
        } else {
            player.getInventory().setItemInOffHand(this.updateCounts(item, itemAction, player));
        }
    }

    private ItemStack updateCounts(ItemStack itemStack, ActionUtils.ItemAction itemAction, Player player) {
        var cloneItem = itemStack.clone();
        var pdc = cloneItem.getItemMeta().getPersistentDataContainer();
        var commandsItem = this.toCommandsItem(cloneItem);
        var alertType = this.configFactory.primaryConfig().coolTime().coolTimeAlertType().toLowerCase();

        if (pdc.has(NamespaceKeyUtils.usageKey())
                && (commandsItem.byPlayerCommands().containsKey(itemAction) || commandsItem.byConsoleCommands().containsKey(itemAction))) {

            if (alertType.equals("vanilla")) {
                player.setCooldown(cloneItem.getType(), commandsItem.coolTime(player) * 20);
            }

            if (commandsItem.maxUses(player) <= -1) {
                return cloneItem;
            }

            int counts = Objects.requireNonNull(pdc.get(NamespaceKeyUtils.usageKey(), PersistentDataType.INTEGER)) + 1;

            cloneItem.editMeta(meta -> meta.getPersistentDataContainer().set(NamespaceKeyUtils.usageKey(), PersistentDataType.INTEGER, counts));

            if (counts >= commandsItem.maxUses(player)) {
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

            itemMeta.getPersistentDataContainer().set(NamespaceKeyUtils.idKey(), PersistentDataType.STRING, commandsItem.key().value());
            itemMeta.getPersistentDataContainer().set(NamespaceKeyUtils.usageKey(), PersistentDataType.INTEGER, 0);
            if (!commandsItem.stackable()) {
                itemMeta.getPersistentDataContainer().set(NamespaceKeyUtils.uuidKey(), PersistentDataType.STRING, UUID.randomUUID().toString());
            }
        });
        return itemStack;
    }

    public CommandsItem toCommandsItem(ItemStack itemStack) {
        var pdc = itemStack.getItemMeta().getPersistentDataContainer();
        @Subst("namespace")
        var namespace = NamespaceKeyUtils.namespace();
        @Subst("value")
        var value = Objects.requireNonNull(pdc.get(NamespaceKeyUtils.idKey(), PersistentDataType.STRING));
        return Objects.requireNonNull(this.commandItemRegistry.get(Key.key(namespace, value)));
    }

    public void executeCommand(CommandsItem commandsItem, Player player, ActionUtils.ItemAction itemAction) {
        List<CustomCommand> byPlayerCommands = commandsItem.byPlayerCommands().get(itemAction) != null ?
                Lists.newArrayList(commandsItem.byPlayerCommands().get(itemAction)) : Collections.emptyList();

        List<CustomCommand> byConsoleCommands = commandsItem.byConsoleCommands().get(itemAction) != null ?
                Lists.newArrayList(commandsItem.byConsoleCommands().get(itemAction)) : Collections.emptyList();

        var timeLeft = this.itemCoolTimeManager.getRemainingItemCoolTime(player.getUniqueId(), commandsItem.key());

        if (timeLeft.isZero() || timeLeft.isNegative()) {
            byPlayerCommands.forEach(customCommand ->
                    customCommand.repeatCommands(player, customCommand, this.commandItem, false));

            byConsoleCommands.forEach(customCommand ->
                    customCommand.repeatCommands(player, customCommand, this.commandItem, true));

            if (!byPlayerCommands.isEmpty() || !byConsoleCommands.isEmpty()) {
                this.itemCoolTimeManager.removeItemCoolTime(player.getUniqueId(), commandsItem.key());
                this.itemCoolTimeManager.setItemCoolTime(player.getUniqueId(), commandsItem.key(), Duration.ofSeconds(commandsItem.coolTime(player)));
            }
        }
    }
}
