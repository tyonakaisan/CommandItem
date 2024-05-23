package github.tyonakaisan.commanditem.command.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import github.tyonakaisan.commanditem.command.CommandItemCommand;
import github.tyonakaisan.commanditem.item.CommandItemHandler;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public final class GiveCommand implements CommandItemCommand {

    private final ItemRegistry itemRegistry;
    private final CommandItemHandler commandItemHandler;

    @Inject
    public GiveCommand(
            final ItemRegistry itemRegistry,
            final CommandItemHandler commandItemHandler
    ) {
        this.itemRegistry = itemRegistry;
        this.commandItemHandler = commandItemHandler;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> init() {
        return literal("give")
                .requires(source -> source.getSender().hasPermission("commanditem.command.give"))
                .then(argument("targets", ArgumentTypes.players())
                        .then(argument("item", ArgumentTypes.key())
                                .suggests(this::suggest)
                                .executes(this::execute)
                                .then(argument("count", IntegerArgumentType.integer())
                                        .executes(this::execute)))
                );
    }

    private CompletableFuture<Suggestions> suggest(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        this.itemRegistry.keys().stream()
                .map(Key::asString)
                .filter(s -> s.contains(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private int execute(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var sender = context.getSource().getSender();
        final List<Player> targets = context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
        final var key = context.getArgument("item", Key.class);
        final int count = context.getNodes().size() == 4
                ? context.getArgument("count", int.class)
                : 1;

        this.commandItemHandler.giveItem(targets, sender, key, count);
        return Command.SINGLE_SUCCESS;
    }
}
