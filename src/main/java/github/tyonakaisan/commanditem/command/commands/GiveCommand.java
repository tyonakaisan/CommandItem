package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemHandler;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.data.Selector;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

@DefaultQualifier(NonNull.class)
@SuppressWarnings({"java:S1192", "PatternValidation"})
public final class GiveCommand implements CommandItemCommand {

    private final ItemRegistry itemRegistry;
    private final CommandItemHandler commandItemHandler;
    private final CommandManager<CommandSender> commandManager;

    @Inject
    public GiveCommand(
            final ItemRegistry itemRegistry,
            final CommandItemHandler commandItemHandler,
            final CommandManager<CommandSender> commandManager
    ) {
        this.itemRegistry = itemRegistry;
        this.commandItemHandler = commandItemHandler;
        this.commandManager = commandManager;
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder("commanditem", "cmdi", "ci")
                .literal("give")
                .permission("commanditem.command.give")
                .senderType(CommandSender.class)
                .required("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                .required("id",
                        StringParser.stringParser(),
                        SuggestionProvider.blockingStrings((context, input) ->
                                this.itemRegistry.keys().stream()
                                        .map(Key::asString)
                                        .toList()))
                .optional("count", IntegerParser.integerParser(), SuggestionProvider.noSuggestions())
                .handler(handler -> {
                    final var sender = handler.sender();
                    final Selector<Player> players = handler.get("player");
                    final var keyString = this.addNamespaceIfMissing(handler.get("id"));
                    final var count = (int) handler.optional("count").orElse(1);

                    final var key = Key.key(keyString);
                    this.commandItemHandler.giveItem(players.values(), sender, key, count);
                })
                .build();
        this.commandManager.command(command);
    }

    private String addNamespaceIfMissing(final String string) {
        return string.startsWith(NamespacedKeyUtils.namespace())
                ? string
                : NamespacedKeyUtils.namespace() + ":" + string;
    }
}