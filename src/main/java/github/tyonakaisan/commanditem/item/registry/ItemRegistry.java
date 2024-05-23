package github.tyonakaisan.commanditem.item.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import github.tyonakaisan.commanditem.util.ItemUtils;
import github.tyonakaisan.commanditem.item.Item;
import github.tyonakaisan.commanditem.util.NamespacedKeyUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.intellij.lang.annotations.Subst;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("PatternValidation")
@DefaultQualifier(NonNull.class)
@Singleton
public final class ItemRegistry {

    private final Path itemConfigDir;
    private final ConfigFactory configFactory;
    private final ComponentLogger logger;

    private final ObjectMapper<Item> mapper;

    private final BiMap<Key, Item> itemMap = Maps.synchronizedBiMap(HashBiMap.create());

    @Inject
    public ItemRegistry(
            final Path dataDirectory,
            final ConfigFactory configFactory,
            final ComponentLogger logger
    ) throws SerializationException {
        this.itemConfigDir = dataDirectory.resolve("items");
        this.configFactory = configFactory;
        this.logger = logger;

        this.mapper = ObjectMapper.factory().get(Item.class);
        this.reloadItemConfig();
    }

    public void reloadItemConfig() {
        this.itemMap.clear();
        this.logger.info("Reloading items...");
        this.loadItemConfig();
    }

    public void createItemConfig(@Subst("value") final String fileName, final ItemStack itemStack) {
        if (!Files.exists(this.itemConfigDir)) {
            try {
                Files.createDirectories(this.itemConfigDir);
            } catch (final IOException e) {
                this.logger.error(String.format("Failed to create parent directories for '%s'", this.itemConfigDir), e);
                return;
            }
        }

        final var file = this.itemConfigDir.resolve(fileName + ".conf");
        final var loader = this.configFactory.configurationLoader(file);

        try {
            final var root = loader.load();
            @Subst("key")
            final var namespace = NamespacedKeyUtils.namespace();
            final Item config = ItemUtils.defaultItem(Key.key(namespace, fileName), itemStack);

            root.set(Item.class, config);
            loader.save(root);

            this.logger.info("Successfully {}.conf file created!", fileName);

        } catch (final ConfigurateException exception) {
            this.logger.error("Failed to create item config.", exception);
        }
    }

    private void loadItemConfig() {
        if (!Files.exists(this.itemConfigDir)) {
            try {
                Files.createDirectories(this.itemConfigDir);
            } catch (final IOException e) {
                this.logger.error(String.format("Failed to create parent directories for '%s'", this.itemConfigDir), e);
                return;
            }
        }

        try (final Stream<Path> paths = Files.walk(this.itemConfigDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".conf"))
                    .forEach(file -> {
                        final var fileName = file.getFileName().toString();
                        final var loader = this.configFactory.configurationLoader(file);
                        this.logger.info("Loading {}", fileName);

                        try {
                            final var loaded = loader.load();
                            loader.save(loaded);
                            final var item = this.mapper.load(loaded);
                            this.register(item.attributes().key(), item);
                        } catch (final ConfigurateException exception) {
                            this.logger.warn("Failed to load item '{}'", fileName, exception);
                        }
                    });
            this.logger.info("Successfully {} items loaded!", this.itemMap.keySet().size());
        } catch (final IOException e) {
            this.logger.error("Failed to load item.", e);
        }
    }

    public void register(final Key key, final Item value) {
        this.itemMap.put(key, value);
    }

    public @NonNull Set<Key> keys() {
        return Collections.unmodifiableSet(this.itemMap.keySet());
    }

    public @NonNull Set<Item> items() {
        return Collections.unmodifiableSet(this.itemMap.values());
    }

    public @Nullable Item item(final Key key) {
        return this.itemMap.get(key);
    }
}
