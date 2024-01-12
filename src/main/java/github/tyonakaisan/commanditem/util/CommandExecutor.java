package github.tyonakaisan.commanditem.util;

import github.tyonakaisan.commanditem.item.CustomCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CommandExecutor {

    private CommandExecutor() {
        throw new AssertionError();
    }

    public static void executeByPlayer(String command, Player player) {
        player.performCommand(command);
    }

    public static void executeByPlayer(CustomCommand customCommand, Player player) {
        customCommand.commands(player).forEach(command -> executeByPlayer(command, player));
    }

    public static void executeByConsole(String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    public static void executeByConsole(CustomCommand customCommand, Player player) {
        customCommand.commands(player).forEach(CommandExecutor::executeByConsole);
    }

    public static void executeMessage(Component message, Player player) {
        player.sendMessage(message);
    }

    public static void executeMessage(CustomCommand customCommand, Player player) {
        customCommand.messages(player).forEach(message -> executeMessage(message, player));
    }

    public static void executeBroadCast(Component message) {
        Bukkit.getServer().broadcast(message);
    }

    public static void executeBroadCast(CustomCommand customCommand, Player player) {
        customCommand.messages(player).forEach(CommandExecutor::executeBroadCast);
    }
}
