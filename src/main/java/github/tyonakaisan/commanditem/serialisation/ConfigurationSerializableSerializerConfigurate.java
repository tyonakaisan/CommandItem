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
    private static final String COLOR = "color";

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
    public ConfigurationSerializable deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        Map<String, Object> deSerializeMap = new HashMap<>();

        for (ConfigurationNode serializableNode : node.childrenMap().values()) {
            var key = Objects.requireNonNull(serializableNode.key()).toString();
            var rawValue = Objects.requireNonNull(serializableNode.raw());

            switch (key) {
                case DISPLAY_NAME -> Optional.ofNullable(serializableNode.get(Component.class))
                        .ifPresent(displayName -> deSerializeMap.put(DISPLAY_NAME, JSONComponentSerializer.json().serialize(displayName)));

                case LORE -> Optional.ofNullable(serializableNode.getList(Component.class))
                        .ifPresent(rawLoreList -> {
                            var loreList = rawLoreList.stream()
                                    .map(lore -> JSONComponentSerializer.json().serialize(lore))
                                    .toList();
                            deSerializeMap.put(LORE, loreList);
                        });

                case SKULL_TEXTURE -> Optional.ofNullable(serializableNode.getString())
                        .ifPresent(texture -> {
                            var playerProfile = Bukkit.createProfile(UUID.randomUUID(), "commandItem");
                            var playerProperty = new ProfileProperty("textures", texture);
                            playerProfile.setProperty(playerProperty);
                            deSerializeMap.put("skull-owner", playerProfile);
                        });

                // 使おうと思ったけど悩み
                case COLOR -> Optional.ofNullable(serializableNode.get(Color.class))
                        .ifPresent(color -> deSerializeMap.put(key, color));

                default -> {
                    deSerializeMap.put(key, rawValue);

                    if (rawValue instanceof Collection) {
                        serializableNode.childrenList().forEach(child -> {
                            var raw = Objects.requireNonNull(child.raw());
                            Class<?> clazz = ConfigurationSerializable.class;
                            if (raw instanceof Map<?,?> map) {
                                if (map.get("==").toString().equals("Color")) {
                                    clazz = Color.class;
                                }

                                if (map.containsKey("v")) {
                                    clazz = ItemStack.class;
                                }

                                try {
                                    deSerializeMap.put(key, Objects.requireNonNull(serializableNode.getList(clazz)));
                                } catch (SerializationException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }
                }
            }
        }
        return Objects.requireNonNull(ConfigurationSerialization.deserializeObject(deSerializeMap));
    }

    @Override
    public void serialize(final Type type, @Nullable ConfigurationSerializable obj,final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.node(ConfigurationSerialization.SERIALIZED_TYPE_KEY).set(ConfigurationSerialization.getAlias(obj.getClass()));
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
                        // 空のリストがあるとスキップされてロードした時に読み込めなくなる対策
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
