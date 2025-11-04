package pvp.simpleClan.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ChatManager;
import pvp.simpleClan.managers.ClanManager;

/**
 * Obsługuje czat klanowy
 */
public class ClanChatListener implements Listener {

    private final SimpleClan plugin;
    private final ClanManager clanManager;
    private final ChatManager chatManager;

    public ClanChatListener(SimpleClan plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.chatManager = new ChatManager();
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Sprawdź czy gracz ma włączony tryb czatu klanowego
        if (!chatManager.isClanChatEnabled(player.getUniqueId())) {
            return;
        }

        // Sprawdź czy gracz jest w klanie
        Clan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            chatManager.disableClanChat(player.getUniqueId());
            return;
        }

        // Anuluj normalny czat
        event.setCancelled(true);

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
        String message = format
                .replace("{role}", role)
                .replace("{player}", player.getName())
                .replace("{message}", event.getMessage())
                .replace("&", "§");

        // Wyślij do wszystkich członków klanu
        for (Player member : Bukkit.getOnlinePlayers()) {
            if (clan.isMember(member.getUniqueId())) {
                member.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Wyczyść tryb czatu klanowego gdy gracz opuszcza serwer
        chatManager.removePlayer(event.getPlayer().getUniqueId());

        // Wyczyść zaproszenia do klanu gdy gracz opuszcza serwer
        clanManager.removeInvite(event.getPlayer().getUniqueId());
    }
}
