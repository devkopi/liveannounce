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
 * Recibe anuncios de otros servidores a través de BungeeCord
 */
public class AnnouncementReceiver implements PluginMessageListener {
    private static final String CUSTOM_CHANNEL = "LiveAnnounce:Announce";

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Solo procesar nuestro canal personalizado
        if (!channel.equals(CUSTOM_CHANNEL)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            // Leer datos en el mismo orden que se enviaron
            String playerName = in.readUTF();
            String platform = in.readUTF();
            String link = in.readUTF();

            LiveAnnounce.getInstance().getLogger().info(
                    "Recibiendo anuncio de " + playerName + " (" + platform + ")"
            );

            // Regenerar el anuncio localmente
            TextComponent[] components = AnnouncementFormatter.createAnnouncement(
                    playerName, platform, link
            );

            if (components == null || components.length == 0) {
                LiveAnnounce.getInstance().getLogger().warning("No se pudieron crear componentes del anuncio recibido");
                return;
            }

            // Enviar a jugadores locales
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                // No enviar al jugador que actuó como "transportador"
                if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                    for (TextComponent component : components) {
                        onlinePlayer.spigot().sendMessage(component);
                    }
                }
            }

        } catch (Exception e) {
            LiveAnnounce.getInstance().getLogger().severe("Error procesando anuncio recibido: " + e.getMessage());
        }
    }
}