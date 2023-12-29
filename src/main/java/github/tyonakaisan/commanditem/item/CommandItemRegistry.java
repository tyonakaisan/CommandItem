package github.tyonakaisan.commanditem.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.config.primary.CommandItemConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Singleton
@DefaultQualifier(NonNull.class)
public final class CommandItemRegistry implements Registry<Key, CommandsItem> {
    private final Path dataDirectory;
    private final ConfigFactory configFactory;
    private final ComponentLogger logger;

    private static @MonotonicNonNull ObjectMapper<CommandsItem> mapper;

    static {
        try {
            mapper = ObjectMapper.factory().get(CommandsItem.class);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    private final BiMap<Key, CommandsItem> registeredItemMap = Maps.synchronizedBiMap(HashBiMap.create());

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
        this.logger.info("Reloading items...");
        this.loadItemConfig();
    }

    public void createItemConfig(final String fileName, final ItemStack itemStack) throws IOException {
        var itemFilePath = this.dataDirectory.resolve("items");

        if (!Files.exists(itemFilePath)) {
            Files.createDirectories(itemFilePath);
        }

        final var file = itemFilePath.resolve(fileName + ".conf");
        final var loader = this.configFactory.configurationLoader(file);

        try {
            final var root = loader.load();
            final CommandItemConfig config = Objects.requireNonNull(root.get(CommandItemConfig.class));

            config.setKey(fileName);
            config.setItemStack(itemStack);

            root.set(CommandItemConfig.class, config);
            loader.save(root);

            this.logger.info("Successfully {}.conf file created!", fileName);

        } catch (final ConfigurateException exception) {
            exception.printStackTrace();
        }

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
                        final @Nullable CommandsItem item = this.registerItemFromPath(itemFile);

                        if (item == null) {
                            this.logger.warn("Failed to load file {}", fileName);
                        }
                    });
                this.logger.info("Successfully {} items loaded!", this.registeredItemMap.keySet().size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private @Nullable CommandsItem registerItemFromPath(final Path path) {
        final @Nullable CommandsItem item = this.loadCommandItem(path);

        if (item == null) {
            return null;
        }

        this.register(item.key(), item);

        return item;
    }

    @Nullable
    private CommandsItem loadCommandItem(final Path itemFile) {
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
    public @NonNull CommandsItem register(final @NonNull Key key, final @NonNull CommandsItem value) {
        this.registeredItemMap.put(key, value);
        return value;
    }

    @Override
    public @Nullable CommandsItem get(final @NonNull Key key) {
        return this.registeredItemMap.get(key);
    }

    @Override
    public @Nullable Key key(final @NonNull CommandsItem value) {
        return this.registeredItemMap.inverse().get(value);
    }

    @Override
    public @NonNull Set<Key> keySet() {
        return Collections.unmodifiableSet(this.registeredItemMap.keySet());
    }

    @Override
    public @NonNull Set<CommandsItem> valueSet() {
        return Collections.unmodifiableSet(this.registeredItemMap.values());
    }

    @Override
    public @NonNull Iterator<CommandsItem> iterator() {
        return Iterators.unmodifiableIterator(this.registeredItemMap.values().iterator());
    }
}
