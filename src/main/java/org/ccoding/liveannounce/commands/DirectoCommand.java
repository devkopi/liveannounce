package org.ccoding.liveannounce.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.ChatUtils;
import org.ccoding.liveannounce.utils.PlatformUtils;
import org.ccoding.liveannounce.utils.PlatformUtils.PlatformInfo;

public class DirectoCommand implements CommandExecutor {

    private final LiveAnnounce plugin;

    public DirectoCommand() {
        this.plugin = LiveAnnounce.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Verificar si el plugin está habilitado
        if (!plugin.getConfig().getBoolean("enabled", true)) {
            ChatUtils.send(sender, "&cEl plugin LiveAnnounce está deshabilitado.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("liveannounce.directo")) {
            ChatUtils.send(player, "&cNo tienes permiso.");
            return true;
        }

        if (args.length < 2) {
            ChatUtils.showDirectoHelp(player);
            return true;
        }

        String plataforma = args[0].toLowerCase();
        String canal = args[1];

        if (!PlatformUtils.isValid(plataforma)) {
            ChatUtils.send(player, "&c&l¡ERROR! &7Plataforma desconocida.");
            ChatUtils.send(player, "&7Usa: &e" + PlatformUtils.getAvailable());
            return true;
        }

        PlatformInfo info = PlatformUtils.getPlatform(plataforma);
        String link = info.generateLink(canal);

        String anuncio = ChatUtils.createAnnouncement(
                player.getName(),
                info.displayName,
                info.color,
                canal,
                link
        );

        ChatUtils.broadcastWithSpacers(anuncio);


        return true;
    }

}