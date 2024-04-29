package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.CommandExecutor;
import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public record CustomCommand(
        ActionUtils.CommandAction action,
        List<String> commands,
        String repeat,
        String period,
        String delay,
        String runWeight
) {

    public static CustomCommand empty() {
        return new CustomCommand(ActionUtils.CommandAction.COMMAND, List.of(), "0", "0", "0", "0");
    }

    public List<String> commands(Player player) {
        return this.commands.stream()
                .map(text -> PlaceholderUtils.getPlainText(player, text))
                .toList();
    }

    public List<Component> messages(Player player) {
        return this.commands.stream()
                .map(text -> PlaceholderUtils.getComponent(player, text))
                .toList();
    }

    public double repeat(Player player) {
        return PlaceholderUtils.calculate(player, this.repeat);
    }

    public double period(Player player) {
        return PlaceholderUtils.calculate(player, this.period);
    }

    public double delay(Player player) {
        return PlaceholderUtils.calculate(player, this.delay);
    }

    public double runWeight(Player player) {
        return PlaceholderUtils.calculate(player, this.runWeight);
    }

    public void repeatCommands(Player player, CustomCommand customCommand, CommandItem commandItem, boolean console) {
        var period = this.period(player);

        // periodが-1以下の場合はfor文
        if (period <= -1) {
            commandItem.getServer().getScheduler().runTaskLater(commandItem, () -> {
                for (int i = 0; i < this.repeat(player); i++) {
                    new CommandTask(customCommand, player, console).run();
                }
            }, (long) this.delay(player));
        } else {
            new CommandTask(customCommand, player, console).runTaskTimer(commandItem, (long) this.delay(player), (long) period);
        }
    }

    private final class CommandTask extends BukkitRunnable {

        private final CustomCommand customCommand;
        private final Player player;
        private final boolean console;
        private final int repeatCounts;
        private final double weight;

        private int count;

        private CommandTask(
                final CustomCommand customCommand,
                final Player player,
                final boolean console
        ) {
            this.customCommand = customCommand;
            this.player = player;
            this.console = console;

            this.repeatCounts = Math.min((int) this.customCommand.repeat(player), 100);
            this.weight = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
        }

        @Override
        public void run() {
            if (runWeight(player) >= weight || runWeight(player) == 0) {
                count++;

                switch (action()) {
                    case COMMAND -> {
                        if (console) {
                            CommandExecutor.executeByConsole(customCommand, player);
                        } else {
                            CommandExecutor.executeByPlayer(customCommand, player);
                        }
                    }
                    case MESSAGE -> CommandExecutor.executeMessage(customCommand, player);
                    case BROAD_CAST -> CommandExecutor.executeBroadCast(customCommand, player);
                }

                if (count >= repeatCounts) this.cancel();
            }
        }
    }
}
