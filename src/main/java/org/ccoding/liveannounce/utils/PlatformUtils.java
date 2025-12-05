package org.ccoding.liveannounce.utils;

import java.util.HashMap;
import java.util.Map;

public class PlatformUtils {

    private static final Map<String, PlatformInfo> PLATFORMS = new HashMap<>();

    static {
        registerPlatform("twitch", "Twitch", "&d", "https://twitch.tv/%s");
        registerPlatform("youtube", "YouTube", "&c", "https://youtube.com/live/%s");
        registerPlatform("kick", "Kick", "&a", "https://kick.com/%s");
        registerPlatform("tiktok", "TikTok", "&b", "https://tiktok.com/@%s/live");
    }

    private static void registerPlatform(String key, String name, String color, String urlFormat) {
        PLATFORMS.put(key, new PlatformInfo(name, color, urlFormat));
    }

    public static PlatformInfo getPlatform(String key) {
        return PLATFORMS.get(key.toLowerCase());
    }

    public static boolean isValid(String key) {
        return PLATFORMS.containsKey(key.toLowerCase());
    }

    public static String getAvailable() {
        return String.join("&7, &e", PLATFORMS.keySet());
    }

    public static class PlatformInfo {
        public final String displayName;
        public final String color;
        public final String urlFormat;

        public PlatformInfo(String displayName, String color, String urlFormat) {
            this.displayName = displayName;
            this.color = color;
            this.urlFormat = urlFormat;
        }

        public String generateLink(String channel) {
            return String.format(urlFormat, channel);
        }
    }
}