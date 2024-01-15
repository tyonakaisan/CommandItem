package github.tyonakaisan.commanditem.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.PropertyKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@DefaultQualifier(NonNull.class)
@Singleton
public final class MessageManager {

    private final Path dataDirectory;
    private final ComponentLogger logger;

    private final Map<Locale, ResourceBundle> locales = new HashMap<>();
    private final Pattern pattern = Pattern.compile("messages_(.+)\\.properties");
    private static final String BUNDLE = "locale.messages";

    @Inject
    public MessageManager(
            final Path dataDirectory,
            final ComponentLogger logger
    ) throws IOException {
        this.dataDirectory = dataDirectory;
        this.logger = logger;

        this.reloadMessageFile();
    }

    public void reloadMessageFile() throws IOException {
        this.locales.clear();
        this.logger.info("Reloading locales...");
        this.loadMessageFile();
    }

    public void loadMessageFile() throws IOException {
        var path = this.dataDirectory.resolve("locale");

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        // Load default locale
        var defaultLocalePath = path.resolve("messages.properties");
        if (!Files.exists(defaultLocalePath)) {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, Locale.US, UTF8ResourceBundleControl.get());

            // Create default locale
            Properties properties = new Properties();
            bundle.keySet().forEach(key -> properties.setProperty(key, bundle.getString(key)));
            try (Writer outputStream = Files.newBufferedWriter(defaultLocalePath)) {
                properties.store(outputStream, null);
            }

            this.logger.info("Create a new messages.properties because it does not exist or is incorrectly named.");
        }

        this.load(Locale.US, defaultLocalePath);

        // Load messages_*.properties locale
        try (Stream<Path> paths = Files.list(path)) {
            paths.filter(Files::isRegularFile)
                    .forEach(this::loadMatchFile);
        } catch (IOException e) {
            this.logger.error("Failed to load locales.", e);
        }

        this.logger.info("Successfully {} locales loaded! {}", this.locales.keySet().size(), this.locales.keySet());
    }

    public void loadMatchFile(final Path path) {
        var matcher = this.pattern.matcher(path.getFileName().toString());
        if (matcher.matches()) {
            @Nullable Locale locale = Translator.parseLocale(matcher.group(1));

            if (locale == null) {
                this.logger.warn("Invalid locales {}", path.getFileName());
            } else {
                this.load(locale, path);
            }
        }
    }

    public void load(final Locale locale, final Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            this.locales.put(locale, new PropertyResourceBundle(reader));
        } catch (Exception e) {
            this.logger.error(String.format("Failed to load %s", path.getFileName()), e);
        }
    }

    public Component translatable(final Style style, final Audience audience, @PropertyKey(resourceBundle = BUNDLE) final String key) {
        return this.translatable(style, audience, key, TagResolver.empty());
    }

    public Component translatable(final Style style, final Audience audience, @PropertyKey(resourceBundle = BUNDLE) final String key, TagResolver tagResolver) {
        return audience instanceof Player player
                ? this.forPlayer(style, player, key, tagResolver)
                : this.forAudience(style, key, tagResolver);
    }

    private Component forPlayer(final Style style, final Player player, @PropertyKey(resourceBundle = BUNDLE) final String key, TagResolver tagResolver) {
        final var component = Component.empty()
                .color(TextColor.fromCSSHexString(style.hex()))
                .decoration(TextDecoration.ITALIC, false);
        final @Nullable ResourceBundle resource = this.locales.get(player.locale());

        // localeはあるけどkeyがない場合
        if (resource != null && !resource.keySet().contains(key)) {
            return this.forAudience(style, key, tagResolver);
        }

        return resource != null
                ? component.append(MiniMessage.miniMessage().deserialize(resource.getString(key), tagResolver))
                : this.forAudience(style, key, tagResolver);
    }

    private Component forAudience(final Style style, @PropertyKey(resourceBundle = BUNDLE) final String key, TagResolver tagResolver) {
        final var component = Component.empty()
                .color(TextColor.fromCSSHexString(style.hex()))
                .decoration(TextDecoration.ITALIC, false);
        final var resource = this.locales.get(Locale.US);

        // keyがない場合
        if (!resource.keySet().contains(key)) {
            var bundle = ResourceBundle.getBundle(BUNDLE, Locale.US, UTF8ResourceBundleControl.get());
            this.logger.warn("Message retrieved from resource bundle because '{}' does not exist in messages.properties.", key);
            return component.append(MiniMessage.miniMessage().deserialize("<hover:show_text:'<red>This message is taken from the resource bundle'>" + bundle.getString(key), tagResolver));
        }

        return component.append(MiniMessage.miniMessage().deserialize(resource.getString(key), tagResolver));
    }

    public enum Style {
        SUCCESS("#59ffa4"),
        ERROR("#ff4775"),
        INFO("#ffffff");

        private final String hex;

        Style(final String hex) {
            this.hex = hex;
        }

        public String hex() {
            return this.hex;
        }
    }
}
