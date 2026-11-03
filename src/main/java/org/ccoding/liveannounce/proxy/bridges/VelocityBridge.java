package org.ccoding.liveannounce.proxy.bridges;

import net.kyori.adventure.text.Component;
import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.LiveAnnounce;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.List;

public class VelocityBridge implements Bridge {
    private final LiveAnnounce plugin;
    private static final String VELOCITY_CHANNEL = "velocity:announce";

    private boolean paperChecked = false;
    private boolean isPaper = false;

    public VelocityBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public boolean isAvailable() {
        boolean paper = isPaperServer();
        boolean configEnabled = plugin.getConfig().getBoolean("proxy.velocity-enabled", false);
        boolean hasNetwork = plugin.getConfig().getBoolean("proxy.has-network", false);

        if (!paperChecked) {
            if (paper && configEnabled) {
                plugin.getLogger().info("Velocity enabled in config.yml");
                if (hasNetwork) {
                    plugin.getLogger().info("Velocity network enabled in config.yml");
                } else {
                    plugin.getLogger().info("Velocity network disabled in config.yml");
                    plugin.getLogger().info("Set proxy.has-network to true when more than one server is connected");
                }
            }
            paperChecked = true;
            isPaper = paper;
        }

        return paper && configEnabled && hasNetwork;
    }

    private boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            String serverName = Bukkit.getServer().getName();
            return serverName.contains("Paper") || serverName.contains("paper");
        }
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {
        String playerFormat = LiveAnnounce.getInstance().getConfig().getString("player-format", "%player_name%");
        if (!isAvailable()) {
            broadcastLocally(null, platform, link, playerFormat);
            return;
        }

        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();

        if (onlinePlayers.isEmpty()) {
            plugin.getLogger().warning("No player is online to forward the announcement to Velocity");
            broadcastLocally(null, platform, link, playerFormat);
            return;
        }

        try {
            sendToVelocity(playerName, platform, link);
            plugin.getLogger().info("Announcement sent to the Velocity network");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not send the announcement to Velocity: " + e.getMessage());
            broadcastLocally(null, platform, link, playerFormat);
        }
    }

    private void sendToVelocity(String playerName, String platform, String link) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("liveannounce:announce");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        msgout.writeUTF(playerName);
        msgout.writeUTF(platform);
        msgout.writeUTF(link);

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        Player player = plugin.getServer().getOnlinePlayers().iterator().next();
        player.sendPluginMessage(plugin, VELOCITY_CHANNEL, out.toByteArray());
    }

    private void broadcastLocally(Player player, String playerName, String platform, String link) {

        String playerFormat = LiveAnnounce.getInstance().getConfig().getString("player-format", "%player_name%");
        List<Component> components = AnnouncementFormatter.createAnnouncement(null, platform, link, playerFormat);

        if (components == null || components.isEmpty()) return;

        for (Component component : components) {
            LiveAnnounce.getInstance().getAdventure().all().sendMessage(component);
        }
    }
}