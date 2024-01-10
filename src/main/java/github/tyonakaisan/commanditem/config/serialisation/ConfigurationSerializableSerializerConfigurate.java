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
public class ConfigurationSerializableSerializerConfigurate implements TypeSerializer<ConfigurationSerializable> {

    private final ComponentLogger logger;
    private static final String DISPLAY_NAME = "display-name";
    private static final String LORE = "lore";
    private static final String SKULL_TEXTURE = "skull-texture";

    @Inject
    public ConfigurationSerializableSerializerConfigurate(
            final ComponentLogger logger
    ) {
        this.logger = logger;
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

            // No deserialize of displayName and lore here.
            if (key.equals(SKULL_TEXTURE)) {
                Optional.ofNullable(serializableNode.getString())
                        .ifPresent(texture -> {
                            var playerProfile = Bukkit.createProfile(UUID.randomUUID(), "commandItem");
                            var playerProperty = new ProfileProperty("textures", texture);
                            playerProfile.setProperty(playerProperty);
                            deSerializeMap.put("skull-owner", playerProfile);
                        });
            } else {
                deSerializeMap.put(key, rawValue);

                if (rawValue instanceof Collection) {
                    serializableNode.childrenList().forEach(child -> {
                        if (Objects.requireNonNull(child.raw()) instanceof Map<?, ?> map) {
                            Class<?> clazz = map.containsKey("v") ? ItemStack.class : ConfigurationSerializable.class;

                            try {
                                deSerializeMap.put(key, Objects.requireNonNull(serializableNode.getList(clazz)));
                            } catch (SerializationException e) {
                                this.logger.error("", e);
                            }
                        }
                    });
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

                    // No serialize of display names and lore here.
                    if (key.equals(DISPLAY_NAME) || key.equals(LORE)) {
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
                                this.logger.error("", e);
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
