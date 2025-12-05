package org.ccoding.liveannounce.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {

    // ========== MÉTODOS DE COLORES ==========

    // Traducir colores
    public static String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    // Enviar mensaje ya coloreado
    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(color(message));
        }
    }

    // Broadcast a todos los jugadores
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

    // Mostrar ayuda del comando /directo
    public static void showDirectoHelp(Player player) {
        send(player, getLine());
        send(player, "&6&lAYUDA - /directo");
        spacer(player);
        send(player, "&eUso: &f/directo <plataforma> <canal>");
        send(player, "&7Plataformas: &eTwitch, Youtube, Kick, Tiktok");
        spacer(player);
        send(player, "&6Ejemplos:");
        send(player, "&7• &f/directo twitch AuraCraftNW");
        send(player, "&7• &f/directo youtube AuraCraftNW");
        send(player, "&7• &f/directo kick AuraCraftNW");
        send(player, "&7• &f/directo tiktok AuraCraftNW");
        send(player, getLine());
    }

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