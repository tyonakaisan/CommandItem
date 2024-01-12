package github.tyonakaisan.commanditem.config.serialisation;

import com.google.inject.Inject;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;


@DefaultQualifier(NonNull.class)
public final class EnchantmentSerializer implements TypeSerializer<EnchantmentSerializer.Enchant> {

    private final ComponentLogger logger;

    @Inject
    public EnchantmentSerializer(final ComponentLogger logger) {
        this.logger = logger;
    }

    @Override
    public Enchant deserialize(final Type type, final ConfigurationNode node) {
        Map<Enchantment, Integer> enchantMap = new HashMap<>();
        Enchant enchant = new Enchant(enchantMap);

        for (ConfigurationNode enchantMapNode : node.childrenMap().values()) {
            var enchantName = requireNonNull(enchantMapNode.key()).toString();
            var level = enchantMapNode.getInt(1);

            var enchantmentKey = NamespacedKey.minecraft(requireNonNull(enchantName));
            var enchantment = requireNonNull(Enchantment.getByKey(enchantmentKey));
            enchantMap.put(enchantment, level);
            enchant = new Enchant(enchantMap);
        }
        return enchant;
    }

    @Override
    public void serialize(final Type type, final @Nullable Enchant obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
        }
    }

    @DefaultQualifier(NonNull.class)
    public record Enchant(Map<Enchantment, Integer> enchantMap) {

    }
}
