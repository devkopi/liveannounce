package org.ccoding.liveannounce.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the chat lines for a stream announcement.
 */
public class AnnouncementFormatter {

    private enum FormatKey {
        LINE1, TITLE, DESCRIPTION, LINK, HOVER, LINE2;

        public String getConfigKey() {
            return name().toLowerCase();
        }
    }

    private static final String[] FORMATS = new String[FormatKey.values().length];
    private static boolean initialized = false;

    private AnnouncementFormatter() {
    }

    public static void initialize(FileConfiguration config) {
        if (initialized)
            return;

        ConfigurationSection formatsSection = config.getConfigurationSection("announcement-formats.default");

        for (FormatKey key : FormatKey.values()) {
            FORMATS[key.ordinal()] = formatsSection != null
                    ? formatsSection.getString(key.getConfigKey(), getDefaultFormat(key))
                    : getDefaultFormat(key);
        }

        initialized = true;
    }

    private static String getDefaultFormat(FormatKey key) {
        switch (key) {
            case LINE1:
                return "&8&m--------------------------------------------------";
            case TITLE:
                return "&f{color}&lLIVE ON {platform_upper}! &f";
            case DESCRIPTION:
                return "&f{player} &7is now streaming live";
            case LINK:
                return "&7Join now! {color}&n{link}";
            case HOVER:
                return "&eClick to open the stream!";
            case LINE2:
                return "&8&m--------------------------------------------------";
            default:
                return "&7[" + key + "]";
        }
    }

    public static List<Component> createAnnouncement(Player player,
            String platformName,
            String link,
            String playerFormat) {
        if (!initialized) {
            LiveAnnounce.getInstance().getLogger().warning(
                    "AnnouncementFormatter not initialized!");
            return new ArrayList<>();
        }

        AnnouncementData data = new AnnouncementData(
                player != null ? player.getName() : "Unknown",
                playerFormat,
                platformName,
                link);

        List<Component> components = new ArrayList<>();
        components.add(createFormattedLine(FORMATS[FormatKey.LINE1.ordinal()], data, player));
        components.add(createFormattedLine(FORMATS[FormatKey.TITLE.ordinal()], data, player));
        components.add(createFormattedLine(FORMATS[FormatKey.DESCRIPTION.ordinal()], data, player));
        components.add(createClickableLink(
                FORMATS[FormatKey.LINK.ordinal()],
                FORMATS[FormatKey.HOVER.ordinal()],
                data,
                player));
        components.add(createFormattedLine(FORMATS[FormatKey.LINE2.ordinal()], data, player));

        return components;
    }

    private static Component createFormattedLine(String template, AnnouncementData data, Player player) {
        String text = data.applyToTemplate(template, player);
        return ChatUtils.format(text);
    }

    private static Component createClickableLink(String linkTemplate,
            String hoverTemplate,
            AnnouncementData data,
            Player player) {
        String linkText = data.applyToTemplate(linkTemplate, player);
        String hoverText = data.applyToTemplate(hoverTemplate, player);

        Component component = ChatUtils.format(linkText);
        
        if (data.getLink() != null && !data.getLink().isEmpty()) {
            component = component.clickEvent(ClickEvent.openUrl(data.getLink()));
            component = component.hoverEvent(HoverEvent.showText(ChatUtils.format(hoverText)));
        }

        return component;
    }

    public static void reload(FileConfiguration config) {
        for (int i = 0; i < FORMATS.length; i++) {
            FORMATS[i] = null;
        }

        initialized = false;
        initialize(config);

        ColorCache.clear();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static int getFormatCount() {
        return FORMATS.length;
    }
}