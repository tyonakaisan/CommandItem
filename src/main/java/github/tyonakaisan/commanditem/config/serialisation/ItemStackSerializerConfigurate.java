package github.tyonakaisan.commanditem.config.serialisation;

import com.google.inject.Inject;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@DefaultQualifier(NonNull.class)
public final class ItemStackSerializerConfigurate implements TypeSerializer<ItemStack> {

    private final ComponentLogger logger;

    private static final String DATA_VERSION = "v";
    private static final String MATERIAL_TYPE = "type";
    private static final String AMOUNT = "amount";
    private static final String ITEM_META = "item-meta";

    @Inject
    public ItemStackSerializerConfigurate(final ComponentLogger logger) {
        this.logger = logger;
    }

    @Override
    public ItemStack deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        Map<String, Object> objdeSerializeMap = new HashMap<>();

        objdeSerializeMap.put(DATA_VERSION, node.node(DATA_VERSION).getInt(Bukkit.getUnsafe().getDataVersion()));
        objdeSerializeMap.put(MATERIAL_TYPE, Objects.requireNonNull(node.node(MATERIAL_TYPE).getString()));
        objdeSerializeMap.put(AMOUNT, node.node(AMOUNT).getInt(1));

        var metaNode = node.node(ITEM_META);

        if (!metaNode.isNull()) {
            objdeSerializeMap.put("meta", Objects.requireNonNull(metaNode.get(ConfigurationSerializable.class)));
        }

        return ItemStack.deserialize(objdeSerializeMap);
    }

    @Override
    public void serialize(final Type type, final @Nullable ItemStack obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            Map<String, Object> objSerializeMap = obj.serialize();

            node.node(DATA_VERSION).set(objSerializeMap.get(DATA_VERSION));
            node.node(MATERIAL_TYPE).set(objSerializeMap.get(MATERIAL_TYPE));

            if (obj.hasItemMeta()) {
                var metaNode = node.node(ITEM_META);
                metaNode.set(ConfigurationSerializable.class, obj.getItemMeta());
            }
        }
    }
}
