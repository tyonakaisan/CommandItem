package github.tyonakaisan.commanditem;

import com.google.inject.AbstractModule;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.nio.file.Path;

@DefaultQualifier(NonNull.class)
public final class BootstrapModule extends AbstractModule {

    private final BootstrapContext context;

    BootstrapModule(
            final BootstrapContext context
    ) {
        this.context = context;
    }

    @Override
    public void configure() {
        this.bind(ComponentLogger.class).toInstance(this.context.getLogger());
        this.bind(Path.class).toInstance(this.context.getDataDirectory());
    }
}
