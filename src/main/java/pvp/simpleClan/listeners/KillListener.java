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
import pvp.simpleClan.managers.StatsManager;

/**
 * Zlicza zabójstwa i śmierci graczy oraz klanów
 */
public class KillListener implements Listener {

    private final SimpleClan plugin;

    public KillListener(SimpleClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Liczymy tylko zabójstwa przez innego gracza
        if (killer == null || killer.equals(victim)) {
            return;
        }

        ClanManager clanManager = plugin.getClanManager();
        StatsManager statsManager = plugin.getStatsManager();
        Clan killerClan = clanManager.getPlayerClan(killer.getUniqueId());
        Clan victimClan = clanManager.getPlayerClan(victim.getUniqueId());

        // Zabójstwa w obrębie klanu lub sojuszu nie wpływają na statystyki (brak nabijania rankingu)
        if (killerClan != null && victimClan != null
                && (killerClan.equals(victimClan) || clanManager.areAllies(killerClan, victimClan))) {
            return;
        }

        // To samo zabójstwo liczy się raz na kill-cooldown-seconds (ochrona przed farmieniem)
        if (!statsManager.tryRegisterKill(killer.getUniqueId(), victim.getUniqueId())) {
            return;
        }

        statsManager.addKill(killer);
        statsManager.addDeath(victim);

        if (killerClan != null) {
            killerClan.addKill();
        }
        if (victimClan != null) {
            victimClan.addDeath();
        }
        if (killerClan != null || victimClan != null) {
            clanManager.markDirty();
        }

        if (killerClan != null && plugin.getConfig().getBoolean("ranking.broadcast-kills", false)) {
            String message = plugin.getLangManager().get("kill.clan-kill",
                    "killer", killer.getName(),
                    "clan", killerClan.getName(),
                    "victim", victim.getName());
            if (!message.isEmpty()) {
                Bukkit.broadcastMessage(plugin.getLangManager().getPrefix() + message);
            }
        }
    }
}
