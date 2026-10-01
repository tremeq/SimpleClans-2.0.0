package pvp.simpleClan.commands;

import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager.AllyResult;

import java.util.List;
import java.util.Locale;

/**
 * /klan sojusz <zapros|akceptuj|odrzuc|anuluj|zerwij|lista>
 */
class AllianceCommand extends CommandBase {

    static final String[][] SUBCOMMANDS = {
            // {polska nazwa, angielska nazwa, aliasy...}
            {"zapros", "invite", "request"},
            {"akceptuj", "accept"},
            {"odrzuc", "deny", "decline"},
            {"anuluj", "cancel"},
            {"zerwij", "break", "remove"},
            {"lista", "list"}
    };

    AllianceCommand(SimpleClan plugin) {
        super(plugin);
    }

    void handle(Player player, String[] args) {
        if (!clanManager.isAllianceEnabled()) {
            lang.send(player, "alliance.disabled");
            return;
        }
        if (!checkPermission(player, "simpleclan.alliance")) {
            return;
        }
        if (args.length < 2) {
            lang.sendList(player, "help.alliance");
            return;
        }

        String sub = resolve(args[1]);
        if (sub == null) {
            lang.sendList(player, "help.alliance");
            return;
        }

        switch (sub) {
            case "zapros" -> handleRequest(player, args);
            case "akceptuj" -> handleAccept(player, args);
            case "odrzuc" -> handleDeny(player, args);
            case "anuluj" -> handleCancel(player, args);
            case "zerwij" -> handleBreak(player, args);
            case "lista" -> handleList(player);
        }
    }

    /**
     * Zamienia dowolną nazwę/alias na polską nazwę podkomendy
     */
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

    private String maxAlliesText() {
        int max = clanManager.getMaxAllies();
        return max < 0 ? "∞" : String.valueOf(max);
    }

    /**
     * Pobiera klan docelowy z argumentu i sprawdza, czy to nie własny klan
     */
    private Clan requireTargetClan(Player player, Clan own, String[] args, String usagePath) {
        if (args.length < 3) {
            lang.send(player, usagePath);
            return null;
        }
        Clan target = clanManager.getClan(args[2]);
        if (target == null) {
            lang.send(player, "clan.not-found");
            return null;
        }
        if (target.equals(own)) {
            lang.send(player, "alliance.self");
            return null;
        }
        return target;
    }

    private void handleRequest(Player player, String[] args) {
        Clan own = requireLeader(player);
        if (own == null) {
            return;
        }
        Clan target = requireTargetClan(player, own, args, "usage.ally-invite");
        if (target == null) {
            return;
        }

        AllyResult result = clanManager.requestAlliance(own, target);
        switch (result) {
            case SENT -> {
                int seconds = plugin.getConfig().getInt("alliance.request-expire-seconds", 300);
                lang.send(player, "alliance.request-sent", "clan", target.getName());
                notifyManagers(target, "alliance.request-received", "clan", own.getName(), "seconds", seconds);
            }
            case FORMED -> announceFormed(own, target);
            case SELF -> lang.send(player, "alliance.self");
            case ALREADY_ALLIED -> lang.send(player, "alliance.already-allied", "clan", target.getName());
            case ALREADY_REQUESTED -> lang.send(player, "alliance.already-requested", "clan", target.getName());
            case LIMIT_OWN -> lang.send(player, "alliance.limit-own", "max", maxAlliesText());
            case LIMIT_OTHER -> lang.send(player, "alliance.limit-other", "clan", target.getName(), "max", maxAlliesText());
            default -> {
            }
        }
    }

    /**
     * Wybiera prośbę o sojusz: po nazwie klanu albo jedyną oczekującą
     */
    private Clan selectRequest(Player player, Clan own, String[] args, String usagePath) {
        List<Clan> incoming = clanManager.getIncomingAllyRequests(own);
        if (args.length >= 3) {
            Clan from = clanManager.getClan(args[2]);
            if (from == null || !incoming.contains(from)) {
                lang.send(player, "alliance.no-request", "clan", args[2]);
                return null;
            }
            return from;
        }
        if (incoming.isEmpty()) {
            lang.send(player, "alliance.no-requests");
            return null;
        }
        if (incoming.size() > 1) {
            lang.send(player, "alliance.incoming", "clans", clanNames(incoming));
            lang.send(player, usagePath);
            return null;
        }
        return incoming.get(0);
    }

    private void handleAccept(Player player, String[] args) {
        Clan own = requireLeader(player);
        if (own == null) {
            return;
        }
        Clan from = selectRequest(player, own, args, "usage.ally-accept");
        if (from == null) {
            return;
        }

        AllyResult result = clanManager.acceptAlliance(own, from);
        switch (result) {
            case FORMED -> announceFormed(own, from);
            case NO_REQUEST -> lang.send(player, "alliance.no-request", "clan", from.getName());
            case ALREADY_ALLIED -> lang.send(player, "alliance.already-allied", "clan", from.getName());
            case LIMIT_OWN -> lang.send(player, "alliance.limit-own", "max", maxAlliesText());
            case LIMIT_OTHER -> lang.send(player, "alliance.limit-other", "clan", from.getName(), "max", maxAlliesText());
            default -> {
            }
        }
    }

    private void handleDeny(Player player, String[] args) {
        Clan own = requireLeader(player);
        if (own == null) {
            return;
        }
        Clan from = selectRequest(player, own, args, "usage.ally-deny");
        if (from == null) {
            return;
        }

        if (clanManager.denyAlliance(own, from)) {
            lang.send(player, "alliance.request-denied", "clan", from.getName());
            notifyManagers(from, "alliance.request-denied-notify", "clan", own.getName());
        } else {
            lang.send(player, "alliance.no-request", "clan", from.getName());
        }
    }

    private void handleCancel(Player player, String[] args) {
        Clan own = requireLeader(player);
        if (own == null) {
            return;
        }
        Clan target = requireTargetClan(player, own, args, "usage.ally-cancel");
        if (target == null) {
            return;
        }

        // Anulowanie = usunięcie własnej prośby z listy oczekujących klanu docelowego
        if (clanManager.denyAlliance(target, own)) {
            lang.send(player, "alliance.request-cancelled", "clan", target.getName());
            notifyManagers(target, "alliance.request-cancelled-notify", "clan", own.getName());
        } else {
            lang.send(player, "alliance.no-outgoing-request", "clan", target.getName());
        }
    }

    private void handleBreak(Player player, String[] args) {
        Clan own = requireLeader(player);
        if (own == null) {
            return;
        }
        Clan target = requireTargetClan(player, own, args, "usage.ally-break");
        if (target == null) {
            return;
        }

        if (!clanManager.breakAlliance(own, target)) {
            lang.send(player, "alliance.not-allied", "clan", target.getName());
            return;
        }
        broadcastClan(own, "alliance.broken", "clan", target.getName());
        broadcastClan(target, "alliance.broken", "clan", own.getName());
    }

    private void handleList(Player player) {
        Clan own = requireClan(player);
        if (own == null) {
            return;
        }

        List<Clan> allies = clanManager.getAllies(own);
        lang.send(player, "alliance.list-header", "count", allies.size(), "max", maxAlliesText());
        if (allies.isEmpty()) {
            lang.sendRaw(player, "alliance.list-none");
        }
        for (Clan ally : allies) {
            lang.sendRaw(player, "alliance.list-entry",
                    "clan", ally.getName(),
                    "tag", clanManager.getClanTag(ally),
                    "leader", statsManager.getName(ally.getLeader()),
                    "members", ally.getMemberCount(),
                    "online", countOnline(ally));
        }

        // Oczekujące prośby widzi tylko lider i zastępcy
        if (own.isLeaderOrModerator(player.getUniqueId())) {
            List<Clan> incoming = clanManager.getIncomingAllyRequests(own);
            if (!incoming.isEmpty()) {
                lang.sendRaw(player, "alliance.list-incoming", "clans", clanNames(incoming));
            }
            List<Clan> outgoing = clanManager.getOutgoingAllyRequests(own);
            if (!outgoing.isEmpty()) {
                lang.sendRaw(player, "alliance.list-outgoing", "clans", clanNames(outgoing));
            }
        }
    }

    private void announceFormed(Clan a, Clan b) {
        broadcastClan(a, "alliance.formed", "clan", b.getName());
        broadcastClan(b, "alliance.formed", "clan", a.getName());
    }
}
