package pvp.simpleClan.commands;

import java.util.Locale;

/**
 * Podkomendy /klan. Każda ma nazwę polską, angielską i dodatkowe aliasy - wszystkie działają
 * niezależnie od języka, a podpowiedzi (tab) pokazują nazwę w języku ustawionym w configu.
 */
public enum SubCommand {
    CREATE(false, "stworz", "create", "zaloz"),
    INVITE(false, "zapros", "invite"),
    ACCEPT(false, "akceptuj", "accept", "join", "dolacz"),
    DENY(false, "odrzuc", "deny", "decline"),
    LEAVE(false, "opusc", "leave"),
    DISBAND(false, "rozwiaz", "disband"),
    LIST(false, "lista", "list", "czlonkowie", "members"),
    INFO(true, "info", "info"),
    PROMOTE(false, "zastepca", "promote", "mod", "moderator"),
    DEMOTE(false, "degraduj", "demote"),
    KICK(false, "wyrzuc", "kick"),
    LEADER(false, "lider", "leader", "transfer"),
    CHAT(false, "chat", "chat", "c"),
    CLAN_CHAT(false, "cc", "cc", "clanchat"),
    ALLY_CHAT(false, "sc", "ac", "allychat", "sojuszczat"),
    COLOR(false, "kolor", "color"),
    PVP(false, "pvp", "pvp"),
    RANKING(true, "ranking", "top"),
    STATS(true, "staty", "stats", "statystyki", "statistics"),
    ALLY(false, "sojusz", "ally", "sojusze", "alliance"),
    ADMIN(true, "admin", "admin"),
    HELP(true, "pomoc", "help");

    private final boolean consoleAllowed;
    private final String polishName;
    private final String englishName;
    private final String[] aliases;

    SubCommand(boolean consoleAllowed, String polishName, String englishName, String... aliases) {
        this.consoleAllowed = consoleAllowed;
        this.polishName = polishName;
        this.englishName = englishName;
        this.aliases = aliases;
    }

    public boolean isConsoleAllowed() {
        return consoleAllowed;
    }

    public String getName(boolean polish) {
        return polish ? polishName : englishName;
    }

    public static SubCommand match(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        for (SubCommand sub : values()) {
            if (sub.polishName.equals(lower) || sub.englishName.equals(lower)) {
                return sub;
            }
            for (String alias : sub.aliases) {
                if (alias.equals(lower)) {
                    return sub;
                }
            }
        }
        return null;
    }

    /**
     * Słowo potwierdzające nieodwracalne akcje
     */
    public static boolean isConfirmWord(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return lower.equals("potwierdz") || lower.equals("potwierdź") || lower.equals("confirm");
    }

    public static String confirmWord(boolean polish) {
        return polish ? "potwierdz" : "confirm";
    }
}
