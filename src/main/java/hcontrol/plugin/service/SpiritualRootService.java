package hcontrol.plugin.service;

import hcontrol.plugin.model.RootQuality;
import hcontrol.plugin.model.SpiritualRoot;

/**
 * SPIRITUAL ROOT SERVICE
 * Logic ve linh can - random, tinh cultivation speed, damage bonus
 */
public class SpiritualRootService {
    
    /**
     * Random linh can theo ty le tu tien CHUAN
     * - Ngu linh can (5 loai): pho bien (98,900/99,999 ≈ 98.9%)
     * - 4 linh can: 1/100 (1,000/99,999)
     * - 3 linh can: 1/1000 (100/99,999)
     * - Loi/Phong/Bang (bien di): 1/9,999 (10/99,999)
     * - Am Duong/Hon Don (huyen thoai): 1/99,999 (1/99,999)
     */
    public SpiritualRoot randomSpiritualRoot() {
        double roll = Math.random() * 99999;
        
        // 1/99,999: Am Duong / Hon Don (HUYEN THOAI - RARITY 5)
        if (roll < 1) {
            return Math.random() < 0.5 ? SpiritualRoot.YIN_YANG : SpiritualRoot.CHAOS;
        }
        
        // 10/99,999 ≈ 1/9,999: Loi/Phong/Bang (BIEN DI - RARITY 4)
        if (roll < 10) {
            SpiritualRoot[] mutations = {
                SpiritualRoot.THUNDER, 
                SpiritualRoot.WIND, 
                SpiritualRoot.ICE
            };
            return mutations[(int)(Math.random() * mutations.length)];
        }
        
        // 100/99,999 ≈ 1/1,000: 3 linh can (THIEN TAI - RARITY 3)
        if (roll < 100) {
            // TODO: implement 3 linh can system (multi-root)
            // Tam thoi tra ve ngu hanh ngau nhien
            return randomBasicRoot();
        }
        
        // 1,000/99,999 ≈ 1/100: 4 linh can (THIEN PHAN - RARITY 2)
        if (roll < 1000) {
            // TODO: implement 4 linh can system (multi-root)
            // Tam thoi tra ve ngu hanh ngau nhien
            return randomBasicRoot();
        }
        
        // 98,900/99,999: Ngu linh can (PHO BIEN - RARITY 1)
        return randomBasicRoot();
    }
    
    /**
     * Random ngu hanh co ban (Kim Moc Thuy Hoa Tho)
     */
    private SpiritualRoot randomBasicRoot() {
        SpiritualRoot[] basics = {
            SpiritualRoot.METAL,   // Kim
            SpiritualRoot.WOOD,    // Moc
            SpiritualRoot.WATER,   // Thuy
            SpiritualRoot.FIRE,    // Hoa
            SpiritualRoot.EARTH    // Tho
        };
        return basics[(int)(Math.random() * basics.length)];
    }
    
    /**
     * Random linh can pham chat (Quality)
     * - Pham Can (PHAMNHAN): 60%
     * - Linh Can (Spiritual): 30%
     * - Thien Can (Heavenly): 8%
     * - Tien Can (ImPHAMNHAN): 2%
     */
    public RootQuality randomRootQuality() {
        double rand = Math.random();
        if (rand < 0.60) return RootQuality.PHAMNHAN;
        if (rand < 0.90) return RootQuality.SPIRITUAL;
        if (rand < 0.98) return RootQuality.HEAVENLY;
        return RootQuality.CHANTIEN;
    }
    
    /**
     * TINH CULTIVATION SPEED MULTIPLIER
     * Cang hiem thi tu luyen cang nhanh
     * 
     * Formula: rootRarity × quality × (1 - injury/200)
     */
    public double getCultivationMultiplier(SpiritualRoot root, RootQuality quality, double innerInjury) {
        double rarityBonus = getRarityMultiplier(root);
        double qualityBonus = quality.getQualityMultiplier();
        double injuryPenalty = Math.max(0, 1 - innerInjury / 200.0);
        
        return rarityBonus * qualityBonus * injuryPenalty;
    }
    
    /**
     * RARITY MULTIPLIER - CANG HIEM CANG NHANH
     * - Ngu linh can: ×1.0 (baseline)
     * - 4 linh can: ×1.2
     * - 3 linh can: ×1.5
     * - Bien di (Loi/Phong/Bang): ×1.8
     * - Huyen thoai (Am Duong/Hon Don): ×2.5
     */
    private double getRarityMultiplier(SpiritualRoot root) {
        return switch (root) {
            // Huyen thoai (rarity 5)
            case YIN_YANG, CHAOS -> 2.5;
            
            // Bien di (rarity 4)
            case THUNDER, WIND, ICE -> 1.8;
            
            // TODO: 3 linh can (rarity 3) -> 1.5
            // TODO: 4 linh can (rarity 2) -> 1.2
            
            // Ngu hanh (rarity 1)
            default -> 1.0;
        };
    }
    
    /**
     * DAMAGE BONUS THEO RARITY
     * Cang hiem thi damage cang cao (+0.5 moi tier)
     * 
     * - Ngu linh can: +0% damage
     * - 4 linh can: +0.5 (+10%)
     * - 3 linh can: +1.0 (+20%)
     * - Bien di: +1.5 (+30%)
     * - Huyen thoai: +2.5 (+50%)
     */
    public double getDamageBonus(SpiritualRoot root) {
        return switch (root) {
            case YIN_YANG, CHAOS -> 2.5;        // +50% damage
            case THUNDER, WIND, ICE -> 1.5;     // +30% damage
            // TODO: 3 linh can -> 1.0
            // TODO: 4 linh can -> 0.5
            default -> 0.0;                     // baseline
        };
    }
    
    /**
     * TINH RARITY TIER (1-5)
     * De hien thi, log, compare
     */
    public int getRarityTier(SpiritualRoot root) {
        return switch (root) {
            case YIN_YANG, CHAOS -> 5;          // Huyen thoai
            case THUNDER, WIND, ICE -> 4;       // Bien di
            // TODO: 3 linh can -> 3
            // TODO: 4 linh can -> 2
            default -> 1;                       // Ngu hanh
        };
    }
    
    /**
     * GET RARITY NAME (de hien thi)
     */
    public String getRarityName(SpiritualRoot root) {
        return switch (root) {
            case YIN_YANG, CHAOS -> "§5§lHuyen Thoai";
            case THUNDER, WIND, ICE -> "§d§lBien Di";
            default -> "§7Pho Bien";
        };
    }
}
