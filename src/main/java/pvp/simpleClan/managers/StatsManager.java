package pvp.simpleClan.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.PlayerStats;
import pvp.simpleClan.storage.YamlStorage;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statystyki PvP graczy (zabójstwa, śmierci, KDR) oraz pamięć ostatnio znanych nicków.
 * Dane zapisywane są w players.yml.
 */
public class StatsManager {

    private final SimpleClan plugin;
    private final YamlStorage storage;
    private final File dataFile;
    private final Map<UUID, PlayerStats> stats = new ConcurrentHashMap<>();
    private final Map<String, Long> killCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, String> offlineNameCache = new ConcurrentHashMap<>();
    private volatile boolean dirty;

    public StatsManager(SimpleClan plugin, YamlStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        load();
    }

    // ===== Statystyki =====

    public PlayerStats getStats(UUID player) {
        return stats.get(player);
    }

    private PlayerStats getOrCreate(UUID player) {
        PlayerStats s = stats.get(player);
        if (s == null) {
            s = new PlayerStats(lookupName(player), 0, 0);
            PlayerStats previous = stats.putIfAbsent(player, s);
            if (previous != null) {
                s = previous;
            }
        }
        return s;
    }

    public int getKills(UUID player) {
        PlayerStats s = stats.get(player);
        return s != null ? s.getKills() : 0;
    }

    public int getDeaths(UUID player) {
        PlayerStats s = stats.get(player);
        return s != null ? s.getDeaths() : 0;
    }

    public double getKdr(UUID player) {
        return PlayerStats.kdr(getKills(player), getDeaths(player));
    }

    public void addKill(Player player) {
        PlayerStats s = getOrCreate(player.getUniqueId());
        s.setName(player.getName());
        s.addKill();
        dirty = true;
    }

    public void addDeath(Player player) {
        PlayerStats s = getOrCreate(player.getUniqueId());
        s.setName(player.getName());
        s.addDeath();
        dirty = true;
    }

    public void resetStats(UUID player) {
        PlayerStats s = stats.get(player);
        if (s != null) {
            s.reset();
            dirty = true;
        }
    }

    /**
     * Ochrona przed nabijaniem statystyk: to samo zabójstwo (zabójca -> ofiara)
     * liczy się tylko raz na okres ustawiony w configu.
     *
     * @return true jeśli zabójstwo powinno zostać policzone
     */
    public boolean tryRegisterKill(UUID killer, UUID victim) {
        long cooldownMillis = plugin.getConfig().getLong("stats.kill-cooldown-seconds", 300) * 1000L;
        if (cooldownMillis <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        String key = killer + ":" + victim;
        Long last = killCooldowns.get(key);
        if (last != null && now - last < cooldownMillis) {
            return false;
        }
        killCooldowns.put(key, now);
        return true;
    }

    public void cleanupCooldowns() {
        long cooldownMillis = plugin.getConfig().getLong("stats.kill-cooldown-seconds", 300) * 1000L;
        long now = System.currentTimeMillis();
        killCooldowns.values().removeIf(time -> now - time >= cooldownMillis);
    }

    // ===== Nicki =====

    /**
     * Zapamiętuje aktualny nick gracza (wywoływane przy wejściu na serwer i dołączeniu do klanu)
     */
    public void rememberName(UUID player, String name, boolean createIfMissing) {
        PlayerStats s = createIfMissing ? getOrCreate(player) : stats.get(player);
        if (s != null && !name.equals(s.getName())) {
            s.setName(name);
            dirty = true;
        }
    }

    /**
     * Zwraca nick gracza: online -> zapamiętany -> z danych serwera -> "Nieznany"
     */
    public String getName(UUID player) {
        String name = lookupName(player);
        return name != null ? name : plugin.getLangManager().get("general.unknown");
    }

    private String lookupName(UUID player) {
        Player online = Bukkit.getPlayer(player);
        if (online != null) {
            return online.getName();
        }
        PlayerStats s = stats.get(player);
        if (s != null && s.getName() != null) {
            return s.getName();
        }
        // Odczyt nicku gracza offline może sięgać do plików serwera - wynik zapamiętujemy
        String cached = offlineNameCache.get(player);
        if (cached != null) {
            return cached;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(player);
        String name = offline.getName();
        if (name != null) {
            offlineNameCache.put(player, name);
        }
        return name;
    }

    /**
     * Szuka gracza po dokładnym nicku (bez dopasowania prefiksu): najpierw online, potem zapamiętane nicki
     */
    public UUID findPlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            if (name.equalsIgnoreCase(entry.getValue().getName())) {
                return entry.getKey();
            }
        }
        return null;
    }

    // ===== Zapis / odczyt =====

    private void load() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection data = section.getConfigurationSection(key);
                if (data == null) {
                    continue;
                }
                stats.put(uuid, new PlayerStats(data.getString("name"), data.getInt("kills"), data.getInt("deaths")));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Nieprawidłowy UUID w players.yml: " + key);
            }
        }
        plugin.getLogger().info("Wczytano statystyki " + stats.size() + " graczy.");
    }

    public void save(boolean async) {
        if (!dirty) {
            return;
        }
        dirty = false;
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerStats> entry : stats.entrySet()) {
            String path = "players." + entry.getKey();
            PlayerStats s = entry.getValue();
            config.set(path + ".name", s.getName());
            config.set(path + ".kills", s.getKills());
            config.set(path + ".deaths", s.getDeaths());
        }
        storage.save(config, dataFile, async);
    }
}
