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

    public static void execute(String playerName, String platformName, String link) {
        Bukkit.getScheduler().runTaskAsynchronously(
                LiveAnnounce.getInstance(),
                () -> {
                    AnnouncementData data = new AnnouncementData(playerName, platformName, link);
                    org.ccoding.liveannounce.discord.DiscordManager.sendWebhook(data);
                    Bukkit.getScheduler().runTask(
                            LiveAnnounce.getInstance(),
                            () -> broadcast(data)
                    );
                }
        );
    }

    private static void broadcast(AnnouncementData data) {
        List<Component> components = AnnouncementFormatter.createAnnouncement(
                data.getPlayerName(),
                data.getPlatformName(),
                data.getLink()
        );

        if (components == null || components.isEmpty()) {
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Component component : components) {
                LiveAnnounce.getInstance().getAdventure().player(p).sendMessage(component);
            }
        }

        sendToNetwork(data.getPlayerName(), data.getPlatformName(), data.getLink());
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