package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.task.ItemHandler;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
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

    private final ItemHandler itemHandler;

    @Inject
    public ItemUseListener(
            final ItemHandler itemHandler
    ) {
        this.itemHandler = itemHandler;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        final var player = event.getPlayer();
        final @Nullable ItemStack itemStack = event.getItem();
        final @Nullable EquipmentSlot equipmentSlot = event.getHand();

        this.itemHandler.itemUseFromHand(itemStack, player, Action.Item.fromBukkitAction(event.getAction()), equipmentSlot, event);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        final var player = event.getPlayer();
        final var itemStack = event.getItem();
        final var equipmentSlot = event.getHand();

        this.itemHandler.itemUseFromHand(itemStack, player, Action.Item.CONSUME, equipmentSlot, event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        final var player = event.getPlayer();
        final var itemStack = event.getItemInHand();
        final var equipmentSlot = event.getHand();

        this.itemHandler.itemUseFromHand(itemStack, player, Action.Item.PLACE, equipmentSlot, event);
    }

    @EventHandler
    public void onItemFrameInteract(PlayerItemFrameChangeEvent event) {
        final var player = event.getPlayer();
        final var itemStack = event.getItemStack();
        final var action = event.getAction();

        // TODO FrameItemに対応させる
        this.itemHandler.itemUseFromFrame(itemStack, player, Action.Item.fromFrameAction(action), event);
    }
}
