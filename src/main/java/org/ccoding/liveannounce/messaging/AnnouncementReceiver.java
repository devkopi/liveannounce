package org.ccoding.liveannounce.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;

import java.util.List;

/**
 * Recibe anuncios de otros servidores a través del proxy
 */
public class AnnouncementReceiver implements PluginMessageListener {
    private static final String CUSTOM_CHANNEL = "liveannounce:announce";

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Solo procesar nuestro canal personalizado
        if (!channel.equals(CUSTOM_CHANNEL)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            String playerName = in.readUTF();
            String platform = in.readUTF();
            String link = in.readUTF();
            String playerFormat = LiveAnnounce.getInstance().getConfig().getString("player-format", "%vault_prefix% %player_name%");

            List<Component> components = AnnouncementFormatter.createAnnouncement(
                    null, playerName, platform, link
            );

            if (components == null || components.isEmpty()) {
                return;
            }

            // Enviar a todos los jugadores locales usando Adventure
            for (Component component : components) {
                LiveAnnounce.getInstance().getAdventure().all().sendMessage(component);
            }

        } catch (Exception ignored) {}
    }
}