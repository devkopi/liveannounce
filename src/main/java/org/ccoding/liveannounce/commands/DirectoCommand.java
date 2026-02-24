package org.ccoding.liveannounce.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.announcement.service.AnnouncementPipeline;
import org.ccoding.liveannounce.managers.CooldownManager;
import org.ccoding.liveannounce.managers.MessageManager;
import org.ccoding.liveannounce.utils.ChatUtils;

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

        CooldownManager cooldown = LiveAnnounce.getInstance().getAnnouncementCooldown();

        if (cooldown != null && !cooldown.canUse(player.getUniqueId())) {
            long remaining = cooldown.getRemaining(player.getUniqueId());

            String message = LiveAnnounce.getInstance().getConfig().getString("cooldown.message", "&cYou must wait {time}s.");

            player.sendMessage(ChatUtils.color(
                    message.replace("{time}", String.valueOf(remaining))
            ));
            return true;
        }

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

        // Detectar plataforma (SOLO EL NOMBRE, NO EL COLOR)
        String platformName = detectPlatform(link);

        // ENVIAR BROADCAST (sin color - lo obtiene automáticamente)
        AnnouncementPipeline.execute(
                player.getName(),
                platformName,
                link);

        if (cooldown != null) {
            cooldown.apply(player.getUniqueId());
        }
        return true;
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

    // Detectar plataforma (Devuelve solo String)
    private String detectPlatform(String url) {
        String lower = url.toLowerCase();

        if (lower.contains("twitch.tv/")) {
            return "Twitch";
        }

        if (lower.contains("youtube.com/") || lower.contains("youtu.be/")) {
            return "YouTube";
        }

        if (lower.contains("kick.com/")) {
            return "Kick";
        }

        if (lower.contains("tiktok.com/")) {
            return "TikTok";
        }

        return "Directo";
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
        ChatUtils.send(player, "&7• &f/livestream https://kick.com/username");
        ChatUtils.send(player, "&7• &f/directo https://tiktok.com/@username/live");
        ChatUtils.send(player, "");
        ChatUtils.send(player, "&7The plugin will automatically detect the platform.");
        ChatUtils.send(player, ChatUtils.getLine());
    }
}