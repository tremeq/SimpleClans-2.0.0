package pvp.simpleClan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;
import pvp.simpleClan.managers.StatsManager;

import java.util.*;
import java.util.stream.Stream;

/**
 * Tab completion dla komendy /klan - podpowiada nazwy w języku ustawionym w configu
 */
public class ClanTabCompleter implements TabCompleter {

    private final SimpleClan plugin;

    public ClanTabCompleter(SimpleClan plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean polish = plugin.getLangManager().isPolish();
        ClanManager clanManager = plugin.getClanManager();
        Player player = sender instanceof Player p ? p : null;
        Clan clan = player != null ? clanManager.getPlayerClan(player.getUniqueId()) : null;

        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (SubCommand sub : SubCommand.values()) {
                if (player == null && !sub.isConsoleAllowed()) {
                    continue;
                }
                if (sub == SubCommand.ADMIN && !sender.hasPermission("simpleclan.admin")) {
                    continue;
                }
                if ((sub == SubCommand.ALLY || sub == SubCommand.ALLY_CHAT) && !clanManager.isAllianceEnabled()) {
                    continue;
                }
                names.add(sub.getName(polish));
            }
            return filter(names.stream(), args[0]);
        }

        SubCommand sub = SubCommand.match(args[0]);
        if (sub == null) {
            return Collections.emptyList();
        }

        if (sub == SubCommand.ADMIN) {
            return sender.hasPermission("simpleclan.admin") ? completeAdmin(args, polish) : Collections.emptyList();
        }
        if (sub == SubCommand.ALLY) {
            return clan != null ? completeAlly(clan, args, polish) : Collections.emptyList();
        }

        if (args.length == 2) {
            return switch (sub) {
                case INVITE -> filter(Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !clanManager.hasPlayerClan(p.getUniqueId()))
                        .map(Player::getName), args[1]);
                case ACCEPT, DENY -> player != null
                        ? filter(clanManager.getInvites(player.getUniqueId()).stream().map(Clan::getName), args[1])
                        : Collections.emptyList();
                case INFO -> filter(allClanNames(), args[1]);
                case PROMOTE, DEMOTE, KICK, LEADER -> clan != null
                        ? filter(memberNames(clan, player), args[1])
                        : Collections.emptyList();
                case STATS -> filter(Bukkit.getOnlinePlayers().stream().map(Player::getName), args[1]);
                case COLOR -> filter(plugin.getConfig().getStringList("clan.available-colors").stream()
                        .map(c -> c.replace("&", "")), args[1]);
                case PVP -> filter(Stream.of("on", "off"), args[1]);
                case RANKING -> filter(polish
                        ? Stream.of("zabojstwa", "kdr", "czlonkowie")
                        : Stream.of("kills", "kdr", "members"), args[1]);
                case DISBAND -> filter(Stream.of(SubCommand.confirmWord(polish)), args[1]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3 && sub == SubCommand.LEADER) {
            return filter(Stream.of(SubCommand.confirmWord(polish)), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> completeAlly(Clan clan, String[] args, boolean polish) {
        ClanManager clanManager = plugin.getClanManager();
        if (args.length == 2) {
            return filter(Arrays.stream(AllianceCommand.SUBCOMMANDS).map(n -> polish ? n[0] : n[1]), args[1]);
        }
        if (args.length == 3) {
            String sub = AllianceCommand.resolve(args[1]);
            if (sub == null) {
                return Collections.emptyList();
            }
            Stream<String> names = switch (sub) {
                case "zapros" -> clanManager.getAllClans().stream()
                        .filter(c -> !c.equals(clan) && !clanManager.areAllies(clan, c))
                        .map(Clan::getName);
                case "akceptuj", "odrzuc" -> clanManager.getIncomingAllyRequests(clan).stream().map(Clan::getName);
                case "anuluj" -> clanManager.getOutgoingAllyRequests(clan).stream().map(Clan::getName);
                case "zerwij" -> clanManager.getAllies(clan).stream().map(Clan::getName);
                default -> Stream.empty();
            };
            return filter(names, args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> completeAdmin(String[] args, boolean polish) {
        if (args.length == 2) {
            return filter(Arrays.stream(AdminCommand.SUBCOMMANDS).map(n -> polish ? n[0] : n[1]), args[1]);
        }
        String sub = AdminCommand.resolve(args[1]);
        if (sub == null) {
            return Collections.emptyList();
        }
        ClanManager clanManager = plugin.getClanManager();

        if (args.length == 3) {
            return switch (sub) {
                case "rozwiaz", "dodaj", "lider" -> filter(allClanNames(), args[2]);
                case "wyrzuc" -> filter(clanManager.getPlayersInClans().stream()
                        .map(plugin.getStatsManager()::getName), args[2]);
                case "resetstaty" -> filter(polish ? Stream.of("klan", "gracz") : Stream.of("clan", "player"), args[2]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 4) {
            Clan target = clanManager.getClan(args[2]);
            return switch (sub) {
                case "rozwiaz" -> filter(Stream.of(SubCommand.confirmWord(polish)), args[3]);
                case "dodaj" -> filter(Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !clanManager.hasPlayerClan(p.getUniqueId()))
                        .map(Player::getName), args[3]);
                case "lider" -> target != null ? filter(memberNames(target, null), args[3]) : Collections.emptyList();
                case "resetstaty" -> switch (args[2].toLowerCase(Locale.ROOT)) {
                    case "klan", "clan" -> filter(allClanNames(), args[3]);
                    case "gracz", "player" -> filter(Bukkit.getOnlinePlayers().stream().map(Player::getName), args[3]);
                    default -> Collections.emptyList();
                };
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }

    private Stream<String> allClanNames() {
        return plugin.getClanManager().getAllClans().stream().map(Clan::getName);
    }

    private Stream<String> memberNames(Clan clan, Player except) {
        StatsManager statsManager = plugin.getStatsManager();
        return clan.getMembers().stream()
                .filter(uuid -> except == null || !uuid.equals(except.getUniqueId()))
                .map(statsManager::getName);
    }

    private static List<String> filter(Stream<String> options, String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return options
                .filter(Objects::nonNull)
                .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lower))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
