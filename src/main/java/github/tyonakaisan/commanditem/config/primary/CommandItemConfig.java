package github.tyonakaisan.commanditem.config.primary;

import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

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
            If such a thing happens, it cannot be saved file
            """)
    private ItemStack itemStack;
    @Comment("""
            ##### DO NOT EDIT THIS SECTION #####
            """)
    private Key key = Key.key("key:value");
    @Comment("""
            The command can be executed as many times as specified in this field
            Entering a number below 0 disables this feature
            """)
    private int maxUses = 0;
    private boolean stackable = true;
    private boolean placeable = true;
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

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
