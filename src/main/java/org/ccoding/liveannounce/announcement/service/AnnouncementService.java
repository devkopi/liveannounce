package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import org.ccoding.liveannounce.utils.SoundUtils;

import java.util.List;

/**
 * Esta clase es responsable de manejar el flujo completo
 * de un anuncio de stream
 */
public class AnnouncementService {

    /**
     * @param platformName Nombre de la plataformas
     * @param link Enlace del stream
     */
    public static void broadcastAnnouncement(Player player, String platformName, String link) {

        String playerFormat = LiveAnnounce.getInstance().getConfig().getString("player-format", "%vault_prefix% %player_name%");
        // Creamos los componentes del anuncio
        List<Component> components = AnnouncementFormatter.createAnnouncement(
                player,
                platformName,
                link,
                playerFormat
        );

        // Si algo salió mal, se sigue
        if (components == null || components.isEmpty()) {
            Bukkit.getLogger().warning("[LiveAnnounce] No se pudieron crear los componentes del anuncio.");
            return;
        }

        // Enviamos el mensaje a todos los jugadores usando Adventure
        for (Component component : components) {
            LiveAnnounce.getInstance().getAdventure().all().sendMessage(component);
        }

        // Reproducir el sonido
        SoundUtils.playSoundToAll("announce");
    }
}
