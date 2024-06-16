package github.tyonakaisan.commanditem.item;

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
        BREAK(false), // unsupported
        DROP(false), // unsupported
        ITEM_DAMAGE(false), // unsupported
        ITEM_BREAK(false); // unsupported

        private final boolean placeCancellable;

        Item(final boolean placeCancellable) {
            this.placeCancellable = placeCancellable;
        }

        public boolean isPlaceCancellable() {
            return this.placeCancellable;
        }

        public static Action.Item fromBukkitAction(final org.bukkit.event.block.Action action) {
            return switch (action) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> LEFT_CLICK;
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> RIGHT_CLICK;
                case PHYSICAL -> PHYSICAL;
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
