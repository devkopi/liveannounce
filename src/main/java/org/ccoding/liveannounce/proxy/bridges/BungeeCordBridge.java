package org.ccoding.liveannounce.proxy.bridges;

import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.LiveAnnounce;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Collection;

public class BungeeCordBridge implements Bridge {
    private final LiveAnnounce plugin;
    private static final String CUSTOM_CHANNEL = "liveAnnounce:announce";

    public BungeeCordBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "BungeeCord";
    }

    @Override
    public boolean isAvailable() {
        return plugin.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {
        if (!isAvailable()) {
            // Fallback local
            broadcastLocally(playerName, platform, link);
            return;
        }

        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();

        if (onlinePlayers.isEmpty()) {
            plugin.getLogger().warning("No hay jugadores para enviar anuncio a BungeeCord");
            broadcastLocally(playerName, platform, link);
            return;
        }

        try {
            // 1. Enviar a otros servidores vía BungeeCord
            sendToOtherServers(playerName, platform, link);

            // 2. También enviar localmente
            broadcastLocally(playerName, platform, link);

            plugin.getLogger().info("✓ Anuncio enviado a toda la red BungeeCord");

        } catch (Exception e) {
            plugin.getLogger().severe("Error enviando anuncio: " + e.getMessage());
            broadcastLocally(playerName, platform, link);
        }
    }

    /**
     * Envía datos estructurados a otros servidores
     */
    private void sendToOtherServers(String playerName, String platform, String link) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        // Comando BungeeCord: Forward (reenviar otros servidores)
        out.writeUTF("Forward");
        out.writeUTF("ALL");                   // A todos los servidores
        out.writeUTF(CUSTOM_CHANNEL);          // Nuestro canal personalizado

        // Serializar datos
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        msgout.writeUTF(playerName);  // Jugador
        msgout.writeUTF(platform);    // Plataforma
        msgout.writeUTF(link);        // Enlace

        // Escribir longitud y datos
        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        // Enviar a BungeeCord
        Player player = plugin.getServer().getOnlinePlayers().iterator().next();
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /**
     * Envía solo en este servidor (fallback/local)
     */
    private void broadcastLocally(String playerName, String platform, String link) {
        // Usar tu AnnouncementFormatter existente
        net.md_5.bungee.api.chat.TextComponent[] components =
                org.ccoding.liveannounce.utils.AnnouncementFormatter.createAnnouncement(
                        playerName, platform, link
                );

        if (components == null || components.length == 0) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (net.md_5.bungee.api.chat.TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }
    }
}