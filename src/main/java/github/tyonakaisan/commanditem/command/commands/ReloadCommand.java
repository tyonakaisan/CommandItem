package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.ItemRegistry;
import github.tyonakaisan.commanditem.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;

@DefaultQualifier(NonNull.class)
public final class ReloadCommand implements CommandItemCommand {

    private final ConfigFactory configFactory;
    private final ItemRegistry itemRegistry;
    private final Messages messages;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public ReloadCommand(
            final ConfigFactory configFactory,
            final ItemRegistry itemRegistry,
            final Messages messages,
            final CommandManager<CommandSender> commandManager
    ) {
        this.configFactory = configFactory;
        this.itemRegistry = itemRegistry;
        this.messages = messages;
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
                    this.itemRegistry.reloadItemConfig();
                    this.messages.reloadMessage();
                    
                    final var sender = handler.sender();
                    sender.sendMessage(this.messages.translatable(Messages.Style.SUCCESS, sender, "command.reload.success.reload"));
                    sender.sendMessage(this.messages.translatable(Messages.Style.SUCCESS,
                            sender,
                            "command.reload.success.items",
                            TagResolver.builder()
                                    .tag("size", Tag.selfClosingInserting(Component.text(this.itemRegistry.items().size())))
                                    .build()));
                })
                .build();

        this.commandManager.command(command);
    }
}
