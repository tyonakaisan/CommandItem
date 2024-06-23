package github.tyonakaisan.commanditem.item.task;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItemProvider;
import github.tyonakaisan.commanditem.item.Action;
import github.tyonakaisan.commanditem.item.Command;
import github.tyonakaisan.commanditem.item.Item;
import github.tyonakaisan.commanditem.util.ItemUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

@DefaultQualifier(NonNull.class)
public final class CommandHandler {

    @Inject
    public CommandHandler() {}

    public void runCommands(final Item item, final Player player, final Action.Item action) {
        final var commands = item.commands().getOrDefault(action, List.of());
        if (commands.isEmpty()) {
            return;
        }

        final var picks = ItemUtils.picks(item, player, action);
        if (picks == 0 || item.attributes().maxUses(player) == 0) {
            return;
        }

        if (picks < 0) {
            commands.forEach(command -> this.repeatCommand(command, player));
        } else {
            final var weightedCommands = ItemUtils.weightedCommands(item, player, action);

            if (weightedCommands.size() == 0) {
                return;
            }

            for (int i = 0; i < picks; i++) {
                final var command = weightedCommands.select();
                this.repeatCommand(command, player);
            }
        }
    }

    private void repeatCommand(final Command command, final Player player) {
        final var repeat = command.repeat(player);
        if (repeat == 0) {
            return;
        }

        final var period = command.period(player);
        final var delay = command.delay(player);
        final var commandItem = CommandItemProvider.instance();
        final var task = new CommandTask(command, player);

        if (period > 0) {
            task.runTaskTimer(commandItem, delay, period);
        } else {
            if (delay > 0) {
                commandItem.getServer().getScheduler().runTaskLater(commandItem, () -> {
                    for (int i = 0; i < repeat; i++) {
                        task.run();
                    }
                }, delay);
            } else {
                for (int i = 0; i < repeat; i++) {
                    task.run();
                }
            }
        }
    }
}
