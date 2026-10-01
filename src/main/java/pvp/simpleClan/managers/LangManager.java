package pvp.simpleClan.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import pvp.simpleClan.SimpleClan;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangManager {

    private static final String DEFAULT_LANG = "pl";
    private static final String[] BUNDLED_LANGS = {"pl", "en"};
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private final SimpleClan plugin;
    private final File langFolder;
    private YamlConfiguration langConfig;
    private String currentLang;

    public LangManager(SimpleClan plugin) {
        this.plugin = plugin;
        this.langFolder = new File(plugin.getDataFolder(), "lang");

        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        for (String lang : BUNDLED_LANGS) {
            saveLanguageFile(lang + ".yml");
        }

        load(plugin.getConfig().getString("language", DEFAULT_LANG));
    }

    /**
     * Zapisuje plik językowy z zasobów pluginu jeśli nie istnieje
     */
    private void saveLanguageFile(String fileName) {
        File langFile = new File(langFolder, fileName);
        if (langFile.exists()) {
            return;
        }
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

    /**
     * Ładuje plik językowy. Brakujące klucze (np. po aktualizacji pluginu) są uzupełniane
     * wartościami z pliku wbudowanego w plugin i dopisywane do pliku na serwerze.
     */
    public void load(String lang) {
        lang = lang == null ? DEFAULT_LANG : lang.toLowerCase(Locale.ROOT);
        File langFile = new File(langFolder, lang + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Plik językowy " + lang + ".yml nie istnieje! Używam " + DEFAULT_LANG + ".yml");
            lang = DEFAULT_LANG;
            langFile = new File(langFolder, DEFAULT_LANG + ".yml");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        YamlConfiguration defaults = loadBundled(lang);
        if (defaults == null) {
            // Własny język bez odpowiednika w pluginie - brakujące klucze bierzemy z angielskiego
            defaults = loadBundled("en");
        }

        if (defaults != null) {
            config.setDefaults(defaults);
            int missing = 0;
            for (String key : defaults.getKeys(true)) {
                if (!defaults.isConfigurationSection(key) && !config.contains(key, true)) {
                    config.set(key, defaults.get(key));
                    missing++;
                }
            }
            if (missing > 0) {
                try {
                    config.save(langFile);
                    plugin.getLogger().info("Dodano " + missing + " brakujących wiadomości do " + langFile.getName());
                } catch (IOException e) {
                    plugin.getLogger().warning("Nie można zaktualizować pliku " + langFile.getName() + ": " + e.getMessage());
                }
            }
        }

        this.currentLang = lang;
        this.langConfig = config;
        plugin.getLogger().info("Załadowano język: " + lang);
    }

    private YamlConfiguration loadBundled(String lang) {
        InputStream in = plugin.getResource("lang/" + lang + ".yml");
        if (in == null) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Pobiera wiadomość z pliku językowego i podstawia placeholdery.
     *
     * @param path         ścieżka do wiadomości (np. "clan.created")
     * @param replacements pary nazwa-wartość, np. "player", "Steve" zamienia {player} na Steve
     */
    public String get(String path, Object... replacements) {
        String message = langConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Brak tłumaczenia dla: " + path);
            return "§cMissing translation: " + path;
        }
        return replace(colorize(message), replacements);
    }

    /**
     * Pobiera listę linii z pliku językowego (np. pomoc)
     */
    public List<String> getList(String path, Object... replacements) {
        List<String> result = new ArrayList<>();
        for (String line : langConfig.getStringList(path)) {
            result.add(replace(colorize(line), replacements));
        }
        return result;
    }

    /**
     * Wysyła wiadomość z prefixem. Pusta wiadomość w pliku językowym oznacza "nie wysyłaj".
     */
    public void send(CommandSender to, String path, Object... replacements) {
        String message = get(path, replacements);
        if (!message.isEmpty()) {
            to.sendMessage(getPrefix() + message);
        }
    }

    /**
     * Wysyła wiadomość bez prefixu
     */
    public void sendRaw(CommandSender to, String path, Object... replacements) {
        String message = get(path, replacements);
        if (!message.isEmpty()) {
            to.sendMessage(message);
        }
    }

    public void sendList(CommandSender to, String path, Object... replacements) {
        for (String line : getList(path, replacements)) {
            to.sendMessage(line);
        }
    }

    public String getPrefix() {
        return colorize(langConfig.getString("prefix", "&8[&6SimpleClan&8] &r"));
    }

    /**
     * Zamienia kody kolorów &x oraz &#RRGGBB na kolory Minecraft
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(ChatColor.of("#" + matcher.group(1)).toString()));
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    private static String replace(String message, Object... replacements) {
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
        }
        return message;
    }

    public String getCurrentLanguage() {
        return currentLang;
    }

    /**
     * Czy nazwy komend mają być wyświetlane po polsku (tab-complete, podpowiedzi)
     */
    public boolean isPolish() {
        return "pl".equals(currentLang);
    }
}
