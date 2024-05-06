package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
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

    private final ItemRegistry itemRegistry;
    private final CoolTimeManager coolTimeManager;
    private final Convert convert;
    private final Messages messages;

    @Inject
    public CommandItemHandler(
            final ItemRegistry itemRegistry,
            final CoolTimeManager coolTimeManager,
            final Convert convert,
            final Messages messages
    ) {
        this.itemRegistry = itemRegistry;
        this.coolTimeManager = coolTimeManager;
        this.convert = convert;
        this.messages = messages;
    }

    public void itemUse(final Cancellable event, final Player player, final @Nullable ItemStack itemStack, final @Nullable EquipmentSlot hand, final Action.Item action) {
        final @Nullable Item item = this.itemRegistry.toItem(itemStack);

        if (item != null && itemStack != null) {
            var key = item.attributes().key();
            var timeLeft = this.coolTimeManager.getRemainingCoolTime(player.getUniqueId(), key);

            if (this.coolTimeManager.hasRemainingCoolTime(player.getUniqueId(), key)) {
                player.sendMessage(this.messages.translatable(
                        Messages.Style.ERROR,
                        player,
                        "cooltime.error.during_cool_time",
                        TagResolver.builder()
                                .tag("time", Tag.selfClosingInserting(Component.text(timeLeft.toSeconds() + 1)))
                                .build()));
                event.setCancelled(true);
                return;
            }

            this.convert.setPlayerHandItem(player, itemStack, hand, action);

            if (this.itemRegistry.isMaxUsesExceeded(itemStack, player)) {
                player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "commanditem.error.max_uses_exceeded"));
                return;
            }

            var commands = item.commands().getOrDefault(action, List.of());

            if (timeLeft.isZero() || timeLeft.isNegative()) {

                commands.forEach(command ->
                        command.repeatCommands(player, command.isConsole()));

                if (!commands.isEmpty()) {

                    this.coolTimeManager.removeAllCoolTime(player.getUniqueId(), key);
                    this.coolTimeManager.setCoolTime(player.getUniqueId(), key, Duration.ofSeconds(item.attributes().coolTime(player)));
                }
            }
        }
    }
}
