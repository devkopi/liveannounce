package org.ccoding.liveannounce.utils;

import org.ccoding.liveannounce.LiveAnnounce;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.io.IOException;

public class ColorCache {
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULT_COLOR = "&6";
    private static WatchService watchService;
    private static Thread watchThread;
    private static volatile boolean configChanged = false;

    static {
        startConfigWatcher();
    }

    public static String getPlatformColor(String platformKey) {
        String key = platformKey.toLowerCase();

        if (configChanged && CACHE.containsKey(key)) {
            CACHE.remove(key);
            configChanged = false;
        }

        return CACHE.computeIfAbsent(key, ColorCache::loadColor);
    }

    private static void startConfigWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path configDir = LiveAnnounce.getInstance().getDataFolder().toPath();

            configDir.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);

            watchThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key = watchService.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changedFile = (Path) event.context();

                            if ("config.yml".equals(changedFile.toString())) {
                                configChanged = true;
                                LiveAnnounce.getInstance().getLogger().fine(
                                        "config.yml changed. Color cache cleared."
                                );
                            }
                        }

                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LiveAnnounce.getInstance().getLogger().warning(
                            "Config watcher failed: " + e.getMessage()
                    );
                }
            }, "Config-Watcher");

            watchThread.setDaemon(true);
            watchThread.start();

        } catch (IOException e) {
            LiveAnnounce.getInstance().getLogger().warning(
                    "Could not start the config watcher: " + e.getMessage()
            );
        }
    }

    private static String loadColor(String key) {
        String color = LiveAnnounce.getInstance().getConfig()
                .getString("platform-colors." + key, "");

        if (color.trim().isEmpty()) {
            color = LiveAnnounce.getInstance().getConfig()
                    .getString("platform-colors.default", DEFAULT_COLOR);
        }

        if (!color.startsWith("&") && !color.isEmpty()) {
            color = "&" + color;
        }

        return (color.length() == 2 && color.startsWith("&"))
                ? color
                : DEFAULT_COLOR;
    }

    public static void clear() {
        CACHE.clear();
        configChanged = false;
    }

    public static void shutdown() {
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try { watchService.close(); } catch (IOException ignored) {}
        }
    }
}