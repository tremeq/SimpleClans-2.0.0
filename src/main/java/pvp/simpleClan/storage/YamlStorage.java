package pvp.simpleClan.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Zapisuje pliki YAML poza wątkiem głównym.
 * Jeden wątek zapisujący gwarantuje kolejność zapisów, a zapis przez plik tymczasowy
 * chroni dane przed uszkodzeniem przy awarii serwera w trakcie zapisu.
 */
public class YamlStorage {

    private final JavaPlugin plugin;
    private final ExecutorService writer;

    public YamlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.writer = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "SimpleClan-Storage");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Zapisuje konfigurację. Treść jest serializowana od razu (w wątku wywołującym),
     * a sam zapis na dysk odbywa się asynchronicznie gdy async = true.
     */
    public void save(YamlConfiguration config, File file, boolean async) {
        String content = config.saveToString();
        if (async && !writer.isShutdown()) {
            writer.execute(() -> write(content, file));
        } else {
            write(content, file);
        }
    }

    /**
     * Czeka na zakończenie oczekujących zapisów (wywoływane przy wyłączaniu pluginu)
     */
    public void shutdown() {
        writer.shutdown();
        try {
            if (!writer.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Zapis danych trwa zbyt długo - przerywam oczekiwanie.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void write(String content, File file) {
        Path target = file.toPath();
        Path temp = target.resolveSibling(file.getName() + ".tmp");
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Nie można zapisać pliku " + file.getName() + "!", e);
        }
    }
}
