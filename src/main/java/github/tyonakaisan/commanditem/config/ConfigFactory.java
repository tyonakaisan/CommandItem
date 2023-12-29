package github.tyonakaisan.commanditem.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.config.primary.PrimaryConfig;
import github.tyonakaisan.commanditem.serialisation.*;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
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

    private final Path dataDirectory;
    private final ComponentLogger logger;

    private final ItemStackSerializerConfigurate itemStackSerializer;
    private final ConfigurationSerializableSerializerConfigurate configurationSerializableSerializer;
    private final EnchantmentSerializerConfigurate enchantmentSerializer;
    private final BannerPatternSerializerConfigurate bannerPatternSerializer;
    private final ColorSerializer colorSerializer;

    private @Nullable PrimaryConfig primaryConfig = null;

    @Inject
    public ConfigFactory(
            final Path dataDirectory,
            final ComponentLogger logger,
            final ItemStackSerializerConfigurate itemStackSerializer,
            final ConfigurationSerializableSerializerConfigurate configurationSerializableSerializer,
            final EnchantmentSerializerConfigurate enchantmentSerializer,
            final BannerPatternSerializerConfigurate bannerPatternSerializer,
            final ColorSerializer colorSerializer
            ) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.itemStackSerializer = itemStackSerializer;
        this.configurationSerializableSerializer = configurationSerializableSerializer;
        this.enchantmentSerializer = enchantmentSerializer;
        this.bannerPatternSerializer = bannerPatternSerializer;
        this.colorSerializer = colorSerializer;
    }

    public @Nullable PrimaryConfig reloadPrimaryConfig() {
        this.logger.info("Reloading configuration file...");
        try {
            this.primaryConfig = this.load(PrimaryConfig.class, "config.conf");
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return this.primaryConfig;
    }

    public @Nullable PrimaryConfig primaryConfig() {
        if (this.primaryConfig == null) {
            return this.reloadPrimaryConfig();
        }
        return this.primaryConfig;
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
                    final var kyoriSerializer =
                            ConfigurateComponentSerializer.configurate();

                    return opts.shouldCopyDefaults(true).serializers(serializerBuilder ->
                            serializerBuilder
                                    .registerAll(miniMessageSerializer.serializers())
                                    .registerAll(kyoriSerializer.serializers())
                                    .register(ItemStack.class, this.itemStackSerializer)
                                    .register(ConfigurationSerializable.class, this.configurationSerializableSerializer)
                                    .register(Pattern.class, this.bannerPatternSerializer)
                                    .register(EnchantmentSerializerConfigurate.Enchant.class, this.enchantmentSerializer)
                                    .register(Color.class, this.colorSerializer)
                    );
                })
                .path(file)
                .build();
    }

    public <T> @Nullable T load(final Class<T> clazz, final String fileName) throws IOException {
        if (!Files.exists(this.dataDirectory)) {
            Files.createDirectories(this.dataDirectory);
        }

        final Path file = this.dataDirectory.resolve(fileName);

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
            exception.printStackTrace();
            return null;
        }
    }
}
