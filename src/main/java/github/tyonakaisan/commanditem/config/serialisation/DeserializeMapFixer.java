package github.tyonakaisan.commanditem.config.serialisation;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

import java.util.Map;

@DefaultQualifier(NonNull.class)
public final class DeserializeMapFixer {
    private static final Logger logger = ComponentLogger.logger(DeserializeMapFixer.class.getName());

    private static final String SATURATION = "saturation";
    private static final String EAT_SECONDS = "eat-seconds";
    private static final String PROBABILITY = "probability";

    private DeserializeMapFixer() {
    }
    
    public static Map<String, Object> start(Map<String, Object> map) {
        fixFood(map);
        fixFoodEffect(map);
        return map;
    }

    private static void fixFood(Map<String, Object> map) {
        convertValueToFloat(map, SATURATION);
        convertValueToFloat(map, EAT_SECONDS);
    }

    private static void fixFoodEffect(Map<String, Object> map) {
        convertValueToFloat(map, PROBABILITY);
    }

    private static void convertValueToFloat(Map<String, Object> map, final String key) {
        map.computeIfPresent(key, (k, v) -> {
            if (v instanceof Number number) {
                return number.floatValue();
            } else {
                logger.warn("Key '{}' has a non-number value '{}'", k, v);
                return 0;
            }
        });
    }
}
