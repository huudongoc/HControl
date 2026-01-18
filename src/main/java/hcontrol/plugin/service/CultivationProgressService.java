package hcontrol.plugin.service;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;

/**
 * CULTIVATION PROGRESS SERVICE
 * Xu ly logic tinh toan ve cultivation progress, required exp, max level...
 * KHONG chua logic hien thi (chi chua logic tinh toan)
 */
public class CultivationProgressService {
    
    private final LevelService levelService;
    
    public CultivationProgressService(LevelService levelService) {
        this.levelService = levelService;
    }
    
    /**
     * Lay max level cua realm
     */
    public int getMaxLevelForRealm(CultivationRealm realm) {
        return switch(realm) {
            case PHAMNHAN: yield 10;
            case LUYENKHI: yield 10;
            case TRUCCO: yield 10;
            case KIMDAN: yield 10;
            case NGUYENANH: yield 10;
            case HOATHAN: yield 10;
            case LUYENHON: yield 10;
            case HOPTHE: yield 10;
            case DAITHUA: yield 10;
            case DOKIEP: yield 10;
            case CHANTIEN: yield 10;
            default: yield realm.getMaxLevelInRealm();
        };
    }
    
    /**
     * Lay max level cua player dua vao realm hien tai
     */
    public int getMaxLevel(PlayerProfile profile) {
        return getMaxLevelForRealm(profile.getRealm());
    }
    
    /**
     * Tinh cultivation can thiet de len level ke tiep
     * Tra ve 0 neu da max level
     */
    public long getRequiredCultivation(PlayerProfile profile) {
        int level = profile.getLevel();
        int maxLevel = getMaxLevel(profile);
        
        // Da max level
        if (level >= maxLevel) {
            return 0;
        }
        
        // Su dung LevelService de tinh
        long required = levelService.getRequiredCultivation(level + 1, profile.getRealm());
        
        // Đảm bảo không trả về 0 nếu chưa max level (tránh lỗi chia cho 0)
        if (required <= 0 && level < maxLevel) {
            // Fallback: tính lại với level hiện tại + 1
            return levelService.getRequiredCultivation(level + 1, profile.getRealm());
        }
        
        return required;
    }
    
    /**
     * Tinh phan tram cultivation progress (0-100)
     * Tra ve 100 neu da max level hoặc đã đủ tu vi để lên level tiếp
     */
    public double getCultivationPercent(PlayerProfile profile) {
        int level = profile.getLevel();
        int maxLevel = getMaxLevel(profile);
        
        // Da max level
        if (level >= maxLevel) {
            return 100.0;
        }
        
        long currentCult = profile.getCultivation();
        long requiredCult = getRequiredCultivation(profile);
        
        // Check requiredCult hợp lệ - nếu <= 0 thì có vấn đề
        if (requiredCult <= 0) {
            // Debug: log để kiểm tra
            // System.out.println("[DEBUG] getCultivationPercent: requiredCult <= 0, level=" + level + ", maxLevel=" + maxLevel);
            return 100.0;
        }
        
        // Tính phần trăm
        double percent = ((double) currentCult / (double) requiredCult) * 100.0;
        
        // Clamp 0-100 (nếu đã đủ tu vi nhưng chưa level up, vẫn hiển thị 100%)
        return Math.min(100.0, Math.max(0.0, percent));
    }
    
    /**
     * Tinh phan tram HP (0-100)
     */
    public double getHPPercent(double currentHP, double maxHP) {
        if (maxHP <= 0) return 0;
        return (currentHP / maxHP) * 100.0;
    }
    
    /**
     * Tinh phan tram Linh Khi (0-100)
     */
    public double getLingQiPercent(double currentLQ, double maxLQ) {
        if (maxLQ <= 0) return 0;
        return (currentLQ / maxLQ) * 100.0;
    }
    
    /**
     * Check xem player co the len level khong
     */
    public boolean canLevelUp(PlayerProfile profile) {
        int level = profile.getLevel();
        int maxLevel = getMaxLevel(profile);
        
        // Da max level
        if (level >= maxLevel) {
            return false;
        }
        
        long currentCult = profile.getCultivation();
        long requiredCult = getRequiredCultivation(profile);
        
        return currentCult >= requiredCult;
    }
}
