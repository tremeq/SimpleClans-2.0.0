package pvp.simpleClan.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ClanManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completion dla komendy /klan
 */
public class ClanTabCompleter implements TabCompleter {

    private final SimpleClan plugin;
    private final ClanManager clanManager;

    public ClanTabCompleter(SimpleClan plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        // Pierwsz argument - subkomendy
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList(
                    "stworz", "zapros", "akceptuj", "opusc", "rozwiaz",
                    "lista", "info", "zastepca", "degraduj", "wyrzuc",
                    "chat", "cc", "kolor", "pvp", "ranking", "pomoc"
            );

            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Drugi argument - zależnie od subkomendy
        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "zapros":
                case "invite":
                    // Zaproś - lista online graczy którzy nie są w klanie
                    return Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !clanManager.hasPlayerClan(p.getUniqueId()))
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "info":
                    // Info - lista wszystkich klanów
                    return clanManager.getAllClans().stream()
                            .map(Clan::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "zastepca":
                case "mod":
                case "moderator":
                case "degraduj":
                case "demote":
                case "wyrzuc":
                case "kick":
                    // Operacje na członkach - lista członków klanu gracza
                    if (clanManager.hasPlayerClan(player.getUniqueId())) {
                        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
                        return clan.getMembers().stream()
                                .map(Bukkit::getOfflinePlayer)
                                .map(org.bukkit.OfflinePlayer::getName)
                                .filter(name -> name != null && name.toLowerCase().startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    break;

                case "kolor":
                case "color":
                    // Kolory - lista dostępnych kolorów
                    List<String> colors = plugin.getConfig().getStringList("clan.available-colors");
                    return colors.stream()
                            .map(c -> c.replace("&", ""))
                            .filter(c -> c.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());

                case "pvp":
                    // PvP - on/off
                    return Arrays.asList("on", "off").stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
