package org.ccoding.liveannounce.proxy.bridges;

import org.ccoding.liveannounce.proxy.Bridge;
import org.ccoding.liveannounce.LiveAnnounce;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Collection;

public class VelocityBridge implements Bridge {
    private final LiveAnnounce plugin;
    private static final String VELOCITY_CHANNEL = "velocity:announce";

    // Cache para evitar logs repetitivos
    private boolean paperChecked = false;
    private boolean isPaper = false;

    public VelocityBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public boolean isAvailable() {
        // Velocity SOLO funciona en Paper, y requiere configuración manual
        // Es más confiable que el admin especifique si hay red o no

        boolean paper = isPaperServer();
        boolean configEnabled = plugin.getConfig().getBoolean("proxy.velocity-enabled", false);
        boolean hasNetwork = plugin.getConfig().getBoolean("proxy.has-network", false);

        // Solo mostrar logs la primera vez
        if (!paperChecked) {
            if (paper && configEnabled) {
                plugin.getLogger().info("✓ Velocity configurado manualmente en config.yml");
                if (hasNetwork) {
                    plugin.getLogger().info("✓ Red Velocity detectada (configurada manualmente)");
                } else {
                    plugin.getLogger().info("ℹ Red Velocity deshabilitada en config.yml");
                    plugin.getLogger().info("  Establece 'proxy.has-network: true' si hay otros servidores");
                }
            }
            paperChecked = true;
            isPaper = paper;
        }

        // Velocity requiere: Paper + Configuración manual + Red activa
        return paper && configEnabled && hasNetwork;
    }

    /**
     * Verifica si es un servidor Paper (requerido para Velocity)
     */
    private boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            // También verificar por nombre
            String serverName = Bukkit.getServer().getName();
            return serverName.contains("Paper") || serverName.contains("paper");
        }
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {
        // Verificar disponibilidad ANTES de enviar
        if (!isAvailable()) {
            // No logueamos nada aquí para no spamear
            broadcastLocally(playerName, platform, link);
            return;
        }

        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();

        if (onlinePlayers.isEmpty()) {
            plugin.getLogger().warning("No hay jugadores para enviar anuncio a Velocity");
            broadcastLocally(playerName, platform, link);
            return;
        }

        try {
            // Enviar a otros servidores SOLO si hay red configurada
            sendToVelocity(playerName, platform, link);
            plugin.getLogger().info("✓ Anuncio enviado a red Velocity");

            // El envío local ya lo hace AnnouncementPipeline
            // No necesitamos broadcastLocally aquí

        } catch (Exception e) {
            plugin.getLogger().severe("Error enviando anuncio a Velocity: " + e.getMessage());
            broadcastLocally(playerName, platform, link);
        }
    }

    /**
     * Envía datos estructurados a través de Velocity
     */
    private void sendToVelocity(String playerName, String platform, String link) throws Exception {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        // Formato estándar para Velocity/BungeeCord
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("liveannounce:announce");

        // Serializar datos del anuncio
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        msgout.writeUTF(playerName);
        msgout.writeUTF(platform);
        msgout.writeUTF(link);

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        // Enviar usando el canal de Velocity
        Player player = plugin.getServer().getOnlinePlayers().iterator().next();
        player.sendPluginMessage(plugin, VELOCITY_CHANNEL, out.toByteArray());
    }

    /**
     * Fallback: envía solo en este servidor
     */
    private void broadcastLocally(String playerName, String platform, String link) {
        TextComponent[] components =
                org.ccoding.liveannounce.utils.AnnouncementFormatter.createAnnouncement(
                        playerName, platform, link
                );

        if (components == null || components.length == 0) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (TextComponent component : components) {
                player.spigot().sendMessage(component);
            }
        }
    }
}