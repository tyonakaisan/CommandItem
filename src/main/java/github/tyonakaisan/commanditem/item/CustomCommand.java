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
        new BukkitRunnable() {
            final int repeatCounts = Math.min((int) repeat(player), 100);
            final double weight = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
            int count = 0;

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
        }.runTaskTimer(commandItem, (long) delay(player), (long) period(player));
    }
}
