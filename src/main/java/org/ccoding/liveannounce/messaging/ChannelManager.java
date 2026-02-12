package org.ccoding.liveannounce.messaging;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import java.util.HashSet;
import java.util.Set;

public class ChannelManager {

    private final JavaPlugin plugin;
    private final Set<String> registeredChannels = new HashSet<>();

    // Canales personalizados
    public static final String CHANNEL_ANNOUNCE = "liveannounce:announce";
    private String activeBungeeChannel = null;

    public ChannelManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChannels() {
        plugin.getLogger().info("Configurando canales de comunicación...");

        // 1. Registrar canal personalizado (SIEMPRE va con dos puntos)
        registerIncomingChannel(CHANNEL_ANNOUNCE, new AnnouncementReceiver());

        // 2. IMPORTANTE: NO registrar "bungeecord" sin dos puntos
        // En Paper 1.20.4+ SOLO se permiten canales con formato "namespace:channel"
        // Así que mejor NO registramos ningún canal BungeeCord explícitamente

        // 3. Velocity solo si es Paper
        if (isPaperServer()) {
            registerVelocityChannel();
        }

        plugin.getLogger().info("Canales registrados: " + registeredChannels.size());
    }

    /**
     * NO registramos canal BungeeCord explícitamente.
     * En su lugar, obtenemos el canal cuando lo necesitamos.
     */
    public String getBungeeCordChannel() {
        if (activeBungeeChannel == null) {
            // Detectar el canal apropiado según la versión
            if (isPaperModern()) {
                activeBungeeChannel = "bungeecord:main";
            } else {
                activeBungeeChannel = "BungeeCord";
            }
            plugin.getLogger().info("Canal BungeeCord detectado: " + activeBungeeChannel);
        }
        return activeBungeeChannel;
    }

    /**
     * Registra canal de Velocity
     */
    private void registerVelocityChannel() {
        try {
            String velocityChannel = "velocity:announce";
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, velocityChannel);
            registeredChannels.add(velocityChannel);
            plugin.getLogger().info("✓ Canal Velocity registrado");
        } catch (Exception e) {
            plugin.getLogger().info("Velocity no disponible");
        }
    }

    /**
     * Registra canal de entrada
     */
    private void registerIncomingChannel(String channel, AnnouncementReceiver receiver) {
        try {
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, receiver);
            registeredChannels.add(channel);
            plugin.getLogger().info("✓ Canal de recepción registrado: " + channel);
        } catch (Exception e) {
            plugin.getLogger().severe("✗ Error registrando canal: " + e.getMessage());
        }
    }

    /**
     * Verifica si estamos en Paper
     */
    private boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Verifica si es Paper moderno (1.20.4+)
     */
    private boolean isPaperModern() {
        try {
            // Intentar detectar versión
            String version = plugin.getServer().getVersion();
            return version.contains("1.20.4") ||
                    version.contains("1.21") ||
                    version.contains("1.20.5") ||
                    version.contains("1.20.6");
        } catch (Exception e) {
            return false;
        }
    }

    public void unregisterChannels() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        registeredChannels.clear();
        plugin.getLogger().info("Canales desregistrados");
    }
}