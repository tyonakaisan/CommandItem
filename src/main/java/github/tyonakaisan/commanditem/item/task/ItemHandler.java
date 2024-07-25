package github.tyonakaisan.commanditem.item.task;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.Item;
import github.tyonakaisan.commanditem.item.registry.CoolTimeManager;
import github.tyonakaisan.commanditem.item.registry.ItemManager;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import github.tyonakaisan.commanditem.message.Messages;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@DefaultQualifier(NonNull.class)
public final class ItemHandler {

    private final CommandHandler commandHandler;
    private final ConfigFactory configFactory;
    private final ItemRegistry itemRegistry;
    private final ItemManager itemManager;
    private final CoolTimeManager coolTimeManager;
    private final ComponentLogger logger;

    @Inject
    public ItemHandler(
            final CommandHandler commandHandler,
            final ConfigFactory configFactory,
            final ItemRegistry itemRegistry,
            final ItemManager itemManager,
            final CoolTimeManager coolTimeManager,
            final ComponentLogger logger
    ) {
        this.commandHandler = commandHandler;
        this.configFactory = configFactory;
        this.itemRegistry = itemRegistry;
        this.itemManager = itemManager;
        this.coolTimeManager = coolTimeManager;
        this.logger = logger;
    }

    public void itemUseFromFrame(final @Nullable ItemStack itemStack, final Player player, final Action.Item action, final PlayerItemFrameChangeEvent event) {
        if (itemStack == null) {
            return;
        }

        if (this.itemUse(itemStack, player, action, event)) {
            event.setItemStack(this.addUseCountsIfNeeded(itemStack, player, action));
        }
    }

    public void itemUseFromHand(final @Nullable ItemStack itemStack, final Player player, final Action.Item action, final @Nullable EquipmentSlot hand, final Cancellable event) {
        if (itemStack == null) {
            return;
        }

        if (this.itemUse(itemStack, player, action, event)) {
            this.setPlayerHandItem(itemStack, player, action, hand);
        }
    }

    private boolean itemUse(final ItemStack itemStack, final Player player, final Action.Item action, final Cancellable event) {
        final @Nullable Item item = this.itemManager.toItem(itemStack);

        if (item != null && item.commands().containsKey(action)) {
            final var key = item.attributes().key();
            final var timeLeft = this.coolTimeManager.getRemainingCoolTime(player.getUniqueId(), key);

            final var cancel = item.attributes().eventCancel();
            if (action.isCancellable() && cancel) {
                event.setCancelled(true);
            }

            if (this.coolTimeManager.hasRemainingCoolTime(player.getUniqueId(), key)) {
                this.sendCoolTimeMessage(item, player, timeLeft);
                event.setCancelled(true); // Cancel an event to avoid consuming item
                return false;
            }

            if (this.itemManager.isMaxUsesExceeded(itemStack, player)) {
                player.sendMessage(Messages.translate("commanditem.error.max_uses_exceeded", player));
                return false;
            }

            if (!timeLeft.isPositive()) {
                if (item.commands().getOrDefault(action, List.of()).isEmpty()) {
                    return false;
                }

                this.commandHandler.runCommands(item, player, action);
                this.coolTimeManager.removeAllCoolTime(player.getUniqueId(), key);
                this.coolTimeManager.setCoolTime(player.getUniqueId(), key, Duration.ofSeconds(item.attributes().coolTime(player)));
            }
            return true;
        }

        return false;
    }

    private void sendCoolTimeMessage(final Item item, final Player player, final Duration duration) {
        final var type = this.configFactory.primaryConfig().coolTime().coolTimeAlertType();

        if (item.attributes().hideCoolTimeAnnounce()) {
            return;
        }
        final var message = Messages.translate("cooltime.error.during_cool_time", player,
                resolver -> resolver.tag("time", Tag.selfClosingInserting(Component.text(duration.toSeconds() + 1))));

        switch (type) {
            case CHAT -> player.sendMessage(message);
            case ACTION_BAR -> player.sendActionBar(message);
            case VANILLA -> player.setCooldown(item.rawItemStack().getType(), (int) duration.toSeconds());
        }
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

    public ItemStack addUseCountsIfNeeded(final ItemStack itemStack, final Player player, final Action.Item action) {
        final var cloneItemStack = itemStack.clone();
        final @Nullable Item item = this.itemManager.toItem(cloneItemStack);

        if (item == null || item.attributes().maxUses(player) < 0 || !item.commands().containsKey(action)) {
            return itemStack;
        }

        // return usage counts
        final var oldCounts = this.itemManager.getUsagesOrDefault(cloneItemStack, Integer.MAX_VALUE);
        final var newCounts = oldCounts + 1;

        if (newCounts >= item.attributes().maxUses(player)) {
            var reduceAmountItem = item.asItemStack(player);
            reduceAmountItem.setAmount(cloneItemStack.getAmount() - 1);

            return cloneItemStack.getAmount() == 1
                    ? ItemStack.empty()
                    : reduceAmountItem;
        } else {
            cloneItemStack.editMeta(meta -> meta.getPersistentDataContainer().set(NamespacedKeyUtils.usageKey(), PersistentDataType.INTEGER, newCounts));
            return cloneItemStack;
        }
    }

    public void giveItem(final Collection<Player> targets, final Audience audience, final Key key, final int count) {
        this.giveItem(targets, audience, key, count, false);
    }

    public void giveItem(final Collection<Player> targets, final Audience audience, final Key key, final int count, final boolean raw) {
        final @Nullable Item item = this.itemRegistry.item(key);
        if (item == null) {
            audience.sendMessage(Messages.translate("command.give.error.unknown_item", audience,
                    resolver -> resolver.tag("item", Tag.selfClosingInserting(Component.text(key.asString())))));
            return;
        }

        targets.forEach(target -> {
            final var itemStack = raw
                    ? item.rawItemStack()
                    : item.asItemStack(target);

            if (this.isMaxStackSize(audience, itemStack, count)) {
                return;
            }

            this.addItem(target, itemStack, count, item.attributes().stackable());

            // Logging to the console
            if (!(audience instanceof ConsoleCommandSender)) {
                this.logger.info("Gave {} {} to {}.", count, PlainTextComponentSerializer.plainText().serialize(itemStack.displayName()), target.displayName());
            }

            audience.sendMessage(Messages.translate("command.give.info.give", audience, resolver -> {
                resolver.tag("player", Tag.selfClosingInserting(target.displayName()));
                resolver.tag("item", Tag.selfClosingInserting(itemStack.displayName()));
                resolver.tag("count", Tag.selfClosingInserting(Component.text(count)));
            }));

            target.playSound(Sound.sound()
                    .type(Key.key("minecraft:entity.item.pickup"))
                    .volume(0.3f)
                    .pitch(2f)
                    .build());
        });
    }

    private void addItem(final Player target, final ItemStack itemStack, final int count, final boolean stackable) {
        final var maxStackSize = itemStack.getMaxStackSize();

        if (!stackable || maxStackSize == 1) {
            for (int i = 0; i < count; i++) {
                target.getInventory().addItem(itemStack);
            }
        } else {
            final var cloneItemStack = itemStack.clone();
            final var fullStacks = count / maxStackSize;
            final var remainder = count % maxStackSize;

            if (fullStacks > 0) {
                cloneItemStack.setAmount(maxStackSize);
                for (int i = 0; i < fullStacks; i++) {
                    target.getInventory().addItem(cloneItemStack);
                }

                if (remainder > 0) {
                    cloneItemStack.setAmount(remainder);
                    target.getInventory().addItem(cloneItemStack);
                }
            } else {
                cloneItemStack.setAmount(count);
                target.getInventory().addItem(cloneItemStack);
            }
        }
    }

    private boolean isMaxStackSize(final Audience audience, final ItemStack itemStack, final int count) {
        final var maxReceive = itemStack.getMaxStackSize() * 36;

        if (count > maxReceive) {
            audience.sendMessage(Messages.translate("command.give.error.max_count", audience, resolver -> {
                resolver.tag("max", Tag.selfClosingInserting(Component.text(maxReceive)));
                resolver.tag("item", Tag.selfClosingInserting(itemStack.displayName()));
            }));
            return true;
        }
        return false;
    }
}
