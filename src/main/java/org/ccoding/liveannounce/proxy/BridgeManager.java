package org.ccoding.liveannounce.proxy;

import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.proxy.bridges.*;

public class BridgeManager {
    private static Bridge activeBridge;

    public static void initialize(LiveAnnounce plugin) {

        boolean proxyEnabled = plugin.getConfig().getBoolean("proxy.enabled", true);
        String configType = plugin.getConfig().getString("proxy.type", "auto");
        boolean autoDetect = plugin.getConfig().getBoolean("proxy.auto-detect", true);

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

        if (activeBridge == null) {
            plugin.getLogger().warning("Could not initialize any bridge. Using standalone.");
            activeBridge = new StandaloneBridge(plugin);
        }

        logBridgeInfo(plugin);
    }

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

        // BungeeCord is disabled for now.
        // Bridge bungeeBridge = new BungeeCordBridge(plugin);
        // if (bungeeBridge.isAvailable()) {
        //     return bungeeBridge;
        // }

        try {
            Bridge velocityBridge = new VelocityBridge(plugin);
            if (velocityBridge.isAvailable()) {
                plugin.getLogger().info("Velocity detected and available.");
                return velocityBridge;
            }
        } catch (Exception ignored) {}

        String manualType = plugin.getConfig().getString("proxy.type", "");
        if ("velocity".equalsIgnoreCase(manualType)) {
            plugin.getLogger().warning("Velocity configured but not available. Check Paper and Velocity setup.");
        }

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

    public static void reload(LiveAnnounce plugin) {
        activeBridge = null;
        initialize(plugin);
    }

    public static Bridge getActiveBridge() {
        return activeBridge;
    }
}