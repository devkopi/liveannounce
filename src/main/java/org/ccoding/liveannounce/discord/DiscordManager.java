package org.ccoding.liveannounce.discord;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.ccoding.liveannounce.LiveAnnounce;
import org.ccoding.liveannounce.announcement.model.AnnouncementData;
import org.ccoding.liveannounce.announcement.parser.ChannelExtractor;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

public class DiscordManager {

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
            // Obtener URL del avatar del jugador
            String avatarUrl = getPlayerAvatarUrl(data.getPlayerName());

            // Detectar plataforma
            String detectedPlatform = ChannelExtractor.detectPlatform(data.getLink());
            String platformToUse = (data.getPlatformName() != null && !data.getPlatformName().isEmpty())
                    ? data.getPlatformName() : detectedPlatform;

            // Obtener color según plataforma
            int color = getPlatformColor(platformToUse, config);

            // Obtener configuración del embed
            String title = config.getString("discord.embed.title", "🎮 **¡{player} is live!**");
            String descriptionFormat = config.getString("discord.embed.description",
                    "**Platform:** {platform}\n\n**Join now!**\n{link}");
            String footerText = config.getString("discord.embed.footer", "LiveAnnounce • {timestamp}");
            String authorName = config.getString("discord.embed.author", "🎥 New Live Stream");

            // Imagen del embed
            String imageUrlTemplate = config.getString("discord.embed.image-url", "");
            String imageUrl = "";
            if (imageUrlTemplate != null && !imageUrlTemplate.isEmpty()) {
                imageUrl = formatText(imageUrlTemplate, data, platformToUse);
                imageUrl = escapeJson(imageUrl);
            }

            // Mencionm Everyone y Template para que el mensaje se pueda editar
            boolean mentionEveryone = config.getBoolean("discord.mention-everyone", false);
            String messageTemplate = config.getString("discord.message", "");

            // Construir el mensaje final
            String content = "";
            if (messageTemplate != null && !messageTemplate.isEmpty()) {
                // Formatear el mensaje con variables
                String formattedMessage = formatText(messageTemplate, data, platformToUse);

                // Añadir @everyone si está activado
                if (mentionEveryone) {
                    content = "@everyone " + formattedMessage;
                } else {
                    content = formattedMessage;
                }
                content = escapeJson(content);
            }

            // Reemplazamos las variables
            title = formatText(title, data, platformToUse);
            descriptionFormat = formatText(descriptionFormat, data, platformToUse);

            // Formatear timestamp
            String formattedTime = getFormattedTimestamp();
            footerText = footerText.replace("{timestamp}", formattedTime);

            // Escapar contenido del embed
            title = escapeJson(title);
            descriptionFormat = escapeJson(descriptionFormat);
            footerText = escapeJson(footerText);
            authorName = escapeJson(authorName);

            // Construir JSON
            String jsonPayload = buildJsonPayload(title, descriptionFormat, color, authorName, avatarUrl, footerText, content, mentionEveryone, imageUrl);

            // Enviar petición
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "LiveAnnounce-Plugin");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                LiveAnnounce.getInstance().getLogger().warning("[LiveAnnouncer] Failed to send announcement to Discord webhook. Check the URL in config.yml (Error Code: " + responseCode + ")");
            }

            connection.disconnect();

        } catch (Exception e) {
            LiveAnnounce.getInstance().getLogger()
                    .warning("[LiveAnnouncer] Failed to send announcement to Discord webhook. Check the URL in config.yml (Error Message: " + e.getMessage() + ")");
        }
    }

    private static int getPlatformColor(String platform, FileConfiguration config) {
        String path = "discord.platform-colors." + platform.toLowerCase();
        if (config.contains(path)) {
            return config.getInt(path);
        }
        return config.getInt("discord.embed.color", 16711680);
    }

    private static String getFormattedTimestamp() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        String hour = now.format(DateTimeFormatter.ofPattern("h:mm"));
        String ampm = now.format(DateTimeFormatter.ofPattern("a", new Locale("es", "ES")));
        return "hoy a las " + hour + " " + ampm;
    }

    private static String buildJsonPayload(String title, String description, int color,
                                           String authorName, String avatarUrl, String footerText,
                                           String content, boolean mentionEveryone, String imageUrl) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Añadir mensaje personalizado si existe
        if (content != null && !content.isEmpty()) {
            json.append("\"content\": \"").append(content).append("\",");
            // Permitir menciones solo si está activado
            if (mentionEveryone) {
                json.append("\"allowed_mentions\": {\"parse\": [\"everyone\"]},");
            }
        }

        json.append("\"embeds\": [{");
        json.append("\"title\": \"").append(title).append("\",");
        json.append("\"description\": \"").append(description).append("\",");
        json.append("\"color\": ").append(color).append(",");

        json.append("\"author\": {");
        json.append("\"name\": \"").append(authorName).append("\"");
        json.append("},");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            json.append("\"thumbnail\": {");
            json.append("\"url\": \"").append(avatarUrl).append("\"");
            json.append("},");
        }

        // Imagen grande del embed
        if (imageUrl != null && !imageUrl.isEmpty()) {
            json.append("\"image\": {");
            json.append("\"url\": \"").append(imageUrl).append("\"");
            json.append("},");
        }

        json.append("\"footer\": {");
        json.append("\"text\": \"").append(footerText).append("\"");
        json.append("}");

        json.append("}]");
        json.append("}");
        return json.toString();
    }

    private static String getPlayerAvatarUrl(String playerName) {
        try {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                UUID uuid = player.getUniqueId();
                return "https://visage.surgeplay.com/face/128/" + uuid;
            }
        } catch (Exception e) {
            LiveAnnounce.getInstance().getLogger().warning("Avatar could not be obtained for: " + playerName);
        }
        return "https://visage.surgeplay.com/face/128/steve";
    }

    private static String formatText(String text, AnnouncementData data, String platform) {
        if (text == null) return "";
        return text.replace("{player}", data.getPlayerName())
                .replace("{platform}", platform)
                .replace("{link}", data.getLink())
                .replace("{channel}", data.getChannel() != null ? data.getChannel() : "");
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}