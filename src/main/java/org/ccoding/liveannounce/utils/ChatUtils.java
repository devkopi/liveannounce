package org.ccoding.liveannounce.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.managers.PrefixManager;

public class ChatUtils {

    // ========== MÉTODOS DE COLORES ==========

    // Traducir colores
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Enviar mensaje ya coloreado (Metodo para NO APLICAR prefijo a mensajes)
    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(color(message));
        }
    }

    // Metodo para aplicarle prefijo a los mensajes
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(color(PrefixManager.get() + message));
        }
    }

    // Broadcast a todos los jugadores (Sin prefijo para los anuncios)
    public static void broadcast(String message) {
        if (message != null) {
            Bukkit.broadcastMessage(color(message));
        }
    }


    // ========== FORMATOS Y DECORACIONES ==========

    // Línea separadora
    public static String getLine() {
        return color("&8&m--------------------------------------------------");
    }

    // Espaciador
    public static void spacer(CommandSender sender) {
        send(sender, "");
    }

    // Broadcast con espaciadores
    public static void broadcastWithSpacers(String message) {
        broadcast("");
        broadcast(message);
        broadcast("");
    }

    // ========== MENSAJES ESPECÍFICOS DEL PLUGIN ==========

    // Crear mensaje de anuncio de directo
    public static String createAnnouncement(String playerName, String platformDisplay,
                                            String platformColor, String channel, String link) {
        return color(
                getLine() + "\n" +
                        "&f⚡ " + platformColor + "&l¡DIRECTO EN " + platformDisplay.toUpperCase() + "! &f⚡\n" +
                        "&fJugador: &e" + playerName + "\n" +
                        "&fCanal: " + platformColor + channel + "\n" +
                        "&fEnlace: &e&n" + link + "\n" +
                        getLine()
        );
    }
}