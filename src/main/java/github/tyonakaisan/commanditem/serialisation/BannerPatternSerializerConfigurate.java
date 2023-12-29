package github.tyonakaisan.commanditem.serialisation;

import org.bukkit.block.banner.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@DefaultQualifier(NonNull.class)
public class BannerPatternSerializerConfigurate implements TypeSerializer<Pattern> {
    @Override
    public Pattern deserialize(Type type, ConfigurationNode node) throws SerializationException {
        System.out.println(node);
        return null;
    }

    @Override
    public void serialize(Type type, @Nullable Pattern obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            var test = obj.serialize();
            node.set(test);
        }
    }
}
