package org.ccoding.liveannounce.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.managers.PrefixManager;

public class ChatUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final LegacyComponentSerializer BUKKIT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    // ========== MÉTODOS DE COLORES ==========

    /**
     * Convierte un texto con códigos legacy (&) o MiniMessage a un Component de Adventure.
     * @param text El texto a procesar.
     * @return El componente resultante.
     */

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component format(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // Detecta si usa MiniMessage
        if (text.contains("<") && text.contains(">")) {
            return mm.deserialize(text);
        }

        // Si NO, usa Legacy (&)
        return LEGACY_SERIALIZER.deserialize(text);
    }

    /**
     * Texto con el formato que Bukkit espera en sendTitle (códigos de sección).
     */
    public static String toBukkitText(Component component) {
        if (component == null) {
            return "";
        }
        return BUKKIT_SERIALIZER.serialize(component);
    }

    /**
     * Traduce códigos de color legacy (&) a String coloreado de Bukkit.
     * @deprecated Usar {@link #format(String)} para obtener un Component de Adventure.
     */
    @Deprecated
    public static String color(String text) {
        if (text == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', text);
    }

    // Enviar mensaje (Metodo para NO APLICAR prefijo a mensajes)
    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            send(sender, format(message));
        }
    }

    public static void send(CommandSender sender, Component component) {
        if (sender == null || component == null) {
            return;
        }
        if (sender instanceof Player) {
            LiveAnnounce.getInstance().getAdventure().player((Player) sender).sendMessage(component);
        } else {
            LiveAnnounce.getInstance().getAdventure().console().sendMessage(component);
        }
    }

    // Metodo para aplicarle prefijo a los mensajes
    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            send(sender, PrefixManager.get() + message);
        }
    }

    // Broadcast a todos los jugadores (Sin prefijo para los anuncios)
    public static void broadcast(String message) {
        if (message != null) {
            LiveAnnounce.getInstance().getAdventure().all().sendMessage(format(message));
        }
    }


    // ========== FORMATOS Y DECORACIONES ==========

    // Línea separadora
    public static String getLine() {
        return "&8&m--------------------------------------------------";
    }
}