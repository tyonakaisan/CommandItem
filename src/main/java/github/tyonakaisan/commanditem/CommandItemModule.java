package github.tyonakaisan.commanditem;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import github.tyonakaisan.commanditem.listener.ItemUseListener;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CommandItemModule extends AbstractModule {

    private final CommandItem commandItem;

    CommandItemModule(
            final CommandItem commandItem
    ) {
        this.commandItem = commandItem;
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
