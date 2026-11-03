package org.ccoding.liveannounce.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.managers.PrefixManager;

public class ChatUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final LegacyComponentSerializer BUKKIT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    /**
     * Turns a legacy or MiniMessage string into an Adventure component.
     */

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component format(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        if (text.contains("<") && text.contains(">")) {
            return mm.deserialize(text);
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }

    /**
     * Legacy text with section signs, for Bukkit sendTitle.
     */
    public static String toBukkitText(Component component) {
        if (component == null) {
            return "";
        }
        return BUKKIT_SERIALIZER.serialize(component);
    }

    /**
     * @deprecated Use {@link #format(String)}.
     */
    @Deprecated
    public static String color(String text) {
        if (text == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            send(sender, format(message));
        }
    }

    public static void send(CommandSender sender, Component component) {
        if (sender == null || component == null) {
            return;
        }
        if (sender instanceof Player) {
            LiveAnnounce.getInstance().getAdventure().player((Player) sender).sendMessage(component);
        } else {
            LiveAnnounce.getInstance().getAdventure().console().sendMessage(component);
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            send(sender, PrefixManager.get() + message);
        }
    }

    public static void broadcast(String message) {
        if (message != null) {
            LiveAnnounce.getInstance().getAdventure().all().sendMessage(format(message));
        }
    }


    public static String getLine() {
        return "&8&m--------------------------------------------------";
    }
}