package hcontrol.plugin.module.boss;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * BOSS ENTITY MODEL
 * Foundation cho boss system - sau nay mo rong
 */
public class BossEntity {
    
    private final LivingEntity entity;
    private final String bossName;
    private final BossType type;
    
    private int currentPhase;
    private double maxHealth;
    private String displayName;
    
    public BossEntity(LivingEntity entity, String bossName, BossType type) {
        this.entity = entity;
        this.bossName = bossName;
        this.type = type;
        this.currentPhase = 1;
        this.maxHealth = entity.getMaxHealth();
        
        updateDisplayName();
    }
    
    /**
     * Update display name tren dau boss
     */
    private void updateDisplayName() {
        double healthPercent = (entity.getHealth() / maxHealth) * 100;
        
        // Format: [BOSS] BossName [Phase X] HP: XX%
        this.displayName = String.format(
            "%s§c[BOSS] §6%s §7[Phase %d] §c❤ %.0f%%",
            type.getColor(),
            bossName,
            currentPhase,
            healthPercent
        );
        
        entity.setCustomName(displayName);
        entity.setCustomNameVisible(true);
    }
    
    /**
     * Chuyen sang phase tiep theo
     */
    public void nextPhase() {
        currentPhase++;
        updateDisplayName();
    }
    
    /**
     * Update HP bar
     */
    public void updateHealthDisplay() {
        updateDisplayName();
    }
    
    /**
     * Boss noi chuyen (spawn chat bubble)
     */
    public void speak(String message) {
        // TODO: Tao armor stand chat bubble cho boss
        // Giong ChatBubbleService nhung cho entity
    }
    
    /**
     * Check boss da chet chua
     */
    public boolean isDead() {
        return entity.isDead() || entity.getHealth() <= 0;
    }
    
    // Getters
    public LivingEntity getEntity() { return entity; }
    public String getBossName() { return bossName; }
    public BossType getType() { return type; }
    public int getCurrentPhase() { return currentPhase; }
    public Location getLocation() { return entity.getLocation(); }
}
