package org.ccoding.liveannounce.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.ChatUtils;

public class LiveAnnounceCommand implements CommandExecutor {

    private final LiveAnnounce plugin;

    public LiveAnnounceCommand() {
        this.plugin = LiveAnnounce.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, "&cNo tienes permiso.");
                    return true;
                }
                plugin.reloadPlugin();
                ChatUtils.sendMessage(sender, "&cConfiguración recargada.");
                break;

            case "version":
            case "ver":
                showVersion(sender);
                break;

            case "enable":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, "&cNo tienes permiso.");
                    return true;
                }
                plugin.getConfig().set("enabled", true);
                plugin.saveConfig();
                ChatUtils.sendMessage(sender, "&aPlugin habilitado.");
                break;

            case "disable":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, "&cNo tienes permiso.");
                    return true;
                }
                plugin.getConfig().set("enabled", false);
                plugin.saveConfig();
                ChatUtils.sendMessage(sender, "&cPlugin deshabilitado.");
                break;

            case "status":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, "&cNo tienes permiso.");
                    return true;
                }
                boolean enabled = plugin.getConfig().getBoolean("enabled", true);
                ChatUtils.sendMessage(sender, "&7Estado: " + (enabled ? "&aHabilitado" : "&cDeshabilitado"));
                break;

            default:
                ChatUtils.sendMessage(sender, "&cSubcomando no válido. Usa /la help");
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        ChatUtils.send(sender, ChatUtils.getLine());
        ChatUtils.send(sender, "&6&lLiveAnnounce v" + plugin.getDescription().getVersion());
        ChatUtils.send(sender, "");
        ChatUtils.send(sender, "&eComandos:");
        ChatUtils.send(sender, "&7• &f/directo <link>&8- &7Anuncia un directo");
        ChatUtils.send(sender, "&7• &f/la help &8- &7Muestra esta ayuda");
        ChatUtils.send(sender, "&7• &f/la reload &8- &7Recarga configuración");
        ChatUtils.send(sender, "&7• &f/la version &8- &7Muestra la versión");
        ChatUtils.send(sender, "&7• &f/la enable &8- &7Habilita el plugin");
        ChatUtils.send(sender, "&7• &f/la disable &8- &7Deshabilita el plugin");
        ChatUtils.send(sender, "&7• &f/la status &8- &7Muestra estado");
        ChatUtils.send(sender, "");
        ChatUtils.send(sender, "&eAlias: &f/directo, /la");
        ChatUtils.send(sender, ChatUtils.getLine());
    }

    private void showVersion(CommandSender sender) {
        ChatUtils.send(sender, ChatUtils.getLine());
        ChatUtils.send(sender, "&6&lLiveAnnounce &7v" + plugin.getDescription().getVersion());
        ChatUtils.send(sender, "");
        ChatUtils.send(sender, "&7- &fAuthor: &f" + plugin.getDescription().getAuthors());
        ChatUtils.send(sender, "&7- &fVersion: &f" + plugin.getDescription().getVersion());
        ChatUtils.send(sender, "");
        ChatUtils.send(sender, ChatUtils.getLine());
    }
}