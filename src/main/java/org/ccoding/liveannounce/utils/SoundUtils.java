package org.ccoding.liveannounce.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.managers.MuteManager;

public class SoundUtils {

    private static final LiveAnnounce plugin = LiveAnnounce.getInstance();

    /**
     * Plays a sound for every online player who has not muted announcements.
     */
    public static void playSoundToAll(String soundKey) {
        if (!plugin.getConfig().getBoolean("sounds.enabled", true)) {
            return;
        }

        String soundName = plugin.getConfig().getString("sounds." + soundKey + ".sound", "ENTITY_PLAYER_LEVELUP");
        float volume = (float) plugin.getConfig().getDouble("sounds." + soundKey + ".volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("sounds." + soundKey + ".pitch", 1.0);

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Sound sound = Sound.valueOf(soundName);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (MuteManager.isMuted(player.getUniqueId())) {
                        continue;
                    }
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name: " + soundName + " for key: " + soundKey);
            }
        });
    }

    /**
     * Plays a sound for one player.
     */
    public static void playSoundToPlayer(Player player, String soundKey) {
        if (!plugin.getConfig().getBoolean("sounds.enabled", true) || player == null) {
            return;
        }

        String soundName = plugin.getConfig().getString("sounds." + soundKey + ".sound", "ENTITY_PLAYER_LEVELUP");
        float volume = (float) plugin.getConfig().getDouble("sounds." + soundKey + ".volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble("sounds." + soundKey + ".pitch", 1.0);

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name: " + soundName + " for key: " + soundKey);
            }
        });
    }
}