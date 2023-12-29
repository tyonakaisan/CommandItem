package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.util.ActionUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

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

        if (this.convert.isCommandItem(item)) {
            var commandsItem = this.convert.toCommandsItem(item);
            var action = ActionUtils.ItemAction.fromBukkitAction(event.getAction());

            this.convert.setPlayerHandItem(player, event.getHand(), item, action);
            this.convert.executeCommand(commandsItem, player, action);

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

            this.convert.setPlayerHandItem(player, event.getHand(), item, action);
            this.convert.executeCommand(commandsItem, player, action);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var item = event.getItemInHand();

        if (this.convert.isCommandItem(item)) {
            var commandsItem = this.convert.toCommandsItem(item);
            var action = ActionUtils.ItemAction.PLACE;

            this.convert.setPlayerHandItem(player, event.getHand(), item, action);
            this.convert.executeCommand(commandsItem, player, action);
        }
    }
}
