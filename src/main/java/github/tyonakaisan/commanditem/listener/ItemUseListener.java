package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.CommandsItem;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.item.ItemCoolTimeManager;
import github.tyonakaisan.commanditem.message.MessageManager;
import github.tyonakaisan.commanditem.util.ActionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;

@DefaultQualifier(NonNull.class)
public final class ItemUseListener implements Listener {

    private final ConfigFactory configFactory;
    private final MessageManager messageManager;
    private final Convert convert;
    private final ItemCoolTimeManager itemCoolTimeManager;

    @Inject
    public ItemUseListener(
            final ConfigFactory configFactory,
            final MessageManager messageManager,
            final Convert convert,
            final ItemCoolTimeManager itemCoolTimeManager
    ) {
        this.configFactory = configFactory;
        this.messageManager = messageManager;
        this.convert = convert;
        this.itemCoolTimeManager = itemCoolTimeManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        @Nullable ItemStack item = event.getItem();

        if (this.convert.isCommandItem(item) && this.convert.checkInternalCoolTime(player.getUniqueId())) {
            var commandsItem = this.convert.toCommandsItem(item);
            var action = ActionUtils.ItemAction.fromBukkitAction(event.getAction());

            // 別枠
            if (this.isCoolTime(player, commandsItem)) {
                this.eventCanceled(event, player, commandsItem.key());
                return;
            }

            this.execute(player, Objects.requireNonNull(event.getHand()), item, commandsItem, action);

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !commandsItem.placeable()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        var item = event.getItem();

        if (this.convert.isCommandItem(item)) {
            var commandsItem = this.convert.toCommandsItem(item);
            var action = ActionUtils.ItemAction.CONSUME;

            if (this.isCoolTime(player, commandsItem)) {
                this.eventCanceled(event, player, commandsItem.key());
                return;
            }

            this.execute(player, Objects.requireNonNull(event.getHand()), item, commandsItem, action);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var item = event.getItemInHand();

        if (this.convert.isCommandItem(item)) {
            var commandsItem = this.convert.toCommandsItem(item);
            var action = ActionUtils.ItemAction.PLACE;

            if (this.isCoolTime(player, commandsItem)) {
                this.eventCanceled(event, player, commandsItem.key());
                return;
            }

            this.execute(player, Objects.requireNonNull(event.getHand()), item, commandsItem, action);
        }
    }

    private void execute(Player player, EquipmentSlot hand, ItemStack itemStack, CommandsItem commandsItem, ActionUtils.ItemAction action) {
        this.convert.setPlayerHandItem(player, hand, itemStack, action);

        if (this.convert.isMaxUsesExceeded(itemStack, player)) {
            player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "commanditem.error.max_uses_exceeded"));
            return;
        }

        this.convert.executeCommand(commandsItem, player, action);
    }

    private void eventCanceled(Cancellable cancellableEvent, Player player, Key key) {
        var timeLeft = this.itemCoolTimeManager.getRemainingItemCoolTime(player.getUniqueId(), key);
        var resolver = TagResolver.builder()
                .tag("time", Tag.selfClosingInserting(Component.text(timeLeft.toSeconds() + 1)))
                .build();
        player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "cooltime.error.during_cool_time", resolver));
        cancellableEvent.setCancelled(true);
    }

    private boolean isCoolTime(Player player, CommandsItem commandsItem) {
        var alertType = this.configFactory.primaryConfig().coolTime().coolTimeAlertType().toLowerCase();
        return this.itemCoolTimeManager.hasItemCoolTime(player.getUniqueId(), commandsItem.key()) && alertType.equals("message");
    }
}
