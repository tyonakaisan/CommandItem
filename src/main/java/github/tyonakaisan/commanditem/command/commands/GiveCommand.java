package github.tyonakaisan.commanditem.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import cloud.commandframework.bukkit.parsers.selector.MultiplePlayerSelectorArgument;
import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.CommandItemRegistry;
import github.tyonakaisan.commanditem.item.Convert;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.Set;

@DefaultQualifier(NonNull.class)
public final class GiveCommand implements CommandItemCommand {

    private final ConfigFactory configFactory;
    private final CommandItemRegistry commandItemRegistry;
    private final Convert convert;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public GiveCommand(
            final ConfigFactory configFactory,
            final CommandItemRegistry commandItemRegistry,
            final Convert convert,
            final CommandManager<CommandSender> commandManager
    ) {
        this.configFactory = configFactory;
        this.commandItemRegistry = commandItemRegistry;
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
                        if (count > maxReceive) {
                            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>This item max count is <max></red>",
                                    Formatter.number("max", maxReceive)
                            ));
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

                        sender.sendMessage(MiniMessage.miniMessage().deserialize("<white><player>に<white><display_name></white>を<count>個与えました</white>",
                                Placeholder.parsed("player", player.getName()),
                                Placeholder.component("display_name", this.convert.toItemStack(Objects.requireNonNull(this.commandItemRegistry.get(key)), player).displayName()),
                                Formatter.number("count", count)
                        ));

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