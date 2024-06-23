package github.tyonakaisan.commanditem.util;

import github.tyonakaisan.commanditem.item.Command;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CommandExecutor {

    private CommandExecutor() {}

    public static void executeByPlayer(final Command command, final Player player) {
        command.commands(player).forEach(c -> executeByPlayer(c, player));
    }

    public static void executeByPlayer(final String command, final Player player) {
        player.performCommand(command);
    }

    public static void executeFrozen(final Command command, final Player player) {
        command.intValues(player).forEach(c -> executeFrozen(c, player));
    }

    public static void executeFrozen(final int ticks, final Player player) {
        player.setFreezeTicks(ticks);
    }

    public static void executeByConsole(final Command command, final Player player) {
        command.commands(player).forEach(CommandExecutor::executeByConsole);
    }

    public static void executeByConsole(final String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    public static void executeMessage(final Command command, final Player player) {
        command.messages(player).forEach(message -> executeMessage(message, player));
    }

    public static void executeMessage(final Component message, final Player player) {
        player.sendMessage(message);
    }

    public static void executeBroadCast(final Command command, final Player player) {
        command.messages(player).forEach(CommandExecutor::executeBroadCast);
    }

    public static void executeBroadCast(final Component message) {
        Bukkit.getServer().broadcast(message);
    }
}

