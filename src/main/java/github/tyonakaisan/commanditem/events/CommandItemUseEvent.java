package github.tyonakaisan.commanditem.events;

import github.tyonakaisan.commanditem.item.Item;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class CommandItemUseEvent extends Event {

    private final Item item;

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public CommandItemUseEvent(
            final Item item
    ) {
        this.item = item;
    }

    public Item item() {
        return this.item;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
