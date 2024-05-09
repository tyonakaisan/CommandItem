package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
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

    public void itemUse(final Cancellable event, final Player player, final @Nullable ItemStack itemStack, final @Nullable EquipmentSlot hand, final Action.Item action) {
        final @Nullable Item item = this.itemRegistry.toItem(itemStack);

        if (item != null && itemStack != null) {
            var key = item.attributes().key();
            var timeLeft = this.coolTimeManager.getRemainingCoolTime(player.getUniqueId(), key);

            if (this.coolTimeManager.hasRemainingCoolTime(player.getUniqueId(), key) && item.attributes().hideCoolTimeAnnounce()) {
                this.sendCoolTimeMessage(player, itemStack, timeLeft);
                event.setCancelled(true);
                return;
            }

            if (this.itemRegistry.isMaxUsesExceeded(itemStack, player)) {
                player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "commanditem.error.max_uses_exceeded"));
                return;
            }

            this.convert.setPlayerHandItem(player, itemStack, hand, action);

            if (timeLeft.isZero() || timeLeft.isNegative()) {
                item.runRandomCommands(player, action);
                if (!item.commands().getOrDefault(action, List.of()).isEmpty()) {
                    this.coolTimeManager.removeAllCoolTime(player.getUniqueId(), key);
                    this.coolTimeManager.setCoolTime(player.getUniqueId(), key, Duration.ofSeconds(item.attributes().coolTime(player)));
                }
            }
        }
    }

    public void sendCoolTimeMessage(final Player player, final ItemStack itemStack, final Duration duration) {
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
}
