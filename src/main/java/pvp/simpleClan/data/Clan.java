package pvp.simpleClan.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Clan {

    private final String name;
    private UUID leader;
    private Set<UUID> members;
    private Set<UUID> moderators;
    private LocalDateTime created;
    private boolean pvpEnabled;
    private String tagColor;
    private int kills;

    public Clan(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members = new HashSet<>();
        this.moderators = new HashSet<>();
        this.members.add(leader);
        this.created = LocalDateTime.now();
        this.pvpEnabled = false; // Domyślnie PvP wyłączone
        this.tagColor = "&6"; // Domyślnie złoty kolor
        this.kills = 0;
    }

    // Konstruktor do wczytywania z pliku
    public Clan(String name, UUID leader, Set<UUID> members, Set<UUID> moderators, LocalDateTime created,
                boolean pvpEnabled, String tagColor, int kills) {
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.moderators = moderators != null ? moderators : new HashSet<>();
        this.created = created;
        this.pvpEnabled = pvpEnabled;
        this.tagColor = tagColor != null ? tagColor : "&6";
        this.kills = kills;
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
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

    public LocalDateTime getCreated() {
        return created;
    }

    public String getFormattedCreatedDate() {
        return created.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
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

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("leader", leader.toString());
        data.put("created", created.toString());
        data.put("pvpEnabled", pvpEnabled);
        data.put("tagColor", tagColor);
        data.put("kills", kills);

        List<String> membersList = new ArrayList<>();
        for (UUID member : members) {
            membersList.add(member.toString());
        }
        data.put("members", membersList);

        List<String> moderatorsList = new ArrayList<>();
        for (UUID moderator : moderators) {
            moderatorsList.add(moderator.toString());
        }
        data.put("moderators", moderatorsList);

        return data;
    }

    public static Clan deserialize(Map<String, Object> data) {
        String name = (String) data.get("name");
        UUID leader = UUID.fromString((String) data.get("leader"));
        LocalDateTime created = LocalDateTime.parse((String) data.get("created"));

        // Nowe pola z domyślnymi wartościami dla kompatybilności wstecznej
        boolean pvpEnabled = data.containsKey("pvpEnabled") ? (boolean) data.get("pvpEnabled") : false;
        String tagColor = data.containsKey("tagColor") ? (String) data.get("tagColor") : "&6";
        int kills = data.containsKey("kills") ? ((Number) data.get("kills")).intValue() : 0;

        Set<UUID> members = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<String> membersList = (List<String>) data.get("members");
        for (String memberStr : membersList) {
            members.add(UUID.fromString(memberStr));
        }

        Set<UUID> moderators = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<String> moderatorsList = (List<String>) data.get("moderators");
        if (moderatorsList != null) {
            for (String moderatorStr : moderatorsList) {
                moderators.add(UUID.fromString(moderatorStr));
            }
        }

        return new Clan(name, leader, members, moderators, created, pvpEnabled, tagColor, kills);
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
        return name.toLowerCase().hashCode();
    }
}