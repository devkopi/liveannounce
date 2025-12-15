package org.ccoding.liveannounce.utils;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.ccoding.liveannounce.LiveAnnounce;

/**
 * Crea anuncios de stream usando componentes de chat
 * Ahora mucho más optimizado y organizado
 */
public class AnnouncementFormatter {

    // Enumeración para identificar cada parte del formato
    private enum FormatKey {
        LINE1, TITLE, DESCRIPTION, LINK, HOVER, LINE2;

        public String getConfigKey() {
            return name().toLowerCase();
        }
    }

    private static final String[] FORMATS = new String[FormatKey.values().length];
    private static boolean initialized = false;

    // Constructor privado - no se puede instanciar
    private AnnouncementFormatter() {}

    /**
     * Carga los formatos desde la configuración
     */
    public static void initialize(FileConfiguration config) {
        if (initialized) return;

        ConfigurationSection formatsSection =
                config.getConfigurationSection("announcement-formats.default");

        for (FormatKey key : FormatKey.values()) {
            FORMATS[key.ordinal()] = formatsSection != null
                    ? formatsSection.getString(key.getConfigKey(), getDefaultFormat(key))
                    : getDefaultFormat(key);
        }

        initialized = true;
    }



    /**
     * Valores por defecto si no hay configuración
     */
    private static String getDefaultFormat(FormatKey key) {
        switch (key) {
            case LINE1: return "&8&m--------------------------------------------------";
            case TITLE: return "&f⚡ {color}&l¡LIVE ON {platform_upper}! &f⚡";
            case DESCRIPTION: return "&f{player} &7is now streaming live";
            case LINK: return "&7Join now! {color}&n{link}";
            case HOVER: return "&eClick to open the stream!";
            case LINE2: return "&8&m--------------------------------------------------";
            default: return "&7[" + key + "]";
        }
    }

    /**
     * Crea un anuncio completo
     */
    public static TextComponent[] createAnnouncement(String playerName,
                                                     String platformName,
                                                     String link) {
        // Verificar que esté inicializado
        if (!initialized) {
            LiveAnnounce.getInstance().getLogger().warning(
                    "AnnouncementFormatter not initialized!"
            );
            return new TextComponent[0];
        }

        // Crear objeto con todos los datos (REEMPLAZA A LOS HASHMAPS)
        AnnouncementData data = new AnnouncementData(playerName, platformName, link);

        // Crear cada parte del anuncio
        return new TextComponent[] {
                createFormattedLine(FORMATS[FormatKey.LINE1.ordinal()], data),
                createFormattedTitle(FORMATS[FormatKey.TITLE.ordinal()], data),
                createFormattedDescription(FORMATS[FormatKey.DESCRIPTION.ordinal()], data),
                createClickableLink(
                        FORMATS[FormatKey.LINK.ordinal()],
                        FORMATS[FormatKey.HOVER.ordinal()],
                        data
                ),
                createFormattedLine(FORMATS[FormatKey.LINE2.ordinal()], data)
        };
    }

    // ========== MÉTODOS PRIVADOS ==========

    private static TextComponent createFormattedLine(String template, AnnouncementData data) {
        String text = data.applyToTemplate(template);
        return new TextComponent(ChatUtils.color(text));
    }

    private static TextComponent createFormattedTitle(String template, AnnouncementData data) {
        String text = data.applyToTemplate(template);
        return new TextComponent(ChatUtils.color(text));
    }

    private static TextComponent createFormattedDescription(String template, AnnouncementData data) {
        String text = data.applyToTemplate(template);
        return new TextComponent(ChatUtils.color(text));
    }

    private static TextComponent createClickableLink(String linkTemplate,
                                                     String hoverTemplate,
                                                     AnnouncementData data) {
        String linkText = data.applyToTemplate(linkTemplate);
        String hoverText = data.applyToTemplate(hoverTemplate);

        TextComponent component = new TextComponent(ChatUtils.color(linkText));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, data.getLink()));
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatUtils.color(hoverText)).create()
        ));

        return component;
    }

    /**
     * Recarga los formatos
     */
    public static void reload(FileConfiguration config) {
        // Limpiar array
        for (int i = 0; i < FORMATS.length; i++) {
            FORMATS[i] = null;
        }

        initialized = false;
        initialize(config);

        // Limpiar cache de colores
        ColorCache.clear();
    }

    /**
     * Verifica si está inicializado
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Obtiene cantidad de formatos cargados
     */
    public static int getFormatCount() {
        return FORMATS.length;
    }
}