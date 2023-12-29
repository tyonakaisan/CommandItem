package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.Convert;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.IOException;

@DefaultQualifier(NonNull.class)
public final class ConvertCommand implements CommandItemCommand {

    private final CommandItem commandItem;
    private final ConfigFactory configFactory;
    private final CommandItemRegistry commandItemRegistry;
    private final Convert convert;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public ConvertCommand(
            final CommandItem commandItem,
            final ConfigFactory configFactory,
            final CommandItemRegistry commandItemRegistry,
            final Convert convert,
            final CommandManager<CommandSender> commandManager
    ) {
        this.commandItem = commandItem;
        this.configFactory = configFactory;
        this.commandItemRegistry = commandItemRegistry;
        this.convert = convert;
        this.commandManager = commandManager;
    }


    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commaditem", "ci")
                .literal("convert")
                .permission("commanditem.command.convert")
                .senderType(CommandSender.class)
                .argument(StringArgument.of("file_name"))
                .handler(handler -> {
                    final String fileName = handler.get("file_name");
                    final var player = (Player) handler.getSender();
                    final var item = player.getInventory().getItemInMainHand();

                    if (item.getType() == Material.AIR) {
                        player.sendRichMessage("<red>This item cannot be converted!</red>");
                        return;
                    }

                    try {
                        this.commandItemRegistry.createItemConfig(fileName, item);
                        this.commandItemRegistry.reloadItemConfig();
                        player.sendRichMessage("<green>Item converted!</green>");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        commandManager.command(command);
    }
}
