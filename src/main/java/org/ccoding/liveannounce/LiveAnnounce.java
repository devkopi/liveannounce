package org.ccoding.liveannounce;

import org.bukkit.plugin.java.JavaPlugin;
import org.ccoding.liveannounce.commands.DirectoCommand;
import org.ccoding.liveannounce.commands.LiveAnnounceCommand;
import org.ccoding.liveannounce.managers.CooldownManager;
import org.ccoding.liveannounce.managers.MessageManager;
import org.ccoding.liveannounce.managers.PrefixManager;
import org.ccoding.liveannounce.messaging.ChannelManager;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import org.ccoding.liveannounce.proxy.BridgeManager;

public class LiveAnnounce extends JavaPlugin {

    private static LiveAnnounce instance;
    private CooldownManager announcementCooldown;
    private ChannelManager channelManager;

    @Override
    public void onEnable() {
        instance = this;

        // Inicializador gestor de canales
        channelManager = new ChannelManager(this);
        channelManager.registerChannels();


        // Guardar config por defecto
        saveDefaultConfig();
        reloadConfig();

        // Inicializar BridgeManager para manejar proxy (BungeeCord/Velocity)
        org.ccoding.liveannounce.proxy.BridgeManager.initialize(this);

        // Cargamos los managers
        PrefixManager.load(getConfig());
        MessageManager.setup(getConfig());
        AnnouncementFormatter.initialize(getConfig());

        // Verificar si el plugin está habilitado
        if (!getConfig().getBoolean("enabled", true)) {
            getLogger().warning("Plugin deshabilitado en config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean cooldownEnabled = getConfig().getBoolean("cooldown.enabled", true);
        int cooldownSeconds = getConfig().getInt("cooldown.seconds", 30);

        if (cooldownEnabled) {
            announcementCooldown = new CooldownManager(cooldownSeconds);
        }

        this.getCommand("directo").setExecutor(new DirectoCommand());
        this.getCommand("liveannounce").setExecutor(new LiveAnnounceCommand());

        getLogger().info("╔══════════════════════════════════════╗");
        getLogger().info("║                                      ║");
        getLogger().info("║  ██╗     ██╗██╗   ██╗███████╗        ║");
        getLogger().info("║  ██║     ██║██║   ██║██╔════╝        ║");
        getLogger().info("║  ██║     ██║██║   ██║█████╗          ║");
        getLogger().info("║  ██║     ██║╚██╗ ██╔╝██╔══╝          ║");
        getLogger().info("║  ███████╗██║ ╚████╔╝ ███████╗        ║");
        getLogger().info("║  ╚══════╝╚═╝  ╚═══╝  ╚══════╝        ║");
        getLogger().info("║                                      ║");
        getLogger().info("║  Announce v" + getDescription().getVersion() + " - " + getDescription().getAuthors().get(0) + "           ║");
        getLogger().info("╚══════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {
        getLogger().info("LiveAnnounce desactivado");

        // Limpiar canales si están registrados
        if (channelManager != null) {
            channelManager.unregisterChannels();
        }
    }

    public static LiveAnnounce getInstance() {
        return instance;
    }

    public CooldownManager getAnnouncementCooldown(){
        return announcementCooldown;
    }

    public void reloadPlugin() {
        // Recargar configuración
        reloadConfig();

        // Recargar el BridgeManager para detectar cambios en la configuración del proxy
        BridgeManager.reload(this);

        // Managers
        PrefixManager.load(getConfig());
        MessageManager.reload(getConfig());
        AnnouncementFormatter.reload(getConfig());

        // Recarga cooldown
        boolean cooldownEnabled = getConfig().getBoolean("cooldown.enabled", true);
        int cooldownSeconds = getConfig().getInt("cooldown.seconds", 30);

        if (cooldownEnabled) {
            announcementCooldown = new CooldownManager(cooldownSeconds);
        } else {
            announcementCooldown = null;
        }
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }
}