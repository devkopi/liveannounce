package org.ccoding.liveannounce.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ccoding.liveannounce.announcement.service.LiveSessionManager;

/**
 * Quit already runs on the main thread. This handler only removes one map entry.
 */
public class LiveSessionListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LiveSessionManager.onPlayerQuit(event.getPlayer().getName());
    }
}
