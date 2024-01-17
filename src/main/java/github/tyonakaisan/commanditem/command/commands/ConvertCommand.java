package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.message.MessageManager;
import github.tyonakaisan.commanditem.util.NamespaceKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

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
                    final var allKey = this.commandItemRegistry.keySet().stream()
                            .map(Key::value)
                            .toList();

                    if (!NamespaceKeyUtils.checkKeyStringPattern(fileName)) {
                        player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.convert.error.non_matching_character"));
                        return;
                    }

                    if (item.getType() == Material.AIR || item.getItemMeta().getPersistentDataContainer().has(NamespaceKeyUtils.idKey())) {
                        player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.convert.error.can_not_convert"));
                        return;
                    }

                    if (allKey.contains(fileName)) {
                        player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.convert.error.file_name_exists"));
                        return;
                    }

                    this.commandItemRegistry.createItemConfig(fileName, item);
                    this.commandItemRegistry.reloadItemConfig();
                    player.sendMessage(this.messageManager.translatable(MessageManager.Style.SUCCESS,
                            player,
                            "command.convert.success.convert",
                            TagResolver.builder()
                                    .tag("file", Tag.selfClosingInserting(Component.text(fileName + ".conf")))
                                    .build()));

                    player.playSound(Sound.sound()
                            .type(Key.key("minecraft:block.anvil.use"))
                            .volume(0.25f)
                            .pitch(1.25f)
                            .build());
                })
                .build();

        commandManager.command(command);
    }
}
