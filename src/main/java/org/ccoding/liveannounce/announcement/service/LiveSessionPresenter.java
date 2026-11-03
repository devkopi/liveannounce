package org.ccoding.liveannounce.announcement.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.ccoding.liveannounce.announcement.model.LiveSession;
import org.ccoding.liveannounce.utils.ChatUtils;
import org.ccoding.liveannounce.utils.ColorCache;

import java.util.Locale;

/**
 * Renders /la live. Work is bounded by max-display and uses data
 * already stored in memory. No files, no HTTP, no PlaceholderAPI.
 */
public final class LiveSessionPresenter {

    private LiveSessionPresenter() {
    }

    public static void send(CommandSender sender) {
        if (!LiveSessionManager.isEnabled()) {
            ChatUtils.sendMessage(sender, LiveSessionManager.getDisabled());
            return;
        }

        LiveSessionManager.Snapshot snapshot = LiveSessionManager.snapshot();
        ChatUtils.send(sender, ChatUtils.getLine());
        ChatUtils.send(sender, LiveSessionManager.getHeader());

        if (snapshot.getTotal() == 0) {
            ChatUtils.send(sender, "");
            ChatUtils.send(sender, LiveSessionManager.getEmpty());
            ChatUtils.send(sender, ChatUtils.getLine());
            return;
        }

        ChatUtils.send(sender, "");
        long now = System.currentTimeMillis();
        for (LiveSession session : snapshot.getShown()) {
            ChatUtils.send(sender, entryLine(session, now));
        }

        int hidden = snapshot.getTotal() - snapshot.getShown().size();
        if (hidden > 0) {
            ChatUtils.send(sender, LiveSessionManager.getMore().replace("{count}", Integer.toString(hidden)));
        }

        ChatUtils.send(sender, LiveSessionManager.getFooter().replace("{count}", Integer.toString(snapshot.getTotal())));
        ChatUtils.send(sender, ChatUtils.getLine());
    }

    private static Component entryLine(LiveSession session, long now) {
        String time = LiveSessionManager.formatRemaining(session, now);
        String color = ColorCache.getPlatformColor(session.getPlatform().toLowerCase(Locale.ROOT));
        String text = apply(LiveSessionManager.getEntry(), session, time, color);
        Component component = ChatUtils.format(text);

        String link = session.getLink();
        if (link == null || link.isEmpty()) {
            return component;
        }

        try {
            String hover = apply(LiveSessionManager.getHover(), session, time, color);
            component = component.clickEvent(ClickEvent.openUrl(link));
            component = component.hoverEvent(HoverEvent.showText(ChatUtils.format(hover)));
        } catch (IllegalArgumentException ignored) {
            return component;
        }
        return component;
    }

    private static String apply(String template, LiveSession session, String time, String color) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        return template
                .replace("{player}", session.getPlayerName())
                .replace("{platform_upper}", session.getPlatform().toUpperCase(Locale.ROOT))
                .replace("{platform}", session.getPlatform())
                .replace("{channel}", session.getChannel())
                .replace("{link}", session.getLink())
                .replace("{time}", time)
                .replace("{color}", color);
    }
}
