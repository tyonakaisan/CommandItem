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
            CHAT: Send a message to chat.
            ACTION_BAR: Send a message to action bar""")
    private AlertType coolTimeAlertType = AlertType.ACTION_BAR;

    public AlertType coolTimeAlertType() {
        return this.coolTimeAlertType;
    }

    public enum AlertType {
        VANILLA,
        CHAT,
        ACTION_BAR
    }
}
