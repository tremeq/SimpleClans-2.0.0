package pvp.simpleClan;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pvp.simpleClan.commands.ClanCommand;
import pvp.simpleClan.commands.ClanTabCompleter;
import pvp.simpleClan.listeners.ClanChatListener;
import pvp.simpleClan.listeners.KillListener;
import pvp.simpleClan.listeners.PvPListener;
import pvp.simpleClan.managers.ClanManager;
import pvp.simpleClan.managers.LangManager;
import pvp.simpleClan.placeholders.ClanPlaceholders;

public class SimpleClan extends JavaPlugin {

    private static SimpleClan instance;
    private ClanManager clanManager;
    private LangManager langManager;
    private ClanChatListener chatListener;

    @Override
    public void onEnable() {
        instance = this;

        // Zapisz domyślną konfigurację
        saveDefaultConfig();

        // Inicjalizuj manager języków
        langManager = new LangManager(this);

        // Inicjalizuj manager klanów
        clanManager = new ClanManager(this);

        // Zarejestruj listenery
        chatListener = new ClanChatListener(this);
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(this), this);
        Bukkit.getPluginManager().registerEvents(new KillListener(this), this);

        // Zarejestruj komendy
        getCommand("klan").setExecutor(new ClanCommand(this));
        getCommand("klan").setTabCompleter(new ClanTabCompleter(this));

        // Zarejestruj placeholdery jeśli PlaceholderAPI jest dostępne
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholders(this).register();
            getLogger().info("PlaceholderAPI znalezione! Placeholdery zostały zarejestrowane.");
        }

        getLogger().info("SimpleClan został włączony!");
    }

    @Override
    public void onDisable() {
        if (clanManager != null) {
            clanManager.saveData();
        }
        getLogger().info("SimpleClan został wyłączony!");
    }

    public static SimpleClan getInstance() {
        return instance;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public ClanChatListener getChatListener() {
        return chatListener;
    }

    public String getMessage(String path) {
        return langManager.getMessage(path);
    }

    public String getPrefix() {
        return langManager.getPrefix();
    }
}