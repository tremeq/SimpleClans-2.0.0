package pvp.simpleClan.managers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pvp.simpleClan.SimpleClan;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Przechowuje oczekujące potwierdzenia nieodwracalnych akcji
 * (rozwiązanie klanu, przekazanie przywództwa, usunięcie klanu przez admina).
 */
public class ConfirmationManager {

    private record Pending(String action, String target, long expiresAt) {
    }

    private final SimpleClan plugin;
    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    public ConfirmationManager(SimpleClan plugin) {
        this.plugin = plugin;
    }

    public int getTimeoutSeconds() {
        return Math.max(5, plugin.getConfig().getInt("confirmation-timeout-seconds", 30));
    }

    /**
     * Zapisuje akcję czekającą na potwierdzenie (zastępuje poprzednią akcję nadawcy)
     */
    public void request(CommandSender sender, String action, String target) {
        long expiresAt = System.currentTimeMillis() + getTimeoutSeconds() * 1000L;
        pending.put(key(sender), new Pending(action, target.toLowerCase(Locale.ROOT), expiresAt));
    }

    /**
     * Sprawdza i zużywa potwierdzenie
     *
     * @return true jeśli nadawca miał ważne, pasujące potwierdzenie
     */
    public boolean consume(CommandSender sender, String action, String target) {
        Pending p = pending.get(key(sender));
        if (p == null || p.expiresAt() < System.currentTimeMillis()) {
            pending.remove(key(sender));
            return false;
        }
        if (!p.action().equals(action) || !p.target().equals(target.toLowerCase(Locale.ROOT))) {
            return false;
        }
        pending.remove(key(sender));
        return true;
    }

    public void clear(CommandSender sender) {
        pending.remove(key(sender));
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        pending.values().removeIf(p -> p.expiresAt() < now);
    }

    private static String key(CommandSender sender) {
        return sender instanceof Player player ? player.getUniqueId().toString() : "#" + sender.getName();
    }
}
