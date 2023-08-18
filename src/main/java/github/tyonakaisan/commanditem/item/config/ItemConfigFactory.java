package github.tyonakaisan.commanditem.item.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import github.tyonakaisan.commanditem.config.ConfigFactory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Singleton
@DefaultQualifier(NonNull.class)
public class ItemConfigFactory {

    private final Path dataDirectory;
    private final ConfigFactory configFactory;

    @Inject
    public ItemConfigFactory(
            final Path dataDirectory,
            final ConfigFactory configFactory
    ) {
        this.dataDirectory = dataDirectory;
        this.configFactory = configFactory;
    }

    public void createItemConfig(String fileName, ItemStack item) {
        try {
            this.load(fileName + ".conf", item);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public void load(final String fileName, ItemStack item) throws IOException {
        var itemFilePath = this.dataDirectory.resolve("items");

        if (!Files.exists(itemFilePath)) {
            Files.createDirectories(itemFilePath);
        }

        final Path file = itemFilePath.resolve(fileName);
        final var loader = this.configFactory.configurationLoader(file);

        final var root = loader.load();
        final ItemConfigTemplate config = Objects.requireNonNull(root.get(ItemConfigTemplate.class));

        if (!Files.exists(file)) {
            root.set(ItemConfigTemplate.class, config);
            loader.save(root);
        }

        config.setItem(item);
        config.setKey(fileName);
        config.setDisplayName(item.getItemMeta().displayName());
        config.setLore(item.getItemMeta().lore());

        root.set(ItemConfigTemplate.class, config);
        loader.save(root);
    }
}
