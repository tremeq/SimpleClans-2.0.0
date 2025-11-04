package pvp.simpleClan.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pvp.simpleClan.SimpleClan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

public class LangManager {

    private final SimpleClan plugin;
    private final File langFolder;
    private FileConfiguration langConfig;
    private String currentLang;

    public LangManager(SimpleClan plugin) {
        this.plugin = plugin;
        this.langFolder = new File(plugin.getDataFolder(), "lang");

        // Utwórz folder lang jeśli nie istnieje
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Załaduj domyślne pliki językowe
        saveDefaultLanguageFiles();

        // Wczytaj wybrany język z config
        this.currentLang = plugin.getConfig().getString("language", "pl");
        loadLanguage(currentLang);
    }

    /**
     * Zapisuje domyślne pliki językowe z resources do folderu lang/
     */
    private void saveDefaultLanguageFiles() {
        saveLanguageFile("pl.yml");
        saveLanguageFile("en.yml");
    }

    /**
     * Zapisuje pojedynczy plik językowy jeśli nie istnieje
     */
    private void saveLanguageFile(String fileName) {
        File langFile = new File(langFolder, fileName);
        if (!langFile.exists()) {
            try (InputStream in = plugin.getResource("lang/" + fileName)) {
                if (in != null) {
                    Files.copy(in, langFile.toPath());
                    plugin.getLogger().info("Utworzono plik językowy: " + fileName);
                } else {
                    plugin.getLogger().warning("Nie znaleziono pliku językowego w zasobach: " + fileName);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Nie można zapisać pliku językowego: " + fileName);
                e.printStackTrace();
            }
        }
    }

    /**
     * Ładuje plik językowy
     */
    public void loadLanguage(String lang) {
        File langFile = new File(langFolder, lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Plik językowy " + lang + ".yml nie istnieje! Używam pl.yml");
            langFile = new File(langFolder, "pl.yml");
            lang = "pl";
        }

        this.currentLang = lang;
        this.langConfig = YamlConfiguration.loadConfiguration(langFile);

        plugin.getLogger().info("Załadowano język: " + lang);
    }

    /**
     * Przeładowuje plik językowy
     */
    public void reloadLanguage() {
        loadLanguage(currentLang);
    }

    /**
     * Pobiera wiadomość z pliku językowego
     * @param path Ścieżka do wiadomości (np. "clan.created")
     * @return Sformatowana wiadomość z kolorami
     */
    public String getMessage(String path) {
        String message = langConfig.getString(path);

        if (message == null) {
            plugin.getLogger().warning("Brak tłumaczenia dla: " + path);
            return "§cMissing translation: " + path;
        }

        return colorize(message);
    }

    /**
     * Pobiera wiadomość z pliku językowego i zastępuje placeholdery
     * @param path Ścieżka do wiadomości
     * @param replacements Mapa placeholder -> wartość (np. "{player}", "Steve")
     * @return Sformatowana wiadomość z podstawionymi wartościami
     */
    public String getMessage(String path, Map<String, String> replacements) {
        String message = getMessage(path);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return message;
    }

    /**
     * Pobiera wiadomość z pojedynczym placeholderem
     */
    public String getMessage(String path, String placeholder, String value) {
        return getMessage(path).replace(placeholder, value);
    }

    /**
     * Pobiera prefix pluginu
     */
    public String getPrefix() {
        return colorize(langConfig.getString("prefix", "&8[&6SimpleClan&8] &r"));
    }

    /**
     * Zamienia kody kolorów & na §
     */
    private String colorize(String text) {
        return text.replace("&", "§");
    }

    /**
     * Zwraca aktualnie używany język
     */
    public String getCurrentLanguage() {
        return currentLang;
    }

    /**
     * Sprawdza czy plik językowy istnieje
     */
    public boolean languageExists(String lang) {
        return new File(langFolder, lang + ".yml").exists();
    }
}
