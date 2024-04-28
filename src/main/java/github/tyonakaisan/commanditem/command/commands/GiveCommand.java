package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.CommandsItem;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.message.Messages;
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
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.data.Selector;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.intellij.lang.annotations.Subst;

import java.util.Set;

@DefaultQualifier(NonNull.class)
@SuppressWarnings("java:S1192")
public final class GiveCommand implements CommandItemCommand {

    private final CommandItemRegistry commandItemRegistry;
    private final Messages messages;
    private final Convert convert;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public GiveCommand(
            final CommandItemRegistry commandItemRegistry,
            final Messages messages,
            final Convert convert,
            final CommandManager<CommandSender> commandManager
    ) {
        this.commandItemRegistry = commandItemRegistry;
        this.messages = messages;
        this.convert = convert;
        this.commandManager = commandManager;
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commanditem", "cmdi", "ci")
                .literal("give")
                .permission("commanditem.command.give")
                .senderType(CommandSender.class)
                .required("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                .required("key",
                        StringParser.greedyStringParser(),
                        SuggestionProvider.blockingStrings((context, input) -> {
                            final Set<Key> allArgs = this.commandItemRegistry.keySet();
                            return allArgs.stream()
                                    .map(Key::asString)
                                    .toList();
                }))
                .optional("count", IntegerParser.integerParser())
                .handler(handler -> {
                    final var sender = handler.sender();
                    final Selector<Player> players = handler.get("player");
                    final @Subst("value") String keyValue = handler.get("key");
                    final var key = Key.key(keyValue);
                    final var count = (int) handler.optional("count").orElse(1);

                    players.values().forEach(player -> {
                        @Nullable CommandsItem commandsItem = this.commandItemRegistry.get(key);

                        if (commandsItem == null) {
                            sender.sendMessage(this.messages.translatable(
                                    Messages.Style.ERROR,
                                    sender,
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

                        sender.sendMessage(this.messages.translatable(
                                Messages.Style.INFO,
                                sender,
                                "command.give.info.give",
                                TagResolver.builder()
                                        .tag("player", Tag.selfClosingInserting(player.displayName()))
                                        .tag("item", Tag.selfClosingInserting(item.displayName()))
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
                    .tag("item", Tag.selfClosingInserting(itemStack.displayName()))
                    .build();
            player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "command.give.error.max_count", resolver));
            return true;
        }

        return false;
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