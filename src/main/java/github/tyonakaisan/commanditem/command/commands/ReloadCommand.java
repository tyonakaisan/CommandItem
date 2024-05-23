package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import github.tyonakaisan.commanditem.message.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public final class ReloadCommand implements CommandItemCommand {

    private final ConfigFactory configFactory;
    private final ItemRegistry itemRegistry;
    private final Messages messages;

    @Inject
    public ReloadCommand(
            final ConfigFactory configFactory,
            final ItemRegistry itemRegistry,
            final Messages messages
    ) {
        this.configFactory = configFactory;
        this.itemRegistry = itemRegistry;
        this.messages = messages;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> init() {
        return literal("reload")
                .requires(source -> source.getSender().hasPermission("commanditem.command.reload"))
                .executes(context -> {
                    final CommandSender sender = context.getSource().getSender();
                    this.configFactory.reloadPrimaryConfig();
                    this.itemRegistry.reloadItemConfig();
                    this.messages.reloadMessage();

                    sender.sendMessage(this.messages.translatable(Messages.Style.SUCCESS, sender, "command.reload.success.reload"));
                    sender.sendMessage(this.messages.translatable(Messages.Style.SUCCESS,
                            sender,
                            "command.reload.success.items",
                            TagResolver.builder()
                                    .tag("size", Tag.selfClosingInserting(Component.text(this.itemRegistry.items().size())))
                                    .build()));

                    return Command.SINGLE_SUCCESS;
                });
    }
}
