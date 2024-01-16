package github.tyonakaisan.commanditem;

import com.google.inject.Guice;
import com.google.inject.Injector;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.command.commands.ConvertCommand;
import github.tyonakaisan.commanditem.command.commands.GiveCommand;
import github.tyonakaisan.commanditem.command.commands.ReloadCommand;
import github.tyonakaisan.commanditem.listener.ItemUseListener;
import github.tyonakaisan.commanditem.listener.JoinListener;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.nio.file.Path;
import java.util.Set;

@DefaultQualifier(NonNull.class)
public final class CommandItem extends JavaPlugin {

    private static final Set<Class<? extends Listener>> LISTENER_CLASSES = Set.of(
            ItemUseListener.class,
            JoinListener.class
    );
    private static final Set<Class<? extends CommandItemCommand>> COMMAND_CLASSES = Set.of(
            ReloadCommand.class,
            GiveCommand.class,
            ConvertCommand.class
    );
    private final Injector injector;

    public CommandItem(
            final Path dataDirectory,
            final ComponentLogger logger
    ) {
        this.injector = Guice.createInjector(new CommandItemModule(this, dataDirectory, logger));
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Listeners
        for (final Class<? extends Listener> listenerClass : LISTENER_CLASSES) {
            var listener = this.injector.getInstance(listenerClass);
            this.getServer().getPluginManager().registerEvents(listener, this);
        }

        // Commands
        for (final Class<? extends CommandItemCommand> commandClass : COMMAND_CLASSES) {
            var command = this.injector.getInstance(commandClass);
            command.init();
        }

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
