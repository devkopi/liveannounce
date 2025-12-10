package org.ccoding.liveannounce.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.managers.MessageManager;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
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
            sender.sendMessage(MessageManager.get("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("liveannounce.directo") && !player.isOp()) {
            ChatUtils.sendMessage(player, MessageManager.get("no-permission"));
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
            ChatUtils.sendMessage(player, MessageManager.get("invalid-link"));
            ChatUtils.sendMessage(player, MessageManager.get("invalid-link-example"));
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

    // Actualizamos para que utilice AnnouncementFormatter y sea editable
    private void sendClickableBroadcast(String playerName, PlatformInfo platform, String link) {
        // Usar el nuevo formatter
        TextComponent[] components = AnnouncementFormatter.createAnnouncement(
                playerName,
                platform.name,
                platform.color,
                link
        );

        // Envío global
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                p.spigot().sendMessage(component);
            }
        }
    }

    private void showHelp(Player player) {
        ChatUtils.send(player, ChatUtils.getLine());
        ChatUtils.send(player, "&6&lHELP - Stream Announcement");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&eCommands:");
        ChatUtils.send(player, "&7• &f/directo <link>");
        ChatUtils.send(player, "&7• &f/stream <link>");
        ChatUtils.send(player, "&7• &f/live <link>");
        ChatUtils.send(player, "&7• &f/livestream <link>");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&6Examples:");
        ChatUtils.send(player, "&7• &f/stream https://twitch.tv/username");
        ChatUtils.send(player, "&7• &f/live https://youtube.com/live/username");
        ChatUtils.send(player, "&7• &f/broadcast https://kick.com/username");
        ChatUtils.send(player, "&7• &f/directo https://tiktok.com/@username/live");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&7The plugin will automatically detect the platform.");
        ChatUtils.send(player, ChatUtils.getLine());
    }
}