package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.CommandItemHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        final var player = event.getPlayer();
        final @Nullable ItemStack itemStack = event.getItem();
        final @Nullable EquipmentSlot equipmentSlot = event.getHand();

        this.commandItemHandler.itemUse(itemStack, player, Action.Item.fromBukkitAction(event.getAction()), equipmentSlot, event);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        final var player = event.getPlayer();
        final var itemStack = event.getItem();
        final var equipmentSlot = event.getHand();

        this.commandItemHandler.itemUse(itemStack, player, Action.Item.CONSUME, equipmentSlot, event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        final var player = event.getPlayer();
        final var itemStack = event.getItemInHand();
        final var equipmentSlot = event.getHand();

        this.commandItemHandler.itemUse(itemStack, player, Action.Item.PLACE, equipmentSlot, event);
    }
}
