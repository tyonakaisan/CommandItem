package github.tyonakaisan.commanditem.serialisation;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.codehaus.plexus.util.Base64;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@DefaultQualifier(NonNull.class)
public final class ItemStackSerializerConfigurate implements TypeSerializer<ItemStack> {

    private final ComponentLogger logger;

    private static final String MATERIAL = "material";
    private static final String AMOUNT = "amount";

    private static final String ITEM_META = "item-meta";
    private static final String DISPLAY_NAME = "display-name";
    private static final String LORE = "lore";
    private static final String CUSTOM_MODEL_DATA = "custom-model-data";
    private static final String UNBREAKABLE = "unbreakable";
    private static final String ENCHANTMENTS = "enchantments";
    private static final String SKULL_TEXTURE = "skull-texture";
    private static final String SHULKER_BOX = "shulker-box";

    @Inject
    public ItemStackSerializerConfigurate(final ComponentLogger logger) {
        this.logger = logger;
    }

    @Override
    public ItemStack deserialize(final Type type, final ConfigurationNode node) throws SerializationException {

        var material = Objects.requireNonNull(node.node(MATERIAL).getString());
        int amount = node.node(AMOUNT).getInt(1);

        var itemStack = new ItemStack(Objects.requireNonNull(Material.matchMaterial(material)), amount);

        var metaNode = node.node(ITEM_META);
        var itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            var displayNameNode = metaNode.node(DISPLAY_NAME);
            if (!displayNameNode.isNull()) itemMeta.displayName(metaNode.node(DISPLAY_NAME).get(Component.class));

            var loreNode = node.node(LORE);
            if (!loreNode.isNull()) itemMeta.lore(metaNode.node(LORE).getList(Component.class));

            var customModelDataNode = metaNode.node(CUSTOM_MODEL_DATA);
            if (!customModelDataNode.isNull()) itemMeta.setCustomModelData(customModelDataNode.getInt());

            var unbreakableNode = metaNode.node(UNBREAKABLE);
            if (!unbreakableNode.isNull()) itemMeta.setUnbreakable(unbreakableNode.getBoolean());

            var enchantmentsNode = metaNode.node(ENCHANTMENTS);
            if (!enchantmentsNode.isNull()) {
                var enchantments = Objects.requireNonNull(enchantmentsNode.getList(EnchantmentSerializerConfigurate.Enchant.class));
                enchantments.forEach(enchantment -> enchantment.enchantMap().keySet()
                        .forEach(enchant ->
                                itemMeta.addEnchant(
                                        enchant,
                                        enchantment.enchantMap().get(enchant),
                                        true)
                                )
                );
            }

            if (itemMeta instanceof SkullMeta skullMeta) {
                var skullTextureNode = metaNode.node(SKULL_TEXTURE);
                if (!skullTextureNode.isNull()) {
                    var playerProfile = Bukkit.createProfile(UUID.randomUUID(), "commandItem");
                    var playerProperty = new ProfileProperty("textures", Objects.requireNonNull(skullTextureNode.getString()));
                    playerProfile.setProperty(playerProperty);
                    skullMeta.setPlayerProfile(playerProfile);
                }
            }

            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }

    @Override
    public void serialize(final Type type, final @Nullable ItemStack obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
        } else {
            node.node(MATERIAL).set(obj.getType());
            node.node(AMOUNT).set(obj.getAmount());

            var meta = obj.getItemMeta();
            var metaNode = node.node(ITEM_META);

            /*
            if (meta.hasDisplayName()) {
                var displayName = MiniMessage.miniMessage().serialize(Objects.requireNonNull(meta.displayName()));
                node.node(DISPLAY_NAME).set(displayName);
            }
             */

            //var loreNode = node.node(LORE);
            //if (!loreNode.isNull()) metaNode.node(LORE).set(meta.lore());

            if (meta.hasCustomModelData()) metaNode.node(CUSTOM_MODEL_DATA).set(meta.getCustomModelData());

            if (meta.isUnbreakable()) metaNode.node(UNBREAKABLE).set(meta.isUnbreakable());

            if (meta.hasEnchants()) {
                Map<String, Integer> enchantMap = new HashMap<>();
                meta.getEnchants().forEach((enchant, level) -> {
                    var enchantKey = enchant.getKey() ;
                    enchantMap.put(enchantKey.toString().replace("minecraft:", ""), level);
                });
                metaNode.node(ENCHANTMENTS).set(enchantMap);
            }

            if (meta instanceof SkullMeta skullMeta && skullMeta.hasOwner()) {
                String url = Objects.requireNonNull(Objects.requireNonNull(skullMeta.getPlayerProfile()).getTextures().getSkin()).toString();
                byte[] encodeData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
                String encodeString = new String(encodeData, StandardCharsets.UTF_8);
                metaNode.node(SKULL_TEXTURE).set(encodeString);
            }
        }
    }
}
