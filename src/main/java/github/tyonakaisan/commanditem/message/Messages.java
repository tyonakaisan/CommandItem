package github.tyonakaisan.commanditem.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@DefaultQualifier(NonNull.class)
@Singleton
public final class Messages {

    private final ComponentLogger logger;
    private final Path messagesDir;

    private final Map<Locale, ResourceBundle> locales = new HashMap<>();
    private final Map<Locale, String> defaultLocales = Map.of(
            Locale.JAPAN, "messages_ja_JP",
            Locale.US, "messages_en_US"
    );
    private final Pattern pattern = Pattern.compile("messages_(.+)\\.properties");
    private static final String BUNDLE = "locale.messages";
    private static final String PREFIX = "<white>[<gradient:#005aa7:#fffde4>CommandItem</gradient>]</white>";

    private static final Key key = Key.key("command_item", "translation");
    private TranslationRegistry registry = TranslationRegistry.create(key);

    @Inject
    public Messages(
            final Path dataDirectory,
            final ComponentLogger logger
    ) {
        this.logger = logger;
        this.messagesDir = dataDirectory.resolve("locale");

        if (!Files.exists(this.messagesDir)) {
            try {
                Files.createDirectories(this.messagesDir);
            } catch (final IOException e) {
                this.logger.error(String.format("Failed to create directory %s", this.messagesDir), e);
            }
        }

        this.reloadMessage();
    }

    public void reloadMessage() {
        this.locales.clear();
        this.logger.info("Reloading locales...");
        this.loadMessageFile();
    }

    private void loadMessageFile() {
        // Create registry
        GlobalTranslator.translator().removeSource(this.registry);
        this.registry = TranslationRegistry.create(key);

        // Create supported locales
        this.createSupportedLocales(this.messagesDir);

        // Load messages_*.properties locale
        try (final Stream<Path> paths = Files.list(this.messagesDir)) {
            paths.filter(Files::isRegularFile)
                    .forEach(this::loadMatchFile);
        } catch (final IOException e) {
            this.logger.error("Failed to load locales.", e);
        }

        // Register translator
        GlobalTranslator.translator().addSource(this.registry);

        this.logger.info("Successfully {} locales loaded! {}", this.locales.keySet().size(), this.locales.keySet());
    }

    private void createSupportedLocales(final Path path) {
        for (final Map.Entry<Locale, String> localesEntry : this.defaultLocales.entrySet()) {
            final var locale = localesEntry.getKey();
            final var fileName = localesEntry.getValue();
            final var localePath = path.resolve(fileName + ".properties");

            if (!Files.exists(localePath)) {
                final var bundle = ResourceBundle.getBundle("locale." + fileName, locale, UTF8ResourceBundleControl.get());
                this.createProperties(localePath, bundle);
            }
        }
    }

    private void createProperties(final Path path, final ResourceBundle bundle) {
        final var properties = new Properties() {
            @Override
            public synchronized Set<Map.Entry<Object, Object>> entrySet() {
                return Collections.unmodifiableSet(
                        (Set<? extends Map.Entry<Object, Object>>) super.entrySet()
                                .stream()
                                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                                .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        };
        try (final Writer outputStream = Files.newBufferedWriter(path)) {
            properties.putAll(bundle.keySet().stream()
                    .collect(Collectors.toMap(key1 -> key1, bundle::getString)));
            properties.store(outputStream, null);
            this.logger.info("Successfully '{}' created!", path.getFileName());
        } catch (final IOException e) {
            this.logger.error("Failed to create '{}' locales.", bundle.getLocale(), e);
        }
    }

    private void loadMatchFile(final Path path) {
        final var matcher = this.pattern.matcher(path.getFileName().toString());
        if (matcher.matches()) {
            final @Nullable Locale locale = Translator.parseLocale(matcher.group(1));

            if (locale == null) {
                this.logger.warn("Invalid locales {}", path.getFileName());
            } else {
                this.load(locale, path);
            }
        }
    }

    private void load(final Locale locale, final Path path) {
        try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            final var bundle = new PropertyResourceBundle(reader);
            this.locales.put(locale, bundle);
            this.registry.registerAll(locale, bundle, false);
        } catch (final Exception e) {
            this.logger.error(String.format("Failed to load %s", path.getFileName()), e);
        }
    }

    // GlobalTranslator

    /**
     * Simple translation message that resolves prefix.
     * @param key the key
     * @param audience the audience
     * @return the translatable message or key if message not found
     */
    public static Component translate(final @PropertyKey(resourceBundle = BUNDLE) String key, final Audience audience) {
        return format(key, audience, TagResolver.empty());
    }

    /**
     * Simple translation message with {@link TagResolver} including prefix.
     * @param key the key
     * @param audience the audience
     * @param resolver the tagResolver
     * @return the translatable message or key if message not found
     */
    public static Component translate(final @PropertyKey(resourceBundle = BUNDLE) String key, final Audience audience, final Consumer<TagResolver.Builder> resolver) {
        final var tagResolver = TagResolver.builder();
        resolver.accept(tagResolver);
        return format(key, audience, tagResolver.build());
    }

    private static Component format(final @PropertyKey(resourceBundle = BUNDLE) String key, final Audience audience, final TagResolver tagResolver) {
        final var withPrefixTagResolver = TagResolver.builder()
                .tag("prefix", Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(PREFIX)))
                .resolver(tagResolver)
                .build();
        final var component = Component.empty()
                .decoration(TextDecoration.ITALIC, false);
        final @Nullable MessageFormat message = GlobalTranslator.translator().translate(key, audience.pointers().getOrDefault(Identity.LOCALE, Locale.US));

        return message != null
                ? component.append(MiniMessage.miniMessage().deserialize(message.toPattern(), withPrefixTagResolver))
                : component.append(Component.text(key));
    }
}
