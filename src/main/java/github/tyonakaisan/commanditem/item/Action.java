package github.tyonakaisan.commanditem.item;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class Action {

    private Action() {}

    public enum Item {
        RIGHT_CLICK,
        LEFT_CLICK,
        PHYSICAL,
        CONSUME,
        PLACE,
        BREAK, // unsupported
        DROP, // unsupported
        ITEM_DAMAGE, // unsupported
        ITEM_BREAK; // unsupported

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
        MESSAGE,
        BROAD_CAST
    }
}
