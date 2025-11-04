package pvp.simpleClan.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Obsługuje zabójstwa i tracking statystyk
 */
public class KillListener implements Listener {

    private final SimpleClan plugin;
    private final ClanManager clanManager;

    public KillListener(SimpleClan plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Sprawdź czy zabójca to gracz
        if (killer == null) {
            return;
        }

        // Sprawdź czy zabójca jest w klanie
        Clan killerClan = clanManager.getPlayerClan(killer.getUniqueId());
        if (killerClan == null) {
            return;
        }

        // Dodaj zabójstwo do klanu
        killerClan.addKill();
        clanManager.saveData();

        // Sprawdź czy broadcastować zabójstwa
        boolean broadcastKills = plugin.getConfig().getBoolean("ranking.broadcast-kills", true);
        if (broadcastKills) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("{killer}", killer.getName());
            replacements.put("{clan}", killerClan.getName());
            replacements.put("{victim}", victim.getName());

            String message = plugin.getPrefix() +
                    plugin.getLangManager().getMessage("kill.clan-kill", replacements);

            Bukkit.broadcastMessage(message);
        }
    }
}
