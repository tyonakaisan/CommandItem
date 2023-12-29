package github.tyonakaisan.commanditem.serialisation;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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
public class ConfigurationSerializableSerializerConfigurate implements TypeSerializer<ConfigurationSerializable> {

    private static final String DISPLAY_NAME = "display-name";
    private static final String LORE = "lore";
    private static final String SKULL_TEXTURE = "skull-texture";

    @Inject
    public ConfigurationSerializableSerializerConfigurate() {
        // Constructor for the injector.
    }

    // 未対応リスト
    // Attribute付きアイテム
    //
    // アイテムにコマンド付けるのが本職だってのになにしてんだｺﾗｰ！
    // おまけだからｲｲﾀﾞﾛｰ！ﾌｻﾞｹﾙﾅｰ！
    @Override
    public ConfigurationSerializable deserialize(Type type, ConfigurationNode node) throws SerializationException {
        Map<String, Object> deSerializeMap = new HashMap<>();

        for (ConfigurationNode serializableNode : node.childrenMap().values()) {
            var key = Objects.requireNonNull(serializableNode.key()).toString();
            var value = serializableNode.raw();
            if (value != null) {
                deSerializeMap.put(key, value);

                if (key.equals(DISPLAY_NAME)) {
                    Optional.ofNullable(serializableNode.get(Component.class))
                            .ifPresent(displayName -> deSerializeMap.put(DISPLAY_NAME, JSONComponentSerializer.json().serialize(displayName)));
                    continue;
                }

                if (key.equals(LORE)) {
                    Optional.ofNullable(serializableNode.getList(Component.class))
                            .ifPresent(rawLoreList -> {
                                var loreList = rawLoreList.stream()
                                        .map(lore -> JSONComponentSerializer.json().serialize(lore))
                                        .toList();
                                deSerializeMap.put(LORE, loreList);
                            });
                    continue;
                }

                if (key.equals(SKULL_TEXTURE)) {
                    Optional.ofNullable(serializableNode.getString())
                            .ifPresent(texture -> {
                                var playerProfile = Bukkit.createProfile(UUID.randomUUID(), "commandItem");
                                var playerProperty = new ProfileProperty("textures", texture);
                                playerProfile.setProperty(playerProperty);
                                deSerializeMap.put("skull-owner", playerProfile);
                            });
                    continue;
                }

                if (key.equals("color")) {
                    Optional.ofNullable(serializableNode.get(Color.class))
                            .ifPresent(color -> {
                                System.out.println("color...: " + color);
                                deSerializeMap.put(key, color);
                            });
                    continue;
                }

                if (value instanceof Collection) {
                    serializableNode.childrenList().forEach(child -> {
                        var raw = child.raw();
                        Class<?> clazz = null;
                        if (raw instanceof Map map) {
                            System.out.println("raw: " + map);
                            if (map.get("==").toString().equals("Color")) {
                                clazz = Color.class;
                            }
                            if (map.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                                clazz = ConfigurationSerializable.class;
                            } else if (map.containsKey("v")) {
                                clazz = ItemStack.class;
                            }
                            try {
                                deSerializeMap.put(key, serializableNode.getList(clazz));
                            } catch (SerializationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    //deSerializeMap.put(key, serializableNode.getList(ConfigurationSerializable.class));
                }
            }
        }
        System.out.println("totta: " + deSerializeMap);
        //System.out.println("comp: " + ConfigurationSerialization.deserializeObject(deSerializeMap));
        return ConfigurationSerialization.deserializeObject(deSerializeMap);
    }

    @Override
    public void serialize(Type type, @Nullable ConfigurationSerializable obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.node(ConfigurationSerialization.SERIALIZED_TYPE_KEY).set(ConfigurationSerialization.getAlias(obj.getClass()));
            //System.out.println("moto: " + obj.serialize());
            obj.serialize().forEach((key, value) -> {
                try {

                    if (key.equals(DISPLAY_NAME)) {
                        var component = JSONComponentSerializer.json().deserialize((String) value);
                        var displayName = MiniMessage.miniMessage().serialize(component);
                        node.node(key).set(displayName);
                        return;
                    }

                    if (key.equals(LORE) && value instanceof Collection<?> values) {
                        var loreList = values.stream()
                                .map(String.class::cast)
                                .map(raw -> {
                                    var component = JSONComponentSerializer.json().deserialize(raw);
                                    return MiniMessage.miniMessage().serialize(component);
                                })
                                .toList();
                        node.node(key).set(loreList);
                        return;
                    }

                    if (key.equals("skull-owner") && value instanceof PlayerProfile playerProfile) {
                        String url = Objects.requireNonNull(playerProfile.getTextures().getSkin()).toString();
                        byte[] encodeData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
                        String encodeString = new String(encodeData, StandardCharsets.UTF_8);
                        node.node(SKULL_TEXTURE).set(encodeString);
                        return;
                    }

                    if (value instanceof Collection<?> collections) {
                        System.out.println("collections: " + collections);
                        if (collections.isEmpty()) {
                            node.node(key).set(Collections.emptyList());
                            return;
                        }
                        collections.forEach(collection -> {
                            try {
                                node.node(key).appendListNode().set(collection);
                            } catch (SerializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        return;
                    }

                    // TODO Attribute付きアイテム…

                    node.node(key).set(value);
                } catch (SerializationException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
