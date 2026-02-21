package org.ccoding.liveannounce.proxy;

import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.proxy.bridges.*;

public class BridgeManager {
    private static Bridge activeBridge;

    public static void initialize(LiveAnnounce plugin) {

        // Leer configuración del proxy
        boolean proxyEnabled = plugin.getConfig().getBoolean("proxy.enabled", true);
        String configType = plugin.getConfig().getString("proxy.type", "auto");
        boolean autoDetect = plugin.getConfig().getBoolean("proxy.auto-detect", true);

        // Si el proxy está deshabilitado, usar standalone inmediatamente
        if (!proxyEnabled) {
            plugin.getLogger().info("Proxy disabled. Using standalone mode.");
            activeBridge = new StandaloneBridge(plugin);
            logBridgeInfo(plugin);
            return;
        }

        Bridge detectedBridge = null;

        if ("auto".equals(configType) || autoDetect) {
            detectedBridge = detectProxy(plugin);
        } else {
            detectedBridge = getBridgeByType(configType, plugin);
        }

        activeBridge = detectedBridge;

        // Validación final
        if (activeBridge == null) {
            plugin.getLogger().warning("Could not initialize any bridge. Using standalone.");
            activeBridge = new StandaloneBridge(plugin);
        }

        logBridgeInfo(plugin);
    }

    /**
     * Muestra información del bridge activo
     */
    private static void logBridgeInfo(LiveAnnounce plugin) {
        if (activeBridge != null) {
            String bridgeName = activeBridge.getName();
            boolean isAvailable = activeBridge.isAvailable();

            if (isAvailable) {
                plugin.getLogger().info("[LiveAnnouncer] → Bridge active: " + bridgeName);
            } else {
                plugin.getLogger().warning("Bridge '" + bridgeName + "' is not available. Using standalone.");
                activeBridge = new StandaloneBridge(plugin);
            }
        }
    }

    private static Bridge detectProxy(LiveAnnounce plugin) {

        // --- BUNGEE CORD (DESHABILITADO TEMPORALMENTE) ---
        // Bridge bungeeBridge = new BungeeCordBridge(plugin);
        // if (bungeeBridge.isAvailable()) {
        //     return bungeeBridge;
        // }

        // Intentar Velocity (único proxy soportado actualmente)
        try {
            Bridge velocityBridge = new VelocityBridge(plugin);
            if (velocityBridge.isAvailable()) {
                plugin.getLogger().info("Velocity detected and available.");
                return velocityBridge;
            }
        } catch (Exception ignored) {}

        // Verificar si el usuario forzó Velocity manualmente
        String manualType = plugin.getConfig().getString("proxy.type", "");
        if ("velocity".equalsIgnoreCase(manualType)) {
            plugin.getLogger().warning("Velocity configured but not available. Check Paper and Velocity setup.");
        }

        // Sin proxy detectado
        return new StandaloneBridge(plugin);
    }

    private static Bridge getBridgeByType(String type, LiveAnnounce plugin) {
        if (type == null || "auto".equalsIgnoreCase(type)) {
            return detectProxy(plugin);
        }

        switch (type.toLowerCase()) {
            case "velocity":
                return new VelocityBridge(plugin);
            /*case "bungeecord":
            case "bungee":
                return new BungeeCordBridge(plugin);*/
            case "none":
            case "standalone":
            case "local":
                return new StandaloneBridge(plugin);
            default:
                plugin.getLogger().warning("Unknown proxy type: " + type + ". Using auto detection.");
                return detectProxy(plugin);
        }
    }

    // Recargar el sistema de proxy con la configuración actualizada
    public static void reload(LiveAnnounce plugin) {
        activeBridge = null; // Limpiar bridge actual para evitar conflictos
        initialize(plugin);
    }

    public static Bridge getActiveBridge() {
        return activeBridge;
    }
}