package org.ccoding.liveannounce.announcement.service;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.ccoding.liveannounce.announcement.model.LiveSession;
import org.ccoding.liveannounce.announcement.parser.ChannelExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * In-memory list of announced streams.
 * A put, a remove and a command read are constant time.
 * Expired rows are deleted by a single async task that never touches
 * Bukkit, players, the config, or the network.
 */
public final class LiveSessionManager {

    private static final long SAFETY_CAP_SECONDS = 6L * 60L * 60L;
    private static final int MIN_CLEANUP_SECONDS = 10;
    private static final int MAX_DISPLAY_CAP = 50;

    private static final Map<String, LiveSession> SESSIONS = new ConcurrentHashMap<String, LiveSession>();

    private static volatile boolean enabled;
    private static volatile boolean removeOnQuit;
    private static volatile long durationMillis;
    private static volatile int cleanupIntervalSeconds = 30;
    private static volatile int maxDisplay = 10;

    private static volatile String header = "&6&lLIVE NOW";
    private static volatile String entry = "&f{player} &7on {color}{platform} &8• &e{channel} &8(&7{time}&8)";
    private static volatile String hover = "&eClick to open the stream";
    private static volatile String empty = "&7Nobody is live right now.";
    private static volatile String footer = "&7{count} live";
    private static volatile String more = "&8+{count} more";
    private static volatile String disabled = "&cThe live list is disabled.";
    private static volatile String timeUnlimited = "until disconnect";

    private static volatile BukkitTask cleanupTask;
    private static volatile int cleanupToken;

    private LiveSessionManager() {
    }

    public static final class Snapshot {
        private final List<LiveSession> shown;
        private final int total;

        private Snapshot(List<LiveSession> shown, int total) {
            this.shown = shown;
            this.total = total;
        }

        public List<LiveSession> getShown() {
            return shown;
        }

        public int getTotal() {
            return total;
        }
    }

    public static void load(FileConfiguration config, Logger logger) {
        enabled = config.getBoolean("live-sessions.enabled", true);
        removeOnQuit = config.getBoolean("live-sessions.remove-on-quit", false);

        int durationSeconds = config.getInt("live-sessions.duration-seconds", 5400);
        if (durationSeconds < 0) {
            durationSeconds = 0;
        }
        if (durationSeconds == 0 && !removeOnQuit) {
            durationSeconds = (int) SAFETY_CAP_SECONDS;
            logger.warning("live-sessions would never expire (duration-seconds: 0 and remove-on-quit: false). Using a 6 hour cap.");
        }
        durationMillis = durationSeconds * 1000L;

        int interval = config.getInt("live-sessions.cleanup-interval-seconds", 30);
        cleanupIntervalSeconds = Math.max(MIN_CLEANUP_SECONDS, interval);

        int display = config.getInt("live-sessions.max-display", 10);
        if (display < 1) {
            display = 1;
        }
        maxDisplay = Math.min(display, MAX_DISPLAY_CAP);

        header = config.getString("live-sessions.messages.header", "&6&lLIVE NOW");
        entry = config.getString("live-sessions.messages.entry", "&f{player} &7on {color}{platform} &8• &e{channel} &8(&7{time}&8)");
        hover = config.getString("live-sessions.messages.hover", "&eClick to open the stream");
        empty = config.getString("live-sessions.messages.empty", "&7Nobody is live right now.");
        footer = config.getString("live-sessions.messages.footer", "&7{count} live");
        more = config.getString("live-sessions.messages.more", "&8+{count} more");
        disabled = config.getString("live-sessions.messages.disabled", "&cThe live list is disabled.");
        timeUnlimited = config.getString("live-sessions.messages.time-unlimited", "until disconnect");
    }

    public static void reload(JavaPlugin plugin) {
        load(plugin.getConfig(), plugin.getLogger());
        if (!enabled) {
            SESSIONS.clear();
        }
        startCleanup(plugin);
    }

    public static void startCleanup(JavaPlugin plugin) {
        stopCleanup();
        if (!enabled || durationMillis <= 0) {
            return;
        }

        final int token = cleanupToken;
        long ticks = cleanupIntervalSeconds * 20L;
        cleanupTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (token != cleanupToken) {
                    return;
                }
                purgeExpired(System.currentTimeMillis());
            }
        }, ticks, ticks);
    }

    public static void shutdown() {
        stopCleanup();
        SESSIONS.clear();
    }

    public static void register(String playerName, String platform, String link, String channel) {
        if (!enabled || playerName == null || link == null) {
            return;
        }

        String name = playerName.trim();
        if (name.isEmpty() || link.isEmpty()) {
            return;
        }

        String platformName = (platform == null || platform.trim().isEmpty()) ? "Live" : platform.trim();
        String resolvedChannel = resolveChannel(channel, link, name);
        long now = System.currentTimeMillis();
        long expiresAt = durationMillis > 0 ? now + durationMillis : 0L;

        SESSIONS.put(key(name), new LiveSession(name, platformName, resolvedChannel, link, now, expiresAt));
    }

    public static void onPlayerQuit(String playerName) {
        if (!removeOnQuit || playerName == null || playerName.isEmpty()) {
            return;
        }
        SESSIONS.remove(key(playerName));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int activeCount() {
        long now = System.currentTimeMillis();
        int count = 0;
        for (LiveSession session : SESSIONS.values()) {
            if (!session.isExpired(now)) {
                count++;
            }
        }
        return count;
    }

    public static Snapshot snapshot() {
        long now = System.currentTimeMillis();
        List<LiveSession> active = new ArrayList<LiveSession>();
        for (LiveSession session : SESSIONS.values()) {
            if (!session.isExpired(now)) {
                active.add(session);
            }
        }

        Collections.sort(active, new Comparator<LiveSession>() {
            @Override
            public int compare(LiveSession left, LiveSession right) {
                return Long.compare(right.getAnnouncedAt(), left.getAnnouncedAt());
            }
        });

        int total = active.size();
        int limit = Math.min(maxDisplay, total);
        List<LiveSession> shown = new ArrayList<LiveSession>(limit);
        for (int i = 0; i < limit; i++) {
            shown.add(active.get(i));
        }
        return new Snapshot(Collections.unmodifiableList(shown), total);
    }

    public static String formatRemaining(LiveSession session, long now) {
        if (session.getExpiresAt() <= 0) {
            return timeUnlimited;
        }

        long seconds = Math.max(0L, (session.getExpiresAt() - now) / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return seconds + "s";
    }

    public static String getHeader() {
        return header;
    }

    public static String getEntry() {
        return entry;
    }

    public static String getHover() {
        return hover;
    }

    public static String getEmpty() {
        return empty;
    }

    public static String getFooter() {
        return footer;
    }

    public static String getMore() {
        return more;
    }

    public static String getDisabled() {
        return disabled;
    }

    private static void stopCleanup() {
        cleanupToken++;
        BukkitTask current = cleanupTask;
        cleanupTask = null;
        if (current != null) {
            current.cancel();
        }
    }

    /**
     * Removes only the instance that was already expired.
     * A player who announces again keeps the new row.
     */
    private static void purgeExpired(long now) {
        List<String> expiredKeys = new ArrayList<String>();
        for (Map.Entry<String, LiveSession> entry : SESSIONS.entrySet()) {
            if (entry.getValue().isExpired(now)) {
                expiredKeys.add(entry.getKey());
            }
        }
        for (int i = 0; i < expiredKeys.size(); i++) {
            String key = expiredKeys.get(i);
            LiveSession current = SESSIONS.get(key);
            if (current != null && current.isExpired(now)) {
                SESSIONS.remove(key, current);
            }
        }
    }

    private static String resolveChannel(String channel, String link, String playerName) {
        if (channel == null || channel.trim().isEmpty() || channel.indexOf('%') >= 0 || channel.indexOf('{') >= 0) {
            return ChannelExtractor.extract(link, playerName);
        }
        return channel.trim();
    }

    private static String key(String playerName) {
        return playerName.toLowerCase(Locale.ROOT);
    }
}
