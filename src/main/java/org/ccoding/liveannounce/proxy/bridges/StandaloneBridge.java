package org.ccoding.liveannounce.proxy.bridges;

import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.LiveAnnounce;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.TextComponent;

public class StandaloneBridge implements Bridge {
    private final LiveAnnounce plugin;

    public StandaloneBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Standalone (Sin proxy)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {
        // Solo envía localmente
        broadcastLocally(playerName, platform, link);
    }

    private void broadcastLocally(String playerName, String platform, String link) {
        TextComponent[] components =
                org.ccoding.liveannounce.utils.AnnouncementFormatter.createAnnouncement(
                        playerName, platform, link
                );

        if (components == null || components.length == 0) return;

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }
    }
}