package github.tyonakaisan.commanditem.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.item.registry.Registry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CommandItemRegistry implements Registry<Key, ItemRecord> {
    private final Path dataDirectory;
    private final ConfigFactory configFactory;
    private final ComponentLogger logger;

    private static @MonotonicNonNull ObjectMapper<ItemRecord> mapper;

    static {
        try {
            mapper = ObjectMapper.factory().get(ItemRecord.class);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    private final BiMap<Key, ItemRecord> registeredItemMap = Maps.synchronizedBiMap(HashBiMap.create());

    @Inject
    CommandItemRegistry(
            final Path dataDirectory,
            final ConfigFactory configFactory,
            final ComponentLogger logger
    ) {
        this.dataDirectory = dataDirectory;
        this.configFactory = configFactory;
        this.logger = logger;
    }

    public void reloadItemConfig() throws IOException {
        this.registeredItemMap.clear();
        this.loadItemConfig();
    }

    public void loadItemConfig() throws IOException {
        var itemFilePath = this.dataDirectory.resolve("items");

        if (!Files.exists(itemFilePath)) {
            Files.createDirectories(itemFilePath);
        }

        try (Stream<Path> paths = Files.walk(itemFilePath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".conf"))
                    .forEach(itemFile -> {
                        final var fileName = itemFile.getFileName().toString();
                        final @Nullable ItemRecord itemRecord = this.registerItemFromPath(itemFile);
                        this.logger.info("Loading {}", fileName);

                        if (itemRecord == null) {
                            this.logger.warn("Failed to load file '{}'", fileName);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private @Nullable ItemRecord registerItemFromPath(final Path path) {
        final @Nullable ItemRecord itemRecord = this.loadCommandItem(path);

        if (itemRecord == null) {
            return null;
        }

        this.register(itemRecord.key(), itemRecord);

        return itemRecord;
    }

    @Nullable
    private ItemRecord loadCommandItem(final Path itemFile) {
        final ConfigurationLoader<?> loader = this.configFactory.configurationLoader(itemFile);

        try {
            final var loaded = loader.load();
            loader.save(loaded);
            return mapper.load(loaded);
        } catch (final ConfigurateException exception) {
            this.logger.warn("Failed to load file '{}'", itemFile, exception);
        }

        return null;
    }

    @Override
    public @NonNull ItemRecord register(final @NonNull Key key, final @NonNull ItemRecord value) {
        this.registeredItemMap.put(key, value);
        return value;
    }

    @Override
    public @Nullable ItemRecord get(final @NonNull Key key) {
        return this.registeredItemMap.get(key);
    }

    @Override
    public @Nullable Key key(final @NonNull ItemRecord value) {
        return this.registeredItemMap.inverse().get(value);
    }

    @Override
    public @NonNull Set<Key> keySet() {
        return Collections.unmodifiableSet(this.registeredItemMap.keySet());
    }

    @Override
    public @NonNull Set<ItemRecord> valueSet() {
        return Collections.unmodifiableSet(this.registeredItemMap.values());
    }

    @Override
    public @NonNull Iterator<ItemRecord> iterator() {
        return Iterators.unmodifiableIterator(this.registeredItemMap.values().iterator());
    }
}
