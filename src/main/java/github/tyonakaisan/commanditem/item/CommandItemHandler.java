package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.CommandItemProvider;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.registry.CoolTimeManager;
import github.tyonakaisan.commanditem.item.registry.ItemManager;
import github.tyonakaisan.commanditem.item.registry.ItemRegistry;
import github.tyonakaisan.commanditem.message.Messages;
import github.tyonakaisan.commanditem.util.ItemUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@DefaultQualifier(NonNull.class)
public final class CommandItemHandler {

    private final ConfigFactory configFactory;
    private final ItemRegistry itemRegistry;
    private final ItemManager itemManager;
    private final CoolTimeManager coolTimeManager;
    private final Messages messages;
    private final ComponentLogger logger;

    @Inject
    public CommandItemHandler(
            final ConfigFactory configFactory,
            final ItemRegistry itemRegistry,
            final ItemManager itemManager,
            final CoolTimeManager coolTimeManager,
            final Messages messages,
            final ComponentLogger logger
            ) {
        this.configFactory = configFactory;
        this.itemRegistry = itemRegistry;
        this.itemManager = itemManager;
        this.coolTimeManager = coolTimeManager;
        this.messages = messages;
        this.logger = logger;
    }

    public void canPlaceItem(final @Nullable ItemStack itemStack, final Cancellable event) {
        final @Nullable Item item = this.itemManager.toItem(itemStack);
        if (item != null && item.attributes().placeable()) {
            event.setCancelled(true);
        }
    }

    public void itemUse(final @Nullable ItemStack itemStack, final Player player, final Action.Item action, final @Nullable EquipmentSlot hand, final Cancellable event) {
        final @Nullable Item item = this.itemManager.toItem(itemStack);

        if (item != null && itemStack != null && item.commands().containsKey(action)) {
            final var key = item.attributes().key();
            final var timeLeft = this.coolTimeManager.getRemainingCoolTime(player.getUniqueId(), key);

            if (action.isPlaceCancellable()) {
                event.setCancelled(!item.attributes().placeable());
            }

            if (this.coolTimeManager.hasRemainingCoolTime(player.getUniqueId(), key)) {
                this.sendCoolTimeMessage(item, player, timeLeft);
                event.setCancelled(true);
                return;
            }

            if (this.itemManager.isMaxUsesExceeded(itemStack, player)) {
                player.sendMessage(this.messages.translatable(Messages.Style.ERROR, player, "commanditem.error.max_uses_exceeded"));
                return;
            }

            this.itemManager.setPlayerHandItem(itemStack, player, action, hand);

            if (timeLeft.isZero() || timeLeft.isNegative()) {
                this.runRandomCommands(item, player, action);
                if (!item.commands().getOrDefault(action, List.of()).isEmpty()) {
                    this.coolTimeManager.removeAllCoolTime(player.getUniqueId(), key);
                    this.coolTimeManager.setCoolTime(player.getUniqueId(), key, Duration.ofSeconds(item.attributes().coolTime(player)));
                }
            }
        }
    }

    private void sendCoolTimeMessage(final Item item, final Player player, final Duration duration) {
        final var type = this.configFactory.primaryConfig().coolTime().coolTimeAlertType();

        if (item.attributes().hideCoolTimeAnnounce()) {
            return;
        }

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
            case VANILLA -> player.setCooldown(item.rawItemStack().getType(), (int) duration.toSeconds());
        }
    }

    private void runRandomCommands(final Item item, final Player player, final Action.Item action) {
        if (item.commands().getOrDefault(action, List.of()).isEmpty()) {
            return;
        }

        final var picks = ItemUtils.picks(item, player, action);
        final var weightedCommands = ItemUtils.weightedCommands(item, player, action);
        if (weightedCommands.size() == 0 || picks == 0 || item.attributes().maxUses(player) == 0) {
            return;
        }

        if (picks <= -1) {
            item.commands().get(action).forEach(command -> this.repeatCommands(command, player));
        } else {
            for (int i = 0; i < picks; i++) {
                final var command = weightedCommands.select();
                this.repeatCommands(command, player);
            }
        }
    }

    private void repeatCommands(final Command command, final Player player) {
        if (command.repeat(player) == 0) {
            return;
        }

        final var period = command.period(player);
        final var console = command.isConsole();
        final var commandItem = CommandItemProvider.instance();

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

    public void giveItem(final Collection<Player> targets, final Audience audience, final Key key, final int count) {
        final @Nullable Item item = this.itemRegistry.item(key);
        if (item == null) {
            audience.sendMessage(this.messages.translatable(
                    Messages.Style.ERROR,
                    audience,
                    "command.give.error.unknown_item",
                    TagResolver.builder()
                            .tag("item", Tag.selfClosingInserting(Component.text(key.asString())))
                            .build()));
            return;
        }

        targets.forEach(target -> {
            final ItemStack itemStack = this.itemManager.toItemStack(item, target);

            if (this.isMaxStackSize(audience, itemStack, count)) {
                return;
            }

            this.addItem(target, itemStack, count, item.attributes().stackable());

            // Logging to the console
            if (!(audience instanceof ConsoleCommandSender)) {
                this.logger.info("Gave {} {} to {}.", count, PlainTextComponentSerializer.plainText().serialize(itemStack.displayName()), target.displayName());
            }

            audience.sendMessage(this.messages.translatable(
                    Messages.Style.INFO,
                    audience,
                    "command.give.info.give",
                    TagResolver.builder()
                            .tag("player", Tag.selfClosingInserting(target.displayName()))
                            .tag("item", Tag.selfClosingInserting(itemStack.displayName()))
                            .tag("count", Tag.selfClosingInserting(Component.text(count)))
                            .build()));
            target.playSound(Sound.sound()
                    .type(Key.key("minecraft:entity.item.pickup"))
                    .volume(0.3f)
                    .pitch(2f)
                    .build());
        });
    }

    private void addItem(final Player target, final ItemStack itemStack, final int count, final boolean stackable) {
        final var maxStackSize = itemStack.getMaxStackSize();

        if (!stackable || maxStackSize == 1) {
            for (int i = 0; i < count; i++) {
                target.getInventory().addItem(itemStack);
            }
        } else {
            final var cloneItemStack = itemStack.clone();
            final var fullStacks = count / maxStackSize;
            final var remainder = count % maxStackSize;

            if (fullStacks > 0) {
                cloneItemStack.setAmount(maxStackSize);
                for (int i = 0; i < fullStacks; i++) {
                    target.getInventory().addItem(cloneItemStack);
                }

                if (remainder > 0) {
                    cloneItemStack.setAmount(remainder);
                    target.getInventory().addItem(cloneItemStack);
                }
            } else {
                cloneItemStack.setAmount(count);
                target.getInventory().addItem(cloneItemStack);
            }
        }
    }

    private boolean isMaxStackSize(final Audience audience, final ItemStack itemStack, final int count) {
        final var maxReceive = itemStack.getMaxStackSize() * 36;

        if (count > maxReceive) {
            audience.sendMessage(this.messages.translatable(
                    Messages.Style.ERROR,
                    audience,
                    "command.give.error.max_count",
                    TagResolver.builder()
                            .tag("max", Tag.selfClosingInserting(Component.text(maxReceive)))
                            .tag("item", Tag.selfClosingInserting(itemStack.displayName()))
                            .build()));
            return true;
        }
        return false;
    }
}
