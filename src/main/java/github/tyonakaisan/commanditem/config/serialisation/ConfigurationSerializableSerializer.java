package github.tyonakaisan.commanditem.config.serialisation;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.inject.Inject;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.codehaus.plexus.util.Base64;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

@DefaultQualifier(NonNull.class)
@SuppressWarnings("java:S3776")
public class ConfigurationSerializableSerializer implements TypeSerializer<ConfigurationSerializable> {

    private final ComponentLogger logger;

    private static final String SKULL_TEXTURE = "skull-texture";

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
            var name = entry.getKey().toString();
            var serializableNode = entry.getValue();

            if (name.equals(SKULL_TEXTURE)) {
                Optional.ofNullable(serializableNode.getString())
                        .ifPresent(texture -> {
                            var playerProfile = Bukkit.createProfile(UUID.randomUUID(), "commandItem");
                            var playerProperty = new ProfileProperty("textures", texture);
                            playerProfile.setProperty(playerProperty);
                            deserializeMap.put("skull-owner", playerProfile);
                        });
            } else if (serializableNode.isMap()) {
                var serializableMap = serializableNode.childrenMap();

                if (serializableMap.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    Optional.ofNullable(serializableNode.get(ConfigurationSerializable.class))
                                    .ifPresent(object -> deserializeMap.put(name, object));
                } else {
                    LinkedHashMap<String, Object> mapInMap = new LinkedHashMap<>();

                    for (Map.Entry<Object, ? extends ConfigurationNode> secondEntry : serializableMap.entrySet()) {
                        var secondSerializableNode = secondEntry.getValue();
                        var secondEntryKey = secondEntry.getKey().toString();

                        if (secondSerializableNode.isMap()) {
                            Optional.ofNullable(secondSerializableNode.get(ConfigurationSerializable.class))
                                            .ifPresent(object -> mapInMap.put(secondEntryKey, object));
                        } else if (secondSerializableNode.isList()) {
                            var objects = new ArrayList<>();

                            for (ConfigurationNode arrayElement : secondSerializableNode.childrenList()) {
                                objects.add(arrayElement.get(ConfigurationSerializable.class));
                            }

                            mapInMap.put(secondEntryKey, objects);
                        } else {
                            mapInMap.put(secondEntryKey, Objects.requireNonNull(secondSerializableNode.raw()));
                        }
                    }
                    deserializeMap.put(name, mapInMap);
                }
            } else if (serializableNode.isList()) {
                var objectsList = new ArrayList<>();

                for (ConfigurationNode arrayNode : serializableNode.childrenList()) {
                    if (arrayNode.isMap() && arrayNode.childrenMap().containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                        objectsList.add(arrayNode.get(ConfigurationSerializable.class));
                    } else if (arrayNode.isMap() && arrayNode.childrenMap().containsKey("v")) {
                        objectsList.add(arrayNode.get(ItemStack.class));
                    } else {
                        objectsList.add(arrayNode.raw());
                    }
                }

                deserializeMap.put(name, objectsList);
            } else {
                deserializeMap.put(name, Objects.requireNonNull(serializableNode.raw()));
            }
        }
        return Objects.requireNonNull(ConfigurationSerialization.deserializeObject(deserializeMap));
    }

    @Override
    public void serialize(final Type type, @Nullable ConfigurationSerializable obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.node(ConfigurationSerialization.SERIALIZED_TYPE_KEY).set(ConfigurationSerialization.getAlias(obj.getClass()));
            obj.serialize().forEach((key, object) -> {
                try {
                    if (key.equals("skull-owner") && object instanceof PlayerProfile playerProfile) {
                        String url = Objects.requireNonNull(playerProfile.getTextures().getSkin()).toString();
                        byte[] encodeData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
                        String encodeString = new String(encodeData, StandardCharsets.UTF_8);
                        node.node(SKULL_TEXTURE).set(encodeString);
                        return;
                    }

                    if (object instanceof Collection<?> collections) {
                        // 空のリストがあるとスキップされてロードした時に読み込めなくなる対策
                        if (collections.isEmpty()) {
                            node.node(key).set(Collections.emptyList());
                        } else {
                            collections.forEach(collection -> {
                                try {
                                    node.node(key).appendListNode().set(collection);
                                } catch (SerializationException e) {
                                    this.logger.error("", e);
                                }
                            });
                        }
                        return;
                    }

                    if (object instanceof Map<?, ?> map) {
                        map.forEach((mapKey, mapValue) -> {
                            if (mapValue instanceof Collection<?> collections1) {
                                if (!collections1.isEmpty()) {
                                    collections1.forEach(collection1 -> {
                                        try {
                                            node.node(key).node(mapKey).appendListNode().set(collection1);
                                        } catch (SerializationException e) {
                                            this.logger.error("", e);
                                        }
                                    });
                                } else {
                                    try {
                                        node.node(key).node(mapKey).set(Collections.emptyList());
                                    } catch (SerializationException e) {
                                        this.logger.error("", e);
                                    }
                                }
                            } else {
                                try {
                                    node.node(key).node(mapKey).set(mapValue);
                                } catch (SerializationException e) {
                                    this.logger.error("", e);
                                }
                            }
                        });
                        return;
                    }

                    node.node(key).set(object);
                } catch (SerializationException e) {
                    this.logger.error("", e);
                }
            });
        }
    }
}
