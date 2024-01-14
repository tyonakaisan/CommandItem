package github.tyonakaisan.commanditem.config.primary;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
@SuppressWarnings("FieldMayBeFinal")
public class PrimaryConfig {

    private CoolTimeSettings coolTime = new CoolTimeSettings();

    public CoolTimeSettings coolTime() {
        return this.coolTime;
    }
}
