package pvp.simpleClan.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pvp.simpleClan.SimpleClan;

/**
 * Aktualizuje zapamiętane nicki i czyści dane tymczasowe po wyjściu gracza
 */
public class PlayerListener implements Listener {

    private final SimpleClan plugin;

    public PlayerListener(SimpleClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Nick zapamiętujemy dla członków klanów i graczy ze statystykami (np. po zmianie nicku)
        boolean inClan = plugin.getClanManager().hasPlayerClan(player.getUniqueId());
        plugin.getStatsManager().rememberName(player.getUniqueId(), player.getName(), inClan);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getChatManager().reset(player.getUniqueId());
        plugin.getConfirmationManager().clear(player);
    }
}
