package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.CommandItem;
import github.tyonakaisan.commanditem.util.ActionUtils;
import github.tyonakaisan.commanditem.util.CommandExecutor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static github.tyonakaisan.commanditem.item.CommandsItem.defaultComponent;
import static github.tyonakaisan.commanditem.item.CommandsItem.miniMessage;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
public record CustomCommand(
        ActionUtils.CommandAction action,
        List<String> commands,
        int repeat,
        int period,
        int delay,
        double runWeight
) {

    public List<String> commands(Player player) {
        return this.commands.stream().map(text -> {
            var papiParser = PlaceholderAPI.setPlaceholders(player, text);
            return papiParser.replace("<player>", player.getName());
        }).toList();
    }

    public List<Component> messages(Player player) {
        return this.commands.stream().map(text -> {
            var papiParser = PlaceholderAPI.setPlaceholders(player, text);
            return defaultComponent.append(miniMessage(player).deserialize(papiParser));
        }).toList();
    }

    public void repeatCommands(Player player, CustomCommand customCommand, CommandItem commandItem, boolean console) {
        new BukkitRunnable() {
            final int repeatCounts = Math.min(this.repeatCounts, 100);
            int count = 0;
            final double weight = ThreadLocalRandom.current().nextDouble();
            @Override
            public void run() {
                if (runWeight() >= weight || runWeight() == 0) {
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
        }.runTaskTimer(commandItem, this.delay, this.period);
    }
}
