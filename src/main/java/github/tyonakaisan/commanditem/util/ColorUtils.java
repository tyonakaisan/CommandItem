package github.tyonakaisan.commanditem.util;

import org.bukkit.Color;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ColorUtils {

    private ColorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static org.bukkit.Color fromHexString(String string) {
        if (!string.startsWith("#")) {
            string = "#" + string;
        }
        var color = java.awt.Color.decode(string);
        return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
    }
}
