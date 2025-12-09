package org.ccoding.liveannounce.managers;

import org.bukkit.configuration.file.FileConfiguration;

public class MessageManager {
    private static FileConfiguration config;

    public static void setup(FileConfiguration config) {
        MessageManager.config = config;
    }

    public static String get(String key) {
        if (config == null) {
            return getDefault(key);
        }

        String message = config.getString("messages." + key);
        return message != null ? message : getDefault(key);
    }


    private static String getDefault(String key) {
        switch (key) {
            case "no-permission":
                return "&cYou don't have permission to execute this command.";
            case "invalid-link":
                return "&cInvalid link.&r &fSupported platforms: &dTwitch, &cYouTube, &aKick, &bTikTok";
            case "invalid-link-example":
                return "&fExample: &b/directo https://twitch.tv/ccoding";
            case "player-only":
                return "&cThis command can only be used by players.";
            case "unknown-command":
                return "&Unknown command. Use &b/la help";
            case "reloaded":
                return "&aConfiguration reloaded.";
            case "enabled":
                return "&aPlugin enabled.";
            case "disabled":
                return "&cPlugin disabled.";
            case "status-enabled":
                return "&7Status: &aEnabled";
            case "status-disabled":
                return "&7Status: &cDisabled";
            default:
                return "&cThis message does not exist. Contact an administrator.";
        }
    }

    public static void reload(FileConfiguration newConfig) {
        config = newConfig;
    }
}
