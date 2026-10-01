package pvp.simpleClan.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pvp.simpleClan.SimpleClan;
import pvp.simpleClan.data.Clan;
import pvp.simpleClan.managers.ChatManager;
import pvp.simpleClan.managers.ChatManager.ChatMode;

/**
 * Przekierowuje wiadomości graczy z włączonym trybem czatu klanowego lub sojuszniczego
 */
public class ClanChatListener implements Listener {

    private final SimpleClan plugin;

    public ClanChatListener(SimpleClan plugin) {
        this.plugin = plugin;
    }

    // ignoreCancelled = true: wiadomość wyciszona przez inny plugin (np. mute) nie trafi na czat klanu
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatManager chatManager = plugin.getChatManager();
        ChatMode mode = chatManager.getMode(player.getUniqueId());

        if (mode == ChatMode.PUBLIC) {
            return;
        }

        // Gracz z trybem czatu klanowego nigdy nie powinien przypadkiem pisać na czacie publicznym
        event.setCancelled(true);

        Clan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan == null) {
            chatManager.reset(player.getUniqueId());
            plugin.getLangManager().send(player, "chat.mode-reset");
            return;
        }

        if (mode == ChatMode.ALLY && plugin.getClanManager().isAllianceEnabled()) {
            chatManager.sendAllyMessage(player, clan, event.getMessage());
        } else {
            chatManager.sendClanMessage(player, clan, event.getMessage());
        }
    }
}
