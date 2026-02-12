package org.ccoding.liveannounce.proxy;

public interface Bridge {
    String getName();
    boolean isAvailable();
    void broadcastAnnouncement(String playerName, String platform, String link);
}