package org.ccoding.liveannounce;

import org.bukkit.plugin.java.JavaPlugin;
import org.ccoding.liveannounce.commands.DirectoCommand;
import org.ccoding.liveannounce.commands.LiveAnnounceCommand;
import org.ccoding.liveannounce.managers.PrefixManager;

public class LiveAnnounce extends JavaPlugin {

    private static LiveAnnounce instance;

    @Override
    public void onEnable() {
        instance = this;

        // Guardar config por defecto
        saveDefaultConfig();
        reloadConfig();

        // Cargar el prefijo desde la configuración
        PrefixManager.load(getConfig());

        // Verificar si el plugin está habilitado
        if (!getConfig().getBoolean("enabled", true)) {
            getLogger().warning("Plugin deshabilitado en config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getCommand("directo").setExecutor(new DirectoCommand());
        this.getCommand("liveannounce").setExecutor(new LiveAnnounceCommand());


        getLogger().info("==================================");
        getLogger().info("LiveAnnounce v" + getDescription().getVersion());
        getLogger().info("Plugin activado correctamente!");
        getLogger().info("==================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("LiveAnnounce desactivado");
    }

    public static LiveAnnounce getInstance() {
        return instance;
    }

    public void reloadPlugin() {
        reloadConfig();
        PrefixManager.load(getConfig());
    }
}