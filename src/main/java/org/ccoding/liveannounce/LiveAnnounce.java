package org.ccoding.liveannounce;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.ccoding.liveannounce.announcement.service.AnnouncementDisplay;
import org.ccoding.liveannounce.announcement.service.ChristmasGreeting;
import org.ccoding.liveannounce.announcement.service.LiveSessionManager;
import org.ccoding.liveannounce.commands.DirectoCommand;
import org.ccoding.liveannounce.commands.LiveAnnounceCommand;
import org.ccoding.liveannounce.listeners.LiveSessionListener;
import org.ccoding.liveannounce.managers.CooldownManager;
import org.ccoding.liveannounce.managers.MessageManager;
import org.ccoding.liveannounce.managers.MuteManager;
import org.ccoding.liveannounce.managers.PrefixManager;
import org.ccoding.liveannounce.messaging.ChannelManager;
import org.ccoding.liveannounce.utils.AnnouncementFormatter;
import org.ccoding.liveannounce.proxy.BridgeManager;

public class LiveAnnounce extends JavaPlugin {

    private static LiveAnnounce instance;
    private CooldownManager announcementCooldown;
    private ChannelManager channelManager;
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        instance = this;

        this.adventure = BukkitAudiences.create(this);
        this.miniMessage = MiniMessage.miniMessage();

        channelManager = new ChannelManager(this);
        channelManager.registerChannels();

        saveDefaultConfig();
        reloadConfig();

        org.ccoding.liveannounce.proxy.BridgeManager.initialize(this);

        PrefixManager.load(getConfig());
        MessageManager.setup(getConfig());
        AnnouncementFormatter.initialize(getConfig());

        if (!getConfig().getBoolean("enabled", true)) {
            getLogger().warning("Plugin disabled in config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean cooldownEnabled = getConfig().getBoolean("cooldown.enabled", true);
        int cooldownSeconds = getConfig().getInt("cooldown.seconds", 30);

        if (cooldownEnabled) {
            announcementCooldown = new CooldownManager(cooldownSeconds);
        }

        LiveSessionManager.load(getConfig(), getLogger());
        LiveSessionManager.startCleanup(this);
        AnnouncementDisplay.load(getConfig(), getLogger());
        ChristmasGreeting.load(getConfig(), getLogger());
        MuteManager.start(this);
        getServer().getPluginManager().registerEvents(new LiveSessionListener(), this);

        DirectoCommand directoCommand = new DirectoCommand();
        this.getCommand("directo").setExecutor(directoCommand);
        this.getCommand("directo").setTabCompleter(directoCommand);

        LiveAnnounceCommand liveAnnounceCommand = new LiveAnnounceCommand();
        this.getCommand("liveannounce").setExecutor(liveAnnounceCommand);
        this.getCommand("liveannounce").setTabCompleter(liveAnnounceCommand);

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
        getLogger().info("LiveAnnounce disabled");

        AnnouncementDisplay.shutdown();
        MuteManager.shutdown();
        LiveSessionManager.shutdown();

        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }

        if (channelManager != null) {
            channelManager.unregisterChannels();
        }
    }

    public static LiveAnnounce getInstance() {
        return instance;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public CooldownManager getAnnouncementCooldown(){
        return announcementCooldown;
    }

    public void reloadPlugin() {
        reloadConfig();

        BridgeManager.reload(this);

        PrefixManager.load(getConfig());
        MessageManager.reload(getConfig());
        AnnouncementFormatter.reload(getConfig());

        boolean cooldownEnabled = getConfig().getBoolean("cooldown.enabled", true);
        int cooldownSeconds = getConfig().getInt("cooldown.seconds", 30);

        if (cooldownEnabled) {
            announcementCooldown = new CooldownManager(cooldownSeconds);
        } else {
            announcementCooldown = null;
        }

        LiveSessionManager.reload(this);
        AnnouncementDisplay.load(getConfig(), getLogger());
        ChristmasGreeting.load(getConfig(), getLogger());
        MuteManager.reload(getConfig());
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }
}