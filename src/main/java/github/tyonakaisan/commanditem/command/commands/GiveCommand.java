package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.CommandsItem;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.message.MessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;

import java.util.Set;

@DefaultQualifier(NonNull.class)
@SuppressWarnings("java:S1192")
public final class GiveCommand implements CommandItemCommand {

    private final CommandItemRegistry commandItemRegistry;
    private final MessageManager messageManager;
    private final Convert convert;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public GiveCommand(
            final CommandItemRegistry commandItemRegistry,
            final MessageManager messageManager,
            final Convert convert,
            final CommandManager<CommandSender> commandManager
    ) {
        this.commandItemRegistry = commandItemRegistry;
        this.messageManager = messageManager;
        this.convert = convert;
        this.commandManager = commandManager;
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commanditem", "cmdi", "ci")
                .literal("give")
                .permission("commanditem.command.give")
                .senderType(CommandSender.class)
                .argument(MultiplePlayerSelectorArgument.of("player"))
                .argument(this.commandManager.argumentBuilder(String.class, "key")
                        .withSuggestionsProvider(
                                ((context, string) -> {
                                    final Set<Key> allArgs = this.commandItemRegistry.keySet();
                                    return allArgs.stream()
                                            .map(Key::asString)
                                            .toList();
                                })
                        )
                        .build())
                .argument(IntegerArgument.optional("count"))
                .handler(handler -> {
                    final var sender = handler.getSender();
                    final MultiplePlayerSelector players = handler.get("player");
                    final @Subst("value") String keyValue = handler.get("key");
                    final var key = Key.key(keyValue);
                    final var count = (int) handler.getOptional("count").orElse(1);

                    players.getPlayers().forEach(player -> {
                        @Nullable CommandsItem commandsItem = this.commandItemRegistry.get(key);

                        if (commandsItem == null) {
                            sender.sendMessage(this.messageManager.translatable(
                                    MessageManager.Style.ERROR,
                                    player,
                                    "command.give.error.unknown_item",
                                    TagResolver.builder()
                                            .tag("item", Tag.selfClosingInserting(Component.text(key.asString())))
                                            .build()));
                            return;
                        }

                        var item = this.convert.toItemStack(commandsItem, player);

                        if (this.isMaxStackSize(player, item, count)) {
                            return;
                        }

                        this.giveItem(player, count, commandsItem);

                        sender.sendMessage(this.messageManager.translatable(
                                MessageManager.Style.INFO,
                                player,
                                "command.give.info.give",
                                TagResolver.builder()
                                        .tag("player", Tag.selfClosingInserting(player.displayName()))
                                        .tag("display_name", Tag.selfClosingInserting(item.displayName()))
                                        .tag("count", Tag.selfClosingInserting(Component.text(count)))
                                        .build()));

                        player.playSound(Sound.sound()
                                .type(Key.key("minecraft:entity.item.pickup"))
                                .volume(0.3f)
                                .pitch(2f)
                                .build());
                    });
                })
                .build();
        this.commandManager.command(command);
    }

    private boolean isMaxStackSize(Player player, ItemStack itemStack, int count) {
        var maxReceive = itemStack.getMaxStackSize() * 36;

        if (count > maxReceive) {
            var resolver = TagResolver.builder()
                    .tag("max", Tag.selfClosingInserting(Component.text(maxReceive)))
                    .build();
            player.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.give.error.max_count", resolver));
            return false;
        }

        return true;
    }

    private void giveItem(Player player, int count, CommandsItem commandsItem) {
        var itemStack = this.convert.toItemStack(commandsItem, player);
        if (!commandsItem.stackable() || itemStack.getMaxStackSize() == 1) {
            for (int i = 0; i < count; i++) {
                player.getInventory().addItem(itemStack);
            }
        } else {
            itemStack.setAmount(count);
            player.getInventory().addItem(itemStack);
        }
    }
}