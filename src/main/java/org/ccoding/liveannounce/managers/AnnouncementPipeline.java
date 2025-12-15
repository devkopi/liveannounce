package org.ccoding.liveannounce.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.AnnouncementData;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Esta clase divide el proceso del anuncio en fases
 *
 * 1, Async: Logica pura sin bukkit
 * 2, Main Theread: interaccion con jugador
 *
 * El principal objetivo de esta clase es reducir los picos de CPU
 * y mejorar el rendimiento
 */
public class AnnouncementPipeline {
    // Ejecuta el anuncio usando un pipeline async -> main
    public static void execute(String playerName,
                               String platformName,
                               String link) {
        // Logica de async
        Bukkit.getScheduler().runTaskAsynchronously(
                LiveAnnounce.getInstance(),
                () -> {
                    // Creamos los datos del anuncio
                    // No empleo bukkit, solo strings y utils seguros
                    AnnouncementData data = new AnnouncementData(playerName, platformName, link);
                    Bukkit.getScheduler().runTask(
                            LiveAnnounce.getInstance(),
                            () -> broadcast(data)
                    );
                }
        );
    }

    // Envio del anuncio al hilo principal (main theread)
    // Empleamos bukkit
    private static void broadcast(AnnouncementData data) {

        // Creamos los componentes del anuncio
        TextComponent[] components =
                AnnouncementFormatter.createAnnouncement(
                        data.getPlayerName(),
                        data.getPlatformName(),
                        data.getLink()
                );
        if (components.length == 0) return;

        // Enviar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }
    }
}
