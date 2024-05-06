package github.tyonakaisan.commanditem.config.primary;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
@SuppressWarnings("FieldMayBeFinal")
public final class CoolTimeSettings {

    @Comment("""
            Alert type during cool time.
            "vanilla" applies to all items of the same type.
            
            VANILLA: Vanilla specification alert (animation after use of ender pearls).
            MESSAGE: Send a message to chat.""")
    private AlertType coolTimeAlertType = AlertType.MESSAGE;

    public AlertType coolTimeAlertType() {
        return this.coolTimeAlertType;
    }

    public enum AlertType {
        VANILLA,
        MESSAGE
    }
}
