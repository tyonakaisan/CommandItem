package github.tyonakaisan.commanditem.util;

import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

@DefaultQualifier(NonNull.class)
@SuppressWarnings({"unchecked", "unused"})
public final class ItemBuilder<B extends ItemBuilder<B, M>, M extends ItemMeta> {

    private final ItemStack itemStack;
    private final M itemMeta;

    private static final Component DISABLE_ITALICS = Component.empty().decoration(TextDecoration.ITALIC, false);

    private ItemBuilder(final ItemStack itemStack, final M itemMeta) {
        this.itemStack = itemStack.clone();
        this.itemMeta = itemMeta;
    }

    private ItemBuilder(final Material material) {
        this(new ItemStack(material));
    }

    private ItemBuilder(final ItemStack stack) {
        this.itemStack = stack.clone();
        this.itemMeta = (M) stack.getItemMeta();
    }

    private B of(final ItemStack stack, final M meta) {
        return (B) new ItemBuilder<B, M>(stack, meta);
    }

    public static <I extends ItemMeta> ItemBuilder<?, I> of(final Material material) {
        return new ItemBuilder<>(material);
    }

    public static <I extends ItemMeta> ItemBuilder<?, I> of(final ItemStack stack) {
        return new ItemBuilder<>(stack);
    }

    public @Nullable Component displayName() {
        return this.itemMeta.displayName();
    }

    public B displayName(final @Nullable Component name) {
        if (name == null) {
            this.itemMeta.displayName(null);
        } else {
            this.itemMeta.displayName(DISABLE_ITALICS.append(name));
        }
        return (B) this;
    }

    public @Nullable List<Component> lore() {
        return this.itemMeta.lore();
    }

    public B lore(final @Nullable List<Component> lines) {
        if (lines == null) {
            this.itemMeta.lore(null);
            return (B) this;
        }

        final List<Component> toAdd = new ArrayList<>(lines);
        toAdd.replaceAll(DISABLE_ITALICS::append);

        this.itemMeta.lore(toAdd);
        return (B) this;
    }

    public B loreList(final @NonNull Component... lines) {
        return this.lore(List.of(lines));
    }

    public B loreModifier(final Consumer<List<Component>> consumer) {
        final List<Component> lore = Optional
                .ofNullable(this.itemMeta.lore())
                .orElse(new ArrayList<>());

        consumer.accept(lore);

        this.itemMeta.lore(lore);
        return (B) this;
    }

    public Material material() {
        return this.itemStack.getType();
    }

    public B material(final Material material) {
        this.itemStack.setType(material);
        return (B) this;
    }

    public int amount() {
        return this.itemStack.getAmount();
    }

    public B amount(@Nullable final Integer amount) {
        if (amount != null) {
            this.itemStack.setAmount(amount);
        }
        return (B) this;
    }

    public <T, Z> @Nullable Z getData(
            final NamespacedKey key,
            final PersistentDataType<T, Z> type
    ) {
        return this.itemMeta.getPersistentDataContainer().get(key, type);
    }

    public <T, Z> B setData(
            final NamespacedKey key,
            final PersistentDataType<T, Z> type,
            final Z object
    ) {
        this.itemMeta.getPersistentDataContainer().set(key, type, object);
        return (B) this;
    }

    public B removeData(
            final NamespacedKey key
    ) {
        this.itemMeta.getPersistentDataContainer().remove(key);
        return (B) this;
    }

    public Set<ItemFlag> flags() {
        return this.itemMeta.getItemFlags();
    }

    public B flags(final @Nullable List<ItemFlag> flags) {
        this.clearFlags();
        if (flags != null) {
            this.itemMeta.addItemFlags(flags.toArray(new ItemFlag[0]));
        }
        return (B) this;
    }

    private void clearFlags() {
        this.itemMeta.removeItemFlags(this.itemMeta.getItemFlags().toArray(new ItemFlag[0]));
    }

    public B addFlag(final ItemFlag... flag) {
        this.itemMeta.addItemFlags(flag);
        return (B) this;
    }

    public B removeFlag(final ItemFlag... flag) {
        this.itemMeta.removeItemFlags(flag);
        return (B) this;
    }

    public Map<Enchantment, Integer> enchants() {
        return new HashMap<>(this.itemStack.getEnchantments());
    }

    public B enchants(final @Nullable Map<Enchantment, Integer> enchants) {
        this.clearEnchants();
        if (enchants != null) {
            this.itemStack.addEnchantments(enchants);
        }
        return (B) this;
    }

    private void clearEnchants() {
        for (final Enchantment enchantment : this.itemMeta.getEnchants().keySet()) {
            this.itemMeta.removeEnchant(enchantment);
        }
    }

    public B addEnchant(final Enchantment enchantment, final int level) {
        this.itemMeta.addEnchant(enchantment, level, true);
        return (B) this;
    }

    public B removeEnchant(final Enchantment... enchantment) {
        for (final Enchantment item : enchantment) {
            this.itemMeta.removeEnchant(item);
        }
        return (B) this;
    }

    public B unbreakable(final @Nullable Boolean unbreakable) {
        if (unbreakable != null) {
            this.itemMeta.setUnbreakable(unbreakable);
        }
        return (B) this;
    }

    public int maxStackSize() {
        return this.itemStack.getMaxStackSize();
    }

    public @Nullable Integer customModelData() {
        // we use the wrapper with null signifying absent for api consistency
        if (!this.itemMeta.hasCustomModelData()) {
            return null;
        }
        return this.itemMeta.getCustomModelData();
    }

    public B customModelData(final @Nullable Integer customModelData) {
        this.itemMeta.setCustomModelData(customModelData);
        return (B) this;
    }

    public @Nullable Multimap<Attribute, AttributeModifier> attributeModifiers() {
        return this.itemMeta.getAttributeModifiers();
    }

    public @NonNull B attributeModifiers(final @Nullable Multimap<Attribute, AttributeModifier> attributeModifiers) {
        this.itemMeta.setAttributeModifiers(attributeModifiers);
        return (B) this;
    }

    public B addAttributeModifier(
            final Attribute attribute,
            final AttributeModifier attributeModifier
    ) {
        this.itemMeta.addAttributeModifier(attribute, attributeModifier);
        return (B) this;
    }

    public B removeAttributeModifier(
            final Attribute attribute,
            final AttributeModifier attributeModifier
    ) {
        this.itemMeta.removeAttributeModifier(attribute, attributeModifier);
        return (B) this;
    }

    public B removeAttributeModifier(final Attribute... attribute) {
        for (final Attribute item : attribute) {
            this.itemMeta.removeAttributeModifier(item);
        }
        return (B) this;
    }

    public B editMeta(final Consumer<M> consumer) {
        final M meta = (M) this.itemMeta.clone();
        consumer.accept(meta);
        return this.of(this.itemStack, meta);
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack.clone();
    }

}
