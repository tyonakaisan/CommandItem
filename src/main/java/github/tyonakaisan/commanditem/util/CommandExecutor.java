package github.tyonakaisan.commanditem.util;

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
        player.performCommand(command.replace("<player>", player.getName()));
    }

    public static void executeByConsole(String command, Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("<player>", player.getName()));
    }
}
