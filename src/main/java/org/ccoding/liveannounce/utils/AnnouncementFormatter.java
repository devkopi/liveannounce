package org.ccoding.liveannounce.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;

import java.util.ArrayList;
import java.util.List;

/**
 * Crea anuncios de stream usando componentes de chat modernos (Adventure)
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
    private AnnouncementFormatter() {
    }

    /**
     * Carga los formatos desde la configuración
     */
    public static void initialize(FileConfiguration config) {
        if (initialized)
            return;

        ConfigurationSection formatsSection = config.getConfigurationSection("announcement-formats.default");

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
            case LINE1:
                return "&8&m--------------------------------------------------";
            case TITLE:
                return "&f⚡ {color}&l¡LIVE ON {platform_upper}! &f⚡";
            case DESCRIPTION:
                return "&f{player} &7is now streaming live";
            case LINK:
                return "&7Join now! {color}&n{link}";
            case HOVER:
                return "&eClick to open the stream!";
            case LINE2:
                return "&8&m--------------------------------------------------";
            default:
                return "&7[" + key + "]";
        }
    }

    /**
     * Crea un anuncio completo
     */
    public static List<Component> createAnnouncement(String playerName,
            String platformName,
            String link) {
        // Verificar que esté inicializado
        if (!initialized) {
            LiveAnnounce.getInstance().getLogger().warning(
                    "AnnouncementFormatter not initialized!");
            return new ArrayList<>();
        }

        // Crear objeto con todos los datos
        AnnouncementData data = new AnnouncementData(playerName, platformName, link);

        List<Component> components = new ArrayList<>();
        components.add(createFormattedLine(FORMATS[FormatKey.LINE1.ordinal()], data));
        components.add(createFormattedLine(FORMATS[FormatKey.TITLE.ordinal()], data));
        components.add(createFormattedLine(FORMATS[FormatKey.DESCRIPTION.ordinal()], data));
        components.add(createClickableLink(
                FORMATS[FormatKey.LINK.ordinal()],
                FORMATS[FormatKey.HOVER.ordinal()],
                data));
        components.add(createFormattedLine(FORMATS[FormatKey.LINE2.ordinal()], data));

        return components;
    }

    // ========== MÉTODOS PRIVADOS ==========

    private static Component createFormattedLine(String template, AnnouncementData data) {
        String text = data.applyToTemplate(template);
        return ChatUtils.format(text);
    }

    private static Component createClickableLink(String linkTemplate,
            String hoverTemplate,
            AnnouncementData data) {
        String linkText = data.applyToTemplate(linkTemplate);
        String hoverText = data.applyToTemplate(hoverTemplate);

        Component component = ChatUtils.format(linkText);
        
        // Añadir eventos si hay link
        if (data.getLink() != null && !data.getLink().isEmpty()) {
            component = component.clickEvent(ClickEvent.openUrl(data.getLink()));
            component = component.hoverEvent(HoverEvent.showText(ChatUtils.format(hoverText)));
        }

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