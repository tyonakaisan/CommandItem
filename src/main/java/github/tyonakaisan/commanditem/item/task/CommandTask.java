package github.tyonakaisan.commanditem.item.task;

import github.tyonakaisan.commanditem.item.Command;
import github.tyonakaisan.commanditem.util.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CommandTask extends BukkitRunnable {
    private final Command command;
    private final Player player;
    private final int repeatCounts;

    private int count;

    public CommandTask(
            final Command command,
            final Player player
    ) {
        this.command = command;
        this.player = player;

        this.repeatCounts = Math.min(command.repeat(player), 100);
    }

    @Override
    public void run() {
        this.count++;

        switch (this.command.type()) {
            case COMMAND -> {
                if (this.command.isConsole()) {
                    CommandExecutor.executeByConsole(this.command, this.player);
                } else {
                    CommandExecutor.executeByPlayer(this.command, this.player);
                }
            }
            case FROZEN -> CommandExecutor.executeFrozen(this.command, this.player);
            case MESSAGE -> CommandExecutor.executeMessage(this.command, this.player);
            case BROAD_CAST -> CommandExecutor.executeBroadCast(this.command, this.player);
        }

        if (this.count >= this.repeatCounts) {
            this.cancel();
        }
    }
}
