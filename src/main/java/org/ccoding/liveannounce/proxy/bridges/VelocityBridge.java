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

    public VelocityBridge(LiveAnnounce plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public boolean isAvailable() {
        return isPaperServer() && isVelocityEnabled();
    }

    /**
     * Verifica si es un servidor Paper (requerido para Velocity)
     */
    private boolean isPaperServer() {
        try {
            // Método 1: Verificar clase PaperConfig
            Class.forName("com.destroystokyo.paper.PaperConfig");
            plugin.getLogger().info("✓ Servidor Paper detectado");
            return true;
        } catch (ClassNotFoundException e) {
            // Método 2: Verificar si es PaperSpigot
            String serverName = Bukkit.getServer().getName();
            if (serverName.contains("Paper") || serverName.contains("paper")) {
                plugin.getLogger().info("✓ Servidor Paper detectado por nombre: " + serverName);
                return true;
            }

            // Método 3: Verificar versión
            String bukkitVersion = Bukkit.getBukkitVersion();
            if (bukkitVersion.contains("Paper") || bukkitVersion.contains("paper")) {
                plugin.getLogger().info("✓ Servidor Paper detectado por versión: " + bukkitVersion);
                return true;
            }

            plugin.getLogger().info("✗ No es un servidor Paper. Velocity no está disponible.");
            return false;
        }
    }

    /**
     * Verifica si Velocity está habilitado
     */
    private boolean isVelocityEnabled() {
        try {
            // Método 1: Verificar mediante configuración del servidor
            Class<?> paperConfigClass = Class.forName("com.destroystokyo.paper.PaperConfig");
            java.lang.reflect.Field velocityField = paperConfigClass.getDeclaredField("velocitySupport");
            velocityField.setAccessible(true);
            Object velocityValue = velocityField.get(null);

            if (velocityValue instanceof Boolean) {
                boolean enabled = (Boolean) velocityValue;
                if (enabled) {
                    plugin.getLogger().info("✓ Velocity habilitado en Paper");
                }
                return enabled;
            }
        } catch (Exception e) {
            // Método 2: Verificar si el canal de Velocity está registrado
            try {
                if (Bukkit.getServer().getMessenger().getOutgoingChannels(plugin)
                        .stream().anyMatch(channel -> channel.equalsIgnoreCase("velocity:announce"))) {
                    plugin.getLogger().info("✓ Canal Velocity detectado");
                    return true;
                }
            } catch (Exception e2) {
                // Ignorar
            }

            // Método 3: Verificar si hay players con Velocity
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                Player player = Bukkit.getOnlinePlayers().iterator().next();
                try {
                    // Intentar enviar un mensaje de prueba
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Test");
                    player.sendPluginMessage(plugin, "velocity:announce", out.toByteArray());
                    plugin.getLogger().info("✓ Velocity funcionando (test exitoso)");
                    return true;
                } catch (Exception e3) {
                    // No funciona
                }
            }
        }

        plugin.getLogger().info("✗ Velocity no está habilitado o configurado");
        return false;
    }

    @Override
    public void broadcastAnnouncement(String playerName, String platform, String link) {
        if (!isAvailable()) {
            plugin.getLogger().info("Velocity no disponible. Enviando solo localmente.");
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
            // 1. Enviar a otros servidores vía Velocity
            sendToVelocity(playerName, platform, link);

            // 2. Ya se envió localmente en AnnouncementPipeline
            plugin.getLogger().info("✓ Anuncio enviado a toda la red Velocity");

        } catch (Exception e) {
            plugin.getLogger().severe("Error enviando anuncio a Velocity: " + e.getMessage());
            // Fallback local
            broadcastLocally(playerName, platform, link);
        }
    }

    /**
     * Envía datos estructurados a través de Velocity
     */
    private void sendToVelocity(String playerName, String platform, String link) throws Exception {
        // Velocity usa un formato similar
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        // Canal para Velocity (puede ser diferente según versión)
        String velocityChannel = getVelocityChannel();

        // Serializar datos
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);

        msgout.writeUTF(playerName);
        msgout.writeUTF(platform);
        msgout.writeUTF(link);

        // Enviar a Velocity
        Player player = plugin.getServer().getOnlinePlayers().iterator().next();
        player.sendPluginMessage(plugin, velocityChannel, msgbytes.toByteArray());
    }

    /**
     * Obtiene el canal correcto para Velocity
     */
    private String getVelocityChannel() {
        // Intentar diferentes canales según versión
        String[] possibleChannels = {
                "velocity:announce",
                "velocity:main",
                "velocity",
                "velocity:plugin",
                "velocity:liveannounce"
        };

        // Verificar qué canales están registrados
        for (String channel : possibleChannels) {
            if (Bukkit.getMessenger().isOutgoingChannelRegistered(plugin, channel)) {
                return channel;
            }
        }

        // Por defecto
        return VELOCITY_CHANNEL;
    }

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