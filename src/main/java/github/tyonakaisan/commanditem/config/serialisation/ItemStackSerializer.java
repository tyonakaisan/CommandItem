package github.tyonakaisan.commanditem.config.serialisation;

import com.google.inject.Inject;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Base64;

@DefaultQualifier(NonNull.class)
public final class ItemStackSerializer implements TypeSerializer<ItemStack> {

    private final ComponentLogger logger;

    @Inject
    public ItemStackSerializer(
            final ComponentLogger logger
    ) {
        this.logger = logger;
    }

    @Override
    public ItemStack deserialize(final Type type, final ConfigurationNode node) {
        final var value = node.getString("");

        this.logger.debug("value: {}", value);
        if (value.isEmpty()) {
            return ItemStack.empty();
        }

        final var decode = Base64.getDecoder().decode(value);
        this.logger.debug("decode: {}", decode);
        return ItemStack.deserializeBytes(decode);
    }

    @Override
    public void serialize(final Type type, final @Nullable ItemStack obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.set(Base64.getEncoder().encodeToString(obj.serializeAsBytes()));
        }
    }
}
