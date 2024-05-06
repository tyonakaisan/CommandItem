package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.CommandItemHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ItemUseListener implements Listener {
    private final CommandItemHandler commandItemHandler;

    @Inject
    public ItemUseListener(
            final CommandItemHandler commandItemHandler
    ) {
        this.commandItemHandler = commandItemHandler;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        @Nullable ItemStack itemStack = event.getItem();
        @Nullable EquipmentSlot equipmentSlot = event.getHand();

        this.commandItemHandler.itemUse(event, player, itemStack, equipmentSlot, Action.Item.fromBukkitAction(event.getAction()));
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        var itemStack = event.getItem();
        var equipmentSlot = event.getHand();

        this.commandItemHandler.itemUse(event, player, itemStack, equipmentSlot, Action.Item.CONSUME);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var itemStack = event.getItemInHand();
        var equipmentSlot = event.getHand();

        this.commandItemHandler.itemUse(event, player, itemStack, equipmentSlot, Action.Item.PLACE);
    }
}
