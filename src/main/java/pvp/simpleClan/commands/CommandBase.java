package pvp.simpleClan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wspólne narzędzia dla obsługi komend
 */
abstract class CommandBase {

    protected final SimpleClan plugin;
    protected final LangManager lang;
    protected final ClanManager clanManager;
    protected final StatsManager statsManager;
    protected final ConfirmationManager confirmations;

    protected CommandBase(SimpleClan plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        this.clanManager = plugin.getClanManager();
        this.statsManager = plugin.getStatsManager();
        this.confirmations = plugin.getConfirmationManager();
    }

    /**
     * Zwraca klan gracza albo wysyła "nie jesteś w klanie" i zwraca null
     */
    protected Clan requireClan(Player player) {
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            lang.send(player, "membership.not-in-clan");
        }
        return clan;
    }

    /**
     * Zwraca klan gracza jeśli jest jego liderem, w przeciwnym razie wysyła błąd i zwraca null
     */
    protected Clan requireLeader(Player player) {
        Clan clan = requireClan(player);
        if (clan != null && !clan.isLeader(player.getUniqueId())) {
            lang.send(player, "membership.not-leader");
            return null;
        }
        return clan;
    }

    protected Clan requireLeaderOrModerator(Player player) {
        Clan clan = requireClan(player);
        if (clan != null && !clan.isLeaderOrModerator(player.getUniqueId())) {
            lang.send(player, "membership.not-leader-or-mod");
            return null;
        }
        return clan;
    }

    protected boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            lang.send(sender, "no-permission");
            return false;
        }
        return true;
    }

    /**
     * Wysyła wiadomość do wszystkich członków klanu będących online
     */
    protected void broadcastClan(Clan clan, String path, Object... replacements) {
        broadcastClanExcept(clan, null, path, replacements);
    }

    protected void broadcastClanExcept(Clan clan, UUID except, String path, Object... replacements) {
        sendToAll(clan.getMembers(), except, path, replacements);
    }

    protected void sendToAll(Collection<UUID> players, UUID except, String path, Object... replacements) {
        for (UUID uuid : players) {
            if (uuid.equals(except)) {
                continue;
            }
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                lang.send(online, path, replacements);
            }
        }
    }

    /**
     * Wysyła wiadomość do lidera i zastępców klanu będących online
     */
    protected void notifyManagers(Clan clan, String path, Object... replacements) {
        for (UUID uuid : clan.getMembers()) {
            if (clan.isLeaderOrModerator(uuid)) {
                Player online = Bukkit.getPlayer(uuid);
                if (online != null) {
                    lang.send(online, path, replacements);
                }
            }
        }
    }

    protected static String joinArgs(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    protected static String clanNames(List<Clan> clans) {
        return clans.stream().map(Clan::getName).collect(Collectors.joining(", "));
    }

    protected boolean polish() {
        return lang.isPolish();
    }

    protected String confirmWord() {
        return SubCommand.confirmWord(polish());
    }

    protected int countOnline(Clan clan) {
        int online = 0;
        for (UUID uuid : clan.getMembers()) {
            if (Bukkit.getPlayer(uuid) != null) {
                online++;
            }
        }
        return online;
    }

    /**
     * Rozwiązuje klan i powiadamia członków oraz sojuszników
     */
    protected void disbandAndNotify(Clan clan) {
        List<Clan> allies = clanManager.getAllies(clan);
        clanManager.disbandClan(clan);
        sendToAll(clan.getMembers(), null, "clan.disbanded", "clan", clan.getName());
        for (Clan ally : allies) {
            broadcastClan(ally, "alliance.ally-disbanded", "clan", clan.getName());
        }
    }
}
