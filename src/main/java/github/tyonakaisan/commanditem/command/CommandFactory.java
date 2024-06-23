package github.tyonakaisan.commanditem.command;

import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.tyonakaisan.commanditem.command.commands.ConvertCommand;
import github.tyonakaisan.commanditem.command.commands.GiveCommand;
import github.tyonakaisan.commanditem.command.commands.DebugCommand;
import github.tyonakaisan.commanditem.command.commands.ReloadCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public final class CommandFactory {

    private final ConvertCommand convertCommand;
    private final GiveCommand giveCommand;
    private final DebugCommand debugCommand;
    private final ReloadCommand reloadCommand;

    private static final LiteralArgumentBuilder<CommandSourceStack> FIRST_LITERAL_ARGUMENT = Commands.literal("commanditem");
    private static final List<String> ALIASES = List.of("cmdi", "ci");

    @Inject
    public CommandFactory(
            final ConvertCommand convertCommand,
            final GiveCommand giveCommand,
            final DebugCommand debugCommand,
            final ReloadCommand reloadCommand
    ) {
        this.convertCommand = convertCommand;
        this.giveCommand = giveCommand;
        this.debugCommand = debugCommand;
        this.reloadCommand = reloadCommand;
    }

    public void registerViaBootstrap(final BootstrapContext context) {
        final LifecycleEventManager<BootstrapContext> lifecycleManager = context.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(this.bootstrapCommands(), null, ALIASES)
        );
    }

    public void registerViaEnable(final JavaPlugin plugin) {
        final LifecycleEventManager<Plugin> lifecycleManager = plugin.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(this.pluginCommands(), null, ALIASES)
        );
    }

    private LiteralCommandNode<CommandSourceStack> bootstrapCommands() {
        return this.literal()
                .build();
    }

    private LiteralCommandNode<CommandSourceStack> pluginCommands() {
        return this.literal()
                .then(this.convertCommand.init())
                .then(this.giveCommand.init())
                .then(this.debugCommand.init())
                .then(this.reloadCommand.init())
                .build();

    }

    private LiteralArgumentBuilder<CommandSourceStack> literal() {
        return FIRST_LITERAL_ARGUMENT
                .requires(source -> source.getSender().hasPermission("commanditem.command"));
    }
}
