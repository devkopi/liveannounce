package org.ccoding.liveannounce.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    private final long cooldownMillis;

    public CooldownManager(long cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000L;
    }

    public boolean canUse(UUID uuid) {
        Long expiresAt = cooldowns.get(uuid);

        if (expiresAt == null) {
            return true;
        }

        if (System.currentTimeMillis() >= expiresAt) {
            cooldowns.remove(uuid);
            return true;
        }

        return false;
    }

    public void apply(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis() + cooldownMillis);
    }

    public long getRemaining(UUID uuid) {
        Long expiresAt = cooldowns.get(uuid);

        if (expiresAt == null) {
            return 0;
        }

        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public void clear() {
        cooldowns.clear();
    }
}
