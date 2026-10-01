package pvp.simpleClan.data;

import java.util.Locale;

/**
 * Statystyki PvP pojedynczego gracza oraz jego ostatnio znany nick
 */
public class PlayerStats {

    private volatile String name;
    private volatile int kills;
    private volatile int deaths;

    public PlayerStats(String name, int kills, int deaths) {
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public double getKdr() {
        return kdr(kills, deaths);
    }

    public void reset() {
        this.kills = 0;
        this.deaths = 0;
    }

    /**
     * KDR = zabójstwa / śmierci (przy 0 śmierci KDR równa się liczbie zabójstw)
     */
    public static double kdr(int kills, int deaths) {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public static String formatKdr(double kdr) {
        return String.format(Locale.ROOT, "%.2f", kdr);
    }
}
