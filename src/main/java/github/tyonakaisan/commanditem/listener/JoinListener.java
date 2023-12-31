package github.tyonakaisan.commanditem.listener;

import com.google.inject.Inject;
import github.tyonakaisan.commanditem.item.Convert;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

    private final Convert convert;

    @Inject
    public JoinListener(
            final Convert convert
    ) {
        this.convert = convert;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.convert.initInternalCoolTime(event.getPlayer().getUniqueId());
    }
}
