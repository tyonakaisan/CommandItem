package github.tyonakaisan.commanditem.util;

import org.bukkit.NamespacedKey;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class NamespacedKeyUtils {

    private NamespacedKeyUtils() {
    }

    private static final Pattern PATTERN = Pattern.compile("[a-z0-9_\\-.]+");
    private static final String NAMESPACE = "command_item";

    private static final NamespacedKey ID_KEY = new NamespacedKey(NAMESPACE, "id");
    private static final NamespacedKey USAGE_KEY = new NamespacedKey(NAMESPACE, "uses");
    private static final NamespacedKey UUID_KEY = new NamespacedKey(NAMESPACE, "uuid");
    private static final NamespacedKey TIMESTAMP_KEY = new NamespacedKey(NAMESPACE, "timestamp");

    public static NamespacedKey idKey() {
        return ID_KEY;
    }

    public static NamespacedKey usageKey() {
        return USAGE_KEY;
    }

    public static NamespacedKey uuidKey() {
        return UUID_KEY;
    }

    public static NamespacedKey timestampKey() {
        return TIMESTAMP_KEY;
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
