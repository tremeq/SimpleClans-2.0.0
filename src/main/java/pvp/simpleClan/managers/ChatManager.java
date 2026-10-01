package pvp.simpleClan.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zarządza trybami czatu (klanowy / sojuszniczy) i wysyła wiadomości na te czaty.
 * Metody wysyłające mogą być wywoływane z wątku asynchronicznego (AsyncPlayerChatEvent).
 */
public class ChatManager {

    public enum ChatMode {
        PUBLIC, CLAN, ALLY
    }

    private final SimpleClan plugin;
    private final Map<UUID, ChatMode> modes = new ConcurrentHashMap<>();

    public ChatManager(SimpleClan plugin) {
        this.plugin = plugin;
    }

    public ChatMode getMode(UUID player) {
        return modes.getOrDefault(player, ChatMode.PUBLIC);
    }

    public boolean isClanChatEnabled(UUID player) {
        return getMode(player) == ChatMode.CLAN;
    }

    /**
     * Przełącza podany tryb: jeśli jest już aktywny, wraca do czatu publicznego
     *
     * @return nowy tryb gracza
     */
    public ChatMode toggle(UUID player, ChatMode mode) {
        if (getMode(player) == mode) {
            modes.remove(player);
            return ChatMode.PUBLIC;
        }
        modes.put(player, mode);
        return mode;
    }

    /**
     * Przywraca czat publiczny (np. po opuszczeniu klanu)
     */
    public void reset(UUID player) {
        modes.remove(player);
    }

    /**
     * Wysyła wiadomość do wszystkich członków klanu
     */
    public void sendClanMessage(Player sender, Clan clan, String message) {
        String formatted = format("chat.format", sender, clan, message);
        Set<UUID> recipients = new LinkedHashSet<>(clan.getMembers());
        deliver(recipients, formatted);
    }

    /**
     * Wysyła wiadomość do członków klanu i wszystkich klanów sojuszniczych
     */
    public void sendAllyMessage(Player sender, Clan clan, String message) {
        String formatted = format("chat.ally-format", sender, clan, message);
        Set<UUID> recipients = new LinkedHashSet<>(clan.getMembers());
        for (Clan ally : plugin.getClanManager().getAllies(clan)) {
            recipients.addAll(ally.getMembers());
        }
        deliver(recipients, formatted);
    }

    private String format(String formatPath, Player sender, Clan clan, String message) {
        LangManager lang = plugin.getLangManager();

        String role = "";
        if (clan.isLeader(sender.getUniqueId())) {
            role = lang.get("chat.leader-role");
        } else if (clan.isModerator(sender.getUniqueId())) {
            role = lang.get("chat.moderator-role");
        }

        // Kolory w treści tylko dla graczy z uprawnieniem - w przeciwnym razie "&" zostaje zwykłym znakiem
        String content = sender.hasPermission("simpleclan.chat.color") ? LangManager.colorize(message) : message;

        // {message} podstawiamy na końcu, żeby treść gracza nie była interpretowana jako placeholder
        return lang.get(formatPath,
                "role", role,
                "player", sender.getName(),
                "tag", plugin.getClanManager().getClanTag(clan))
                .replace("{message}", content);
    }

    private void deliver(Set<UUID> recipients, String formatted) {
        for (UUID uuid : recipients) {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) {
                member.sendMessage(formatted);
            }
        }
        if (plugin.getConfig().getBoolean("chat.log-to-console", true)) {
            Bukkit.getConsoleSender().sendMessage(formatted);
        }
    }
}
