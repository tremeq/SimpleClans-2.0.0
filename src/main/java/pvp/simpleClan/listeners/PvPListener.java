package pvp.simpleClan.listeners;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blokuje obrażenia między członkami tego samego klanu (gdy PvP w klanie jest wyłączone)
 * oraz między sojusznikami (gdy alliance.friendly-fire = false).
 * Obsługuje ataki bezpośrednie, pociski, TNT, oswojone zwierzęta, mikstury i podpalenie.
 */
public class PvPListener implements Listener {

    private enum Relation {
        NONE, CLAN, ALLY
    }

    private final SimpleClan plugin;
    private final Map<UUID, Long> lastMessage = new ConcurrentHashMap<>();

    public PvPListener(SimpleClan plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) {
            return;
        }

        Relation relation = getProtectedRelation(attacker, victim);
        if (relation != Relation.NONE) {
            event.setCancelled(true);
            notifyBlocked(attacker, relation);
        }
    }

    // Fire Aspect i Flame podpalają przez osobny event - bez tego ogień zadawałby obrażenia mimo blokady
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCombust(EntityCombustByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = resolveAttacker(event.getCombuster());
        if (attacker != null && !attacker.equals(victim) && getProtectedRelation(attacker, victim) != Relation.NONE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof Player thrower)) {
            return;
        }
        if (!isHarmful(event.getPotion().getEffects())) {
            return; // Mikstury leczące itp. mogą trafiać w sojuszników
        }
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player victim && !victim.equals(thrower)) {
                Relation relation = getProtectedRelation(thrower, victim);
                if (relation != Relation.NONE) {
                    event.setIntensity(victim, 0);
                    notifyBlocked(thrower, relation);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLingeringPotion(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        if (!(cloud.getSource() instanceof Player thrower)) {
            return;
        }
        List<PotionEffect> effects = new ArrayList<>(cloud.getCustomEffects());
        PotionType baseType = cloud.getBasePotionType();
        if (baseType != null) {
            effects.addAll(baseType.getPotionEffects());
        }
        if (!isHarmful(effects)) {
            return;
        }
        event.getAffectedEntities().removeIf(entity -> entity instanceof Player victim
                && !victim.equals(thrower)
                && getProtectedRelation(thrower, victim) != Relation.NONE);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastMessage.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Ustala gracza odpowiedzialnego za obrażenia
     */
    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            return shooter instanceof Player player ? player : null;
        }
        if (damager instanceof TNTPrimed tnt) {
            return tnt.getSource() instanceof Player player ? player : null;
        }
        if (damager instanceof AreaEffectCloud cloud) {
            return cloud.getSource() instanceof Player player ? player : null;
        }
        if (damager instanceof EvokerFangs fangs) {
            return fangs.getOwner() instanceof Player player ? player : null;
        }
        if (damager instanceof Tameable tameable && tameable.isTamed()) {
            return tameable.getOwner() instanceof Player player ? player : null;
        }
        return null;
    }

    private Relation getProtectedRelation(Player attacker, Player victim) {
        ClanManager clanManager = plugin.getClanManager();
        Clan attackerClan = clanManager.getPlayerClan(attacker.getUniqueId());
        Clan victimClan = clanManager.getPlayerClan(victim.getUniqueId());

        if (attackerClan == null || victimClan == null) {
            return Relation.NONE;
        }
        if (attackerClan.equals(victimClan)) {
            return victimClan.isPvpEnabled() ? Relation.NONE : Relation.CLAN;
        }
        if (clanManager.isAllianceEnabled() && clanManager.areAllies(attackerClan, victimClan)
                && !plugin.getConfig().getBoolean("alliance.friendly-fire", false)) {
            return Relation.ALLY;
        }
        return Relation.NONE;
    }

    private static boolean isHarmful(Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            if (effect.getType().getCategory() == PotionEffectTypeCategory.HARMFUL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wysyła informację o zablokowanym ataku, maksymalnie raz na kilka sekund (bez spamu przy każdym ciosie)
     */
    private void notifyBlocked(Player attacker, Relation relation) {
        long cooldown = plugin.getConfig().getLong("pvp.message-cooldown-seconds", 3) * 1000L;
        long now = System.currentTimeMillis();
        Long last = lastMessage.get(attacker.getUniqueId());
        if (last != null && now - last < cooldown) {
            return;
        }
        lastMessage.put(attacker.getUniqueId(), now);
        plugin.getLangManager().send(attacker, relation == Relation.CLAN ? "pvp.blocked" : "pvp.blocked-ally");
    }
}
