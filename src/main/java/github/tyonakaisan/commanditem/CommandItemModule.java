package github.tyonakaisan.commanditem;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.command.commands.ConvertCommand;
import github.tyonakaisan.commanditem.command.commands.GiveCommand;
import github.tyonakaisan.commanditem.command.commands.ReloadCommand;
import github.tyonakaisan.commanditem.listener.ItemUseListener;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;

@DefaultQualifier(NonNull.class)
public final class CommandItemModule extends AbstractModule {

    private final CommandItem commandItem;

    CommandItemModule(
            final CommandItem commandItem
    ) {
        this.commandItem = commandItem;
    }

    @Provides
    @Singleton
    public CommandManager<CommandSender> commandManager() {
        final PaperCommandManager<CommandSender> commandManager = new PaperCommandManager<>(
                this.commandItem,
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
        );
        commandManager.registerBrigadier();
        return commandManager;
    }

    @Override
    public void configure() {
        this.bind(CommandItem.class).toInstance(this.commandItem);
        this.bind(Server.class).toInstance(this.commandItem.getServer());

        this.configureListener();
        this.configureCommand();
    }

    private void configureListener() {
        final Multibinder<Listener> listeners = Multibinder.newSetBinder(this.binder(), Listener.class);
        listeners.addBinding().to(ItemUseListener.class).in(Scopes.SINGLETON);
    }

    private void configureCommand() {
        final Multibinder<CommandItemCommand> commands = Multibinder.newSetBinder(this.binder(), CommandItemCommand.class);
        commands.addBinding().to(ConvertCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(GiveCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(ReloadCommand.class).in(Scopes.SINGLETON);
    }
}
