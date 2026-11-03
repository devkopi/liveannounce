package org.ccoding.liveannounce.announcement.parser;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChannelExtractor {

    private static class PlatformInfo {
        String platform;
        Pattern pattern;

        PlatformInfo(String platform, Pattern pattern) {
            this.platform = platform;
            this.pattern = pattern;
        }
    }

    private static final PlatformInfo[] PLATFORM_PATTERNS = {
            new PlatformInfo("twitch", Pattern.compile("(?:https?://)?(?:www\\.)?twitch\\.tv/([^/?#&]+)")),
            new PlatformInfo("youtube", Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/(?:c/|user/|@)?([^/?#&]+)")),
            new PlatformInfo("youtube", Pattern.compile("(?:https?://)?(?:www\\.)?youtu\\.be/([^/?#&]+)")),
            new PlatformInfo("kick", Pattern.compile("(?:https?://)?(?:www\\.)?kick\\.com/([^/?#&]+)")),
            new PlatformInfo("tiktok", Pattern.compile("(?:https?://)?(?:www\\.)?tiktok\\.com/@([^/?#&]+)"))
    };

    /**
     * Reads the channel name from a stream URL.
     */
    public static String extract(String link, String defaultChannel) {
        if (link == null || link.isEmpty()) return defaultChannel;

        for (PlatformInfo info : PLATFORM_PATTERNS) {
            Matcher matcher = info.pattern.matcher(link);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return defaultChannel;
    }

    /**
     * Detects the streaming platform from a URL.
     */
    public static String detectPlatform(String url) {
        if (url == null) return "unknown";

        for (PlatformInfo info : PLATFORM_PATTERNS) {
            if (info.pattern.matcher(url).find()) {
                return info.platform;
            }
        }

        return "unknown";
    }
}