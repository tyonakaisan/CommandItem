package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItemProvider;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.message.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.List;

@DefaultQualifier(NonNull.class)
public final class CommandItemHandler {

    private final ConfigFactory configFactory;
    private final ItemRegistry itemRegistry;
    private final CoolTimeManager coolTimeManager;
    private final Convert convert;
    private final Messages messages;

    @Inject
    public CommandItemHandler(
            final ConfigFactory configFactory,
            final ItemRegistry itemRegistry,
            final CoolTimeManager coolTimeManager,
            final Convert convert,
            final Messages messages
    ) {
        this.configFactory = configFactory;
        this.itemRegistry = itemRegistry;
        this.coolTimeManager = coolTimeManager;
        this.convert = convert;
        this.messages = messages;
    }

    public void itemUse(final @Nullable ItemStack itemStack, final Player player, final Action.Item action, final @Nullable EquipmentSlot hand, final Cancellable event) {
        final @Nullable Item item = this.itemRegistry.toItem(itemStack);

        if (item != null && itemStack != null) {
            var key = item.attributes().key();
            var timeLeft = this.coolTimeManager.getRemainingCoolTime(player.getUniqueId(), key);

            if (this.coolTimeManager.hasRemainingCoolTime(player.getUniqueId(), key) && item.attributes().hideCoolTimeAnnounce()) {
                this.sendCoolTimeMessage(itemStack, player, timeLeft);
                event.setCancelled(true);
                return;
            }

            if (this.itemRegistry.isMaxUsesExceeded(itemStack, player)) {
                player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "commanditem.error.max_uses_exceeded"));
                return;
            }

            this.convert.setPlayerHandItem(itemStack, player, action, hand);

            if (timeLeft.isZero() || timeLeft.isNegative()) {
                this.runRandomCommands(item, player, action);
                if (!item.commands().getOrDefault(action, List.of()).isEmpty()) {
                    this.coolTimeManager.removeAllCoolTime(player.getUniqueId(), key);
                    this.coolTimeManager.setCoolTime(player.getUniqueId(), key, Duration.ofSeconds(item.attributes().coolTime(player)));
                }
            }
        }
    }

    private void sendCoolTimeMessage(final ItemStack itemStack, final Player player, final Duration duration) {
        var type = this.configFactory.primaryConfig().coolTime().coolTimeAlertType();

        switch (type) {
            case CHAT -> player.sendMessage(this.messages.translatable(
                    Messages.Style.ERROR,
                    player,
                    "cooltime.error.during_cool_time",
                    TagResolver.builder()
                            .tag("time", Tag.selfClosingInserting(Component.text(duration.toSeconds() + 1)))
                            .build()));
            case ACTION_BAR -> player.sendActionBar(this.messages.translatable(
                    Messages.Style.ERROR,
                    player,
                    "cooltime.error.during_cool_time",
                    TagResolver.builder()
                            .tag("time", Tag.selfClosingInserting(Component.text(duration.toSeconds() + 1)))
                            .build()));
            case VANILLA -> player.setCooldown(itemStack.getType(), (int) duration.toSeconds());
        }
    }

    private void runRandomCommands(final Item item, final Player player, final Action.Item action) {
        if (item.commands().getOrDefault(action, List.of()).isEmpty()) {
            return;
        }

        var max = this.convert.pickCommands(item, player, action);
        var weightedCommands = this.convert.weightedCommands(item, player, action);
        if (weightedCommands.size() == 0 || max == 0 || item.attributes().maxUses(player) == 0) {
            return;
        }

        if (max <= -1) {
            item.commands().get(action).forEach(command -> this.repeatCommands(command, player));
        } else {
            for (int i = 0; i < max; i++) {
                var command = weightedCommands.select();
                this.repeatCommands(command, player);
            }
        }
    }

    private void repeatCommands(final Command command, final Player player) {
        if (command.repeat(player) == 0) {
            return;
        }

        var period = command.period(player);
        var console = command.isConsole();
        var commandItem = CommandItemProvider.instance();

        // periodが-1以下の場合はfor文
        if (period <= -1) {
            commandItem.getServer().getScheduler().runTaskLater(commandItem, () -> {
                for (int i = 0; i < command.repeat(player); i++) {
                    new CommandTask(command, player, console).run();
                }
            }, command.delay(player));
        } else {
            new CommandTask(command, player, console).runTaskTimer(commandItem, command.delay(player), period);
        }
    }
}
