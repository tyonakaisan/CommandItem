package github.tyonakaisan.commanditem.item.config;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public final class ItemConfigTemplate {
    private @Nullable ItemStack item;
    private @Nullable String displayName = "";
    private @Nullable List<String> lore = List.of();
    private @Nullable Key key = Key.key("command_item:empty");
    private int usageCounts = 0;
    private boolean unStackable = false;
    private boolean canPlace = false;
    private @Nullable Map<String, List<String>> byPlayerCommands = Map.of();
    private @Nullable Map<String, List<String>> byConsoleCommands = Map.of();

    public void setItem(final ItemStack item) {
        this.item = item.clone();
    }

    public void setDisplayName(@Nullable final Component displayName) {
        if (displayName == null) {
            this.displayName = "";
        } else {
            this.displayName = MiniMessage.miniMessage().serialize(displayName);
        }
    }

    public void setLore(@Nullable final List<Component> loreList) {
        if (loreList == null) {
            this.lore = List.of();
        } else {
            loreList.forEach(text -> Objects.requireNonNull(this.lore).add(MiniMessage.miniMessage().serialize(text)));
        }
    }
    public void setKey(final String value) {
        this.key = Key.key("command_item:" + value.replace(".conf", ""));
    }
}
