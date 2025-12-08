package org.ccoding.liveannounce.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
            ChatUtils.send(player, "&fEjemplo: &7/directo https://twitch.tv/username");
            return true;
        }

        // Detectar plataforma y color
        PlatformInfo platform = detectPlatform(link);


        sendClickableBroadcast(player.getName(), platform, link);

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
    private void sendClickableBroadcast(String playerName, PlatformInfo platform, String link) {

        TextComponent line1 = new TextComponent(ChatUtils.color(ChatUtils.getLine()));

        TextComponent title = new TextComponent(ChatUtils.color(
                "&f⚡ " + platform.color + "&l¡DIRECTO EN " + platform.name.toUpperCase() + "! &f⚡"
        ));

        TextComponent desc = new TextComponent(ChatUtils.color(
                "&f" + playerName + " &7está transmitiendo en vivo"
        ));

        TextComponent url = new TextComponent(ChatUtils.color(
                "&7¡Únete ahora! " + platform.color + "&n" + link
        ));
        url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatUtils.color("&e¡Haz clic para abrir!")).create()
        ));

        TextComponent line2 = new TextComponent(ChatUtils.color(ChatUtils.getLine()));

        // Envío global
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(line1);
            p.spigot().sendMessage(title);
            p.spigot().sendMessage(desc);
            p.spigot().sendMessage(url);
            p.spigot().sendMessage(line2);
        }
    }

    private void showHelp(Player player) {
        ChatUtils.send(player, ChatUtils.getLine());
        ChatUtils.send(player, "&6&lAYUDA - /directo");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&eUso: &f/directo <link>");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&6Ejemplos:");
        ChatUtils.send(player, "&7• &f/directo https://twitch.tv/username");
        ChatUtils.send(player, "&7• &f/directo https://youtube.com/live/username");
        ChatUtils.send(player, "&7• &f/directo https://kick.com/username");
        ChatUtils.send(player, "&7• &f/directo https://tiktok.com/@username/live");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&7El plugin detectará automáticamente la plataforma.");
        ChatUtils.send(player, ChatUtils.getLine());
    }
}