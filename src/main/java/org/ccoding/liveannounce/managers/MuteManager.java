package org.ccoding.liveannounce.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Players who hid stream announcements.
 * The check is one set lookup. The file is written off the main thread.
 */
public final class MuteManager {

    private static final Set<UUID> MUTED = ConcurrentHashMap.newKeySet();
    private static final Object IO_LOCK = new Object();
    private static final AtomicInteger pendingSaves = new AtomicInteger();
    private static final AtomicBoolean saveScheduled = new AtomicBoolean();

    private static volatile String mutedMessage = "&7Stream announcements are hidden. Use &f/la toggle &7to show them again.";
    private static volatile String unmutedMessage = "&aStream announcements are visible again.";
    private static volatile boolean closed;
    private static JavaPlugin plugin;
    private static Path file;

    private MuteManager() {
    }

    public static void start(JavaPlugin liveAnnounce) {
        plugin = liveAnnounce;
        closed = false;
        file = liveAnnounce.getDataFolder().toPath().resolve("muted-players.txt");
        reload(liveAnnounce.getConfig());
        loadFile();
    }

    public static void reload(FileConfiguration config) {
        mutedMessage = config.getString("toggle.muted", "&7Stream announcements are hidden. Use &f/la toggle &7to show them again.");
        unmutedMessage = config.getString("toggle.unmuted", "&aStream announcements are visible again.");
    }

    public static void shutdown() {
        closed = true;
        writeSnapshot();
    }

    public static boolean isMuted(UUID uuid) {
        return uuid != null && MUTED.contains(uuid);
    }

    /**
     * @return true when the player is now muted
     */
    public static boolean toggle(UUID uuid) {
        boolean nowMuted;
        if (MUTED.remove(uuid)) {
            nowMuted = false;
        } else {
            MUTED.add(uuid);
            nowMuted = true;
        }
        queueSave();
        return nowMuted;
    }

    public static int count() {
        return MUTED.size();
    }

    public static String mutedMessage() {
        return mutedMessage;
    }

    public static String unmutedMessage() {
        return unmutedMessage;
    }

    private static void loadFile() {
        if (file == null || !Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (value.isEmpty()) {
                    continue;
                }
                try {
                    MUTED.add(UUID.fromString(value));
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().warning("Ignoring invalid muted player id: " + value);
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not read muted players: " + ex.getMessage());
        }
    }

    private static void queueSave() {
        pendingSaves.incrementAndGet();
        if (!saveScheduled.compareAndSet(false, true)) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    while (pendingSaves.get() > 0 && !closed) {
                        pendingSaves.set(0);
                        writeSnapshot();
                    }
                } finally {
                    saveScheduled.set(false);
                    if (!closed && pendingSaves.get() > 0) {
                        queueSave();
                    }
                }
            }
        });
    }

    private static void writeSnapshot() {
        if (file == null) {
            return;
        }
        List<String> lines = new ArrayList<String>(MUTED.size());
        for (UUID uuid : MUTED) {
            lines.add(uuid.toString());
        }
        synchronized (IO_LOCK) {
            Path temp = file.resolveSibling(file.getFileName().toString() + ".tmp");
            try {
                Path parent = file.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                    for (int i = 0; i < lines.size(); i++) {
                        writer.write(lines.get(i));
                        writer.newLine();
                    }
                }
                try {
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicFailed) {
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                if (plugin != null) {
                    plugin.getLogger().warning("Could not save muted players: " + ex.getMessage());
                }
            }
        }
    }
}
