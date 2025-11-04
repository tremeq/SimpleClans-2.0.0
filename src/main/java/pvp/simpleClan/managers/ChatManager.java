package pvp.simpleClan.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zarządza trybem czatu klanowego dla graczy
 */
public class ChatManager {

    // Gracze którzy mają włączony tryb czatu klanowego
    private final Set<UUID> clanChatMode;

    public ChatManager() {
        this.clanChatMode = ConcurrentHashMap.newKeySet();
    }

    /**
     * Sprawdza czy gracz ma włączony tryb czatu klanowego
     */
    public boolean isClanChatEnabled(UUID player) {
        return clanChatMode.contains(player);
    }

    /**
     * Włącza tryb czatu klanowego dla gracza
     */
    public void enableClanChat(UUID player) {
        clanChatMode.add(player);
    }

    /**
     * Wyłącza tryb czatu klanowego dla gracza
     */
    public void disableClanChat(UUID player) {
        clanChatMode.remove(player);
    }

    /**
     * Przełącza tryb czatu klanowego dla gracza
     * @return true jeśli włączony, false jeśli wyłączony
     */
    public boolean toggleClanChat(UUID player) {
        if (clanChatMode.contains(player)) {
            clanChatMode.remove(player);
            return false;
        } else {
            clanChatMode.add(player);
            return true;
        }
    }

    /**
     * Usuwa gracza z trybu czatu (np. gdy opuszcza klan)
     */
    public void removePlayer(UUID player) {
        clanChatMode.remove(player);
    }
}
