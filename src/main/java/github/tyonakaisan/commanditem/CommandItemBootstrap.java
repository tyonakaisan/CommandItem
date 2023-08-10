package github.tyonakaisan.commanditem;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
@DefaultQualifier(NonNull.class)
public final class CommandItemBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        // メソッド実装なし
    }

    @Override
    public @NotNull JavaPlugin createPlugin(PluginProviderContext context) {
        return new CommandItem(context.getDataDirectory(), context.getLogger());
    }
}
