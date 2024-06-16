package github.tyonakaisan.commanditem.item;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class Action {

    private Action() {}

    @SuppressWarnings("unused")
    public enum Item {
        RIGHT_CLICK(true),
        LEFT_CLICK(false),
        PHYSICAL(false),
        CONSUME(false),
        PLACE(true),
        ITEM_FRAME_PLACE(false),
        ITEM_FRAME_REMOVE(false),
        ITEM_FRAME_ROTATE(true),
        BREAK(false), // unsupported
        DROP(false), // unsupported
        ITEM_DAMAGE(false), // unsupported
        ITEM_BREAK(false); // unsupported

        private final boolean cancellable;

        Item(final boolean cancellable) {
            this.cancellable = cancellable;
        }

        public boolean isCancellable() {
            return this.cancellable;
        }

        public static Action.Item fromBukkitAction(final org.bukkit.event.block.Action action) {
            return switch (action) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> LEFT_CLICK;
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> RIGHT_CLICK;
                case PHYSICAL -> PHYSICAL;
            };
        }

        public static Action.Item fromBukkitAction(final PlayerItemFrameChangeEvent.ItemFrameChangeAction action) {
            return switch (action) {
                case PLACE -> ITEM_FRAME_PLACE;
                case REMOVE -> ITEM_FRAME_REMOVE;
                case ROTATE -> ITEM_FRAME_ROTATE;
            };
        }
    }

    public enum Command {
        COMMAND,
        FROZEN,
        MESSAGE,
        BROAD_CAST
    }
}
