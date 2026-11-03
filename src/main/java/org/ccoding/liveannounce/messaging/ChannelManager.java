package org.ccoding.liveannounce.messaging;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.util.HashSet;
import java.util.Set;

public class ChannelManager {

    private final JavaPlugin plugin;
    private final Set<String> registeredChannels = new HashSet<>();

    public static final String CHANNEL_ANNOUNCE = "liveannounce:announce";

    public ChannelManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChannels() {
        registerIncomingChannel(CHANNEL_ANNOUNCE, new AnnouncementReceiver());

        if (isPaperServer()) {
            registerVelocityChannel();
        }
    }

    /* BungeeCord channel registration is disabled.
    public String getBungeeCordChannel() {
        if (activeBungeeChannel == null) {
            if (isPaperModern()) {
                activeBungeeChannel = "bungeecord:main";
            } else {
                activeBungeeChannel = "BungeeCord";
            }
        }
        return activeBungeeChannel;
    }
    */

    private void registerVelocityChannel() {
        try {
            String velocityChannel = "velocity:announce";
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, velocityChannel);
            registeredChannels.add(velocityChannel);
            plugin.getLogger().info("Velocity channel registered.");
        } catch (Exception ignored) {}
    }

    private void registerIncomingChannel(String channel, AnnouncementReceiver receiver) {
        try {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, receiver);
            registeredChannels.add(channel);
            plugin.getLogger().info("Incoming channel registered: " + channel);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register channel: " + channel);
        }
    }

    private boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /* Kept for a future BungeeCord bridge. Not used.
    private boolean isPaperModern() {
        try {
            String version = plugin.getServer().getVersion();
            return version.contains("1.20.4") ||
                    version.contains("1.21") ||
                    version.contains("1.20.5") ||
                    version.contains("1.20.6");
        } catch (Exception e) {
            return false;
        }
    }
    */

    public void unregisterChannels() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        registeredChannels.clear();
    }
}