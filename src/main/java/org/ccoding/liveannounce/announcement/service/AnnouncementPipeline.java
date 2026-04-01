package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.proxy.BridgeManager;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import java.util.List;

public class AnnouncementPipeline {

    public static void execute(Player player, String platformName, String link) {
        Bukkit.getScheduler().runTaskAsynchronously(
                LiveAnnounce.getInstance(),
                () -> {
                    String playerFormat = LiveAnnounce.getInstance().getConfig().getString("player-format", "%vault_prefix% %player_name%");
                    AnnouncementData data = new AnnouncementData(player.getName(), playerFormat, platformName, link);
                    org.ccoding.liveannounce.discord.DiscordManager.sendWebhook(data);
                    Bukkit.getScheduler().runTask(
                            LiveAnnounce.getInstance(),
                            () -> broadcast(player, data)
                    );
                }
        );
    }

    private static void broadcast(Player player, AnnouncementData data) {
        List<Component> components = AnnouncementFormatter.createAnnouncement(
                player,
                data.getPlatformName(),
                data.getLink(),
                data.getPlayerFormat()
        );

        if (components == null || components.isEmpty()) {
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Component component : components) {
                LiveAnnounce.getInstance().getAdventure().player(p).sendMessage(component);
            }
        }

        sendToNetwork(player.getName(), data.getPlatformName(), data.getLink());
    }

    private static void sendToNetwork(String playerName, String platform, String link) {
        try {
            Bridge bridge = BridgeManager.getActiveBridge();
            if (bridge == null || !bridge.isAvailable()) {
                return;
            }
            bridge.broadcastAnnouncement(playerName, platform, link);
        } catch (Exception ignored) {}
    }
}