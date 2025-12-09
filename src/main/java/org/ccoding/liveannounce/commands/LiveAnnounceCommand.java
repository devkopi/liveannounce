package org.ccoding.liveannounce.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.managers.MessageManager;
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
                    ChatUtils.sendMessage(sender, MessageManager.get("no-permission"));
                    return true;
                }
                plugin.reloadPlugin();
                ChatUtils.sendMessage(sender, MessageManager.get("reloaded"));
                break;

            case "version":
            case "ver":
                showVersion(sender);
                break;

            case "enable":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, MessageManager.get("no-permission"));
                    return true;
                }
                plugin.getConfig().set("enabled", true);
                plugin.saveConfig();
                ChatUtils.sendMessage(sender, MessageManager.get("enabled"));
                break;

            case "disable":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, MessageManager.get("no-permission"));
                    return true;
                }
                plugin.getConfig().set("enabled", false);
                plugin.saveConfig();
                ChatUtils.sendMessage(sender, MessageManager.get("disabled"));
                break;

            case "status":
                if (!sender.hasPermission("liveannounce.admin")) {
                    ChatUtils.sendMessage(sender, MessageManager.get("no-permission"));
                    return true;
                }
                boolean enabled = plugin.getConfig().getBoolean("enabled", true);

                String statusKey = enabled ? "status-enabled" : "status-disabled";
                ChatUtils.sendMessage(sender, MessageManager.get(statusKey));
                break;

            default:
                ChatUtils.sendMessage(sender, MessageManager.get("unknown-command"));
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        ChatUtils.send(sender, ChatUtils.getLine());
        ChatUtils.send(sender, "&6&lLiveAnnounce v" + plugin.getDescription().getVersion());
        ChatUtils.send(sender, "");
        ChatUtils.send(sender, "&eCommands:");
        ChatUtils.send(sender, "&7• &f/directo <link>&8- &7Announce a live stream");
        ChatUtils.send(sender, "&7• &f/la help &8- &7Shows this help");
        ChatUtils.send(sender, "&7• &f/la reload &8- &7Reloads configuration");
        ChatUtils.send(sender, "&7• &f/la version &8- &7Shows version");
        ChatUtils.send(sender, "&7• &f/la enable &8- &7Enables the plugin");
        ChatUtils.send(sender, "&7• &f/la disable &8- &7Disables the plugin");
        ChatUtils.send(sender, "&7• &f/la status &8- &7Shows status");
        ChatUtils.send(sender, "");
        ChatUtils.send(sender, "&eAliases: &f/directo, /la");
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