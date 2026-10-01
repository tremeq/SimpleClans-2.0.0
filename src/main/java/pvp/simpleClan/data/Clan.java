package pvp.simpleClan.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Model klanu.
 * Zbiory są thread-safe, bo czytają je również wątki asynchroniczne (czat, PlaceholderAPI).
 */
public class Clan {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final String name;
    private volatile UUID leader;
    private final Set<UUID> members = ConcurrentHashMap.newKeySet();
    private final Set<UUID> moderators = ConcurrentHashMap.newKeySet();
    private final Set<String> allies = ConcurrentHashMap.newKeySet();
    private final LocalDateTime created;
    private volatile boolean pvpEnabled;
    private volatile String tagColor;
    private volatile int kills;
    private volatile int deaths;

    public Clan(String name, UUID leader, boolean pvpEnabled) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
        this.created = LocalDateTime.now();
        this.pvpEnabled = pvpEnabled;
        this.tagColor = "&6"; // Domyślnie złoty kolor
    }

    // Konstruktor do wczytywania z pliku
    private Clan(String name, UUID leader, LocalDateTime created) {
        this.name = name;
        this.leader = leader;
        this.created = created;
    }

    public String getName() {
        return name;
    }

    /**
     * Klucz klanu używany w mapach i w pliku danych (nazwa małymi literami)
     */
    public String getKey() {
        return name.toLowerCase(Locale.ROOT);
    }

    public UUID getLeader() {
        return leader;
    }

    /**
     * Ustawia lidera. Nowy lider musi być członkiem klanu i przestaje być zastępcą.
     */
    public void setLeader(UUID leader) {
        members.add(leader);
        moderators.remove(leader);
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public Set<UUID> getModerators() {
        return new HashSet<>(moderators);
    }

    public void addMember(UUID member) {
        members.add(member);
    }

    public void removeMember(UUID member) {
        members.remove(member);
        moderators.remove(member); // Usuń z zastępców jeśli był zastępcą
    }

    public void addModerator(UUID moderator) {
        if (members.contains(moderator) && !moderator.equals(leader)) {
            moderators.add(moderator);
        }
    }

    public void removeModerator(UUID moderator) {
        moderators.remove(moderator);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isLeader(UUID player) {
        return leader.equals(player);
    }

    public boolean isModerator(UUID player) {
        return moderators.contains(player);
    }

    public boolean isLeaderOrModerator(UUID player) {
        return isLeader(player) || isModerator(player);
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getModeratorCount() {
        return moderators.size();
    }

    // ===== Sojusze =====

    public Set<String> getAllies() {
        return new HashSet<>(allies);
    }

    public boolean isAlly(String clanKey) {
        return allies.contains(clanKey);
    }

    public void addAlly(String clanKey) {
        allies.add(clanKey);
    }

    public void removeAlly(String clanKey) {
        allies.remove(clanKey);
    }

    public int getAllyCount() {
        return allies.size();
    }

    // ===== Pozostałe =====

    public LocalDateTime getCreated() {
        return created;
    }

    public String getFormattedCreatedDate() {
        return created.format(DATE_FORMAT);
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }

    // ===== Statystyki (modyfikowane tylko w wątku głównym) =====

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public double getKdr() {
        return PlayerStats.kdr(kills, deaths);
    }

    public void resetStats() {
        this.kills = 0;
        this.deaths = 0;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("leader", leader.toString());
        data.put("created", created.toString());
        data.put("pvpEnabled", pvpEnabled);
        data.put("tagColor", tagColor);
        data.put("kills", kills);
        data.put("deaths", deaths);
        data.put("members", members.stream().map(UUID::toString).sorted().toList());
        data.put("moderators", moderators.stream().map(UUID::toString).sorted().toList());
        data.put("allies", allies.stream().sorted().toList());
        return data;
    }

    public static Clan deserialize(Map<String, Object> data) {
        String name = (String) data.get("name");
        UUID leader = UUID.fromString((String) data.get("leader"));
        Object createdRaw = data.get("created");
        LocalDateTime created = createdRaw != null ? LocalDateTime.parse(createdRaw.toString()) : LocalDateTime.now();

        Clan clan = new Clan(name, leader, created);

        // Pola dodane w nowszych wersjach mają wartości domyślne dla kompatybilności wstecznej
        clan.pvpEnabled = data.get("pvpEnabled") instanceof Boolean b && b;
        clan.tagColor = data.get("tagColor") instanceof String s ? s : "&6";
        clan.kills = data.get("kills") instanceof Number n ? n.intValue() : 0;
        clan.deaths = data.get("deaths") instanceof Number n ? n.intValue() : 0;

        for (String member : stringList(data.get("members"))) {
            clan.members.add(UUID.fromString(member));
        }
        clan.members.add(leader);

        for (String moderator : stringList(data.get("moderators"))) {
            UUID uuid = UUID.fromString(moderator);
            if (clan.members.contains(uuid) && !uuid.equals(leader)) {
                clan.moderators.add(uuid);
            }
        }

        for (String ally : stringList(data.get("allies"))) {
            clan.allies.add(ally.toLowerCase(Locale.ROOT));
        }

        return clan;
    }

    private static List<String> stringList(Object raw) {
        List<String> result = new ArrayList<>();
        if (raw instanceof Collection<?> collection) {
            for (Object o : collection) {
                if (o != null) {
                    result.add(o.toString());
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Clan clan = (Clan) obj;
        return name.equalsIgnoreCase(clan.name);
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
