package org.ccoding.liveannounce.proxy.bridges;

import net.kyori.adventure.text.Component;
import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;

import java.util.List;

public class StandaloneBridge implements Bridge {
    private final LiveAnnounce plugin;

    public StandaloneBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Standalone";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {
        // En Standalone, el broadcast ya lo maneja el AnnouncementPipeline
    }

    private void broadcastLocally(String playerName, String platform, String link) {
        List<Component> components = AnnouncementFormatter.createAnnouncement(playerName, platform, link);

        if (components == null || components.isEmpty()) return;

        for (Component component : components) {
            LiveAnnounce.getInstance().getAdventure().all().sendMessage(component);
        }
    }
}