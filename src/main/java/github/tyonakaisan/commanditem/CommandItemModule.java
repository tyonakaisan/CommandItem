package github.tyonakaisan.commanditem;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
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
    }

    private void configureListener() {
        final Multibinder<Listener> listeners = Multibinder.newSetBinder(this.binder(), Listener.class);
        listeners.addBinding().to(ItemUseListener.class).in(Scopes.SINGLETON);
    }
}
