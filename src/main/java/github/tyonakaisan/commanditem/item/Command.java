package github.tyonakaisan.commanditem.item;

import github.tyonakaisan.commanditem.CommandItemProvider;
import github.tyonakaisan.commanditem.util.PlaceholderUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public record Command(
        Action.Command type,
        List<String> commands,
        boolean isConsole,
        String repeat,
        String period,
        String delay,
        String runWeight
) {
    public static Command defaultCreate() {
        return new Command(Action.Command.COMMAND, List.of(), true, "1", "1", "0", "1");
    }

    public List<String> commands(final Player player) {
        return this.commands.stream()
                .map(text -> PlaceholderUtils.getPlainText(player, text))
                .toList();
    }

    public List<Component> messages(final Player player) {
        return this.commands.stream()
                .map(text -> PlaceholderUtils.getComponent(player, text))
                .toList();
    }

    public int repeat(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.repeat);
    }

    public int period(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.period);
    }

    public int delay(final Player player) {
        return (int) PlaceholderUtils.calculate(player, this.delay);
    }

    public double runWeight(final Player player) {
        return PlaceholderUtils.calculate(player, this.runWeight);
    }

    public void repeatCommands(final Player player, boolean console) {
        if (this.repeat(player) == 0) {
            return;
        }

        var period = this.period(player);
        var commandItem = CommandItemProvider.instance();

        // periodが-1以下の場合はfor文
        if (period <= -1) {
            commandItem.getServer().getScheduler().runTaskLater(commandItem, () -> {
                for (int i = 0; i < this.repeat(player); i++) {
                    new CommandTask(this, player, console).run();
                }
            }, this.delay(player));
        } else {
            new CommandTask(this, player, console).runTaskTimer(commandItem, this.delay(player), period);
        }
    }
}
