package github.tyonakaisan.commanditem;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.command.argument.KeyArgument;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.nio.file.Path;
import java.util.function.Function;

@DefaultQualifier(NonNull.class)
public final class CommandItemModule extends AbstractModule {
    private final ComponentLogger logger;
    private final CommandItem commandItem;
    private final Path dataDirectory;

    CommandItemModule(
            final CommandItem commandItem,
            final Path dataDirectory,
            final ComponentLogger logger
    ) {
        this.commandItem = commandItem;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    @Provides
    @Singleton
    public CommandManager<CommandSender> commandManager() {
        final PaperCommandManager<CommandSender> commandManager;
        try {
            commandManager = new PaperCommandManager<>(
                    this.commandItem,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );
            commandManager.parserRegistry().registerParserSupplier(TypeToken.get(Key.class), options -> new KeyArgument<>());
        } catch (final Exception exception) {
            throw new RuntimeException("Failed to initialize command manager.", exception);
        }
        commandManager.registerAsynchronousCompletions();
        return commandManager;
    }

    @Override
    public void configure() {
        this.bind(ComponentLogger.class).toInstance(this.logger);
        this.bind(CommandItem.class).toInstance(this.commandItem);
        this.bind(Server.class).toInstance(this.commandItem.getServer());
        this.bind(Path.class).toInstance(this.dataDirectory);
    }
}
