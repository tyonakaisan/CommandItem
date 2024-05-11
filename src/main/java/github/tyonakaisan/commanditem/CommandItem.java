package github.tyonakaisan.commanditem;

import com.google.inject.*;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.nio.file.Path;
import java.util.Set;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CommandItem extends JavaPlugin {

    private final Injector injector;

    public CommandItem(
            final Path dataDirectory,
            final ComponentLogger logger
    ) {
        this.injector = Guice.createInjector(new CommandItemModule(this, dataDirectory, logger));

        CommandItemProvider.register(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        final Set<Listener> listeners = this.injector.getInstance(Key.get(new TypeLiteral<>() {}));
        listeners.forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));

        final Set<CommandItemCommand> commands = this.injector.getInstance(Key.get(new TypeLiteral<>() {}));
        commands.forEach(CommandItemCommand::init);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static boolean papiLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static boolean miniPlaceholdersLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders");
    }
}
