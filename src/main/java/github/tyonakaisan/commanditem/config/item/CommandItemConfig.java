package github.tyonakaisan.commanditem.config.item;

import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
@SuppressWarnings("unused")
public class CommandItemConfig {

    @Comment("""
                                ##### Experimental features ######
            ##### DO NOT EDIT THE SECTIONS "v", "==", "meta-type", "PublicBukkitValues" #####
            Some items may not convert correctly or may be missing data
            If such a thing happens, it cannot be loaded file
            """)
    private ItemStack itemStack;
    @Comment("""
            Item display name
            """)
    private String displayName = "";
    @Comment("""
            Item lore
            """)
    private List<String> lore = new ArrayList<>();
    @Comment("""
            ##### DO NOT EDIT THIS SECTION #####
            """)
    private Key key = Key.key("key:value");
    @Comment("""
            The command can be executed as many times as specified in this field
            Entering a number below -1 disable this feature
            """)
    private int maxUses = 0;
    private boolean stackable = true;
    private boolean placeable = true;
    private int coolTime = 0;
    @Comment("""
            Specifies the command to be executed from player
            """)
    private Map<ActionUtils.ItemAction, List<String>> byPlayerCommands = Map.of();
    @Comment("""
            Specifies the command to be executed from console
            """)
    private Map<ActionUtils.ItemAction, List<String>> byConsoleCommands = Map.of();

    public void setKey(@Subst("value") final String value) {
        @Subst("key")
        var keyValue = NamespacedKeyUtils.namespace();
        this.key = Key.key(keyValue, value);
    }

    public void setItemStack(final ItemStack itemStack) {
        this.itemStack = itemStack;
        this.setDisplayName(itemStack.getItemMeta().displayName());
        this.setLore(itemStack.getItemMeta().lore());
    }

    private void setDisplayName(@Nullable final Component displayName) {
        this.displayName = displayName == null ? "" : MiniMessage.miniMessage().serialize(displayName);
    }

    private void setLore(@Nullable final List<Component> lore) {
        this.lore = lore == null ? Collections.emptyList() : lore.stream()
                .map(text -> MiniMessage.miniMessage().serialize(text))
                .toList();
    }

}
