package github.tyonakaisan.commanditem.item;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@DefaultQualifier(NonNull.class)
@Singleton
public final class ItemCoolTimeManager {

    private final Map<UUID, List<CoolTime>> coolTimeMap = new HashMap<>();

    @Inject
    public ItemCoolTimeManager() {
        // Empty
    }

    public void setItemCoolTime(UUID uuid, Key itemKey, Duration duration) {
        this.coolTimeMap.computeIfAbsent(uuid, v -> new ArrayList<>()).add(new CoolTime(itemKey, Instant.now().plus(duration)));
    }

    public boolean hasItemCoolTime(UUID uuid, Key key) {
        if (this.coolTimeMap.get(uuid) != null) {
            var coolTimeOpt = this.coolTimeMap.get(uuid).stream()
                    .filter(coolTime -> coolTime.usedItemKey().equals(key))
                    .findFirst();
            if (coolTimeOpt.isPresent()) {
                var coolTime = coolTimeOpt.get().coolTime();
                var itemKey = coolTimeOpt.get().usedItemKey();
                var now = Instant.now();

                return now.isBefore(coolTime) && itemKey.equals(key);
            }
        }
        return false;
    }

    public void removeItemCoolTime(UUID uuid, Key key) {
        if (this.coolTimeMap.get(uuid) != null) {
            var coolTimeList = this.coolTimeMap.get(uuid).stream()
                    .filter(coolTime -> coolTime.usedItemKey().equals(key))
                    .toList();

            this.coolTimeMap.get(uuid).removeAll(coolTimeList);
        }
    }

    public Duration getRemainingItemCoolTime(UUID uuid, Key key) {
        if (this.coolTimeMap.get(uuid) != null) {
            var coolTimeOpt = this.coolTimeMap.get(uuid).stream()
                    .filter(coolTime -> coolTime.usedItemKey().equals(key))
                    .findFirst();
            if (coolTimeOpt.isPresent()) {
                var coolTime = coolTimeOpt.get().coolTime();
                var itemKey = coolTimeOpt.get().usedItemKey();
                var now = Instant.now();

                return (now.isBefore(coolTime) && itemKey.equals(key))
                        ? Duration.between(now, coolTime)
                        : Duration.ZERO;
            }
        }
        return Duration.ZERO;
    }

    public record CoolTime(
            Key usedItemKey,
            Instant coolTime
    ) {

    }
}
