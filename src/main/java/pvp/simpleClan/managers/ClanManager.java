package pvp.simpleClan.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ClanManager {

    private final SimpleClan plugin;
    private final Map<String, Clan> clans;
    private final Map<UUID, String> playerClans;
    private final Map<UUID, String> invites;
    private final File dataFile;
    private final Pattern namePattern;

    public ClanManager(SimpleClan plugin) {
        this.plugin = plugin;
        this.clans = new ConcurrentHashMap<>();
        this.playerClans = new ConcurrentHashMap<>();
        this.invites = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "clans.yml");
        this.namePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");

        // Utwórz folder jeśli nie istnieje
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        loadData();
    }

    public boolean createClan(Player player, String name) {
        // Sprawdź czy gracz już ma klan
        if (hasPlayerClan(player.getUniqueId())) {
            return false;
        }

        // Sprawdź czy klan już istnieje
        if (clans.containsKey(name.toLowerCase())) {
            return false;
        }

        // Sprawdź długość nazwy
        int minLength = plugin.getConfig().getInt("clan.min-name-length", 3);
        int maxLength = plugin.getConfig().getInt("clan.max-name-length", 16);

        if (name.length() < minLength || name.length() > maxLength) {
            return false;
        }

        // Sprawdź czy nazwa zawiera tylko dozwolone znaki
        if (!namePattern.matcher(name).matches()) {
            return false;
        }

        // Utwórz klan
        Clan clan = new Clan(name, player.getUniqueId());
        clans.put(name.toLowerCase(), clan);
        playerClans.put(player.getUniqueId(), name.toLowerCase());

        saveData();
        return true;
    }

    public boolean disbandClan(Player player) {
        String clanName = getPlayerClanName(player.getUniqueId());
        if (clanName == null) {
            return false;
        }

        Clan clan = clans.get(clanName);
        if (clan == null || !clan.isLeader(player.getUniqueId())) {
            return false;
        }

        // Usuń wszystkich członków z mapy
        for (UUID member : clan.getMembers()) {
            playerClans.remove(member);
        }

        // Usuń klan
        clans.remove(clanName);

        // Powiadom wszystkich członków
        for (UUID memberUuid : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(plugin.getPrefix() + plugin.getMessage("clan-disbanded"));
            }
        }

        saveData();
        return true;
    }

    public void invitePlayer(Player inviter, Player target) {
        String clanName = getPlayerClanName(inviter.getUniqueId());
        if (clanName != null) {
            invites.put(target.getUniqueId(), clanName);
        }
    }

    public boolean acceptInvite(Player player) {
        String clanName = invites.get(player.getUniqueId());
        if (clanName == null) {
            return false;
        }

        Clan clan = clans.get(clanName);
        if (clan == null) {
            invites.remove(player.getUniqueId());
            return false;
        }

        // Sprawdź czy klan nie jest pełny
        int maxMembers = plugin.getConfig().getInt("clan.max-members", 10);
        if (clan.getMemberCount() >= maxMembers) {
            invites.remove(player.getUniqueId());
            return false;
        }

        // Dodaj gracza do klanu
        clan.addMember(player.getUniqueId());
        playerClans.put(player.getUniqueId(), clanName);
        invites.remove(player.getUniqueId());

        // Powiadom członków klanu
        for (UUID memberUuid : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage(plugin.getPrefix() +
                        plugin.getMessage("invite-accepted").replace("{player}", player.getName()));
            }
        }

        saveData();
        return true;
    }

    public boolean leaveClan(Player player) {
        String clanName = getPlayerClanName(player.getUniqueId());
        if (clanName == null) {
            return false;
        }

        Clan clan = clans.get(clanName);
        if (clan == null) {
            return false;
        }

        // Jeśli gracz jest liderem i jest jedynym członkiem, rozwiąż klan
        if (clan.isLeader(player.getUniqueId()) && clan.getMemberCount() == 1) {
            clans.remove(clanName);
        } else if (clan.isLeader(player.getUniqueId())) {
            // Jeśli lider opuszcza klan, przekaż przywództwo innemu członkowi
            for (UUID memberUuid : clan.getMembers()) {
                if (!memberUuid.equals(player.getUniqueId())) {
                    clan.setLeader(memberUuid);
                    break;
                }
            }
            clan.removeMember(player.getUniqueId());
        } else {
            clan.removeMember(player.getUniqueId());
        }

        playerClans.remove(player.getUniqueId());

        // Powiadom pozostałych członków
        for (UUID memberUuid : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(plugin.getPrefix() +
                        plugin.getMessage("player-left").replace("{player}", player.getName()));
            }
        }

        saveData();
        return true;
    }

    public boolean hasPlayerClan(UUID player) {
        return playerClans.containsKey(player);
    }

    public String getPlayerClanName(UUID player) {
        return playerClans.get(player);
    }

    public Clan getPlayerClan(UUID player) {
        String clanName = getPlayerClanName(player);
        return clanName != null ? clans.get(clanName) : null;
    }

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    public boolean hasInvite(UUID player) {
        return invites.containsKey(player);
    }

    public String getInviteClanName(UUID player) {
        return invites.get(player);
    }

    public Map<UUID, String> getPlayerClans() {
        return playerClans;
    }

    public void removePlayerFromClan(UUID player) {
        playerClans.remove(player);
    }

    public void removeInvite(UUID player) {
        invites.remove(player);
    }

    public String getPlayerClanTag(UUID player) {
        Clan clan = getPlayerClan(player);
        if (clan == null) {
            return "";
        }

        String format = plugin.getConfig().getString("clan.tag-format", "{color}[{clan}]&r");
        return format
                .replace("{color}", clan.getTagColor())
                .replace("{clan}", clan.getName())
                .replace("&", "§");
    }

    private void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);

        if (config.contains("clans")) {
            for (String clanName : config.getConfigurationSection("clans").getKeys(false)) {
                try {
                    Map<String, Object> data = config.getConfigurationSection("clans." + clanName).getValues(false);
                    Clan clan = Clan.deserialize(data);
                    clans.put(clanName.toLowerCase(), clan);

                    // Dodaj członków do mapy
                    for (UUID member : clan.getMembers()) {
                        playerClans.put(member, clanName.toLowerCase());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Błąd podczas wczytywania klanu: " + clanName);
                    e.printStackTrace();
                }
            }
        }

        plugin.getLogger().info("Wczytano " + clans.size() + " klanów.");
    }

    public void saveData() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Clan> entry : clans.entrySet()) {
            String clanName = entry.getKey();
            Clan clan = entry.getValue();

            for (Map.Entry<String, Object> dataEntry : clan.serialize().entrySet()) {
                config.set("clans." + clanName + "." + dataEntry.getKey(), dataEntry.getValue());
            }
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie można zapisać danych klanów!");
            e.printStackTrace();
        }
    }
}