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

        plugin.getLogger().info("=== CONFIGURACIÓN DE PROXY ===");
        plugin.getLogger().info("Proxy habilitado: " + proxyEnabled);
        plugin.getLogger().info("Tipo configurado: " + configType);
        plugin.getLogger().info("Auto-detección: " + autoDetect);

        // Si el proxy está deshabilitado, usar standalone inmediatamente
        if (!proxyEnabled) {
            plugin.getLogger().info("Proxy deshabilitado en config.yml. Usando modo standalone.");
            activeBridge = new StandaloneBridge(plugin);
            logBridgeInfo(plugin);
            return;
        }

        Bridge detectedBridge = null;

        if ("auto".equals(configType) || autoDetect) {
            plugin.getLogger().info("Iniciando detección automática de proxy...");
            detectedBridge = detectProxy(plugin);
        } else {
            plugin.getLogger().info("Usando proxy configurado manualmente: " + configType);
            detectedBridge = getBridgeByType(configType, plugin);
        }

        activeBridge = detectedBridge;

        // Validación final
        if (activeBridge == null) {
            plugin.getLogger().warning("No se pudo inicializar ningún bridge");
            plugin.getLogger().warning("Usando modo standalone por defecto");
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

            plugin.getLogger().info("══════════════════════════════════════════");
            if (isAvailable) {
                plugin.getLogger().info("✓ Bridge activo: " + bridgeName);
            } else {
                plugin.getLogger().warning("✗ Bridge '" + bridgeName + "' NO está disponible");
                plugin.getLogger().warning("  Usando modo standalone como fallback");
                activeBridge = new StandaloneBridge(plugin);
            }
            plugin.getLogger().info("══════════════════════════════════════════");
        }
    }

    private static Bridge detectProxy(LiveAnnounce plugin) {
        plugin.getLogger().info("Buscando proxy disponible...");

        // 1. Intentar BungeeCord
        Bridge bungeeBridge = new BungeeCordBridge(plugin);
        if (bungeeBridge.isAvailable()) {
            plugin.getLogger().info("✓ BungeeCord detectado y disponible");
            return bungeeBridge;
        }

        // 2. Intentar Velocity (si implementado)
        try {
            Bridge velocityBridge = new VelocityBridge(plugin);
            if (velocityBridge.isAvailable()) {
                plugin.getLogger().info("✓ Velocity detectado y disponible");
                return velocityBridge;
            }
        } catch (Exception e) {
            // Velocity no está implementado o hay error
            plugin.getLogger().info("Velocity no disponible: " + e.getMessage());
        }

        // 3. Verificar si el usuario forzó un proxy específico
        String manualType = plugin.getConfig().getString("proxy.type", "");
        if (!manualType.isEmpty() && !"auto".equals(manualType)) {
            switch (manualType.toLowerCase()) {
                case "bungeecord":
                case "bungee":
                    plugin.getLogger().warning("⚠ BungeeCord configurado manualmente pero no está habilitado");
                    plugin.getLogger().warning("  Asegúrate de que 'settings.bungeecord: true' esté en spigot.yml");
                    break;
                case "velocity":
                    plugin.getLogger().warning("⚠ Velocity configurado manualmente pero no está disponible");
                    plugin.getLogger().warning("  Asegúrate de usar Paper y tener Velocity configurado");
                    break;
            }
        }

        // 4. Sin proxy detectado
        plugin.getLogger().info("✓ No se detectó proxy. Usando modo standalone.");
        return new StandaloneBridge(plugin);
    }

    private static Bridge getBridgeByType(String type, LiveAnnounce plugin) {
        if (type == null || "auto".equalsIgnoreCase(type)) {
            return detectProxy(plugin);
        }

        switch (type.toLowerCase()) {
            case "velocity":
                return new VelocityBridge(plugin);
            case "bungeecord":
            case "bungee":
                return new BungeeCordBridge(plugin);
            case "none":
            case "standalone":
            case "local":
                return new StandaloneBridge(plugin);
            default:
                plugin.getLogger().warning("⚠ Tipo de proxy desconocido: " + type);
                plugin.getLogger().warning("  Usando detección automática...");
                return detectProxy(plugin);
        }
    }

    // Recar el sistema de proxy con la configuracion actualizada
    public static void reload(LiveAnnounce plugin) {
        plugin.getLogger().info("Recargando configuración de proxy...");
        activeBridge = null; // Limpiar bridge actual para evitar conflictos
        initialize(plugin);
    }

    public static Bridge getActiveBridge() {
        return activeBridge;
    }
}