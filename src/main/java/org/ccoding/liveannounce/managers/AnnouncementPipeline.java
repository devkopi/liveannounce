package org.ccoding.liveannounce.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.proxy.BridgeManager;
import org.ccoding.liveannounce.utils.AnnouncementData;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Esta clase divide el proceso del anuncio en fases para optimizar rendimiento.
 *
 * 1. Async: Lógica pura sin Bukkit API (thread seguro)
 * 2. Main Thread: Interacción con jugadores
 *
 * El objetivo principal es reducir los picos de CPU y mejorar la experiencia
 * cuando hay muchos jugadores online.
 */
public class AnnouncementPipeline {

    /**
     * Ejecuta el anuncio usando un pipeline async -> main thread.
     * Esto evita bloquear el servidor mientras se procesa el formato del mensaje.
     */
    public static void execute(String playerName, String platformName, String link) {
        // Ejecutamos en async para procesamiento pesado (formateo, validaciones)
        Bukkit.getScheduler().runTaskAsynchronously(
                LiveAnnounce.getInstance(),
                () -> {
                    // Creamos los datos del anuncio (solo objetos Java, sin Bukkit API)
                    AnnouncementData data = new AnnouncementData(playerName, platformName, link);

                    // Volvemos al main thread para interactuar con jugadores
                    Bukkit.getScheduler().runTask(
                            LiveAnnounce.getInstance(),
                            () -> broadcast(data)
                    );
                }
        );
    }

    /**
     * Envía el anuncio a los jugadores. DEBE ejecutarse en el main thread
     * porque usa Bukkit API (spigot().sendMessage()).
     *
     * También maneja el envío a otros servidores si hay proxy configurado.
     */
    private static void broadcast(AnnouncementData data) {
        // Creamos los componentes del anuncio usando nuestro formateador
        TextComponent[] components = AnnouncementFormatter.createAnnouncement(
                data.getPlayerName(),
                data.getPlatformName(),
                data.getLink()
        );

        // Si algo salió mal con el formateo, nos detenemos aquí
        if (components.length == 0) {
            return;
        }

        // Envío LOCAL
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }

        // Envío a RED (si hay proxy disponible)
        sendToNetwork(data.getPlayerName(), data.getPlatformName(), data.getLink());
    }

    /**
     * Envía el anuncio a otros servidores a través del proxy configurado.
     */
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