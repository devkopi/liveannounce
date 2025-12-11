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

    /**
     * Obtiene el color de una plataforma (con cache inteligente)
     */
    public static String getPlatformColor(String platformKey) {
        String key = platformKey.toLowerCase();

        // Si la config cambió, limpiar cache para ESTA plataforma
        if (configChanged && CACHE.containsKey(key)) {
            CACHE.remove(key);
            configChanged = false;
        }

        return CACHE.computeIfAbsent(key, ColorCache::loadColor);
    }

    /**
     * Monitorea cambios en config.yml SIN leer constantemente
     */
    private static void startConfigWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path configDir = LiveAnnounce.getInstance().getDataFolder().toPath();

            // Solo escuchar CREAR y MODIFICAR archivos
            configDir.register(watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE);

            watchThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key = watchService.take(); // Espera pasiva (NO CONSUME CPU)

                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changedFile = (Path) event.context();

                            if ("config.yml".equals(changedFile.toString())) {
                                configChanged = true;
                                LiveAnnounce.getInstance().getLogger().fine(
                                        "Config.yml modificado - cache invalidada"
                                );
                            }
                        }

                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LiveAnnounce.getInstance().getLogger().warning(
                            "Error en watcher de config: " + e.getMessage()
                    );
                }
            }, "Config-Watcher");

            watchThread.setDaemon(true);
            watchThread.start();

        } catch (IOException e) {
            LiveAnnounce.getInstance().getLogger().warning(
                    "No se pudo iniciar watcher de config: " + e.getMessage()
            );
        }
    }

    /**
     * Lee el color desde la configuración (solo cuando es necesario)
     */
    private static String loadColor(String key) {
        String color = LiveAnnounce.getInstance().getConfig()
                .getString("platform-colors." + key, "");

        if (color.trim().isEmpty()) {
            color = LiveAnnounce.getInstance().getConfig()
                    .getString("platform-colors.default", DEFAULT_COLOR);
        }

        // Normalizar color
        if (!color.startsWith("&") && !color.isEmpty()) {
            color = "&" + color;
        }

        return (color.length() == 2 && color.startsWith("&"))
                ? color
                : DEFAULT_COLOR;
    }

    /**
     * Limpia toda la cache (para reload manual)
     */
    public static void clear() {
        CACHE.clear();
        configChanged = false;
    }

    /**
     * Detiene el watcher al desactivar el plugin
     */
    public static void shutdown() {
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try { watchService.close(); } catch (IOException ignored) {}
        }
    }
}