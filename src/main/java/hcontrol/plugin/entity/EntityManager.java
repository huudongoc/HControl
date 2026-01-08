package hcontrol.plugin.entity;

import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PHASE 7 — ENTITY MANAGER
 * Quan ly profile cua tat ca entity (vanilla + custom)
 * Tuong tu PlayerManager nhung cho mob
 */
public class EntityManager {
    
    // cache RAM: UUID -> EntityProfile
    private final Map<UUID, EntityProfile> entities = new HashMap<>();
    
    /**
     * Them entity profile
     */
    public void add(EntityProfile profile) {
        entities.put(profile.getEntityUUID(), profile);
    }
    
    /**
     * Lay profile theo UUID
     */
    public EntityProfile get(UUID uuid) {
        return entities.get(uuid);
    }
    
    /**
     * Lay hoac tao profile cho entity
     * Neu chua co -> tao mac dinh
     * KHONG tao cho Player (player co PlayerProfile rieng)
     */
    public EntityProfile getOrCreate(LivingEntity entity) {
        // Safety check: KHONG tao EntityProfile cho Player
        if (entity instanceof org.bukkit.entity.Player) {
            return null;
        }
        
        UUID uuid = entity.getUniqueId();
        EntityProfile profile = entities.get(uuid);
        
        if (profile == null) {
            // tao profile mac dinh
            profile = new EntityProfile(uuid, entity.getType());
            entities.put(uuid, profile);
        }
        
        return profile;
    }
    
    /**
     * Remove entity profile (khi mob chet)
     */
    public void remove(UUID uuid) {
        entities.remove(uuid);
    }
    
    /**
     * Check co profile khong
     */
    public boolean has(UUID uuid) {
        return entities.containsKey(uuid);
    }
    
    /**
     * Clear tat ca (reload)
     */
    public void clear() {
        entities.clear();
    }
    
    /**
     * Get so luong entity dang track
     */
    public int size() {
        return entities.size();
    }
}
