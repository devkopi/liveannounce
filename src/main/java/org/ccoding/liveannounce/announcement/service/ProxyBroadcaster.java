package org.ccoding.liveannounce.announcement.service;

import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.proxy.BridgeManager;
import net.md_5.bungee.api.chat.TextComponent;

public class ProxyBroadcaster {

    public static void broadcastThroughProxy(TextComponent[] components,
                                             String playerName,
                                             String platform,
                                             String link) {
        if (components == null || components.length == 0) {
            return;
        }

        Bridge bridge = BridgeManager.getActiveBridge();

        if (bridge == null) {
            // Fallback local
            broadcastLocally(components);
            return;
        }

        // Usar el método nuevo que envía datos estructurados
        bridge.broadcastAnnouncement(playerName, platform, link);

        // NOTA: No llamamos broadcastLocally aquí porque
        // cada bridge ya maneja el envío local
    }

    private static void broadcastLocally(TextComponent[] components) {
        // ... tu código actual
    }
}