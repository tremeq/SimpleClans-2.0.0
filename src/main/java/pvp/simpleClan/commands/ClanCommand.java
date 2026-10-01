package pvp.simpleClan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.data.PlayerStats;
import pvp.simpleClan.managers.ChatManager;
import pvp.simpleClan.managers.ChatManager.ChatMode;
import pvp.simpleClan.managers.LangManager;
import pvp.simpleClan.managers.ClanManager.CreateResult;
import pvp.simpleClan.managers.ClanManager.RankingType;

import java.util.*;

public class ClanCommand extends CommandBase implements CommandExecutor {

    private final AllianceCommand allianceCommand;
    private final AdminCommand adminCommand;

    public ClanCommand(SimpleClan plugin) {
        super(plugin);
        this.allianceCommand = new AllianceCommand(plugin);
        this.adminCommand = new AdminCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        SubCommand sub = SubCommand.match(args[0]);
        if (sub == null) {
            lang.send(sender, "unknown-command");
            return true;
        }

        if (!(sender instanceof Player player)) {
            if (!sub.isConsoleAllowed()) {
                lang.send(sender, "player-only");
                return true;
            }
            switch (sub) {
                case INFO -> handleInfo(sender, args);
                case RANKING -> handleRanking(sender, args);
                case STATS -> handleStats(sender, args);
                case ADMIN -> adminCommand.handle(sender, args);
                default -> showHelp(sender);
            }
            return true;
        }

        switch (sub) {
            case CREATE -> handleCreate(player, args);
            case INVITE -> handleInvite(player, args);
            case ACCEPT -> handleAccept(player, args);
            case DENY -> handleDeny(player, args);
            case LEAVE -> handleLeave(player);
            case DISBAND -> handleDisband(player, args);
            case LIST -> handleList(player);
            case INFO -> handleInfo(player, args);
            case PROMOTE -> handlePromote(player, args);
            case DEMOTE -> handleDemote(player, args);
            case KICK -> handleKick(player, args);
            case LEADER -> handleLeader(player, args);
            case CHAT -> handleChat(player, args);
            case CLAN_CHAT -> handleClanChat(player, args);
            case ALLY_CHAT -> handleAllyChat(player, args);
            case COLOR -> handleColor(player, args);
            case PVP -> handlePvP(player, args);
            case RANKING -> handleRanking(player, args);
            case STATS -> handleStats(player, args);
            case ALLY -> allianceCommand.handle(player, args);
            case ADMIN -> adminCommand.handle(player, args);
            case HELP -> showHelp(player);
        }
        return true;
    }

    // ===== Tworzenie i członkostwo =====

    private void handleCreate(Player player, String[] args) {
        if (!checkPermission(player, "simpleclan.create")) {
            return;
        }
        if (args.length < 2) {
            lang.send(player, "usage.create");
            return;
        }

        String clanName = args[1];
        CreateResult result = clanManager.createClan(player, clanName);
        switch (result) {
            case SUCCESS -> lang.send(player, "clan.created", "clan", clanName);
            case ALREADY_IN_CLAN -> lang.send(player, "membership.already-in-clan");
            case NAME_TAKEN -> lang.send(player, "clan.already-exists");
            case TOO_SHORT -> lang.send(player, "name.too-short",
                    "min", plugin.getConfig().getInt("clan.min-name-length", 3));
            case TOO_LONG -> lang.send(player, "name.too-long",
                    "max", plugin.getConfig().getInt("clan.max-name-length", 16));
            case INVALID_CHARACTERS -> lang.send(player, "name.invalid-characters");
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (!checkPermission(player, "simpleclan.invite")) {
            return;
        }
        if (args.length < 2) {
            lang.send(player, "usage.invite");
            return;
        }

        Clan clan = requireLeaderOrModerator(player);
        if (clan == null) {
            return;
        }

        // getPlayerExact - bez dopasowywania po początku nicku
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            lang.send(player, "membership.player-not-found");
            return;
        }
        if (clanManager.hasPlayerClan(target.getUniqueId())) {
            lang.send(player, "membership.player-already-in-clan");
            return;
        }
        if (clanManager.hasInvite(target.getUniqueId(), clan)) {
            lang.send(player, "invite.already-invited", "player", target.getName());
            return;
        }
        if (clanManager.isFull(clan)) {
            lang.send(player, "clan.full");
            return;
        }

        clanManager.invite(clan, target.getUniqueId());
        int seconds = plugin.getConfig().getInt("clan.invite-expire-seconds", 120);
        lang.send(player, "invite.sent", "player", target.getName());
        lang.send(target, "invite.received", "clan", clan.getName(), "seconds", seconds);
    }

    /**
     * Wybiera zaproszenie: po nazwie klanu albo jedyne istniejące
     *
     * @return wybrany klan albo null (komunikat został już wysłany)
     */
    private Clan selectInvite(Player player, String[] args, String usagePath) {
        List<Clan> invites = clanManager.getInvites(player.getUniqueId());
        if (invites.isEmpty()) {
            lang.send(player, "invite.no-invite");
            return null;
        }
        if (args.length >= 2) {
            for (Clan clan : invites) {
                if (clan.getName().equalsIgnoreCase(args[1])) {
                    return clan;
                }
            }
            lang.send(player, "invite.no-invite-from", "clan", args[1]);
            return null;
        }
        if (invites.size() > 1) {
            lang.send(player, "invite.multiple", "clans", clanNames(invites));
            lang.send(player, usagePath);
            return null;
        }
        return invites.get(0);
    }

    private void handleAccept(Player player, String[] args) {
        if (clanManager.hasPlayerClan(player.getUniqueId())) {
            lang.send(player, "membership.already-in-clan");
            return;
        }

        Clan clan = selectInvite(player, args, "usage.accept");
        if (clan == null) {
            return;
        }
        if (clanManager.isFull(clan)) {
            lang.send(player, "clan.full");
            return;
        }

        clanManager.addMember(clan, player.getUniqueId());
        lang.send(player, "invite.joined-clan", "clan", clan.getName());
        broadcastClanExcept(clan, player.getUniqueId(), "invite.accepted", "player", player.getName());
    }

    private void handleDeny(Player player, String[] args) {
        Clan clan = selectInvite(player, args, "usage.deny");
        if (clan == null) {
            return;
        }

        clanManager.removeInvite(player.getUniqueId(), clan);
        lang.send(player, "invite.denied", "clan", clan.getName());
        notifyManagers(clan, "invite.denied-notify", "player", player.getName());
    }

    private void handleLeave(Player player) {
        Clan clan = requireClan(player);
        if (clan == null) {
            return;
        }

        // Lider musi najpierw przekazać przywództwo albo rozwiązać klan
        if (clan.isLeader(player.getUniqueId())) {
            lang.send(player, "leave.leader-cannot-leave");
            return;
        }

        clanManager.removeMember(clan, player.getUniqueId());
        lang.send(player, "leave.left-clan");
        broadcastClan(clan, "leave.player-left", "player", player.getName());
    }

    private void handleDisband(Player player, String[] args) {
        Clan clan = requireLeader(player);
        if (clan == null) {
            return;
        }

        if (args.length >= 2 && SubCommand.isConfirmWord(args[1])) {
            if (!confirmations.consume(player, "disband", clan.getKey())) {
                lang.send(player, "confirm.nothing-pending");
                return;
            }
            disbandAndNotify(clan);
            return;
        }

        confirmations.request(player, "disband", clan.getKey());
        lang.send(player, "disband.confirm",
                "clan", clan.getName(),
                "seconds", confirmations.getTimeoutSeconds(),
                "confirm", confirmWord());
    }

    // ===== Informacje =====

    private void handleList(Player player) {
        Clan clan = requireClan(player);
        if (clan != null) {
            showMembers(player, clan);
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        Clan clan;
        if (args.length >= 2) {
            clan = clanManager.getClan(args[1]);
            if (clan == null) {
                lang.send(sender, "clan.not-found");
                return;
            }
        } else if (sender instanceof Player player) {
            clan = requireClan(player);
            if (clan == null) {
                return;
            }
        } else {
            lang.send(sender, "usage.info");
            return;
        }

        List<String> moderatorNames = new ArrayList<>();
        for (UUID modUuid : clan.getModerators()) {
            moderatorNames.add(statsManager.getName(modUuid));
        }
        Collections.sort(moderatorNames, String.CASE_INSENSITIVE_ORDER);
        String none = lang.get("general.none");

        List<Clan> allies = clanManager.getAllies(clan);

        lang.send(sender, "info.header", "clan", clan.getName());
        lang.sendRaw(sender, "info.leader", "leader", statsManager.getName(clan.getLeader()));
        lang.sendRaw(sender, "info.moderators",
                "moderators", moderatorNames.isEmpty() ? none : String.join(", ", moderatorNames));
        lang.sendRaw(sender, "info.members",
                "count", clan.getMemberCount(), "max", clanManager.getMaxMembers(), "online", countOnline(clan));
        lang.sendRaw(sender, "info.created", "date", clan.getFormattedCreatedDate());
        lang.sendRaw(sender, "info.tag", "tag", clanManager.getClanTag(clan));
        lang.sendRaw(sender, "info.pvp", "status",
                lang.get(clan.isPvpEnabled() ? "pvp.status-enabled" : "pvp.status-disabled"));
        lang.sendRaw(sender, "info.stats",
                "kills", clan.getKills(),
                "deaths", clan.getDeaths(),
                "kdr", PlayerStats.formatKdr(clan.getKdr()),
                "rank", clanManager.getRank(clan));
        if (clanManager.isAllianceEnabled()) {
            lang.sendRaw(sender, "info.allies", "allies", allies.isEmpty() ? none : clanNames(allies));
        }

        sender.sendMessage("");
        showMembers(sender, clan);
    }

    private void showMembers(CommandSender sender, Clan clan) {
        lang.sendRaw(sender, "members.header", "clan", clan.getName(), "count", clan.getMemberCount());

        // Kolejność: lider, zastępcy, członkowie - alfabetycznie w grupach
        List<UUID> members = new ArrayList<>(clan.getMembers());
        members.sort(Comparator
                .comparingInt((UUID uuid) -> clan.isLeader(uuid) ? 0 : clan.isModerator(uuid) ? 1 : 2)
                .thenComparing(statsManager::getName, String.CASE_INSENSITIVE_ORDER));

        for (UUID memberUuid : members) {
            String status;
            if (clan.isLeader(memberUuid)) {
                status = lang.get("members.leader-status");
            } else if (clan.isModerator(memberUuid)) {
                status = lang.get("members.moderator-status");
            } else {
                status = lang.get("members.member-status");
            }
            String online = lang.get(Bukkit.getPlayer(memberUuid) != null ? "members.online" : "members.offline");

            lang.sendRaw(sender, "members.format",
                    "player", statsManager.getName(memberUuid),
                    "status", status,
                    "online", online);
        }
    }

    private void handleRanking(CommandSender sender, String[] args) {
        RankingType type = RankingType.KILLS;
        if (args.length >= 2) {
            type = switch (args[1].toLowerCase(Locale.ROOT)) {
                case "zabojstwa", "zabójstwa", "kills" -> RankingType.KILLS;
                case "kdr" -> RankingType.KDR;
                case "czlonkowie", "członkowie", "members" -> RankingType.MEMBERS;
                default -> null;
            };
            if (type == null) {
                lang.send(sender, "usage.ranking");
                return;
            }
        }

        List<Clan> ranking = clanManager.getRanking(type);
        if (ranking.isEmpty()) {
            lang.send(sender, "ranking.no-clans");
            return;
        }

        int topClans = Math.max(1, plugin.getConfig().getInt("ranking.top-clans", 10));
        List<Clan> top = ranking.subList(0, Math.min(topClans, ranking.size()));

        lang.sendRaw(sender, "ranking.title",
                "type", lang.get("ranking.types." + type.name().toLowerCase(Locale.ROOT)),
                "count", top.size());

        int position = 1;
        for (Clan clan : top) {
            lang.sendRaw(sender, "ranking.format",
                    "position", position++,
                    "clan", clan.getName(),
                    "tag", clanManager.getClanTag(clan),
                    "kills", clan.getKills(),
                    "deaths", clan.getDeaths(),
                    "kdr", PlayerStats.formatKdr(clan.getKdr()),
                    "members", clan.getMemberCount());
        }

        lang.sendRaw(sender, "ranking.footer");
    }

    private void handleStats(CommandSender sender, String[] args) {
        UUID target;
        if (args.length >= 2) {
            target = statsManager.findPlayer(args[1]);
            if (target == null) {
                lang.send(sender, "membership.player-not-found");
                return;
            }
        } else if (sender instanceof Player player) {
            target = player.getUniqueId();
        } else {
            lang.send(sender, "usage.stats");
            return;
        }

        Clan clan = clanManager.getPlayerClan(target);
        lang.send(sender, "stats.header", "player", statsManager.getName(target));
        lang.sendRaw(sender, "stats.kills", "kills", statsManager.getKills(target));
        lang.sendRaw(sender, "stats.deaths", "deaths", statsManager.getDeaths(target));
        lang.sendRaw(sender, "stats.kdr", "kdr", PlayerStats.formatKdr(statsManager.getKdr(target)));
        lang.sendRaw(sender, "stats.clan", "clan", clan != null ? clan.getName() : lang.get("general.none"));
    }

    // ===== Zarządzanie członkami =====

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            lang.send(player, "usage.promote");
            return;
        }
        Clan clan = requireLeader(player);
        if (clan == null) {
            return;
        }

        UUID target = clanManager.findMember(clan, args[1]);
        if (target == null) {
            lang.send(player, "membership.not-member", "player", args[1]);
            return;
        }
        if (clan.isLeader(target)) {
            lang.send(player, "errors.cannot-promote-leader");
            return;
        }
        if (clan.isModerator(target)) {
            lang.send(player, "moderator.already-moderator");
            return;
        }
        int maxModerators = clanManager.getMaxModerators();
        if (clan.getModeratorCount() >= maxModerators) {
            lang.send(player, "moderator.max-moderators", "max", maxModerators);
            return;
        }

        clan.addModerator(target);
        clanManager.saveSoon();

        String targetName = statsManager.getName(target);
        lang.send(player, "moderator.added", "player", targetName);
        Player online = Bukkit.getPlayer(target);
        if (online != null) {
            lang.send(online, "moderator.player-promoted", "clan", clan.getName());
        }
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            lang.send(player, "usage.demote");
            return;
        }
        Clan clan = requireLeader(player);
        if (clan == null) {
            return;
        }

        UUID target = clanManager.findMember(clan, args[1]);
        if (target == null) {
            lang.send(player, "membership.not-member", "player", args[1]);
            return;
        }
        if (clan.isLeader(target)) {
            lang.send(player, "kick.cannot-demote-leader");
            return;
        }
        if (!clan.isModerator(target)) {
            lang.send(player, "moderator.not-moderator");
            return;
        }

        clan.removeModerator(target);
        clanManager.saveSoon();

        lang.send(player, "moderator.removed", "player", statsManager.getName(target));
        Player online = Bukkit.getPlayer(target);
        if (online != null) {
            lang.send(online, "moderator.player-demoted");
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            lang.send(player, "usage.kick");
            return;
        }
        Clan clan = requireLeaderOrModerator(player);
        if (clan == null) {
            return;
        }

        UUID target = clanManager.findMember(clan, args[1]);
        if (target == null) {
            lang.send(player, "membership.not-member", "player", args[1]);
            return;
        }
        if (target.equals(player.getUniqueId())) {
            lang.send(player, "kick.cannot-kick-yourself");
            return;
        }
        if (clan.isLeader(target)) {
            lang.send(player, "kick.cannot-kick-leader");
            return;
        }
        // Tylko lider może wyrzucać zastępców
        if (clan.isModerator(target) && !clan.isLeader(player.getUniqueId())) {
            lang.send(player, "kick.cannot-kick-moderator");
            return;
        }

        String targetName = statsManager.getName(target);
        clanManager.removeMember(clan, target);

        Player online = Bukkit.getPlayer(target);
        if (online != null) {
            lang.send(online, "kick.player-kicked", "clan", clan.getName());
        }
        broadcastClan(clan, "kick.member-kicked", "player", targetName);
    }

    private void handleLeader(Player player, String[] args) {
        if (args.length < 2) {
            lang.send(player, "usage.leader");
            return;
        }
        Clan clan = requireLeader(player);
        if (clan == null) {
            return;
        }

        UUID target = clanManager.findMember(clan, args[1]);
        if (target == null) {
            lang.send(player, "membership.not-member", "player", args[1]);
            return;
        }
        if (clan.isLeader(target)) {
            lang.send(player, "leader.already-leader");
            return;
        }

        String targetName = statsManager.getName(target);
        String confirmTarget = clan.getKey() + ":" + target;

        if (args.length >= 3 && SubCommand.isConfirmWord(args[2])) {
            if (!confirmations.consume(player, "leader", confirmTarget)) {
                lang.send(player, "confirm.nothing-pending");
                return;
            }
            clanManager.transferLeadership(clan, target);
            broadcastClan(clan, "leader.changed", "player", targetName, "old", player.getName());
            return;
        }

        confirmations.request(player, "leader", confirmTarget);
        lang.send(player, "leader.confirm",
                "player", targetName,
                "seconds", confirmations.getTimeoutSeconds(),
                "confirm", confirmWord());
    }

    // ===== Czat =====

    private void handleChat(Player player, String[] args) {
        if (!checkPermission(player, "simpleclan.chat")) {
            return;
        }
        if (args.length < 2) {
            lang.send(player, "usage.chat");
            return;
        }
        Clan clan = requireClan(player);
        if (clan != null) {
            plugin.getChatManager().sendClanMessage(player, clan, joinArgs(args, 1));
        }
    }

    private void handleClanChat(Player player, String[] args) {
        if (!checkPermission(player, "simpleclan.chat")) {
            return;
        }
        Clan clan = requireClan(player);
        if (clan == null) {
            return;
        }

        ChatManager chatManager = plugin.getChatManager();
        if (args.length >= 2) {
            chatManager.sendClanMessage(player, clan, joinArgs(args, 1));
            return;
        }

        ChatMode mode = chatManager.toggle(player.getUniqueId(), ChatMode.CLAN);
        lang.send(player, mode == ChatMode.CLAN ? "chat.toggled-on" : "chat.toggled-off");
    }

    private void handleAllyChat(Player player, String[] args) {
        if (!clanManager.isAllianceEnabled()) {
            lang.send(player, "alliance.disabled");
            return;
        }
        if (!checkPermission(player, "simpleclan.chat")) {
            return;
        }
        Clan clan = requireClan(player);
        if (clan == null) {
            return;
        }

        ChatManager chatManager = plugin.getChatManager();
        boolean switchingOff = chatManager.getMode(player.getUniqueId()) == ChatMode.ALLY && args.length < 2;
        if (!switchingOff && clanManager.getAllies(clan).isEmpty()) {
            lang.send(player, "alliance.no-allies");
            return;
        }

        if (args.length >= 2) {
            chatManager.sendAllyMessage(player, clan, joinArgs(args, 1));
            return;
        }

        ChatMode mode = chatManager.toggle(player.getUniqueId(), ChatMode.ALLY);
        lang.send(player, mode == ChatMode.ALLY ? "chat.ally-toggled-on" : "chat.toggled-off");
    }

    // ===== Ustawienia klanu =====

    private void handleColor(Player player, String[] args) {
        Clan clan = requireLeader(player);
        if (clan == null) {
            return;
        }

        List<String> availableColors = plugin.getConfig().getStringList("clan.available-colors");

        if (args.length < 2) {
            List<String> preview = new ArrayList<>();
            for (String color : availableColors) {
                preview.add(color.replace("&", "") + ": " + LangManager.colorize(color) + clan.getName() + "§r");
            }
            lang.send(player, "color.available", "colors", String.join("§7, ", preview));
            return;
        }

        String colorCode = args[1].toLowerCase(Locale.ROOT);
        if (!colorCode.startsWith("&")) {
            colorCode = "&" + colorCode;
        }

        if (!availableColors.contains(colorCode)) {
            List<String> codes = availableColors.stream().map(c -> c.replace("&", "")).toList();
            lang.send(player, "color.invalid", "colors", String.join(", ", codes));
            return;
        }

        clan.setTagColor(colorCode);
        clanManager.saveSoon();
        lang.send(player, "color.tag-changed", "tag", clanManager.getClanTag(clan));
    }

    private void handlePvP(Player player, String[] args) {
        Clan clan = requireLeader(player);
        if (clan == null) {
            return;
        }
        if (args.length < 2) {
            lang.send(player, "pvp.status", "status",
                    lang.get(clan.isPvpEnabled() ? "pvp.status-enabled" : "pvp.status-disabled"));
            lang.send(player, "usage.pvp");
            return;
        }

        boolean enable;
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "on", "true", "tak", "włącz", "wlacz" -> enable = true;
            case "off", "false", "nie", "wyłącz", "wylacz" -> enable = false;
            default -> {
                lang.send(player, "usage.pvp");
                return;
            }
        }

        clan.setPvpEnabled(enable);
        clanManager.saveSoon();
        broadcastClan(clan, enable ? "pvp.enabled" : "pvp.disabled");
    }

    // ===== Pomoc =====

    private void showHelp(CommandSender sender) {
        lang.sendList(sender, "help.player");
        if (sender.hasPermission("simpleclan.admin")) {
            lang.sendRaw(sender, "help.admin-hint");
        }
    }
}
