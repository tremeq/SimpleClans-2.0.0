package pvp.simpleClan;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import pvp.simpleClan.commands.ClanCommand;
import pvp.simpleClan.commands.ClanTabCompleter;
import pvp.simpleClan.listeners.ClanChatListener;
import pvp.simpleClan.listeners.KillListener;
import pvp.simpleClan.listeners.PlayerListener;
import pvp.simpleClan.listeners.PvPListener;
import pvp.simpleClan.managers.*;
import pvp.simpleClan.placeholders.ClanPlaceholders;
import pvp.simpleClan.storage.YamlStorage;

public class SimpleClan extends JavaPlugin {

    private static SimpleClan instance;
    private YamlStorage storage;
    private LangManager langManager;
    private ChatManager chatManager;
    private StatsManager statsManager;
    private ClanManager clanManager;
    private ConfirmationManager confirmationManager;
    private BukkitTask autosaveTask;

    @Override
    public void onEnable() {
        instance = this;

        // Zapisz domyślną konfigurację i dopisz nowe opcje do istniejącego configu
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        storage = new YamlStorage(this);
        langManager = new LangManager(this);
        chatManager = new ChatManager(this);
        statsManager = new StatsManager(this, storage);
        clanManager = new ClanManager(this, storage);
        confirmationManager = new ConfirmationManager(this);

        // Zarejestruj listenery
        Bukkit.getPluginManager().registerEvents(new ClanChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(this), this);
        Bukkit.getPluginManager().registerEvents(new KillListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        // Zarejestruj komendy
        PluginCommand command = getCommand("klan");
        if (command != null) {
            command.setExecutor(new ClanCommand(this));
            command.setTabCompleter(new ClanTabCompleter(this));
        }

        // Zarejestruj placeholdery jeśli PlaceholderAPI jest dostępne
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholders(this).register();
            getLogger().info("PlaceholderAPI znalezione! Placeholdery zostały zarejestrowane.");
        }

        startAutosave();
        getLogger().info("SimpleClan został włączony!");
    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
        if (storage != null) {
            // Najpierw dokończ zapisy w tle, potem zapisz aktualny stan synchronicznie
            storage.shutdown();
            if (clanManager != null) {
                clanManager.save(false);
            }
            if (statsManager != null) {
                statsManager.save(false);
            }
        }
        getLogger().info("SimpleClan został wyłączony!");
    }

    /**
     * Przeładowuje config i plik językowy (dane klanów zostają w pamięci)
     */
    public void reload() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        langManager.load(getConfig().getString("language", "pl"));
        startAutosave();
    }

    private void startAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
        long interval = Math.max(10, getConfig().getLong("storage.autosave-interval-seconds", 60)) * 20L;
        autosaveTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            clanManager.save(true);
            statsManager.save(true);
            clanManager.cleanup();
            statsManager.cleanupCooldowns();
            confirmationManager.cleanup();
        }, interval, interval);
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

    public ChatManager getChatManager() {
        return chatManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ConfirmationManager getConfirmationManager() {
        return confirmationManager;
    }
}
