package org.ccoding.liveannounce.managers;

import org.bukkit.configuration.file.FileConfiguration;

public class PrefixManager {

    private static String prefix;

    private PrefixManager() {

    }

    public static void load(FileConfiguration config) {
        prefix = config.getString("prefix", "&8[&bLiveAnnounce&8] &7");
    }

    public static String get() {
        return prefix != null ? prefix : "&8[&bLiveAnnounce&8] &7";
    }
}
