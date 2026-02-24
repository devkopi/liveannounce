package org.ccoding.liveannounce.announcement.parser;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChannelExtractor {
    private static final Pattern[] PATTERNS = {
            Pattern.compile("(?:https?://)?(?:www\\.)?twitch\\.tv/([^/?#&]+)"),
            Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/(?:c/|user/|@)?([^/?#&]+)"),
            Pattern.compile("(?:https?://)?(?:www\\.)?youtu\\.be/([^/?#&]+)"),
            Pattern.compile("(?:https?://)?(?:www\\.)?kick\\.com/([^/?#&]+)"),
            Pattern.compile("(?:https?://)?(?:www\\.)?tiktok\\.com/@([^/?#&]+)")
    };

    public static String extract(String link, String defaultChannel) {
        if (link == null || link.isEmpty()) return defaultChannel;

        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(link);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return defaultChannel;
    }
}