package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.Convert;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
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

        if (convert.isCommandItem(event.getItem())) {
            var item = event.getItem();
            var itemRecord = this.convert.toCommandItemRecord(item);
            String action = "";

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !itemRecord.canPlace()) {
                event.setCancelled(true);
                return;
            }

            switch (event.getAction()) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> action = "LEFT_CLICK";
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> action = "RIGHT_CLICK";
                case PHYSICAL -> action = "PHYSICAL";
            }

            convert.setPlayerHandItem(player, event.getHand(), item, action);
            convert.executeCommand(itemRecord, player, action);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();

        if (convert.isCommandItem(event.getItem())) {
            var item = event.getItem();
            var itemRecord = this.convert.toCommandItemRecord(item);
            String action = "CONSUME";

            convert.executeCommand(itemRecord, player, action);
        }
    }
}
