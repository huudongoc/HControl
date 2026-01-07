package hcontrol.plugin.util;

import hcontrol.plugin.model.CultivationRealm;

import hcontrol.plugin.model.CultivatorProfile;

import java.util.Random;

/**
 * DAMAGE FORMULA TU TIEN - 4 TANG NHAN
 * Theo triet ly: canh gioi > cong phap > can co > phap thuat
 * 
 * FinalDamage = BaseRealmDamage × RealmSuppression × TechniqueModifier × (1 - DefenseMitigation) × RandomDaoFactor
 */
public class DamageFormula {
    
    private static final Random random = new Random();
    
    /**
     * TINH DAMAGE TU TIEN CHUAN
     * @param attacker - tu si tan cong
     * @param defender - tu si phong thu
     * @param techniqueModifier - he so cong phap (1.0 = pham phap, 4.0 = cam thuat)
     * @return final damage
     */
    public static double calculateCultivationDamage(CultivatorProfile attacker, CultivatorProfile defender, double techniqueModifier) {
        // (1) Base Realm Damage
        double baseDamage = getBaseRealmDamage(attacker);
        
        // (2) Realm Suppression
        double realmSuppression = getRealmSuppression(attacker.getRealm(), defender.getRealm());
        
        // (3) Technique Modifier (cong phap)
        // da truyen vao param
        
        // (4) Defense Mitigation
        double defenseMitigation = getDefenseMitigation(defender, baseDamage);
        
        // (5) Random Dao Factor (ngo dao, khong phai crit)
        double daoFactor = getDaoFactor();
        
        // Final calculation
        double finalDamage = baseDamage 
                           * realmSuppression 
                           * techniqueModifier 
                           * (1 - defenseMitigation) 
                           * daoFactor;
        
        return Math.max(1, finalDamage);  // toi thieu 1 damage
    }
    
    /**
     * (1) BASE REALM DAMAGE
     * Damage theo canh gioi, KHONG theo level
     * Level chi anh huong rat nhe (+1% moi level)
     * LINH CAN chi anh huong damage (cang hiem +0.5)
     */
    private static double getBaseRealmDamage(CultivatorProfile attacker) {
        double realmBase = attacker.getRealm().getBaseDamage();
        
        // Level bonus rat nhe (+1% moi level)
        int level = attacker.getRealmLevel();
        double levelBonus = 1.0 + (level * 0.01);
        
        // Linh can damage bonus (cang hiem cang manh)
        double rootBonus = attacker.getSpiritualRoot().getDamageBonus();
        
        return realmBase * levelBonus * rootBonus;
    }
    
    /**
     * (2) REALM SUPPRESSION - UY AP CANH GIOI
     * Cao canh gioi danh thap = ap che
     * Thap danh cao = phan phe
     */
    private static double getRealmSuppression(CultivationRealm attackerRealm, CultivationRealm defenderRealm) {
        int diff = attackerRealm.ordinal() - defenderRealm.ordinal();
        
        if (diff >= 1) {
            // Cao hon -> ap che (+50% moi realm)
            return 1.0 + (diff * 0.5);
        } else if (diff < 0) {
            // Thap hon -> phan phe (giam 70% moi realm, toi thieu 10%)
            double suppression = 1.0 + (diff * 0.7);
            return Math.max(0.1, suppression);
        } else {
            // Bang nhau
            return 1.0;
        }
    }
    
    /**
     * (3) TECHNIQUE MODIFIER PRESETS
     * Cong phap / phap thuat modifier
     */
    public static final double TECHNIQUE_MORTAL = 1.0;      // Pham phap
    public static final double TECHNIQUE_SPIRIT = 1.3;      // Linh cap
    public static final double TECHNIQUE_EARTH = 1.7;       // Dia cap
    public static final double TECHNIQUE_HEAVEN = 2.5;      // Thien cap
    public static final double TECHNIQUE_FORBIDDEN = 4.0;   // Cam thuat (co phan phe)
    
    /**
     * (4) DEFENSE MITIGATION - PHONG THU TU TIEN
     * Khong dung armor kieu RPG
     * Formula: defense / (defense + attackerBase * 3)
     * Cap toi da 80% giam sat thuong
     */
    private static double getDefenseMitigation(CultivatorProfile defender, double attackerBase) {
        // Defense = VIT stat + realm defense bonus
        double defense = defender.getStats().getVitality() * 2;  // tam thoi dung VIT
        defense += defender.getRealm().ordinal() * 10;  // moi realm +10 defense
        
        // Formula: khong bao gio giam qua 80%
        double mitigation = defense / (defense + attackerBase * 3);
        return Math.min(0.8, mitigation);
    }
    
    /**
     * (5) RANDOM DAO FACTOR - "NGO DAO"
     * Khong phai crit chi mang
     * Chi random nhe 0.9 - 1.1 (±10%)
     */
    private static double getDaoFactor() {
        return 0.9 + (random.nextDouble() * 0.2);  // 0.9 -> 1.1
    }
    
    /**
     * TINH DAMAGE THIEN KIEP (KHONG GIONG COMBAT)
     * TribulationDamage = MaxHP × RealmMultiplier × Random(0.8 – 1.2)
     * @param cultivator - tu si do kiep
     * @param waveNumber - song thien kiep thu may
     * @return tribulation damage
     */
    public static double calculateTribulationDamage(CultivatorProfile cultivator, int waveNumber) {
        double maxHP = cultivator.getStats().getMaxHP();
        
        // Realm multiplier
        double realmMultiplier = switch (cultivator.getRealm()) {
            case GOLDEN_CORE -> 0.50;      // 40-60% HP
            case NASCENT_SOUL -> 0.70;     // 60-80% HP
            case SOUL_FORMATION -> 1.0;    // 80-120% HP
            case VOID_REFINEMENT -> 1.3;
            case BODY_INTEGRATION -> 1.6;
            case MAHAYANA -> 2.0;
            case TRIBULATION -> 2.5;
            default -> 0.3;
        };
        
        // Tang dan theo song (wave 1 = 100%, wave 9 = 180%)
        double waveMultiplier = 1.0 + (waveNumber * 0.1);
        
        // Random factor
        double randomFactor = 0.8 + (random.nextDouble() * 0.4);  // 0.8 - 1.2
        
        // Level trong realm giam damage thien kiep (-3% moi level, cap 30%)
        int level = cultivator.getRealmLevel();
        double levelReduction = Math.min(0.3, level * 0.03);
        
        double damage = maxHP * realmMultiplier * waveMultiplier * randomFactor * (1 - levelReduction);
        
        return Math.max(1, damage);
    }
    
    /**
     * VUOT CANH GIOI DANH NHAU - INNER INJURY
     * Thap canh gioi danh cao -> gay noi thuong cho chinh minh
     * @return noi thuong % (0-100)
     */
    public static double calculateInnerInjuryFromAttack(CultivatorProfile attacker, CultivatorProfile defender) {
        int diff = attacker.getRealm().ordinal() - defender.getRealm().ordinal();
        
        if (diff >= -1) {
            return 0;  // khong bi noi thuong neu bang hoac cao hon
        }
        
        // Vuot 2+ realm danh -> bi noi thuong
        double injuryRate = Math.abs(diff) * 5.0;  // moi realm 5%
        return Math.min(50, injuryRate);  // cap 50%
    }
}
