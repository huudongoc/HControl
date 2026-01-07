package hcontrol.plugin.module.boss;

import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BOSS MANAGER
 * Quan ly tat ca boss dang active (foundation cho sau)
 */
public class BossManager {
    
    private final Map<UUID, BossEntity> activeBosses = new HashMap<>();
    
    /**
     * Register boss moi
     */
    public void registerBoss(BossEntity boss) {
        activeBosses.put(boss.getEntity().getUniqueId(), boss);
    }
    
    /**
     * Get boss tu entity UUID
     */
    public BossEntity getBoss(UUID entityUuid) {
        return activeBosses.get(entityUuid);
    }
    
    /**
     * Check entity co phai boss khong
     */
    public boolean isBoss(LivingEntity entity) {
        return activeBosses.containsKey(entity.getUniqueId());
    }
    
    /**
     * Remove boss (da chet hoac despawn)
     */
    public void removeBoss(UUID entityUuid) {
        activeBosses.remove(entityUuid);
    }
    
    /**
     * Update tat ca boss health display
     */
    public void updateAllBosses() {
        activeBosses.values().forEach(boss -> {
            if (boss.isDead()) {
                removeBoss(boss.getEntity().getUniqueId());
            } else {
                boss.updateHealthDisplay();
            }
        });
    }
    
    /**
     * Get so luong boss dang active
     */
    public int getActiveBossCount() {
        return activeBosses.size();
    }
    
    /**
     * Clear tat ca boss
     */
    public void clear() {
        activeBosses.clear();
    }
}
