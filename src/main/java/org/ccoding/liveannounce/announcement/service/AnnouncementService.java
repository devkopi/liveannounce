package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
     * @param playerName Nombre del jugador que anuncia
     * @param platformName Nombre de la plataformas
     * @param link Enlace del stream
     */
    public static void broadcastAnnouncement(String playerName, String platformName, String link) {

        // Creamos los componentes del anuncio
        List<Component> components = AnnouncementFormatter.createAnnouncement(
                playerName,
                platformName,
                link
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
