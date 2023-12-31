package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.CommandsItem;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.util.ActionUtils;
import org.bukkit.entity.Player;
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
    private final CommandItem commandItem;
    private final Convert convert;
    private final CommandItemRegistry commandItemRegistry;

    @Inject
    ItemUseListener(
            final CommandItem commandItem,
            final Convert convert,
            final CommandItemRegistry commandItemRegistry
    ) {
        this.commandItem = commandItem;
        this.convert = convert;
        this.commandItemRegistry = commandItemRegistry;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        @Nullable ItemStack item = event.getItem();

        if (this.convert.isCommandItem(item) && this.convert.checkInternalCoolTime(player.getUniqueId())) {
            var commandsItem = this.convert.toCommandsItem(item);
            var action = ActionUtils.ItemAction.fromBukkitAction(event.getAction());

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

            this.execute(player, Objects.requireNonNull(event.getHand()), item, commandsItem, action);
        }
    }


    private void execute(Player player, EquipmentSlot hand, ItemStack itemStack, CommandsItem commandsItem, ActionUtils.ItemAction action) {
        this.convert.setPlayerHandItem(player, hand, itemStack, action);

        if (this.convert.isMaxUsesExceeded(itemStack)) {
            player.sendRichMessage("<red>最大使用回数を超えているためコマンドは実行されません!");
            return;
        }

        this.convert.executeCommand(commandsItem, player, action);
    }
}
