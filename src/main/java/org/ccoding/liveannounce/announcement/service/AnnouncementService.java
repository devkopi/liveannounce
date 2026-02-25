package org.ccoding.liveannounce.announcement.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import net.md_5.bungee.api.chat.TextComponent;
import org.ccoding.liveannounce.utils.SoundUtils;

/**
 * Esta clase es responsable de manejar el flujo completo
 * de un anuncio de stream
 *
 * Se decide que se ejecuta en el main thread y que se puede
 * mover a async
 */
public class AnnouncementService {

    /**
     * @param playerName Nombre del jugador que anuncia
     * @param platformName Nombre de la plataformas
     * @param link Enlace del stream
     */
    public static void broadcastAnnouncement(String playerName, String platformName, String link) {

        // Creamos los componentes del anuncio
        // Es logica de formato
        TextComponent[] components = AnnouncementFormatter.createAnnouncement(
                playerName,
                platformName,
                link
        );

        // Si algo salió mal, se sigue
        if (components == null || components.length == 0) {
            Bukkit.getLogger().warning("[LiveAnnounce] No se pudieron crear los componentes del anuncio.");
            return;
        }

        // Enviamos el mensaje a todos los jugadores
        // Esto DEBE ejecutarse en main thread, NUNCA enviar mensaje desde async
        for (Player player: Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }

        // Reproducir el sonido
        SoundUtils.playSoundToAll("announce");
    }
}
