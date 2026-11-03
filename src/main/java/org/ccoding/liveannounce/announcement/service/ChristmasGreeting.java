package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;
import org.ccoding.liveannounce.utils.ChatUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * A short seasonal line and an optional bossbar color.
 * The date is checked once per announcement. There is no repeating task.
 */
public final class ChristmasGreeting {

    private static final MonthDay DEFAULT_FROM = MonthDay.of(12, 1);
    private static final MonthDay DEFAULT_TO = MonthDay.of(12, 26);

    private static volatile boolean enabled = true;
    private static volatile String mode = "auto";
    private static volatile MonthDay from = DEFAULT_FROM;
    private static volatile MonthDay to = DEFAULT_TO;
    private static volatile String line = "&f❄ &c&lHappy Holidays&f ❄";
    private static volatile BossBar.Color bossbarColor = BossBar.Color.RED;

    private ChristmasGreeting() {
    }

    public static void load(FileConfiguration config, Logger logger) {
        enabled = config.getBoolean("christmas.enabled", true);
        String configuredMode = config.getString("christmas.mode", "auto");
        if (configuredMode == null) {
            configuredMode = "auto";
        }
        String normalized = configuredMode.trim().toLowerCase(Locale.ROOT);
        if (!"auto".equals(normalized) && !"on".equals(normalized) && !"off".equals(normalized)) {
            logger.warning("christmas.mode must be auto, on, or off. Using auto.");
            normalized = "auto";
        }
        mode = normalized;
        from = parseDay(config.getString("christmas.from", "12-01"), DEFAULT_FROM, "christmas.from", logger);
        to = parseDay(config.getString("christmas.to", "12-26"), DEFAULT_TO, "christmas.to", logger);
        line = config.getString("christmas.line", "&f❄ &c&lHappy Holidays&f ❄");
        bossbarColor = parseColor(config.getString("christmas.bossbar-color", "RED"), logger);
    }

    public static boolean isActive() {
        if (!enabled || "off".equals(mode)) {
            return false;
        }
        if ("on".equals(mode)) {
            return true;
        }
        MonthDay today = MonthDay.from(LocalDate.now());
        if (from.compareTo(to) <= 0) {
            return !today.isBefore(from) && !today.isAfter(to);
        }
        return !today.isBefore(from) || !today.isAfter(to);
    }

    public static void addLine(List<Component> components, AnnouncementData data, Player player) {
        if (!isActive() || components == null || components.isEmpty() || line == null || line.trim().isEmpty()) {
            return;
        }
        String text = data.applyToTemplate(line, player);
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        int index = components.size() > 1 ? 1 : components.size();
        components.add(index, ChatUtils.format(text));
    }

    public static BossBar.Color bossbarColor(BossBar.Color fallback) {
        if (!isActive() || bossbarColor == null) {
            return fallback;
        }
        return bossbarColor;
    }

    public static String statusLine() {
        if (!enabled || "off".equals(mode)) {
            return "&7Christmas: &cOff";
        }
        if (isActive()) {
            return "&7Christmas: &aOn";
        }
        return "&7Christmas: &7Scheduled";
    }

    private static MonthDay parseDay(String raw, MonthDay fallback, String path, Logger logger) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        String[] parts = raw.trim().split("-");
        if (parts.length != 2) {
            logger.warning(path + " must look like 12-01. Using the default.");
            return fallback;
        }
        try {
            return MonthDay.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (NumberFormatException ex) {
            logger.warning(path + " must look like 12-01. Using the default.");
            return fallback;
        } catch (DateTimeException ex) {
            logger.warning(path + " is not a real date. Using the default.");
            return fallback;
        }
    }

    private static BossBar.Color parseColor(String raw, Logger logger) {
        if (raw == null || raw.trim().isEmpty() || "none".equalsIgnoreCase(raw.trim())) {
            return null;
        }
        try {
            return BossBar.Color.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warning("Unknown christmas bossbar color '" + raw + "'. The normal bossbar color will be used.");
            return null;
        }
    }
}
