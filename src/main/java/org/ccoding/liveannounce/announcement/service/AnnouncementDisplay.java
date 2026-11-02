package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;
import org.ccoding.liveannounce.utils.ChatUtils;

import java.time.Duration;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Title and bossbar for a stream announcement.
 * Both are sent once, on the main thread, when the announcement already happens.
 * The bossbar is not updated every tick. One delayed task hides it.
 */
public final class AnnouncementDisplay {

    private static final int MAX_FADE_TICKS = 40;
    private static final int MAX_STAY_TICKS = 200;
    private static final int MAX_BOSSBAR_SECONDS = 30;

    private static volatile boolean titleEnabled = true;
    private static volatile String titleText = "&e⚡ &f&lLIVE ON {platform_upper} &e⚡";
    private static volatile String subtitleText = "&f{player} &7is live on {platform}";
    private static volatile int fadeIn = 10;
    private static volatile int stay = 70;
    private static volatile int fadeOut = 10;

    private static volatile boolean bossbarEnabled = true;
    private static volatile String bossbarText = "&e⚡ &f{player} &7is live on {color}{platform}";
    private static volatile BossBar.Color bossbarColor = BossBar.Color.YELLOW;
    private static volatile BossBar.Overlay bossbarOverlay = BossBar.Overlay.PROGRESS;
    private static volatile float bossbarProgress = 1.0F;
    private static volatile int bossbarSeconds = 12;

    private static BossBar activeBar;
    private static BukkitTask hideTask;

    private AnnouncementDisplay() {
    }

    public static void load(FileConfiguration config, Logger logger) {
        titleEnabled = config.getBoolean("display.title.enabled", true);
        titleText = config.getString("display.title.title", "&e⚡ &f&lLIVE ON {platform_upper} &e⚡");
        subtitleText = config.getString("display.title.subtitle", "&f{player} &7is live on {platform}");
        fadeIn = capped(config, "display.title.fade-in", 10, 0, MAX_FADE_TICKS, logger);
        stay = capped(config, "display.title.stay", 70, 0, MAX_STAY_TICKS, logger);
        fadeOut = capped(config, "display.title.fade-out", 10, 0, MAX_FADE_TICKS, logger);

        boolean wasEnabled = bossbarEnabled;
        bossbarEnabled = config.getBoolean("display.bossbar.enabled", true);
        bossbarText = config.getString("display.bossbar.text", "&e⚡ &f{player} &7is live on {color}{platform}");
        bossbarColor = parseColor(config.getString("display.bossbar.color", "YELLOW"), logger);
        bossbarOverlay = parseOverlay(config.getString("display.bossbar.style", "SOLID"), logger);
        bossbarProgress = cappedProgress(config, logger);
        bossbarSeconds = capped(config, "display.bossbar.seconds", 12, 1, MAX_BOSSBAR_SECONDS, logger);

        if (wasEnabled && !bossbarEnabled && Bukkit.isPrimaryThread()) {
            hideActive();
        }
    }

    public static void show(final Player source, final AnnouncementData data) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(LiveAnnounce.getInstance(), new Runnable() {
                @Override
                public void run() {
                    show(source, data);
                }
            });
            return;
        }

        try {
            if (titleEnabled) {
                sendTitle(source, data);
            }
            if (bossbarEnabled) {
                showBar(source, data);
            }
        } catch (Exception ex) {
            LiveAnnounce.getInstance().getLogger().warning("Could not show the on-screen announcement: " + ex.getMessage());
        }
    }

    public static void shutdown() {
        if (!Bukkit.isPrimaryThread()) {
            return;
        }
        hideActive();
    }

    public static String statusLine() {
        return "&7Title: " + state(titleEnabled) + " &8| &7Bossbar: " + state(bossbarEnabled);
    }

    private static void sendTitle(Player source, AnnouncementData data) {
        String titleRaw = data.applyToTemplate(titleText, source);
        String subtitleRaw = data.applyToTemplate(subtitleText, source);
        if (isBlank(titleRaw) && isBlank(subtitleRaw)) {
            return;
        }

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L));
        Title title = Title.title(ChatUtils.format(titleRaw), ChatUtils.format(subtitleRaw), times);

        for (Player online : Bukkit.getOnlinePlayers()) {
            LiveAnnounce.getInstance().getAdventure().player(online).showTitle(title);
        }
    }

    private static void showBar(Player source, AnnouncementData data) {
        hideActive();

        Component name = ChatUtils.format(data.applyToTemplate(bossbarText, source));
        BossBar bar = BossBar.bossBar(name, bossbarProgress, bossbarColor, bossbarOverlay);
        activeBar = bar;

        for (Player online : Bukkit.getOnlinePlayers()) {
            LiveAnnounce.getInstance().getAdventure().player(online).showBossBar(bar);
        }

        final BossBar shown = bar;
        hideTask = Bukkit.getScheduler().runTaskLater(LiveAnnounce.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (activeBar == shown) {
                    hideActive();
                }
            }
        }, bossbarSeconds * 20L);
    }

    private static void hideActive() {
        BukkitTask task = hideTask;
        hideTask = null;
        if (task != null) {
            task.cancel();
        }

        BossBar bar = activeBar;
        activeBar = null;
        LiveAnnounce plugin = LiveAnnounce.getInstance();
        if (bar == null || plugin == null || plugin.getAdventure() == null) {
            return;
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            plugin.getAdventure().player(online).hideBossBar(bar);
        }
    }

    private static int capped(FileConfiguration config, String path, int fallback, int min, int max, Logger logger) {
        int configured = config.getInt(path, fallback);
        int value = configured;
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        if (config.contains(path) && value != configured) {
            logger.warning(path + " was capped to " + value + ".");
        }
        return value;
    }

    private static float cappedProgress(FileConfiguration config, Logger logger) {
        double configured = config.getDouble("display.bossbar.progress", 1.0D);
        float value = (float) configured;
        if (value < 0F) {
            value = 0F;
        } else if (value > 1F) {
            value = 1F;
        }
        if (config.contains("display.bossbar.progress") && value != (float) configured) {
            logger.warning("display.bossbar.progress must be between 0 and 1. Using " + value + ".");
        }
        return value;
    }

    private static BossBar.Color parseColor(String raw, Logger logger) {
        if (raw == null || raw.trim().isEmpty()) {
            return BossBar.Color.YELLOW;
        }
        try {
            return BossBar.Color.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warning("Unknown bossbar color '" + raw + "'. Using YELLOW.");
            return BossBar.Color.YELLOW;
        }
    }

    private static BossBar.Overlay parseOverlay(String raw, Logger logger) {
        if (raw == null || raw.trim().isEmpty()) {
            return BossBar.Overlay.PROGRESS;
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        if ("SOLID".equals(key) || "PROGRESS".equals(key)) {
            return BossBar.Overlay.PROGRESS;
        }
        if ("SEGMENTED_6".equals(key) || "NOTCHED_6".equals(key)) {
            return BossBar.Overlay.NOTCHED_6;
        }
        if ("SEGMENTED_10".equals(key) || "NOTCHED_10".equals(key)) {
            return BossBar.Overlay.NOTCHED_10;
        }
        if ("SEGMENTED_12".equals(key) || "NOTCHED_12".equals(key)) {
            return BossBar.Overlay.NOTCHED_12;
        }
        if ("SEGMENTED_20".equals(key) || "NOTCHED_20".equals(key)) {
            return BossBar.Overlay.NOTCHED_20;
        }
        logger.warning("Unknown bossbar style '" + raw + "'. Using SOLID.");
        return BossBar.Overlay.PROGRESS;
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static String state(boolean enabled) {
        return enabled ? "&aOn" : "&cOff";
    }
}
