package hcontrol.plugin.model;

import java.util.UUID;

import org.bukkit.entity.LivingEntity;

/**
 * LIVING ACTOR — Interface chung cho Player + Entity trong combat
 * 
 * Muc dich: Thong nhat logic combat (PvP + PvE) thay vi 2 method rieng
 * PlayerProfile va EntityProfile deu implement interface nay
 */
public interface LivingActor {
    
    // ========== IDENTITY ==========
    
    /**
     * UUID cua actor (player hoac entity)
     */
    UUID getUUID();
    
    /**
     * Ten hien thi (player name hoac entity custom name)
     */
    String getDisplayName();
    
    // ========== CULTIVATION ==========
    
    /**
     * Canh gioi tu luyen (realm)
     */
    CultivationRealm getRealm();
    
    /**
     * Level trong canh gioi (1-10)
     */
    int getLevel();
    
    // ========== COMBAT STATS ==========
    
    /**
     * HP toi da
     */
    double getMaxHP();
    
    /**
     * HP hien tai
     */
    double getCurrentHP();
    
    /**
     * Set HP hien tai (sau khi bi damage hoac hoi mau)
     */
    void setCurrentHP(double hp);
    
    /**
     * Suc tan cong (base attack)
     */
    double getAttack();
    
    /**
     * Phong thu (defense/mitigation)
     */
    double getDefense();
    
    // ========== LING QI (OPTIONAL - CHI PLAYER CO) ==========
    
    /**
     * Linh khi toi da (mana)
     * Default: 0 (entity khong co linh khi)
     */
    default double getMaxLingQi() {
        return 0;
    }
    
    /**
     * Linh khi hien tai
     */
    default double getCurrentLingQi() {
        return 0;
    }
    
    /**
     * Set linh khi hien tai
     */
    default void setCurrentLingQi(double qi) {
        // Do nothing - chi player implement
    }
    
    // ========== FLAGS ==========
    
    /**
     * Co phai boss khong? (elite/world boss)
     */
    default boolean isBoss() {
        return false;
    }
    
    /**
     * Co phai elite mob khong?
     */
    default boolean isElite() {
        return false;
    }
    
    // ========== BUKKIT ENTITY (OPTIONAL) ==========
    
    /**
     * Lay Bukkit LivingEntity (de spawn effect, damage indicator...)
     * Nullable - co the return null neu entity da chet hoac offline
     */
    default LivingEntity getEntity() {
        return null;
    }
}
