package org.ccoding.liveannounce.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.ChatUtils;

public class DirectoCommand implements CommandExecutor {

    private final LiveAnnounce plugin;

    public DirectoCommand() {
        this.plugin = LiveAnnounce.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("liveannounce.directo") && !player.isOp()) {
            ChatUtils.send(player, "&c¡ERROR! &fNo tienes permiso.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        // Unir todos los argumentos (el link)
        String link = String.join(" ", args);

        // Verificar que sea un link válido
        if (!isValidLink(link)) {
            ChatUtils.send(player, "&c¡ERROR! &fDebes proporcionar un &elink &fválido.");
            ChatUtils.send(player, "&fEjemplo: &7/directo https://twitch.tv/auracraftnw");
            return true;
        }

        // Detectar plataforma y color
        PlatformInfo platform = detectPlatform(link);


        String mensaje = createMessage(player.getName(), platform, link);

        // Envío global
        ChatUtils.broadcastWithSpacers(mensaje);

        return true;
    }

    // Datos de plataforma
    private static class PlatformInfo {
        String name;
        String color;

        PlatformInfo(String name, String color) {
            this.name = name;
            this.color = color;
        }
    }

    // Validar link básico
    private boolean isValidLink(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }

        String lower = url.toLowerCase();
        return lower.contains("twitch.tv/") ||
                lower.contains("youtube.com/") ||
                lower.contains("youtu.be/") ||
                lower.contains("kick.com/") ||
                lower.contains("tiktok.com/");
    }

    // Detectar plataforma
    private PlatformInfo detectPlatform(String url) {
        String lower = url.toLowerCase();

        if (lower.contains("twitch.tv/")) {
            return new PlatformInfo("Twitch", "&d");
        }

        if (lower.contains("youtube.com/") || lower.contains("youtu.be/")) {
            return new PlatformInfo("YouTube", "&c");
        }

        if (lower.contains("kick.com/")) {
            return new PlatformInfo("Kick", "&a");
        }

        if (lower.contains("tiktok.com/")) {
            return new PlatformInfo("TikTok", "&b");
        }

        return new PlatformInfo("Directo", "&6");
    }

    // Crear mensaje con el mismo FORMATO visual
    private String createMessage(String playerName, PlatformInfo platform, String link) {
        return ChatUtils.color(
                ChatUtils.getLine() + "\n" +
                        "&f⚡ " + platform.color + "&l¡DIRECTO EN " + platform.name.toUpperCase() + "! &f⚡\n" +
                        "&f" + playerName + " &7está transmitiendo en vivo\n" +
                        "&7¡Únete ahora! " + platform.color + "&n" + link + "\n" +
                        ChatUtils.getLine()
        );
    }

    private void showHelp(Player player) {
        ChatUtils.send(player, ChatUtils.getLine());
        ChatUtils.send(player, "&6&lAYUDA - /directo");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&eUso: &f/directo <link>");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&6Ejemplos:");
        ChatUtils.send(player, "&7• &f/directo https://twitch.tv/auracraftnw");
        ChatUtils.send(player, "&7• &f/directo https://youtube.com/live/auracraftnw");
        ChatUtils.send(player, "&7• &f/directo https://kick.com/auracraftnw");
        ChatUtils.send(player, "&7• &f/directo https://tiktok.com/@auracraftnw/live");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&7El plugin detectará automáticamente la plataforma.");
        ChatUtils.send(player, ChatUtils.getLine());
    }
}