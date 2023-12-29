package github.tyonakaisan.commanditem.command.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import com.google.inject.Inject;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.Queue;

@DefaultQualifier(NonNull.class)
public final class KeyArgument<C> implements ArgumentParser<C, Key> {

    @Inject
    public KeyArgument(
    ) {
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Key> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull Queue<@NonNull String> inputQueue) {
        final String input = Objects.requireNonNull(inputQueue.peek());
        if (input == null) {
            return ArgumentParseResult.failure(new NoInputProvidedException(KeyArgument.class, commandContext));
        }

        try {
            Key key = Key.key(input);
            inputQueue.remove();
            return ArgumentParseResult.success(key);
        } catch (final IllegalArgumentException e) {
            return ArgumentParseResult.failure(new NoInputProvidedException(KeyArgument.class, commandContext));
        }
    }

    @Override
    public boolean isContextFree() {
        return true;
    }
}
