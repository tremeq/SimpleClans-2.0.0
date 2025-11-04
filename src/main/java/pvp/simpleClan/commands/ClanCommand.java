package pvp.simpleClan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClanCommand implements CommandExecutor {

    private final SimpleClan plugin;
    private final ClanManager clanManager;

    public ClanCommand(SimpleClan plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "stworz":
            case "create":
                handleCreate(player, args);
                break;
            case "zapros":
            case "invite":
                handleInvite(player, args);
                break;
            case "akceptuj":
            case "accept":
                handleAccept(player);
                break;
            case "opusc":
            case "leave":
                handleLeave(player);
                break;
            case "rozwiaz":
            case "disband":
                handleDisband(player);
                break;
            case "lista":
            case "list":
                handleList(player);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "zastepca":
            case "mod":
            case "moderator":
                handlePromote(player, args);
                break;
            case "degraduj":
            case "demote":
                handleDemote(player, args);
                break;
            case "wyrzuc":
            case "kick":
                handleKick(player, args);
                break;
            case "chat":
            case "c":
                handleChat(player, args);
                break;
            case "cc":
            case "clanchat":
                handleClanChatToggle(player);
                break;
            case "kolor":
            case "color":
                handleColor(player, args);
                break;
            case "pvp":
                handlePvP(player, args);
                break;
            case "ranking":
            case "top":
                handleRanking(player);
                break;
            case "pomoc":
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage(plugin.getPrefix() + plugin.getMessage("unknown-command"));
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("simpleclan.create")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan stworz <nazwa>");
            return;
        }

        if (clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("already-in-clan"));
            return;
        }

        String clanName = args[1];

        // Sprawdź długość nazwy
        int minLength = plugin.getConfig().getInt("clan.min-name-length", 3);
        int maxLength = plugin.getConfig().getInt("clan.max-name-length", 16);

        if (clanName.length() < minLength) {
            player.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("name-too-short").replace("{min}", String.valueOf(minLength)));
            return;
        }

        if (clanName.length() > maxLength) {
            player.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("name-too-long").replace("{max}", String.valueOf(maxLength)));
            return;
        }

        if (clanManager.createClan(player, clanName)) {
            player.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("clan-created").replace("{clan}", clanName));
        } else {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("clan-already-exists"));
        }
    }

    private void handleInvite(Player player, String[] args) {
        if (!player.hasPermission("simpleclan.invite")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan zapros <gracz>");
            return;
        }

        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeaderOrModerator(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-leader-or-mod"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return;
        }

        if (clanManager.hasPlayerClan(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-already-in-clan"));
            return;
        }

        // Sprawdź czy klan nie jest pełny
        int maxMembers = plugin.getConfig().getInt("clan.max-members", 10);
        if (clan.getMemberCount() >= maxMembers) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("clan-full"));
            return;
        }

        clanManager.invitePlayer(player, target);

        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("invite-sent").replace("{player}", target.getName()));
        target.sendMessage(plugin.getPrefix() +
                plugin.getMessage("invite-received").replace("{clan}", clan.getName()));
    }

    private void handleAccept(Player player) {
        if (!clanManager.hasInvite(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-invite"));
            return;
        }

        if (clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("already-in-clan"));
            return;
        }

        String clanName = clanManager.getInviteClanName(player.getUniqueId());
        Clan clan = clanManager.getClan(clanName);

        if (clanManager.acceptInvite(player)) {
            player.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("joined-clan").replace("{clan}", clan.getName()));
        } else {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("clan-full"));
        }
    }

    private void handleLeave(Player player) {
        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        if (clanManager.leaveClan(player)) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("left-clan"));
        }
    }

    private void handleDisband(Player player) {
        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-leader"));
            return;
        }

        clanManager.disbandClan(player);
    }

    private void handleList(Player player) {
        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("members-header").replace("{clan}", clan.getName()));

        for (UUID memberUuid : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUuid);
            String playerName;
            if (member != null) {
                playerName = member.getName();
            } else {
                String offlineName = Bukkit.getOfflinePlayer(memberUuid).getName();
                playerName = offlineName != null ? offlineName : "Unknown";
            }

            String status;
            if (clan.isLeader(memberUuid)) {
                status = plugin.getMessage("leader-status");
            } else if (clan.isModerator(memberUuid)) {
                status = plugin.getMessage("moderator-status");
            } else {
                status = plugin.getMessage("member-status");
            }

            player.sendMessage(plugin.getMessage("member-format")
                    .replace("{player}", playerName)
                    .replace("{status}", status));
        }
    }

    private void handleInfo(Player player, String[] args) {
        Clan clan;

        if (args.length < 2) {
            // Pokaż info o własnym klanie
            if (!clanManager.hasPlayerClan(player.getUniqueId())) {
                player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
                return;
            }
            clan = clanManager.getPlayerClan(player.getUniqueId());
        } else {
            // Pokaż info o konkretnym klanie
            clan = clanManager.getClan(args[1]);
            if (clan == null) {
                player.sendMessage(plugin.getPrefix() + plugin.getMessage("clan-not-found"));
                return;
            }
        }

        Player leader = Bukkit.getPlayer(clan.getLeader());
        String leaderName;
        if (leader != null) {
            leaderName = leader.getName();
        } else {
            String offlineName = Bukkit.getOfflinePlayer(clan.getLeader()).getName();
            leaderName = offlineName != null ? offlineName : "Unknown";
        }
        int maxMembers = plugin.getConfig().getInt("clan.max-members", 10);

        // Lista zastępców
        StringBuilder moderators = new StringBuilder();
        for (UUID modUuid : clan.getModerators()) {
            Player mod = Bukkit.getPlayer(modUuid);
            String modName;
            if (mod != null) {
                modName = mod.getName();
            } else {
                String offlineName = Bukkit.getOfflinePlayer(modUuid).getName();
                modName = offlineName != null ? offlineName : "Unknown";
            }
            if (moderators.length() > 0) {
                moderators.append(", ");
            }
            moderators.append(modName);
        }
        String moderatorsText = moderators.length() > 0 ? moderators.toString() : "Brak";

        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("clan-info-header").replace("{clan}", clan.getName()));
        player.sendMessage(plugin.getMessage("clan-info-leader").replace("{leader}", leaderName));
        player.sendMessage(plugin.getMessage("clan-info-moderators").replace("{moderators}", moderatorsText));
        player.sendMessage(plugin.getMessage("clan-info-members")
                .replace("{count}", String.valueOf(clan.getMemberCount()))
                .replace("{max}", String.valueOf(maxMembers)));
        player.sendMessage(plugin.getMessage("clan-info-created").replace("{date}", clan.getFormattedCreatedDate()));
        player.sendMessage(plugin.getMessage("info.tag-color")
                .replace("{color}", clan.getTagColor())
                .replace("{clan}", clan.getName()));
        player.sendMessage(plugin.getMessage("info.pvp")
                .replace("{status}", clan.isPvpEnabled() ?
                        plugin.getMessage("pvp.status-enabled") : plugin.getMessage("pvp.status-disabled")));
        player.sendMessage(plugin.getMessage("info.kills")
                .replace("{kills}", String.valueOf(clan.getKills())));

        // Dodaj listę wszystkich członków
        player.sendMessage("");
        player.sendMessage(plugin.getMessage("members-header").replace("{clan}", clan.getName()));

        for (UUID memberUuid : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUuid);
            String playerName;
            if (member != null) {
                playerName = member.getName();
            } else {
                String offlineName = Bukkit.getOfflinePlayer(memberUuid).getName();
                playerName = offlineName != null ? offlineName : "Unknown";
            }

            String status;
            if (clan.isLeader(memberUuid)) {
                status = plugin.getMessage("leader-status");
            } else if (clan.isModerator(memberUuid)) {
                status = plugin.getMessage("moderator-status");
            } else {
                status = plugin.getMessage("member-status");
            }

            player.sendMessage(plugin.getMessage("member-format")
                    .replace("{player}", playerName)
                    .replace("{status}", status));
        }
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan zastepca <gracz>");
            return;
        }

        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-leader"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return;
        }

        if (!clan.isMember(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return;
        }

        if (clan.isLeader(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + "&cNie możesz mianować lidera zastępcą!");
            return;
        }

        if (clan.isModerator(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("already-moderator"));
            return;
        }

        int maxModerators = plugin.getConfig().getInt("clan.max-moderators", 2);
        if (clan.getModeratorCount() >= maxModerators) {
            player.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("max-moderators").replace("{max}", String.valueOf(maxModerators)));
            return;
        }

        clan.addModerator(target.getUniqueId());
        clanManager.saveData();

        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("mod-added").replace("{player}", target.getName()));
        target.sendMessage(plugin.getPrefix() +
                plugin.getMessage("player-promoted").replace("{clan}", clan.getName()));
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan degraduj <gracz>");
            return;
        }

        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-leader"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return;
        }

        if (!clan.isMember(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return;
        }

        if (clan.isLeader(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("cannot-demote-leader"));
            return;
        }

        if (!clan.isModerator(target.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-moderator"));
            return;
        }

        clan.removeModerator(target.getUniqueId());
        clanManager.saveData();

        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("mod-removed").replace("{player}", target.getName()));
        target.sendMessage(plugin.getPrefix() + plugin.getMessage("player-demoted"));
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan wyrzuc <gracz>");
            return;
        }

        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeaderOrModerator(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("not-leader-or-mod"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUuid;
        String targetName;

        if (target != null && target.isOnline()) {
            targetUuid = target.getUniqueId();
            targetName = target.getName();
        } else {
            // Sprawdź offline gracza
            targetName = args[1];
            boolean found = false;
            targetUuid = null;

            for (UUID memberUuid : clan.getMembers()) {
                Player onlineMember = Bukkit.getPlayer(memberUuid);
                String memberName;
                if (onlineMember != null) {
                    memberName = onlineMember.getName();
                } else {
                    memberName = Bukkit.getOfflinePlayer(memberUuid).getName();
                }
                if (memberName != null && memberName.equalsIgnoreCase(targetName)) {
                    targetUuid = memberUuid;
                    targetName = memberName;
                    found = true;
                    break;
                }
            }

            if (!found) {
                player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
                return;
            }
        }

        if (!clan.isMember(targetUuid)) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return;
        }

        if (targetUuid.equals(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("cannot-kick-yourself"));
            return;
        }

        if (clan.isLeader(targetUuid)) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("cannot-kick-leader"));
            return;
        }

        // Tylko lider może wyrzucać zastępców
        if (clan.isModerator(targetUuid) && !clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("cannot-kick-moderator"));
            return;
        }

        // Usuń gracza z klanu
        clan.removeMember(targetUuid);
        clanManager.removePlayerFromClan(targetUuid);
        clanManager.saveData();

        // Powiadom wszystkich
        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("member-kicked").replace("{player}", targetName));

        if (target != null && target.isOnline()) {
            target.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("player-kicked").replace("{clan}", clan.getName()));
        }

        // Powiadom pozostałych członków klanu
        for (UUID memberUuid : clan.getMembers()) {
            if (!memberUuid.equals(player.getUniqueId())) {
                Player member = Bukkit.getPlayer(memberUuid);
                if (member != null && member.isOnline()) {
                    member.sendMessage(plugin.getPrefix() +
                            plugin.getMessage("member-kicked").replace("{player}", targetName));
                }
            }
        }
    }

    private void handleChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan chat <wiadomość>");
            return;
        }

        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("membership.not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());

        // Złącz wszystkie argumenty od indeksu 1 w jedną wiadomość
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

        // Pobierz format i rolę gracza
        String format = plugin.getConfig().getString("chat.format",
                "&8[&6Klan&8] &r{role}&e{player}&7: &f{message}");

        String role = "";
        if (clan.isLeader(player.getUniqueId())) {
            role = plugin.getConfig().getString("chat.leader-role", "&6[Lider] ");
        } else if (clan.isModerator(player.getUniqueId())) {
            role = plugin.getConfig().getString("chat.moderator-role", "&a[Zastępca] ");
        }

        // Sformatuj wiadomość
        String formattedMessage = format
                .replace("{role}", role)
                .replace("{player}", player.getName())
                .replace("{message}", message)
                .replace("&", "§");

        // Wyślij do wszystkich członków klanu
        for (Player member : Bukkit.getOnlinePlayers()) {
            if (clan.isMember(member.getUniqueId())) {
                member.sendMessage(formattedMessage);
            }
        }
    }

    private void handleClanChatToggle(Player player) {
        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("membership.not-in-clan"));
            return;
        }

        boolean enabled = plugin.getChatListener().getChatManager().toggleClanChat(player.getUniqueId());

        if (enabled) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("chat.toggled-on"));
        } else {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("chat.toggled-off"));
        }
    }

    private void handleColor(Player player, String[] args) {
        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("membership.not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("membership.not-leader"));
            return;
        }

        // Jeśli brak argumentu, pokaż dostępne kolory
        if (args.length < 2) {
            List<String> availableColors = plugin.getConfig().getStringList("clan.available-colors");
            StringBuilder colorsDisplay = new StringBuilder();
            for (String color : availableColors) {
                colorsDisplay.append(color.replace("&", "§")).append(clan.getName()).append("&r, ");
            }
            // Usuń ostatni przecinek
            if (colorsDisplay.length() > 2) {
                colorsDisplay.setLength(colorsDisplay.length() - 2);
            }

            player.sendMessage(plugin.getPrefix() +
                    plugin.getMessage("color.available").replace("{colors}", colorsDisplay.toString()));
            return;
        }

        String colorCode = args[1];

        // Dodaj & jeśli nie ma
        if (!colorCode.startsWith("&")) {
            colorCode = "&" + colorCode;
        }

        // Sprawdź czy kolor jest dostępny
        List<String> availableColors = plugin.getConfig().getStringList("clan.available-colors");
        if (!availableColors.contains(colorCode)) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("color.invalid")
                    .replace("{colors}", String.join(", ", availableColors)));
            return;
        }

        // Ustaw kolor
        clan.setTagColor(colorCode);
        clanManager.saveData();

        player.sendMessage(plugin.getPrefix() +
                plugin.getMessage("color.changed")
                        .replace("{color}", colorCode.replace("&", "§"))
                        .replace("{clan}", clan.getName()));
    }

    private void handlePvP(Player player, String[] args) {
        if (!clanManager.hasPlayerClan(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("membership.not-in-clan"));
            return;
        }

        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("membership.not-leader"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan pvp <on/off>");
            return;
        }

        boolean enable;
        String arg = args[1].toLowerCase();

        if (arg.equals("on") || arg.equals("true") || arg.equals("tak") || arg.equals("włącz")) {
            enable = true;
        } else if (arg.equals("off") || arg.equals("false") || arg.equals("nie") || arg.equals("wyłącz")) {
            enable = false;
        } else {
            player.sendMessage(plugin.getPrefix() + "§cUżycie: /klan pvp <on/off>");
            return;
        }

        clan.setPvpEnabled(enable);
        clanManager.saveData();

        if (enable) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("pvp.enabled"));
        } else {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("pvp.disabled"));
        }

        // Powiadom wszystkich członków klanu
        for (Player member : Bukkit.getOnlinePlayers()) {
            if (clan.isMember(member.getUniqueId()) && !member.equals(player)) {
                if (enable) {
                    member.sendMessage(plugin.getPrefix() + plugin.getMessage("pvp.enabled"));
                } else {
                    member.sendMessage(plugin.getPrefix() + plugin.getMessage("pvp.disabled"));
                }
            }
        }
    }

    private void handleRanking(Player player) {
        int topClans = plugin.getConfig().getInt("ranking.top-clans", 10);

        // Pobierz wszystkie klany i posortuj po zabójstwach
        List<Clan> sortedClans = new ArrayList<>(clanManager.getAllClans());
        sortedClans.sort((c1, c2) -> Integer.compare(c2.getKills(), c1.getKills()));

        // Ogranicz do top N
        if (sortedClans.size() > topClans) {
            sortedClans = sortedClans.subList(0, topClans);
        }

        if (sortedClans.isEmpty()) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("ranking.no-clans"));
            return;
        }

        player.sendMessage(plugin.getMessage("ranking.header"));

        int position = 1;
        for (Clan clan : sortedClans) {
            player.sendMessage(plugin.getMessage("ranking.format")
                    .replace("{position}", String.valueOf(position))
                    .replace("{clan}", clan.getName())
                    .replace("{kills}", String.valueOf(clan.getKills())));
            position++;
        }

        player.sendMessage(plugin.getMessage("ranking.footer"));
    }

    private void showHelp(Player player) {
        player.sendMessage(plugin.getPrefix() + plugin.getMessage("help.header"));

        String format = plugin.getMessage("help.format");

        // Lista wszystkich komend z opisami z lang file
        String[][] commands = {
                {"klan stworz <nazwa>", plugin.getMessage("help.commands.create")},
                {"klan zapros <gracz>", plugin.getMessage("help.commands.invite")},
                {"klan akceptuj", plugin.getMessage("help.commands.accept")},
                {"klan opusc", plugin.getMessage("help.commands.leave")},
                {"klan rozwiaz", plugin.getMessage("help.commands.disband")},
                {"klan lista", plugin.getMessage("help.commands.list")},
                {"klan info [klan]", plugin.getMessage("help.commands.info")},
                {"klan zastepca <gracz>", plugin.getMessage("help.commands.promote")},
                {"klan degraduj <gracz>", plugin.getMessage("help.commands.demote")},
                {"klan wyrzuc <gracz>", plugin.getMessage("help.commands.kick")},
                {"klan chat <wiadomość>", plugin.getMessage("help.commands.chat")},
                {"klan cc", plugin.getMessage("help.commands.cc")},
                {"klan kolor [kolor]", plugin.getMessage("help.commands.color")},
                {"klan pvp <on/off>", plugin.getMessage("help.commands.pvp")},
                {"klan ranking", plugin.getMessage("help.commands.ranking")}
        };

        for (String[] cmd : commands) {
            player.sendMessage(format
                    .replace("{command}", cmd[0])
                    .replace("{description}", cmd[1]));
        }
    }
}