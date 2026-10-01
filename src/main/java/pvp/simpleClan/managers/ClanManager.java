package pvp.simpleClan.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.storage.YamlStorage;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ClanManager {

    public enum CreateResult {
        SUCCESS, ALREADY_IN_CLAN, NAME_TAKEN, TOO_SHORT, TOO_LONG, INVALID_CHARACTERS
    }

    public enum AllyResult {
        SENT, FORMED, SELF, ALREADY_ALLIED, ALREADY_REQUESTED, LIMIT_OWN, LIMIT_OTHER, NO_REQUEST
    }

    public enum RankingType {
        KILLS, KDR, MEMBERS
    }

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private final SimpleClan plugin;
    private final YamlStorage storage;
    private final File dataFile;

    // klucz klanu (nazwa małymi literami) -> klan
    private final Map<String, Clan> clans = new ConcurrentHashMap<>();
    // gracz -> klucz klanu
    private final Map<UUID, String> playerClans = new ConcurrentHashMap<>();
    // gracz -> (klucz klanu -> czas wygaśnięcia zaproszenia)
    private final Map<UUID, Map<String, Long>> invites = new ConcurrentHashMap<>();
    // klan docelowy -> (klan wysyłający -> czas wygaśnięcia prośby o sojusz)
    private final Map<String, Map<String, Long>> allyRequests = new ConcurrentHashMap<>();
    private final Map<RankingType, List<Clan>> rankingCache = new ConcurrentHashMap<>();

    private volatile boolean dirty;
    private boolean saveScheduled;

    public ClanManager(SimpleClan plugin, YamlStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.dataFile = new File(plugin.getDataFolder(), "clans.yml");
        load();
    }

    // ===== Tworzenie i usuwanie klanów =====

    public CreateResult validateName(String name) {
        int minLength = plugin.getConfig().getInt("clan.min-name-length", 3);
        int maxLength = plugin.getConfig().getInt("clan.max-name-length", 16);

        if (name.length() < minLength) {
            return CreateResult.TOO_SHORT;
        }
        if (name.length() > maxLength) {
            return CreateResult.TOO_LONG;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            return CreateResult.INVALID_CHARACTERS;
        }
        if (clans.containsKey(key(name))) {
            return CreateResult.NAME_TAKEN;
        }
        return CreateResult.SUCCESS;
    }

    public CreateResult createClan(Player player, String name) {
        if (hasPlayerClan(player.getUniqueId())) {
            return CreateResult.ALREADY_IN_CLAN;
        }

        CreateResult validation = validateName(name);
        if (validation != CreateResult.SUCCESS) {
            return validation;
        }

        Clan clan = new Clan(name, player.getUniqueId(), plugin.getConfig().getBoolean("pvp.default-enabled", false));
        clans.put(clan.getKey(), clan);
        playerClans.put(player.getUniqueId(), clan.getKey());
        invites.remove(player.getUniqueId());
        plugin.getStatsManager().rememberName(player.getUniqueId(), player.getName(), true);

        saveSoon();
        return CreateResult.SUCCESS;
    }

    /**
     * Usuwa klan wraz ze wszystkimi powiązaniami (członkowie, sojusze, zaproszenia, tryby czatu).
     * Powiadomienia wysyła wywołujący - przed wywołaniem powinien pobrać listę członków i sojuszników.
     */
    public void disbandClan(Clan clan) {
        String key = clan.getKey();
        clans.remove(key);

        for (UUID member : clan.getMembers()) {
            playerClans.remove(member);
            plugin.getChatManager().reset(member);
        }

        for (String allyKey : clan.getAllies()) {
            Clan ally = clans.get(allyKey);
            if (ally != null) {
                ally.removeAlly(key);
            }
        }

        allyRequests.remove(key);
        allyRequests.values().forEach(requests -> requests.remove(key));
        invites.values().forEach(clanInvites -> clanInvites.remove(key));

        saveSoon();
    }

    // ===== Członkostwo =====

    public void addMember(Clan clan, UUID player) {
        clan.addMember(player);
        playerClans.put(player, clan.getKey());
        invites.remove(player);
        Player online = Bukkit.getPlayer(player);
        if (online != null) {
            plugin.getStatsManager().rememberName(player, online.getName(), true);
        }
        saveSoon();
    }

    public void removeMember(Clan clan, UUID player) {
        clan.removeMember(player);
        playerClans.remove(player);
        plugin.getChatManager().reset(player);
        saveSoon();
    }

    /**
     * Przekazuje przywództwo. Poprzedni lider zostaje zastępcą, jeśli jest wolne miejsce.
     */
    public void transferLeadership(Clan clan, UUID newLeader) {
        UUID oldLeader = clan.getLeader();
        clan.setLeader(newLeader);
        if (clan.isMember(oldLeader) && !oldLeader.equals(newLeader)
                && clan.getModeratorCount() < getMaxModerators()) {
            clan.addModerator(oldLeader);
        }
        saveSoon();
    }

    /**
     * Wybiera następcę lidera: najpierw zastępca, potem dowolny członek
     */
    public UUID pickSuccessor(Clan clan, UUID excluding) {
        for (UUID moderator : clan.getModerators()) {
            if (!moderator.equals(excluding)) {
                return moderator;
            }
        }
        for (UUID member : clan.getMembers()) {
            if (!member.equals(excluding)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Szuka członka klanu po dokładnym nicku (online lub offline)
     */
    public UUID findMember(Clan clan, String name) {
        StatsManager statsManager = plugin.getStatsManager();
        for (UUID member : clan.getMembers()) {
            if (statsManager.getName(member).equalsIgnoreCase(name)) {
                return member;
            }
        }
        return null;
    }

    public int getMaxMembers() {
        return plugin.getConfig().getInt("clan.max-members", 10);
    }

    public int getMaxModerators() {
        return plugin.getConfig().getInt("clan.max-moderators", 2);
    }

    public boolean isFull(Clan clan) {
        return clan.getMemberCount() >= getMaxMembers();
    }

    public void markDirty() {
        dirty = true;
        rankingCache.clear();
    }

    // ===== Zaproszenia =====

    public void invite(Clan clan, UUID target) {
        long expire = plugin.getConfig().getLong("clan.invite-expire-seconds", 120) * 1000L;
        invites.computeIfAbsent(target, uuid -> new ConcurrentHashMap<>())
                .put(clan.getKey(), System.currentTimeMillis() + expire);
    }

    public boolean hasInvite(UUID player, Clan clan) {
        Map<String, Long> clanInvites = invites.get(player);
        if (clanInvites == null) {
            return false;
        }
        Long expiresAt = clanInvites.get(clan.getKey());
        return expiresAt != null && expiresAt >= System.currentTimeMillis();
    }

    /**
     * Zwraca ważne zaproszenia gracza (wygasłe i do nieistniejących klanów są usuwane)
     */
    public List<Clan> getInvites(UUID player) {
        Map<String, Long> clanInvites = invites.get(player);
        List<Clan> result = new ArrayList<>();
        if (clanInvites == null) {
            return result;
        }
        long now = System.currentTimeMillis();
        clanInvites.entrySet().removeIf(entry -> entry.getValue() < now || !clans.containsKey(entry.getKey()));
        for (String clanKey : clanInvites.keySet()) {
            Clan clan = clans.get(clanKey);
            if (clan != null) {
                result.add(clan);
            }
        }
        if (clanInvites.isEmpty()) {
            invites.remove(player);
        }
        result.sort(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public void removeInvite(UUID player, Clan clan) {
        Map<String, Long> clanInvites = invites.get(player);
        if (clanInvites != null) {
            clanInvites.remove(clan.getKey());
            if (clanInvites.isEmpty()) {
                invites.remove(player);
            }
        }
    }

    // ===== Sojusze =====

    public boolean isAllianceEnabled() {
        return plugin.getConfig().getBoolean("alliance.enabled", true);
    }

    /**
     * Maksymalna liczba sojuszy klanu (-1 = bez limitu)
     */
    public int getMaxAllies() {
        return plugin.getConfig().getInt("alliance.max-allies", 3);
    }

    private boolean reachedAllyLimit(Clan clan) {
        int max = getMaxAllies();
        return max >= 0 && clan.getAllyCount() >= max;
    }

    public boolean areAllies(Clan a, Clan b) {
        return a != null && b != null && !a.equals(b) && a.isAlly(b.getKey()) && b.isAlly(a.getKey());
    }

    public List<Clan> getAllies(Clan clan) {
        List<Clan> result = new ArrayList<>();
        for (String allyKey : clan.getAllies()) {
            Clan ally = clans.get(allyKey);
            if (ally != null) {
                result.add(ally);
            }
        }
        result.sort(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public AllyResult requestAlliance(Clan from, Clan to) {
        if (from.equals(to)) {
            return AllyResult.SELF;
        }
        if (areAllies(from, to)) {
            return AllyResult.ALREADY_ALLIED;
        }
        if (reachedAllyLimit(from)) {
            return AllyResult.LIMIT_OWN;
        }
        if (reachedAllyLimit(to)) {
            return AllyResult.LIMIT_OTHER;
        }
        // Druga strona już wysłała prośbę - od razu zawieramy sojusz
        if (hasAllyRequest(from, to)) {
            formAlliance(from, to);
            return AllyResult.FORMED;
        }
        if (hasAllyRequest(to, from)) {
            return AllyResult.ALREADY_REQUESTED;
        }
        long expire = plugin.getConfig().getLong("alliance.request-expire-seconds", 300) * 1000L;
        allyRequests.computeIfAbsent(to.getKey(), k -> new ConcurrentHashMap<>())
                .put(from.getKey(), System.currentTimeMillis() + expire);
        return AllyResult.SENT;
    }

    public AllyResult acceptAlliance(Clan to, Clan from) {
        if (!hasAllyRequest(to, from)) {
            return AllyResult.NO_REQUEST;
        }
        if (areAllies(to, from)) {
            removeAllyRequest(to, from);
            return AllyResult.ALREADY_ALLIED;
        }
        if (reachedAllyLimit(to)) {
            return AllyResult.LIMIT_OWN;
        }
        if (reachedAllyLimit(from)) {
            return AllyResult.LIMIT_OTHER;
        }
        formAlliance(to, from);
        return AllyResult.FORMED;
    }

    public boolean denyAlliance(Clan to, Clan from) {
        if (!hasAllyRequest(to, from)) {
            return false;
        }
        removeAllyRequest(to, from);
        return true;
    }

    public boolean breakAlliance(Clan a, Clan b) {
        if (!a.isAlly(b.getKey()) && !b.isAlly(a.getKey())) {
            return false;
        }
        a.removeAlly(b.getKey());
        b.removeAlly(a.getKey());
        saveSoon();
        return true;
    }

    /**
     * Czy klan "to" ma ważną prośbę o sojusz od klanu "from"
     */
    public boolean hasAllyRequest(Clan to, Clan from) {
        Map<String, Long> requests = allyRequests.get(to.getKey());
        if (requests == null) {
            return false;
        }
        Long expiresAt = requests.get(from.getKey());
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt < System.currentTimeMillis()) {
            requests.remove(from.getKey());
            return false;
        }
        return true;
    }

    public List<Clan> getIncomingAllyRequests(Clan clan) {
        List<Clan> result = new ArrayList<>();
        Map<String, Long> requests = allyRequests.get(clan.getKey());
        if (requests == null) {
            return result;
        }
        long now = System.currentTimeMillis();
        requests.entrySet().removeIf(entry -> entry.getValue() < now || !clans.containsKey(entry.getKey()));
        for (String fromKey : requests.keySet()) {
            Clan from = clans.get(fromKey);
            if (from != null) {
                result.add(from);
            }
        }
        result.sort(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public List<Clan> getOutgoingAllyRequests(Clan clan) {
        List<Clan> result = new ArrayList<>();
        for (Clan other : clans.values()) {
            if (!other.equals(clan) && hasAllyRequest(other, clan)) {
                result.add(other);
            }
        }
        result.sort(Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private void formAlliance(Clan a, Clan b) {
        a.addAlly(b.getKey());
        b.addAlly(a.getKey());
        removeAllyRequest(a, b);
        removeAllyRequest(b, a);
        saveSoon();
    }

    private void removeAllyRequest(Clan to, Clan from) {
        Map<String, Long> requests = allyRequests.get(to.getKey());
        if (requests != null) {
            requests.remove(from.getKey());
            if (requests.isEmpty()) {
                allyRequests.remove(to.getKey());
            }
        }
    }

    // ===== Ranking =====

    public List<Clan> getRanking(RankingType type) {
        return rankingCache.computeIfAbsent(type, t -> {
            Comparator<Clan> byName = Comparator.comparing(Clan::getName, String.CASE_INSENSITIVE_ORDER);
            Comparator<Clan> comparator = switch (t) {
                case KILLS -> Comparator.comparingInt(Clan::getKills).reversed()
                        .thenComparing(Comparator.comparingDouble(Clan::getKdr).reversed());
                case KDR -> Comparator.comparingDouble(Clan::getKdr).reversed()
                        .thenComparing(Comparator.comparingInt(Clan::getKills).reversed());
                case MEMBERS -> Comparator.comparingInt(Clan::getMemberCount).reversed()
                        .thenComparing(Comparator.comparingInt(Clan::getKills).reversed());
            };
            List<Clan> sorted = new ArrayList<>(clans.values());
            sorted.sort(comparator.thenComparing(byName));
            return Collections.unmodifiableList(sorted);
        });
    }

    /**
     * Pozycja klanu w rankingu zabójstw (od 1)
     */
    public int getRank(Clan clan) {
        return getRanking(RankingType.KILLS).indexOf(clan) + 1;
    }

    // ===== Gettery =====

    public boolean hasPlayerClan(UUID player) {
        return playerClans.containsKey(player);
    }

    public Clan getPlayerClan(UUID player) {
        String clanKey = playerClans.get(player);
        return clanKey != null ? clans.get(clanKey) : null;
    }

    public Clan getClan(String name) {
        return clans.get(key(name));
    }

    public Collection<Clan> getAllClans() {
        return Collections.unmodifiableCollection(clans.values());
    }

    public Set<UUID> getPlayersInClans() {
        return Collections.unmodifiableSet(playerClans.keySet());
    }

    public String getClanTag(Clan clan) {
        String format = plugin.getConfig().getString("clan.tag-format", "{color}[{clan}]&r");
        return LangManager.colorize(format
                .replace("{color}", clan.getTagColor())
                .replace("{clan}", clan.getName()));
    }

    public String getPlayerClanTag(UUID player) {
        Clan clan = getPlayerClan(player);
        return clan != null ? getClanTag(clan) : "";
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    // ===== Porządki =====

    /**
     * Usuwa wygasłe zaproszenia i prośby o sojusz
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        invites.values().forEach(m -> m.values().removeIf(expiresAt -> expiresAt < now));
        invites.values().removeIf(Map::isEmpty);
        allyRequests.values().forEach(m -> m.values().removeIf(expiresAt -> expiresAt < now));
        allyRequests.values().removeIf(Map::isEmpty);
    }

    // ===== Zapis / odczyt =====

    private void load() {
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection section = config.getConfigurationSection("clans");

        if (section != null) {
            for (String clanKey : section.getKeys(false)) {
                try {
                    ConfigurationSection clanSection = section.getConfigurationSection(clanKey);
                    if (clanSection == null) {
                        continue;
                    }
                    Clan clan = Clan.deserialize(clanSection.getValues(false));
                    if (clans.containsKey(clan.getKey())) {
                        plugin.getLogger().warning("Zduplikowany klan w clans.yml: " + clan.getName() + " - pomijam.");
                        continue;
                    }
                    clans.put(clan.getKey(), clan);

                    for (UUID member : clan.getMembers()) {
                        String previous = playerClans.putIfAbsent(member, clan.getKey());
                        if (previous != null && !clan.isLeader(member)) {
                            plugin.getLogger().warning("Gracz " + member + " jest w dwóch klanach (" + previous
                                    + ", " + clan.getKey() + ") - zostaje w " + previous + ".");
                            clan.removeMember(member);
                            dirty = true;
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Błąd podczas wczytywania klanu: " + clanKey);
                    e.printStackTrace();
                }
            }
        }

        // Sojusze muszą być obustronne i wskazywać na istniejące klany
        for (Clan clan : clans.values()) {
            for (String allyKey : clan.getAllies()) {
                Clan ally = clans.get(allyKey);
                if (ally == null || ally.equals(clan) || !ally.isAlly(clan.getKey())) {
                    clan.removeAlly(allyKey);
                    dirty = true;
                    plugin.getLogger().warning("Usunięto nieprawidłowy sojusz " + clan.getName() + " -> " + allyKey);
                }
            }
        }

        plugin.getLogger().info("Wczytano " + clans.size() + " klanów.");
    }

    /**
     * Zapisuje dane w najbliższym ticku (kilka zmian w jednym ticku = jeden zapis)
     */
    public void saveSoon() {
        markDirty();
        if (saveScheduled || !plugin.isEnabled()) {
            return;
        }
        saveScheduled = true;
        Bukkit.getScheduler().runTask(plugin, () -> {
            saveScheduled = false;
            save(true);
        });
    }

    public void save(boolean async) {
        if (!dirty) {
            return;
        }
        dirty = false;

        YamlConfiguration config = new YamlConfiguration();
        for (Clan clan : clans.values()) {
            for (Map.Entry<String, Object> entry : clan.serialize().entrySet()) {
                config.set("clans." + clan.getKey() + "." + entry.getKey(), entry.getValue());
            }
        }
        storage.save(config, dataFile, async);
    }
}
