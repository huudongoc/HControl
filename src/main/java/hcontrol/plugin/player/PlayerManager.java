package hcontrol.plugin.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, PlayerProfile> onlineProfiles = new HashMap<>();

    /* ===== ADD ===== */
    public void add(PlayerProfile profile) {
        onlineProfiles.put(profile.getUuid(), profile);
    }

    /* ===== GET ===== */
    public PlayerProfile get(UUID uuid) {
        return onlineProfiles.get(uuid);
    }

    public boolean isOnline(UUID uuid) {
        return onlineProfiles.containsKey(uuid);
    }

    /* ===== REMOVE ===== */
    public PlayerProfile remove(UUID uuid) {
        return onlineProfiles.remove(uuid);
    }

    /* ===== ALL ONLINE ===== */
    public Collection<PlayerProfile> getAllOnline() {
        return onlineProfiles.values();
    }

    /* ===== CLEAR (SHUTDOWN) ===== */
    public void clear() {
        onlineProfiles.clear();
    }
}
