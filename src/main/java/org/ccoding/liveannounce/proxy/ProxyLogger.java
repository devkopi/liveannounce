package org.ccoding.liveannounce.proxy;

import org.ccoding.liveannounce.LiveAnnounce;

public class ProxyLogger {

    public static void logConfiguration(LiveAnnounce plugin) {
        boolean proxyEnabled = plugin.getConfig().getBoolean("proxy.enabled", true);
        String configType = plugin.getConfig().getString("proxy.type", "auto");
        boolean autoDetect = plugin.getConfig().getBoolean("proxy.auto-detect", true);

        plugin.getLogger().info("+---------------------------------------+");
        plugin.getLogger().info("|         PROXY CONFIGURATION          |");
        plugin.getLogger().info("+---------------------------------------+");
        plugin.getLogger().info("| Enabled    : " + (proxyEnabled ? "true" : "false"));
        plugin.getLogger().info("| Type       : " + configType);
        plugin.getLogger().info("| Auto-detect: " + (autoDetect ? "true" : "false"));
        plugin.getLogger().info("+---------------------------------------+");
    }
}