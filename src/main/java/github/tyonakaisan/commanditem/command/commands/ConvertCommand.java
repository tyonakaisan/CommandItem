package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.message.MessageManager;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.IOException;
import java.util.List;

@DefaultQualifier(NonNull.class)
public final class ConvertCommand implements CommandItemCommand {

    private final ComponentLogger logger;
    private final MessageManager messageManager;
    private final CommandItemRegistry commandItemRegistry;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public ConvertCommand(
            final ComponentLogger logger,
            final MessageManager messageManager,
            final CommandItemRegistry commandItemRegistry,
            final CommandManager<CommandSender> commandManager
    ) {
        this.logger = logger;
        this.messageManager = messageManager;
        this.commandItemRegistry = commandItemRegistry;
        this.commandManager = commandManager;
    }


    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commanditem", "cmdi", "ci")
                .literal("convert")
                .permission("commanditem.command.convert")
                .senderType(CommandSender.class)
                .argument(this.commandManager.argumentBuilder(String.class, "file_name")
                        .withSuggestionsProvider((context, string) -> List.of("<file_name>"))
                        .build())
                .handler(handler -> {
                    final String fileName = handler.get("file_name");
                    final var player = (Player) handler.getSender();
                    final var item = player.getInventory().getItemInMainHand();

                    if (item.getType() == Material.AIR || item.getItemMeta().getPersistentDataContainer().has(NamespacedKeyUtils.idKey())) {
                        player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.convert.error.can_not_convert"));
                        return;
                    }

                    final var allKey = this.commandItemRegistry.keySet().stream()
                            .map(Key::value)
                            .toList();

                    if (allKey.contains(fileName)) {
                        player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.convert.error.file_name_exists"));
                        return;
                    }

                    try {
                        this.commandItemRegistry.createItemConfig(fileName, item);
                        this.commandItemRegistry.reloadItemConfig();
                        player.sendMessage(this.messageManager.translatable(MessageManager.Style.SUCCESS, player, "command.convert.success.convert"));
                    } catch (IOException e) {
                        this.logger.error("Failed to convert item.", e);
                    }
                })
                .build();

        commandManager.command(command);
    }
}
