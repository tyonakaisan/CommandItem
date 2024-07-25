package github.tyonakaisan.commanditem;

import com.google.inject.*;
import github.tyonakaisan.commanditem.command.CommandFactory;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Set;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CommandItem extends JavaPlugin {

    private final Injector injector;
    private final ItemRegistry itemRegistry;

    @Inject
    public CommandItem(
            final Injector bootstrapInjector
    ) {
        this.injector = bootstrapInjector.createChildInjector(new CommandItemModule(this));
        this.itemRegistry = this.injector.getInstance(ItemRegistry.class);

        CommandItemProvider.register(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        final Set<Listener> listeners = this.injector.getInstance(Key.get(new TypeLiteral<>() {
        }));
        listeners.forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));

        this.injector.getInstance(CommandFactory.class).registerViaEnable(this);
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

    public ItemRegistry itemRegistry() {
        return this.itemRegistry;
    }
}
