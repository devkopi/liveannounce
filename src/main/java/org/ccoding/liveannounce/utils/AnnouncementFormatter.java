package org.ccoding.liveannounce.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.ccoding.liveannounce.LiveAnnounce;

import java.util.HashMap;
import java.util.Map;

public class AnnouncementFormatter {

    private static final Map<String, String> FORMATS = new HashMap<>();
    private static boolean initialized = false;

    private AnnouncementFormatter() {}

    public static void initialize(FileConfiguration config){
        if (initialized) return;

        ConfigurationSection formatsSection = config.getConfigurationSection("announcement-formats.default");
        if (formatsSection == null) {
            loadDefaultFormats();
            return;
        }

        // Cargar cada parte del formato
        FORMATS.put("line1", formatsSection.getString("line1", getDefault("line1")));
        FORMATS.put("title", formatsSection.getString("title", getDefault("title")));
        FORMATS.put("description", formatsSection.getString("description", getDefault("description")));
        FORMATS.put("link", formatsSection.getString("link", getDefault("link")));
        FORMATS.put("hover", formatsSection.getString("hover", getDefault("hover")));
        FORMATS.put("line2", formatsSection.getString("line2", getDefault("line2")));

        initialized = true;
        LiveAnnounce.getInstance().getLogger().info("Announcement formats loaded: " + FORMATS.size() + " components");
    }

    private static void loadDefaultFormats() {
        FORMATS.put("line1", "&8&m--------------------------------------------------");
        FORMATS.put("title", "&f⚡ {color}&l¡LIVE ON {platform_upper}! &f⚡");
        FORMATS.put("description", "&f{player} &7is now streaming live");
        FORMATS.put("link", "&7Join now! {color}&n{link}");
        FORMATS.put("hover", "&eClick to open the stream!");
        FORMATS.put("line2", "&8&m--------------------------------------------------");

        initialized = true;
        LiveAnnounce.getInstance().getLogger().info("Loaded default announcement formats");
    }

    private static  String getDefault(String key) {
        return FORMATS.getOrDefault(key, "&7[" + key + "]");
    }

    public static TextComponent[] createAnnouncement(String playerName, String platformName,
                                                     String platformColor, String link) {

        if (!initialized) {
            LiveAnnounce.getInstance().getLogger().warning("AnnouncementFormatter not initialized!");
            return new TextComponent[0];
        }

        // Obtener color desde config (IGNORAR el platformColor que viene del comando)
        String colorFromConfig = getPlatformColor(platformName.toLowerCase());

        // Preparar variables
        Map<String, String> variables = new HashMap<>();
        variables.put("player", playerName);
        variables.put("platform", platformName);
        variables.put("platform_upper", platformName.toUpperCase());
        variables.put("color", colorFromConfig);  // ← Usar "color" no "platform_color"
        variables.put("link", link);

        // Extraer channel del link (opcional)
        String channel = extractChannel(link);
        variables.put("channel", channel != null ? channel : playerName);

        // DEBUG: Mostrar valores
        System.out.println("[DEBUG] Platform: " + platformName);
        System.out.println("[DEBUG] Color from config: '" + colorFromConfig + "'");
        System.out.println("[DEBUG] Template title: " + FORMATS.get("title"));

        // Crear componentes
        return new TextComponent[] {
                createLine("line1", variables),
                createTitle("title", variables),
                createDescription("description", variables),
                createClickableLink("link", "hover", variables, link),
                createLine("line2", variables)
        };
    }

    private static TextComponent createLine(String formatKey, Map<String, String> variables) {
        String text = applyVariablesWithColor(FORMATS.get(formatKey), variables);
        return new TextComponent(ChatUtils.color(text));
    }

    private static TextComponent createTitle(String formatKey, Map<String, String> variables) {
        String text = applyVariablesWithColor(FORMATS.get(formatKey), variables);
        return new TextComponent(ChatUtils.color(text));
    }

    private static TextComponent createDescription(String formatKey, Map<String, String> variables) {
        String text = applyVariablesWithColor(FORMATS.get(formatKey), variables);
        return new TextComponent(ChatUtils.color(text));
    }

    private static TextComponent createClickableLink(String formatKey, String hoverKey,
                                                     Map<String, String> variables, String link) {
        String text = applyVariablesWithColor(FORMATS.get(formatKey), variables);
        String hoverText = applyVariablesWithColor(FORMATS.get(hoverKey), variables);

        TextComponent component = new TextComponent(ChatUtils.color(text));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatUtils.color(hoverText)).create()));

        return component;
    }

    private static String applyVariablesWithColor(String template, Map<String, String> variables) {
        if (template == null) return "";

        String result = template;

        // Reemplazar TODAS las variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }

    private static String extractChannel(String link) {
        try {
            // Simple extracción: twitch.tv/{canal}
            if (link.contains("twitch.tv/")) {
                return link.substring(link.indexOf("twitch.tv/") + 10).split("/")[0];
            }
            // youtube.com/{canal} o youtu.be/{id}
            else if (link.contains("youtube.com/")) {
                return link.substring(link.indexOf("youtube.com/") + 12).split("/")[0];
            }
            else if (link.contains("youtu.be/")) {
                return link.substring(link.indexOf("youtu.be/") + 9);
            }
            // kick.com/{canal}
            else if (link.contains("kick.com/")) {
                return link.substring(link.indexOf("kick.com/") + 9);
            }
            // tiktok.com/@{canal}
            else if (link.contains("tiktok.com/@")) {
                return link.substring(link.indexOf("tiktok.com/@") + 12).split("/")[0];
            }
        } catch (Exception e) {
            // Si falla la extracción, usar player name
        }
        return null;
    }

    public static void reload(FileConfiguration config) {
        FORMATS.clear();
        initialized = false;
        initialize(config);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static int getFormatCount() {
        return FORMATS.size();
    }

    private static String getPlatformColor(String platformKey) {
        // Intentar obtener desde config
        String color = LiveAnnounce.getInstance().getConfig()
                .getString("platform-colors." + platformKey);

        // Si no existe usar default
        if (color == null || color.trim().isEmpty()) {
            color = LiveAnnounce.getInstance().getConfig()
                    .getString("platform-colors.default", "&6");
        }

        // Asegurar que empiece con & y tenga formato correcto
        if (!color.startsWith("&") && color.length() >= 1) {
            color = "&" + color;
        }

        // Asegurar formato & + letra (ej: &d, &c)
        if (color.length() == 2 && color.startsWith("&")) {
            return color;
        }

        return color;
    }
}
