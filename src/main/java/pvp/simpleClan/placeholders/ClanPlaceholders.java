package pvp.simpleClan.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.data.PlayerStats;
import pvp.simpleClan.managers.ClanManager;
import pvp.simpleClan.managers.ClanManager.RankingType;
import pvp.simpleClan.managers.StatsManager;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Placeholdery %simpleclan_...%. Działają też dla graczy offline (np. w hologramach rankingu).
 */
public class ClanPlaceholders extends PlaceholderExpansion {

    private final SimpleClan plugin;

    public ClanPlaceholders(SimpleClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "simpleclan";
    }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String key = params.toLowerCase(Locale.ROOT);

        // Ranking nie zależy od gracza: top_<pozycja>_<pole> lub top_<typ>_<pozycja>_<pole>
        if (key.startsWith("top_")) {
            return topPlaceholder(key.substring(4));
        }

        if (player == null) {
            return "";
        }

        ClanManager clanManager = plugin.getClanManager();
        StatsManager statsManager = plugin.getStatsManager();
        UUID uuid = player.getUniqueId();
        Clan clan = clanManager.getPlayerClan(uuid);

        switch (key) {
            // Statystyki gracza - niezależne od klanu
            case "player_kills":
                return String.valueOf(statsManager.getKills(uuid));
            case "player_deaths":
                return String.valueOf(statsManager.getDeaths(uuid));
            case "player_kdr":
                return PlayerStats.formatKdr(statsManager.getKdr(uuid));
            case "has_clan":
                return String.valueOf(clan != null);
            case "max_members":
                return String.valueOf(clanManager.getMaxMembers());
            case "max_allies":
                int maxAllies = clanManager.getMaxAllies();
                return maxAllies < 0 ? "∞" : String.valueOf(maxAllies);
            case "clan_chat":
                return String.valueOf(plugin.getChatManager().isClanChatEnabled(uuid));
            case "chat_mode":
                return plugin.getChatManager().getMode(uuid).name().toLowerCase(Locale.ROOT);
            default:
                break;
        }

        if (clan == null) {
            return switch (key) {
                case "members", "moderators_count", "kills", "deaths", "allies_count", "rank", "online" -> "0";
                case "kdr" -> PlayerStats.formatKdr(0);
                case "is_leader", "is_moderator", "is_leader_or_mod", "pvp_enabled" -> "false";
                case "tag", "tag_spaced", "name", "leader", "created", "tag_color", "allies", "role" -> "";
                default -> null;
            };
        }

        return switch (key) {
            case "tag" -> clanManager.getClanTag(clan);
            case "tag_spaced" -> clanManager.getClanTag(clan) + " ";
            case "name" -> clan.getName();
            case "leader" -> statsManager.getName(clan.getLeader());
            case "members" -> String.valueOf(clan.getMemberCount());
            case "online" -> String.valueOf(countOnline(clan));
            case "is_leader" -> String.valueOf(clan.isLeader(uuid));
            case "is_moderator" -> String.valueOf(clan.isModerator(uuid));
            case "is_leader_or_mod" -> String.valueOf(clan.isLeaderOrModerator(uuid));
            case "role" -> plugin.getLangManager().get(clan.isLeader(uuid) ? "members.leader-status"
                    : clan.isModerator(uuid) ? "members.moderator-status" : "members.member-status");
            case "moderators_count" -> String.valueOf(clan.getModeratorCount());
            case "created" -> clan.getFormattedCreatedDate();
            case "tag_color" -> clan.getTagColor();
            case "pvp_enabled" -> String.valueOf(clan.isPvpEnabled());
            case "kills" -> String.valueOf(clan.getKills());
            case "deaths" -> String.valueOf(clan.getDeaths());
            case "kdr" -> PlayerStats.formatKdr(clan.getKdr());
            case "rank" -> String.valueOf(clanManager.getRank(clan));
            case "allies" -> clanManager.getAllies(clan).stream().map(Clan::getName).collect(Collectors.joining(", "));
            case "allies_count" -> String.valueOf(clanManager.getAllies(clan).size());
            default -> null;
        };
    }

    /**
     * top_1_name, top_1_kills... (ranking zabójstw) albo top_kdr_1_name, top_members_3_tag...
     */
    private String topPlaceholder(String params) {
        String[] parts = params.split("_");
        RankingType type = switch (parts[0]) {
            case "kills" -> RankingType.KILLS;
            case "kdr" -> RankingType.KDR;
            case "members" -> RankingType.MEMBERS;
            default -> null;
        };
        int index = type != null ? 1 : 0;
        if (type == null) {
            type = RankingType.KILLS;
        }
        if (parts.length != index + 2) {
            return null;
        }

        int position;
        try {
            position = Integer.parseInt(parts[index]);
        } catch (NumberFormatException e) {
            return null;
        }
        String field = parts[index + 1];

        List<Clan> ranking = plugin.getClanManager().getRanking(type);
        if (position < 1 || position > ranking.size()) {
            return switch (field) {
                case "kills", "deaths", "members" -> "0";
                case "kdr" -> PlayerStats.formatKdr(0);
                case "name", "tag", "leader" -> "-";
                default -> null;
            };
        }

        Clan clan = ranking.get(position - 1);
        return switch (field) {
            case "name" -> clan.getName();
            case "tag" -> plugin.getClanManager().getClanTag(clan);
            case "leader" -> plugin.getStatsManager().getName(clan.getLeader());
            case "kills" -> String.valueOf(clan.getKills());
            case "deaths" -> String.valueOf(clan.getDeaths());
            case "kdr" -> PlayerStats.formatKdr(clan.getKdr());
            case "members" -> String.valueOf(clan.getMemberCount());
            default -> null;
        };
    }

    private int countOnline(Clan clan) {
        int online = 0;
        for (UUID member : clan.getMembers()) {
            if (plugin.getServer().getPlayer(member) != null) {
                online++;
            }
        }
        return online;
    }
}
