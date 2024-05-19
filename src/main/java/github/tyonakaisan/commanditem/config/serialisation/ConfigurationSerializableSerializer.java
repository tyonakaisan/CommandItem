package github.tyonakaisan.commanditem.config.serialisation;

import com.google.inject.Inject;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;

@DefaultQualifier(NonNull.class)
@SuppressWarnings("java:S3776")
public class ConfigurationSerializableSerializer implements TypeSerializer<ConfigurationSerializable> {

    private final ComponentLogger logger;

    @Inject
    public ConfigurationSerializableSerializer(
            final ComponentLogger logger
    ) {
        this.logger = logger;
    }

    // 汚い
    @Override
    public ConfigurationSerializable deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        LinkedHashMap<String, Object> deserializeMap = new LinkedHashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            var key = entry.getKey().toString();
            var serializableNode = entry.getValue();

            if (serializableNode.isMap()) {
                var serializableMap = serializableNode.childrenMap();

                if (serializableMap.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    Optional.ofNullable(serializableNode.get(ConfigurationSerializable.class))
                                    .ifPresent(object -> deserializeMap.put(key, object));
                } else {
                    LinkedHashMap<String, Object> mapInMap = new LinkedHashMap<>();

                    for (Map.Entry<Object, ? extends ConfigurationNode> secondEntry : serializableMap.entrySet()) {
                        var secondKey = secondEntry.getKey().toString();
                        var secondSerializableNode = secondEntry.getValue();

                        if (secondSerializableNode.isMap()) {
                            Optional.ofNullable(secondSerializableNode.get(ConfigurationSerializable.class))
                                            .ifPresent(object -> mapInMap.put(secondKey, object));
                        } else if (secondSerializableNode.isList()) {
                            var objects = new ArrayList<>();

                            for (ConfigurationNode listNode : secondSerializableNode.childrenList()) {
                                objects.add(listNode.get(ConfigurationSerializable.class));
                            }

                            mapInMap.put(secondKey, objects);
                        } else {
                            Optional.ofNullable(secondSerializableNode.raw())
                                            .ifPresent(object -> mapInMap.put(secondKey, object));
                        }
                    }
                    deserializeMap.put(key, mapInMap);
                }
            } else if (serializableNode.isList()) {
                var objects = new ArrayList<>();

                for (ConfigurationNode listNode : serializableNode.childrenList()) {
                    if (listNode.isMap() && listNode.childrenMap().containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                        objects.add(listNode.get(ConfigurationSerializable.class));
                    } else if (listNode.isMap() && listNode.childrenMap().containsKey("v")) {
                        objects.add(listNode.get(ItemStack.class));
                    } else {
                        objects.add(listNode.raw());
                    }
                }

                deserializeMap.put(key, objects);
            } else {
                Optional.ofNullable(serializableNode.raw())
                                .ifPresent(object -> deserializeMap.put(key, object));
            }
        }
        this.logger.debug("map: {}", deserializeMap);
        return Objects.requireNonNull(ConfigurationSerialization.deserializeObject(DeserializeMapFixer.start(deserializeMap)));
    }

    @Override
    public void serialize(final Type type, final @Nullable ConfigurationSerializable obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.node(ConfigurationSerialization.SERIALIZED_TYPE_KEY).set(ConfigurationSerialization.getAlias(obj.getClass()));
            obj.serialize().forEach((key, object) -> {
                this.logger.debug("[?] key:{}, obj: {}, class: {}", key, object, object.getClass());

                try {
                    if (object instanceof Collection<?> collections) {
                        // 空のリストがあるとスキップされてロードした時に読み込めなくなる対策
                        if (collections.isEmpty()) {
                            node.node(key).set(Collections.emptyList());
                        } else {
                            collections.forEach(value -> {
                                try {
                                    this.logger.debug("[Collection] key:{}, obj: {}, class: {}", key, value, value.getClass());
                                    node.node(key).appendListNode().set(value);
                                } catch (SerializationException e) {
                                    this.logger.error(String.format("Collections %s: %s serialization failed.", key, value), e);
                                }
                            });
                        }
                        return;
                    }

                    if (object instanceof Map<?, ?> map) {
                        map.forEach((mapKey, mapValue) -> {
                            if (mapValue instanceof Collection<?> collections1) {
                                if (!collections1.isEmpty()) {
                                    collections1.forEach(value -> {
                                        try {
                                            this.logger.debug("[MapInCollection] key:{}, obj: {}, class: {}", key, value, value.getClass());
                                            node.node(key).node(mapKey).appendListNode().set(value);
                                        } catch (SerializationException e) {
                                            this.logger.error(String.format("Map in collections %s:%s serialization failed.", key, map), e);
                                        }
                                    });
                                } else {
                                    try {
                                        node.node(key).node(mapKey).set(Collections.emptyList());
                                    } catch (SerializationException e) {
                                        this.logger.error(String.format("Map in collections %s:%s serialization failed.", key, map), e);
                                    }
                                }
                            } else {
                                try {
                                    this.logger.debug("[MapValue] key:{}, mapKey: {}, obj: {}, class: {}", key, mapKey, mapValue, mapValue.getClass());
                                    node.node(key).node(mapKey).set(mapValue);
                                } catch (SerializationException e) {
                                    this.logger.error(String.format("Map value %s:%s serialization failed.", key, map), e);
                                }
                            }
                        });
                        return;
                    }

                    this.logger.debug("[Normal] key:{}, obj: {}, class: {}", key, object, object.getClass());
                    node.node(key).set(object.getClass(), object);
                } catch (SerializationException e) {
                    this.logger.error("", e);
                }
            });
        }
    }
}
