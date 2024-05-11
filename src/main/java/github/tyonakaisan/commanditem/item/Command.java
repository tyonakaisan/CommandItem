package github.tyonakaisan.commanditem.item;

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
}
