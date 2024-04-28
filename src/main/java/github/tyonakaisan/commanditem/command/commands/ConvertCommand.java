package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.message.Messages;
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
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

@DefaultQualifier(NonNull.class)
public final class ConvertCommand implements CommandItemCommand {

    private final ComponentLogger logger;
    private final Messages messages;
    private final CommandItemRegistry commandItemRegistry;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public ConvertCommand(
            final ComponentLogger logger,
            final Messages messages,
            final CommandItemRegistry commandItemRegistry,
            final CommandManager<CommandSender> commandManager
    ) {
        this.logger = logger;
        this.messages = messages;
        this.commandItemRegistry = commandItemRegistry;
        this.commandManager = commandManager;
    }


    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commanditem", "cmdi", "ci")
                .literal("convert")
                .permission("commanditem.command.convert")
                .senderType(CommandSender.class)
                .required("id", StringParser.stringParser())
                .handler(handler -> {
                    if (handler.sender() instanceof Player sender) {
                        final String fileName = handler.get("id");
                        final var item = sender.getInventory().getItemInMainHand();
                        final var allKey = this.commandItemRegistry.keySet().stream()
                                .map(Key::value)
                                .toList();

                        if (!NamespaceKeyUtils.checkKeyStringPattern(fileName)) {
                            sender.sendMessage(this.messages.translatable(Messages.Style.ERROR, sender, "command.convert.error.non_matching_character"));
                            return;
                        }

                        if (item.getType() == Material.AIR || item.getItemMeta().getPersistentDataContainer().has(NamespaceKeyUtils.idKey())) {
                            sender.sendMessage(this.messages.translatable(Messages.Style.ERROR, sender, "command.convert.error.can_not_convert"));
                            return;
                        }

                        if (allKey.contains(fileName)) {
                            sender.sendMessage(this.messages.translatable(Messages.Style.ERROR, sender, "command.convert.error.file_name_exists"));
                            return;
                        }

                        this.commandItemRegistry.createItemConfig(fileName, item);
                        this.commandItemRegistry.reloadItemConfig();
                        sender.sendMessage(this.messages.translatable(Messages.Style.SUCCESS,
                                sender,
                                "command.convert.success.convert",
                                TagResolver.builder()
                                        .tag("file", Tag.selfClosingInserting(Component.text(fileName + ".conf")))
                                        .build()));

                        sender.playSound(Sound.sound()
                                .type(Key.key("minecraft:block.anvil.use"))
                                .volume(0.25f)
                                .pitch(1.25f)
                                .build());
                    } else {
                        this.logger.warn("Not console commands.");
                    }
                })
                .build();

        commandManager.command(command);
    }
}
