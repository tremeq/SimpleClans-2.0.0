package pvp.simpleClan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;

import java.util.Locale;
import java.util.UUID;

/**
 * /klan admin <reload|rozwiaz|wyrzuc|dodaj|lider|resetstaty> - dostępne także z konsoli
 */
class AdminCommand extends CommandBase {

    static final String[][] SUBCOMMANDS = {
            // {polska nazwa, angielska nazwa, aliasy...}
            {"przeladuj", "reload"},
            {"rozwiaz", "disband", "delete"},
            {"wyrzuc", "kick"},
            {"dodaj", "add"},
            {"lider", "setleader", "leader"},
            {"resetstaty", "resetstats"}
    };

    AdminCommand(SimpleClan plugin) {
        super(plugin);
    }

    void handle(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "simpleclan.admin")) {
            return;
        }
        if (args.length < 2) {
            lang.sendList(sender, "help.admin");
            return;
        }

        String sub = resolve(args[1]);
        if (sub == null) {
            lang.sendList(sender, "help.admin");
            return;
        }

        switch (sub) {
            case "przeladuj" -> handleReload(sender);
            case "rozwiaz" -> handleDisband(sender, args);
            case "wyrzuc" -> handleKick(sender, args);
            case "dodaj" -> handleAdd(sender, args);
            case "lider" -> handleSetLeader(sender, args);
            case "resetstaty" -> handleResetStats(sender, args);
        }
    }

    static String resolve(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        for (String[] names : SUBCOMMANDS) {
            for (String name : names) {
                if (name.equals(lower)) {
                    return names[0];
                }
            }
        }
        return null;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        lang.send(sender, "admin.reloaded", "lang", lang.getCurrentLanguage());
    }

    private void handleDisband(CommandSender sender, String[] args) {
        if (args.length < 3) {
            lang.send(sender, "usage.admin-disband");
            return;
        }
        Clan clan = clanManager.getClan(args[2]);
        if (clan == null) {
            lang.send(sender, "clan.not-found");
            return;
        }

        if (args.length >= 4 && SubCommand.isConfirmWord(args[3])) {
            if (!confirmations.consume(sender, "admin-disband", clan.getKey())) {
                lang.send(sender, "confirm.nothing-pending");
                return;
            }
            disbandAndNotify(clan);
            lang.send(sender, "admin.disbanded", "clan", clan.getName());
            return;
        }

        confirmations.request(sender, "admin-disband", clan.getKey());
        lang.send(sender, "admin.disband-confirm",
                "clan", clan.getName(),
                "seconds", confirmations.getTimeoutSeconds(),
                "confirm", confirmWord());
    }

    /**
     * Szuka gracza po dokładnym nicku - również wśród członków klanów bez zapisanych statystyk
     */
    private UUID findAnyPlayer(String name) {
        UUID uuid = statsManager.findPlayer(name);
        if (uuid != null) {
            return uuid;
        }
        for (UUID member : clanManager.getPlayersInClans()) {
            if (statsManager.getName(member).equalsIgnoreCase(name)) {
                return member;
            }
        }
        return null;
    }

    private void handleKick(CommandSender sender, String[] args) {
        if (args.length < 3) {
            lang.send(sender, "usage.admin-kick");
            return;
        }
        UUID target = findAnyPlayer(args[2]);
        if (target == null) {
            lang.send(sender, "membership.player-not-found");
            return;
        }
        Clan clan = clanManager.getPlayerClan(target);
        if (clan == null) {
            lang.send(sender, "admin.player-not-in-clan", "player", statsManager.getName(target));
            return;
        }

        String targetName = statsManager.getName(target);

        if (clan.isLeader(target)) {
            UUID successor = clanManager.pickSuccessor(clan, target);
            if (successor == null) {
                // Lider był jedynym członkiem - klan przestaje istnieć
                disbandAndNotify(clan);
                lang.send(sender, "admin.disbanded", "clan", clan.getName());
                return;
            }
            clanManager.transferLeadership(clan, successor);
            broadcastClan(clan, "leader.changed", "player", statsManager.getName(successor), "old", targetName);
        }

        clanManager.removeMember(clan, target);

        Player online = Bukkit.getPlayer(target);
        if (online != null) {
            lang.send(online, "kick.player-kicked", "clan", clan.getName());
        }
        broadcastClan(clan, "kick.member-kicked", "player", targetName);
        lang.send(sender, "admin.kicked", "player", targetName, "clan", clan.getName());
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 4) {
            lang.send(sender, "usage.admin-add");
            return;
        }
        Clan clan = clanManager.getClan(args[2]);
        if (clan == null) {
            lang.send(sender, "clan.not-found");
            return;
        }
        UUID target = findAnyPlayer(args[3]);
        if (target == null) {
            lang.send(sender, "membership.player-not-found");
            return;
        }
        if (clanManager.hasPlayerClan(target)) {
            lang.send(sender, "membership.player-already-in-clan");
            return;
        }

        // Admin może dodać gracza ponad limit członków
        String targetName = statsManager.getName(target);
        clanManager.addMember(clan, target);

        Player online = Bukkit.getPlayer(target);
        if (online != null) {
            lang.send(online, "invite.joined-clan", "clan", clan.getName());
        }
        broadcastClanExcept(clan, target, "invite.accepted", "player", targetName);
        lang.send(sender, "admin.added", "player", targetName, "clan", clan.getName());
    }

    private void handleSetLeader(CommandSender sender, String[] args) {
        if (args.length < 4) {
            lang.send(sender, "usage.admin-leader");
            return;
        }
        Clan clan = clanManager.getClan(args[2]);
        if (clan == null) {
            lang.send(sender, "clan.not-found");
            return;
        }
        UUID target = clanManager.findMember(clan, args[3]);
        if (target == null) {
            lang.send(sender, "membership.not-member", "player", args[3]);
            return;
        }
        if (clan.isLeader(target)) {
            lang.send(sender, "leader.already-leader");
            return;
        }

        String oldLeader = statsManager.getName(clan.getLeader());
        String newLeader = statsManager.getName(target);
        clanManager.transferLeadership(clan, target);
        broadcastClan(clan, "leader.changed", "player", newLeader, "old", oldLeader);
        lang.send(sender, "admin.leader-set", "player", newLeader, "clan", clan.getName());
    }

    private void handleResetStats(CommandSender sender, String[] args) {
        if (args.length < 4) {
            lang.send(sender, "usage.admin-resetstats");
            return;
        }

        switch (args[2].toLowerCase(Locale.ROOT)) {
            case "klan", "clan" -> {
                Clan clan = clanManager.getClan(args[3]);
                if (clan == null) {
                    lang.send(sender, "clan.not-found");
                    return;
                }
                clan.resetStats();
                clanManager.saveSoon();
                lang.send(sender, "admin.stats-reset-clan", "clan", clan.getName());
            }
            case "gracz", "player" -> {
                UUID target = findAnyPlayer(args[3]);
                if (target == null) {
                    lang.send(sender, "membership.player-not-found");
                    return;
                }
                statsManager.resetStats(target);
                lang.send(sender, "admin.stats-reset-player", "player", statsManager.getName(target));
            }
            default -> lang.send(sender, "usage.admin-resetstats");
        }
    }
}
