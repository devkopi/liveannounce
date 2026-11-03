package org.ccoding.liveannounce.announcement.model;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.ccoding.liveannounce.announcement.parser.ChannelExtractor;
import org.ccoding.liveannounce.utils.ColorCache;
import org.bukkit.entity.Player;

public class AnnouncementData {

    private final String playerName;
    private final String playerFormat;
    private final String platformName;
    private final String platformUpper;
    private final String color;
    private final String link;
    private final String channel;

    public AnnouncementData(String playerName, String playerFormat, String platformName, String link) {
        this.playerName = playerName;
        this.playerFormat = playerFormat;
        this.platformName = platformName;
        this.platformUpper = platformName.toUpperCase();
        this.color = ColorCache.getPlatformColor(platformName.toLowerCase());
        this.link = link;
        this.channel = ChannelExtractor.extract(link, playerFormat);
    }

    public String getPlayerName() { return playerName;}
    public String getPlayerFormat() { return playerFormat; }
    public String getPlatformName() { return platformName; }
    public String getPlatformUpper() { return platformUpper; }
    public String getColor() { return color; }
    public String getLink() { return link; }
    public String getChannel() { return channel; }

    public String applyToTemplate(String template, Player player) {
        if ( template == null || template.isEmpty()) return "";

        StringBuilder result = new StringBuilder(template);

        replaceAll(result, "{player}", playerFormat);
        replaceAll(result, "{platform}", platformName);
        replaceAll(result, "{platform_upper}", platformUpper);
        replaceAll(result, "{color}", color);
        replaceAll(result, "{link}", link);
        replaceAll(result, "{channel}", channel);

        String finalMessage = result.toString();
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            finalMessage = PlaceholderAPI.setPlaceholders(player, finalMessage);
        }

        return finalMessage;
    }

    private void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index = builder.indexOf(from, index + to.length());
        }
    }
}
