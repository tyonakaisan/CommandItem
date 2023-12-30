package github.tyonakaisan.commanditem.util;

import github.tyonakaisan.commanditem.item.CustomCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class CommandExecutor {

    private CommandExecutor() {
        throw new AssertionError();
    }

    private static final String PLAYER = "<player>";

    public static void executeByPlayer(String command, Player player) {
        player.performCommand(command.replace(PLAYER, player.getName()));
    }

    public static void executeByPlayer(CustomCommand customCommand, Player player) {
        customCommand.commands().forEach(command -> executeByPlayer(command, player));
    }

    public static void executeByConsole(String command, Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace(PLAYER, player.getName()));
    }

    public static void executeByConsole(CustomCommand customCommand, Player player) {
        customCommand.commands().forEach(command -> executeByConsole(command, player));
    }

    public static void executeMessage(String message, Player player) {
        var component = MiniMessage.miniMessage().deserialize(message);
        player.sendMessage(component);
    }

    public static void executeMessage(CustomCommand customCommand, Player player) {
        customCommand.commands().forEach(message -> executeMessage(message, player));
    }
}
