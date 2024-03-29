package github.tyonakaisan.commanditem.util;

import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.regex.Pattern;

@DefaultQualifier(NonNull.class)
@SuppressWarnings("unused")
public final class NamespaceKeyUtils {

    private NamespaceKeyUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern PATTERN = Pattern.compile("[a-z0-9_\\-.]+");
    private static final String NAMESPACE = "command_item";

    private static final NamespacedKey ID_KEY = new NamespacedKey(NAMESPACE, "id");
    private static final NamespacedKey USAGE_KEY = new NamespacedKey(NAMESPACE, "uses");
    private static final NamespacedKey UUID_KEY = new NamespacedKey(NAMESPACE, "uuid");

    public static NamespacedKey idKey() {
        return ID_KEY;
    }

    public static NamespacedKey usageKey() {
        return USAGE_KEY;
    }

    public static NamespacedKey uuidKey() {
        return UUID_KEY;
    }

    public static String namespace() {
        return NAMESPACE;
    }

    public static Pattern pattern() {
        return PATTERN;
    }

    public static boolean checkKeyStringPattern(String value) {
        return PATTERN.matcher(value).matches();
    }
}
