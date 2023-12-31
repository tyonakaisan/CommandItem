package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.IOException;

@DefaultQualifier(NonNull.class)
public final class ReloadCommand implements CommandItemCommand {

    private final ConfigFactory configFactory;
    private final CommandItemRegistry commandItemRegistry;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public ReloadCommand(
            final ConfigFactory configFactory,
            final CommandItemRegistry commandItemRegistry,
            final CommandManager<CommandSender> commandManager
    ) {
        this.configFactory = configFactory;
        this.commandItemRegistry = commandItemRegistry;
        this.commandManager = commandManager;
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commanditem", "cmdi", "ci")
                .literal("reload")
                .permission("commanditem.command.reload")
                .senderType(CommandSender.class)
                .handler(handler -> {
                    this.configFactory.reloadPrimaryConfig();
                    try {
                        this.commandItemRegistry.reloadItemConfig();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    final var sender = (Player) handler.getSender();
                    sender.sendRichMessage("config reloaded!");
                })
                .build();

        this.commandManager.command(command);
    }
}
