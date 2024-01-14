package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.Convert;
import github.tyonakaisan.commanditem.message.MessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.Set;

@DefaultQualifier(NonNull.class)
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
                .argument(this.commandManager.argumentBuilder(Key.class, "key")
                        .withSuggestionsProvider(
                                ((context, string) -> {
                                    final Set<Key> allArgs = commandItemRegistry.keySet();
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
                    final var key = (Key) handler.get("key");
                    final var count = (int) handler.getOptional("count").orElse(1);

                    players.getPlayers().forEach(player -> {
                        var item = this.convert.toItemStack(Objects.requireNonNull(this.commandItemRegistry.get(key)), player);
                        var maxReceive = item.getMaxStackSize() * 36;
                        TagResolver resolver;
                        if (count > maxReceive) {
                            resolver = TagResolver.builder()
                                    .tag("max", Tag.selfClosingInserting(Component.text(maxReceive)))
                                    .build();
                            sender.sendMessage(this.messageManager.translatable(MessageManager.Style.ERROR, player, "command.give.error.max_count", resolver));
                            return;
                        }

                        if (!Objects.requireNonNull(this.commandItemRegistry.get(key)).stackable() || item.getMaxStackSize() == 1) {
                            for (int i = 0; i < count; i++) {
                                player.getInventory().addItem(item);
                            }
                        } else {
                            item.setAmount(count);
                            player.getInventory().addItem(item);
                        }

                        resolver = TagResolver.builder()
                                .tag("player",
                                        Tag.selfClosingInserting(player.displayName()))
                                .tag("display_name",
                                        Tag.selfClosingInserting(this.convert.toItemStack(Objects.requireNonNull(this.commandItemRegistry.get(key)), player).displayName()))
                                .tag("count",
                                        Tag.selfClosingInserting(Component.text(count)))
                                .build();

                        sender.sendMessage(this.messageManager.translatable(MessageManager.Style.INFO, player, "command.give.info.give", resolver));

                        sender.playSound(Sound.sound()
                                .type(Key.key("minecraft:entity.item.pickup"))
                                .volume(0.3f)
                                .pitch(2f)
                                .build());
                    });
                })
                .build();
        this.commandManager.command(command);
    }
}