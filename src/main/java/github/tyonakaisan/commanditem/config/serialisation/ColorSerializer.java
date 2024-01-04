package github.tyonakaisan.commanditem.config.serialisation;

import com.google.inject.Inject;
import org.bukkit.Color;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

@DefaultQualifier(NonNull.class)
public final class ColorSerializer implements TypeSerializer<Color> {

    @Inject
    public ColorSerializer() {
        // Constructor for the injector.
    }

    @Override
    public Color deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        var red = node.node("RED").getInt();
        var green = node.node("GREEN").getInt();
        var blue = node.node("BLUE").getInt();
        return Color.fromRGB(red, green, blue);
    }

    @Override
    public void serialize(final Type type, final @Nullable Color color, final ConfigurationNode node) throws SerializationException {
        node.set(Integer.toHexString(Objects.requireNonNull(color).asRGB()));
    }
}
