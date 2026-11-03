/*package org.ccoding.liveannounce.proxy.bridges;

import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.messaging.ChannelManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Collection;

public class BungeeCordBridge implements Bridge {
    private final LiveAnnounce plugin;
    private boolean available = false;

    public BungeeCordBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        // Check availability
        available = plugin.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);

        if (available) {
            plugin.getLogger().info("BungeeCord bridge available");
        }
    }

    @Override
    public String getName() {
        return "BungeeCord";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {

        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();

        if (onlinePlayers.isEmpty()) {
            plugin.getLogger().warning("No player is online to forward the announcement to BungeeCord");
            return;
        }

        try {
            // Send once through BungeeCord
            sendToOtherServers(playerName, platform, link);

            // The receiving server prints the announcement. Do not print it here.
            plugin.getLogger().info("Announcement sent to the BungeeCord network");

        } catch (Exception e) {
            plugin.getLogger().severe("Could not send the announcement to BungeeCord: " + e.getMessage());
            broadcastLocally(playerName, platform, link);
        }
    }

    private void sendToOtherServers(String playerName, String platform, String link) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF(ChannelManager.CHANNEL_ANNOUNCE);

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        msgout.writeUTF(playerName);
        msgout.writeUTF(platform);
        msgout.writeUTF(link);

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        Player player = plugin.getServer().getOnlinePlayers().iterator().next();


        String bungeeChannel = plugin.getChannelManager().getBungeeCordChannel();
        player.sendPluginMessage(plugin, bungeeChannel, out.toByteArray());
    }

    private void broadcastLocally(String playerName, String platform, String link) {
        TextComponent[] components =
                org.ccoding.liveannounce.utils.AnnouncementFormatter.createAnnouncement(
                        playerName, platform, link
                );

        if (components == null || components.length == 0) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }
    }
}

*/