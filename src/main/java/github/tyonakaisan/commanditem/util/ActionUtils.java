package github.tyonakaisan.commanditem.util;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
@SuppressWarnings("unused")
public final class ActionUtils {

    private ActionUtils() {
        throw new IllegalStateException("Utility class");
    }

    public enum ItemAction {
        RIGHT_CLICK,
        LEFT_CLICK,
        PHYSICAL,
        CONSUME,
        PLACE;

        public static ItemAction fromBukkitAction(org.bukkit.event.block.Action action) {
            return switch (action) {
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> LEFT_CLICK;
                case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> RIGHT_CLICK;
                case PHYSICAL -> PHYSICAL;
            };
        }
    }

    public enum CommandAction {
        COMMAND,
        MESSAGE,
        BROAD_CAST
    }
}
