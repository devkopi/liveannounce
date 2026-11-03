package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import org.ccoding.liveannounce.utils.SoundUtils;

import java.util.List;

/**
 * Builds and sends one stream announcement.
 */
public class AnnouncementService {

    /**
     * @param platformName platform name
     * @param link stream URL
     */
    public static void broadcastAnnouncement(Player player, String platformName, String link) {

        String playerFormat = LiveAnnounce.getInstance().getConfig().getString("player-format", "%player_name%");
        List<Component> components = AnnouncementFormatter.createAnnouncement(
                player,
                platformName,
                link,
                playerFormat
        );

        if (components == null || components.isEmpty()) {
            Bukkit.getLogger().warning("[LiveAnnounce] No se pudieron crear los componentes del anuncio.");
            return;
        }

        for (Component component : components) {
            LiveAnnounce.getInstance().getAdventure().all().sendMessage(component);
        }

        SoundUtils.playSoundToAll("announce");
    }
}
