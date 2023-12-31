package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
                    final var pdc = item.getItemMeta().getPersistentDataContainer();
                    final Set<Key> allKeySet = this.commandItemRegistry.keySet();

                    var allKey = allKeySet.stream()
                            .map(Key::value)
                            .toList();

                    if (allKey.contains(fileName)) {
                        player.sendRichMessage("<red>This file has already been created!</red>");
                        return;
                    }

                    if (item.getType() == Material.AIR || pdc.has(NamespacedKeyUtils.idKey())) {
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
