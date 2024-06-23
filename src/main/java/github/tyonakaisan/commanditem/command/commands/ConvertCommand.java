package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import github.tyonakaisan.commanditem.message.Messages;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public final class ConvertCommand implements CommandItemCommand {

    private final ComponentLogger logger;
    private final Messages messages;
    private final ItemRegistry itemRegistry;

    @Inject
    public ConvertCommand(
            final ComponentLogger logger,
            final Messages messages,
            final ItemRegistry itemRegistry
    ) {
        this.logger = logger;
        this.messages = messages;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> init() {
        return literal("convert")
                .requires(source -> source.getSender().hasPermission("commanditem.command.convert"))
                .then(argument("id", ArgumentTypes.key())
                        .executes(this::execute)
                );
    }

    private int execute(final CommandContext<CommandSourceStack> context) {
        if (context.getSource().getSender() instanceof final Player sender) {
            final var id = context.getArgument("id", Key.class);
            final var itemStack = sender.getInventory().getItemInMainHand();

            if (this.check(sender, itemStack, id)) {
                this.itemRegistry.createItemConfig(id, itemStack);
                this.itemRegistry.reloadItemConfig();

                sender.sendMessage(this.messages.translatable(Messages.Style.SUCCESS,
                        sender,
                        "command.convert.success.convert",
                        TagResolver.builder()
                                .tag("file", Tag.selfClosingInserting(Component.text(id.value() + ".conf")))
                                .build()));

                sender.playSound(Sound.sound()
                        .type(Key.key("minecraft:block.anvil.use"))
                        .volume(0.25f)
                        .pitch(1.25f)
                        .build());
            }
            return Command.SINGLE_SUCCESS;
        } else {
            this.logger.warn("This command can only be executed by player.");
            return 0;
        }
    }

    private boolean check(final Player player, final ItemStack itemStack, final Key id) {
        if (itemStack.isEmpty() || itemStack.getItemMeta().getPersistentDataContainer().has(NamespacedKeyUtils.idKey())) {
            player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "command.convert.error.can_not_convert"));
            return false;
        }

        final var stream = this.itemRegistry.keys().stream();

        if (stream.anyMatch(key -> key.equals(id))) {
            player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "command.convert.error.item_id_exists"));
            return false;
        }
        return true;
    }
}
