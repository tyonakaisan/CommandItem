package github.tyonakaisan.commanditem.config.item;

import github.tyonakaisan.commanditem.item.CustomCommand;
import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.NamespaceKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
@SuppressWarnings({"unused","FieldMayBeFinal"})
public class CommandItemConfig {

    @Comment("""
                                ##### Experimental features ######
            ##### DO NOT EDIT THE SECTIONS "v", "==", "meta-type", "PublicBukkitValues" #####
            Some items may not convert correctly or may be missing data
            If such a thing happens, it cannot be loaded file""")
    private ItemStack itemStack = new ItemStack(Material.AIR);
    @Comment("Item display name")
    private String displayName = "";
    @Comment("Item lore")
    private List<String> lore = new ArrayList<>();
    @Comment("##### DO NOT EDIT THIS SECTION #####")
    private Key key = Key.key("key:value");
    @Comment("""
            The command can be executed as many times as specified in this field
            Entering a number below -1 disable this feature""")
    private int maxUses = 1;
    private boolean stackable = true;
    private boolean placeable = true;
    @Comment("Item cool time")
    private int coolTime = 0;
    @Comment("Specifies the command to be executed from player")
    private Map<ActionUtils.ItemAction, List<CustomCommand>> byPlayerCommands = Map.of();
    @Comment("Specifies the command to be executed from console")
    private Map<ActionUtils.ItemAction, List<CustomCommand>> byConsoleCommands = Map.of(ActionUtils.ItemAction.RIGHT_CLICK,
            List.of(CustomCommand.empty()));

    public void setKey(@Subst("value") final String value) {
        @Subst("key")
        var keyValue = NamespaceKeyUtils.namespace();
        this.key = Key.key(keyValue, value);
    }

    public void setItemStack(final ItemStack itemStack) {
        this.itemStack = itemStack;
        this.setDisplayName(itemStack);
        this.setLore(itemStack.getItemMeta().lore());
    }

    private void setDisplayName(final ItemStack itemStack) {
        @Nullable Component metaDisplayName = itemStack.getItemMeta().displayName();
        var itemTranslationKey = itemStack.getType().getItemTranslationKey() == null
                ? ""
                : String.format("<%s><lang:%s>", itemStack.getRarity().getColor(), itemStack.getType().getItemTranslationKey());

        this.displayName = metaDisplayName == null
                ? itemTranslationKey
                : MiniMessage.miniMessage().serialize(metaDisplayName);
    }

    private void setLore(@Nullable final List<Component> lore) {
        this.lore = lore == null
                ? Collections.emptyList()
                : lore.stream()
                .map(text -> MiniMessage.miniMessage().serialize(text))
                .toList();
    }
}
