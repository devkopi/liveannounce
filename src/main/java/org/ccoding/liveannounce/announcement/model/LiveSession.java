package org.ccoding.liveannounce.announcement.model;

/**
 * One announced stream kept for /la live.
 * Immutable so the async cleanup can drop an expired row
 * without erasing a newer announcement of the same player.
 */
public final class LiveSession {

    private final String playerName;
    private final String platform;
    private final String channel;
    private final String link;
    private final long announcedAt;
    private final long expiresAt;

    public LiveSession(String playerName, String platform, String channel, String link, long announcedAt, long expiresAt) {
        this.playerName = playerName;
        this.platform = platform;
        this.channel = channel;
        this.link = link;
        this.announcedAt = announcedAt;
        this.expiresAt = expiresAt;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getChannel() {
        return channel;
    }

    public String getLink() {
        return link;
    }

    public long getAnnouncedAt() {
        return announcedAt;
    }

    /**
     * @return 0 when the row has no timer and only leaves on quit
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired(long now) {
        return expiresAt > 0 && now >= expiresAt;
    }
}
