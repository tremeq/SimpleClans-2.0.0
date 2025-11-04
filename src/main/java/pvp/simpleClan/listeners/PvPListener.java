package pvp.simpleClan.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;

/**
 * Obsługuje PvP między członkami klanu
 */
public class PvPListener implements Listener {

    private final SimpleClan plugin;
    private final ClanManager clanManager;

    public PvPListener(SimpleClan plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Sprawdź czy ofiara i atakujący to gracze
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        // Sprawdź czy obaj są w tym samym klanie
        Clan victimClan = clanManager.getPlayerClan(victim.getUniqueId());
        Clan attackerClan = clanManager.getPlayerClan(attacker.getUniqueId());

        // Jeśli któryś nie jest w klanie, pozwól na atak
        if (victimClan == null || attackerClan == null) {
            return;
        }

        // Jeśli są w różnych klanach, pozwól na atak
        if (!victimClan.equals(attackerClan)) {
            return;
        }

        // Jeśli PvP w klanie jest włączone, pozwól na atak
        if (victimClan.isPvpEnabled()) {
            return;
        }

        // Zablokuj atak i powiadom gracza
        event.setCancelled(true);
        attacker.sendMessage(plugin.getPrefix() + plugin.getMessage("pvp.blocked"));
    }
}
