package org.ccoding.liveannounce.discord;

import org.bukkit.configuration.file.FileConfiguration;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordManager {

    /**
     * Envía un Embed a Discord vía Webhook.
     * Esta función debe ser ejecutada en un entorno Asíncrono para no tildar el
     * servidor.
     * 
     * @param data Datos del anuncio construidos
     */
    public static void sendWebhook(AnnouncementData data) {
        FileConfiguration config = LiveAnnounce.getInstance().getConfig();
        if (!config.getBoolean("discord.enabled", false)) {
            return;
        }

        String webhookUrl = config.getString("discord.webhook-url", "");
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("/api/webhooks/...")) {
            LiveAnnounce.getInstance().getLogger()
                    .warning("Discord webhook está activado pero la URL es inválida o por defecto.");
            return;
        }

        try {
            String title = config.getString("discord.embed.title", "¡{player} está en directo en {platform}!");
            String description = config.getString("discord.embed.description", "¡Únete ahora! {link}");
            int color = config.getInt("discord.embed.color", 16711680);

            // Reemplazos de variables
            title = formatText(title, data);
            description = formatText(description, data);

            // Escapar contenido para evitar romper el JSON
            title = escapeJson(title);
            description = escapeJson(description);

            String jsonPayload = "{" +
                    "\"embeds\": [{" +
                    "\"title\": \"" + title + "\"," +
                    "\"description\": \"" + description + "\"," +
                    "\"color\": " + color +
                    "}]" +
                    "}";

            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LiveAnnounce-Plugin");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000); // 5 segundos max
            connection.setReadTimeout(5000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                LiveAnnounce.getInstance().getLogger().warning("Discord webhook error " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            LiveAnnounce.getInstance().getLogger()
                    .warning("No se pudo enviar el webhook de Discord: " + e.getMessage());
        }
    }

    private static String formatText(String text, AnnouncementData data) {
        if (text == null)
            return "";
        return text.replace("{player}", data.getPlayerName())
                .replace("{platform}", data.getPlatformName())
                .replace("{link}", data.getLink());
    }

    private static String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
