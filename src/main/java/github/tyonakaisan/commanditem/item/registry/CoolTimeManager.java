package github.tyonakaisan.commanditem.item.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.util.Pair;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@DefaultQualifier(NonNull.class)
@Singleton
public final class CoolTimeManager {
    private final List<Pair<UUID, CoolTime>> coolTimes = new ArrayList<>();

    @Inject
    public CoolTimeManager() {
        // Empty
    }

    public void setCoolTime(final UUID uuid, final Key key, final Duration duration) {
        this.coolTimes.add(Pair.of(uuid, new CoolTime(key, Instant.now().plus(duration))));
    }

    public boolean hasCoolTime(final UUID uuid, final Key key) {
        return this.coolTimes.stream()
                .anyMatch(coolTime -> coolTime.first().equals(uuid) && coolTime.second().key.equals(key));
    }

    public boolean hasRemainingCoolTime(final UUID uuid, final Key key) {
        if (!this.hasCoolTime(uuid, key)) {
            return false;
        }

        final var oldestTime = this.getOldestCoolTime(uuid, key);

        if (oldestTime.isPresent()) {
            final var instant = oldestTime.get().instant;
            final var key1 = oldestTime.get().key;
            final var now = Instant.now();

            return now.isBefore(instant) && key1.equals(key);
        } else {
            return false;
        }
    }

    public void removeAllCoolTime(final UUID uuid, final Key key) {
        final var removes = this.coolTimes.stream()
                .filter(coolTime -> this.hasCoolTime(uuid, key))
                .toList();
        this.coolTimes.removeAll(removes);
    }

    public Duration getRemainingCoolTime(final UUID uuid, final Key key) {
        final var oldestTime = this.getOldestCoolTime(uuid, key);
        if (oldestTime.isPresent()) {
            final var instant = oldestTime.get().instant;
            final var key1 = oldestTime.get().key;
            final var now = Instant.now();

            return (now.isBefore(instant) && key1.equals(key))
                    ? Duration.between(now, instant)
                    : Duration.ZERO;
        } else {
            return Duration.ZERO;
        }
    }

    public Optional<CoolTime> getOldestCoolTime(final UUID uuid, final Key key) {
        return this.coolTimes.stream()
                .filter(coolTime -> this.hasCoolTime(uuid, key))
                .map(Pair::second)
                .min(Comparator.comparing(pair -> pair.instant));
    }

    public record CoolTime(
            Key key,
            Instant instant
    ) {

    }
}