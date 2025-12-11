package org.ccoding.liveannounce.utils;

public class AnnouncementData {
    
    private final String playerName;
    private final String platformName;
    private final String platformUpper;
    private final String color;
    private final String link;
    private final String channel;

    // Constructor - Aquí se calcula todo
    public AnnouncementData(String playerName, String platformName, String link) {
        this.playerName = playerName;
        this.platformName = platformName;
        this.platformUpper = platformName.toUpperCase();
        this.color = ColorCache.getPlatformColor(platformName.toLowerCase());
        this.link = link;
        this.channel = ChannelExtractor.extract(link, playerName);
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public String getPlatformName() { return platformName; }
    public String getPlatformUpper() { return platformUpper; }
    public String getColor() { return color; }
    public String getLink() { return link; }
    public String getChannel() { return channel; }

    // Reemplaza variables
    public String applyToTemplate(String template) {
        if ( template == null || template.isEmpty()) return "";

        StringBuilder result = new StringBuilder(template);

        replaceAll(result, "{player}", playerName);
        replaceAll(result, "{platform}", platformName);
        replaceAll(result, "{platform_upper}", platformUpper);
        replaceAll(result, "{color}", color);
        replaceAll(result, "{link}", link);
        replaceAll(result, "{channel}", channel);

        return result.toString();
    }

    // Metodo auxiliar para reemplazar texto
    private void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index = builder.indexOf(from, index + to.length());
        }
    }
}
