package org.ccoding.liveannounce.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import net.md_5.bungee.api.chat.TextComponent;

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

            TextComponent[] components = AnnouncementFormatter.createAnnouncement(
                    playerName, platform, link
            );

            if (components == null || components.length == 0) {
                return;
            }

            // Enviar a todos los jugadores locales
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                for (TextComponent component : components) {
                    onlinePlayer.spigot().sendMessage(component);
                }
            }

        } catch (Exception ignored) {}
    }
}