package github.tyonakaisan.commanditem.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.config.primary.PrimaryConfig;
import github.tyonakaisan.commanditem.config.serialisation.ItemStackSerializer;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
@DefaultQualifier(NonNull.class)
public class ConfigFactory {

    private static final String PRIMARY_CONFIG_FILE_NAME = "config.conf";

    private final Path dataDirectory;
    private final ComponentLogger logger;

    private final ItemStackSerializer itemStackSerializer;

    private @MonotonicNonNull PrimaryConfig primaryConfig = null;

    @Inject
    public ConfigFactory(
            final Path dataDirectory,
            final ComponentLogger logger,
            final ItemStackSerializer itemStackSerializer
    ) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.itemStackSerializer = itemStackSerializer;

        this.reloadPrimaryConfig();
    }

    public PrimaryConfig reloadPrimaryConfig() {
        this.logger.info("Reloading configuration...");
        final @Nullable PrimaryConfig load = this.load(PrimaryConfig.class, PRIMARY_CONFIG_FILE_NAME);
        if (load != null) {
            this.primaryConfig = load;
        } else {
            this.logger.error("Failed to reload primary config, see above for further details");
        }

        return this.primaryConfig;
    }

    public PrimaryConfig primaryConfig() {
        return this.primaryConfig != null ? this.primaryConfig : this.reloadPrimaryConfig();
    }

    public ConfigurationLoader<?> configurationLoader(final Path file) {
        return HoconConfigurationLoader.builder()
                .prettyPrinting(true)
                .defaultOptions(opts -> {
                    final var miniMessageSerializer =
                            ConfigurateComponentSerializer.builder()
                                    .scalarSerializer(MiniMessage.miniMessage())
                                    .outputStringComponents(true)
                                    .build();
                    final var componentSerializer =
                            ConfigurateComponentSerializer.configurate();

                    return opts.shouldCopyDefaults(true).serializers(serializerBuilder ->
                            serializerBuilder
                                    .registerAll(miniMessageSerializer.serializers())
                                    .registerAll(componentSerializer.serializers())
                                    .register(ItemStack.class, this.itemStackSerializer)
                    );
                })
                .path(file)
                .build();
    }

    public <T> @Nullable T load(final Class<T> clazz, final String fileName) {
        final Path file = this.dataDirectory.resolve(fileName);
        if (!Files.exists(this.dataDirectory)) {
            try {
                Files.createDirectories(this.dataDirectory);
            } catch (final IOException exception) {
                this.logger.error("Failed to create parent directories for '{}'", file, exception);
                return null;
            }
        }

        final var loader = this.configurationLoader(file);
        try {
            final var root = loader.load();
            final @Nullable T config = root.get(clazz);

            if (!Files.exists(file)) {
                root.set(clazz, config);
                loader.save(root);
            }

            this.logger.info("Successfully configuration file loaded!");
            return config;
        } catch (final ConfigurateException exception) {
            this.logger.error("Failed to load config '{}'", file, exception);
            return null;
        }
    }
}
