package pvp.simpleClan.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;

public class ClanPlaceholders extends PlaceholderExpansion {

    private final SimpleClan plugin;
    private final ClanManager clanManager;

    public ClanPlaceholders(SimpleClan plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public String getIdentifier() {
        return "simpleclan";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
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
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());

        switch (params.toLowerCase()) {
            case "tag":
                return clan != null ? clanManager.getPlayerClanTag(player.getUniqueId()) : "";

            case "name":
                return clan != null ? clan.getName() : "";

            case "leader":
                if (clan != null) {
                    Player leader = plugin.getServer().getPlayer(clan.getLeader());
                    if (leader != null) {
                        return leader.getName();
                    } else {
                        String offlineName = plugin.getServer().getOfflinePlayer(clan.getLeader()).getName();
                        return offlineName != null ? offlineName : "Unknown";
                    }
                }
                return "";

            case "members":
                return clan != null ? String.valueOf(clan.getMemberCount()) : "0";

            case "max_members":
                return String.valueOf(plugin.getConfig().getInt("clan.max-members", 10));

            case "is_leader":
                return clan != null && clan.isLeader(player.getUniqueId()) ? "true" : "false";

            case "is_moderator":
                return clan != null && clan.isModerator(player.getUniqueId()) ? "true" : "false";

            case "is_leader_or_mod":
                return clan != null && clan.isLeaderOrModerator(player.getUniqueId()) ? "true" : "false";

            case "moderators_count":
                return clan != null ? String.valueOf(clan.getModeratorCount()) : "0";

            case "has_clan":
                return clan != null ? "true" : "false";

            case "created":
                return clan != null ? clan.getFormattedCreatedDate() : "";

            case "tag_color":
                return clan != null ? clan.getTagColor() : "";

            case "pvp_enabled":
                return clan != null && clan.isPvpEnabled() ? "true" : "false";

            case "kills":
                return clan != null ? String.valueOf(clan.getKills()) : "0";

            case "clan_chat":
                return plugin.getChatListener().getChatManager().isClanChatEnabled(player.getUniqueId()) ? "true" : "false";

            default:
                return null;
        }
    }
}