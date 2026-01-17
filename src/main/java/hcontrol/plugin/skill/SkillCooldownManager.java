package hcontrol.plugin.skill;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHASE 7.2 — SKILL COOLDOWN MANAGER
 * Quan ly cooldown cua skills cho tat ca entities
 */
public class SkillCooldownManager {
    
    // Map: EntityUUID -> (SkillId -> ExpiryTime)
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    
    /**
     * Set cooldown cho skill
     * 
     * @param entityUUID UUID cua entity
     * @param skillId ID cua skill
     * @param cooldownSeconds Thoi gian cooldown (giay)
     */
    public void setCooldown(UUID entityUUID, String skillId, int cooldownSeconds) {
        long expiryTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        
        cooldowns.computeIfAbsent(entityUUID, k -> new ConcurrentHashMap<>())
                 .put(skillId, expiryTime);
    }
    
    /**
     * Kiem tra skill co dang cooldown khong
     * 
     * @param entityUUID UUID cua entity
     * @param skillId ID cua skill
     * @return true neu dang cooldown
     */
    public boolean isOnCooldown(UUID entityUUID, String skillId) {
        Map<String, Long> entityCooldowns = cooldowns.get(entityUUID);
        if (entityCooldowns == null) {
            return false;
        }
        
        Long expiryTime = entityCooldowns.get(skillId);
        if (expiryTime == null) {
            return false;
        }
        
        // Kiem tra het cooldown chua
        if (System.currentTimeMillis() >= expiryTime) {
            // Het cooldown, xoa entry
            entityCooldowns.remove(skillId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Lay thoi gian con lai cua cooldown (giay)
     * 
     * @param entityUUID UUID cua entity
     * @param skillId ID cua skill
     * @return Thoi gian con lai (0 neu khong cooldown)
     */
    public int getRemainingCooldown(UUID entityUUID, String skillId) {
        Map<String, Long> entityCooldowns = cooldowns.get(entityUUID);
        if (entityCooldowns == null) {
            return 0;
        }
        
        Long expiryTime = entityCooldowns.get(skillId);
        if (expiryTime == null) {
            return 0;
        }
        
        long remaining = expiryTime - System.currentTimeMillis();
        if (remaining <= 0) {
            entityCooldowns.remove(skillId);
            return 0;
        }
        
        return (int) (remaining / 1000);
    }
    
    /**
     * Clear tat ca cooldowns cua entity
     * 
     * @param entityUUID UUID cua entity
     */
    public void clearCooldowns(UUID entityUUID) {
        cooldowns.remove(entityUUID);
    }
    
    /**
     * Clear cooldown cua 1 skill cu the
     * 
     * @param entityUUID UUID cua entity
     * @param skillId ID cua skill
     */
    public void clearCooldown(UUID entityUUID, String skillId) {
        Map<String, Long> entityCooldowns = cooldowns.get(entityUUID);
        if (entityCooldowns != null) {
            entityCooldowns.remove(skillId);
        }
    }
    
    /**
     * Clear tat ca cooldowns (reload)
     */
    public void clearAll() {
        cooldowns.clear();
    }
    
    /**
     * Cleanup expired cooldowns (goi dinh ky)
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        
        cooldowns.values().forEach(entityCooldowns -> 
            entityCooldowns.entrySet().removeIf(entry -> entry.getValue() <= now)
        );
        
        // Xoa entities khong con cooldown nao
        cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
