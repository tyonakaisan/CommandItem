package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.util.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.concurrent.ThreadLocalRandom;

@DefaultQualifier(NonNull.class)
public final class ReCommandTask extends BukkitRunnable {
    private final Command command;
    private final Player player;
    private final boolean console;
    private final int repeatCounts;
    private final double weight;

    private int count;

    public ReCommandTask(
            final Command command,
            final Player player,
            final boolean console
    ) {
        this.command = command;
        this.player = player;
        this.console = console;

        this.repeatCounts = Math.min((int) command.repeat(player), 100);
        this.weight = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
    }

    @Override
    public void run() {
        this.count++;

        switch (this.command.type()) {
            case COMMAND -> {
                if (this.console) {
                    CommandExecutor.executeByConsole(this.command, this.player);
                } else {
                    CommandExecutor.executeByPlayer(this.command, this.player);
                }
            }
            case MESSAGE -> CommandExecutor.executeMessage(this.command, this.player);
            case BROAD_CAST -> CommandExecutor.executeBroadCast(this.command, this.player);
        }

        if (this.count >= this.repeatCounts) {
            this.cancel();
        }
    }
}
