package org.ccoding.liveannounce.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    /**
     * Guarda el momento (en ms) en el que el jugador puede volver
     * a usar el comando
     */
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    // Cooldown en milisegundos
    private final long cooldownMillis;

    public CooldownManager(long cooldownSeconds) {
        this.cooldownMillis = cooldownSeconds * 1000L;
    }

    // Verifica si el jugador puede usar el comando
    public boolean canUse(UUID uuid) {
        Long expiresAt = cooldowns.get(uuid);

        // No tiene cooldown
        if (expiresAt == null) {
            return true;
        }

        // Cooldown expirado -> Lo limpia
        if (System.currentTimeMillis() >= expiresAt) {
            cooldowns.remove(uuid);
            return true;
        }

        return false;
    }

    // Aplica el cooldown al jugador
    public void apply(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis() + cooldownMillis);
    }

    // Obtiene el tiempo restante en segundos
    public long getRemaining(UUID uuid) {
        Long expiresAt = cooldowns.get(uuid);

        if (expiresAt == null) {
            return 0;
        }

        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    // Limpia todos los cooldowns
    public void clear() {
        cooldowns.clear();
    }
}
