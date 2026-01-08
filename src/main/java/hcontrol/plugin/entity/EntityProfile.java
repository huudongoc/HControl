package hcontrol.plugin.entity;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.LivingActor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * PHASE 7 — MOB RPG PROFILE
 * Profile cho mob vanilla + custom mod
 * Chua HP, realm, stats...
 * Implements LivingActor de thong nhat combat logic voi Player
 */
public class EntityProfile implements LivingActor {
    
    private final UUID entityUUID;
    private final EntityType entityType;
    private final String customName; // ten hien thi (optional)
    
    // tu tien stats
    private CultivationRealm realm;
    private int level;
    private double maxHP;
    private double currentHP;
    private double attack;
    private double defense;
    
    // flags
    private boolean isBoss;
    private boolean isElite;
    
    /**
     * Constructor cho vanilla mob (mac dinh realm)
     */
    public EntityProfile(UUID entityUUID, EntityType entityType) {
        this.entityUUID = entityUUID;
        this.entityType = entityType;
        this.customName = null;
        
        // mac dinh: mob yeu (Luyen Khi)
        this.realm = CultivationRealm.QI_REFINING;
        this.level = 1;
        this.maxHP = 20.0;
        this.currentHP = 20.0;
        this.attack = 2.0;
        this.defense = 0.0;
        this.isBoss = false;
        this.isElite = false;
    }
    
    /**
     * Constructor cho custom mob (tu dinh nghia)
     */
    public EntityProfile(UUID entityUUID, EntityType entityType, String customName, 
                        CultivationRealm realm, int level, double maxHP, double attack, double defense) {
        this.entityUUID = entityUUID;
        this.entityType = entityType;
        this.customName = customName;
        this.realm = realm;
        this.level = level;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.isBoss = false;
        this.isElite = false;
    }
    
    // ========== GETTERS / SETTERS ==========
    
    public UUID getEntityUUID() { return entityUUID; }
    public EntityType getEntityType() { return entityType; }
    public String getCustomName() { return customName; }
    
    public CultivationRealm getRealm() { return realm; }
    public void setRealm(CultivationRealm realm) { this.realm = realm; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public double getMaxHP() { return maxHP; }
    public void setMaxHP(double maxHP) { 
        this.maxHP = maxHP;
        if (currentHP > maxHP) currentHP = maxHP;
    }
    
    public double getCurrentHP() { return currentHP; }
    public void setCurrentHP(double hp) { 
        this.currentHP = Math.max(0, Math.min(hp, maxHP));
    }
    
    public double getAttack() { return attack; }
    public void setAttack(double attack) { this.attack = attack; }
    
    public double getDefense() { return defense; }
    public void setDefense(double defense) { this.defense = defense; }
    
    public boolean isBoss() { return isBoss; }
    public void setBoss(boolean boss) { this.isBoss = boss; }
    
    public boolean isElite() { return isElite; }
    public void setElite(boolean elite) { this.isElite = elite; }
    
    /**
     * Check con song khong
     */
    public boolean isAlive() {
        return currentHP > 0;
    }
    
    /**
     * Get hien thi name (custom name hoac entity type)
     * Implement LivingActor interface
     */
    @Override
    public String getDisplayName() {
        if (customName != null && !customName.isEmpty()) {
            return customName;
        }
        return entityType.name();
    }
    
    /**
     * To string de debug
     */
    @Override
    public String toString() {
        return String.format("%s [%s L%d] HP: %.0f/%.0f", 
            getDisplayName(), realm.getDisplayName(), level, currentHP, maxHP);
    }
    
    // ========== LIVING ACTOR IMPLEMENTATION ==========
    // Cac method da co roi (getRealm, getLevel, getMaxHP, getCurrentHP, setCurrentHP, getAttack, getDefense, isBoss, isElite)
    // Chi can them @Override cho methods chua co
    
    @Override
    public UUID getUUID() {
        return entityUUID;
    }
    
    // getDisplayName() da co @Override o tren (line ~120)
    
    @Override
    public LivingEntity getEntity() {
        // Tim entity trong world theo UUID
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(entityUUID) && entity instanceof LivingEntity) {
                    return (LivingEntity) entity;
                }
            }
        }
        return null;
    }
}

