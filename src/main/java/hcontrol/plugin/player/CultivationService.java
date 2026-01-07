package hcontrol.plugin.player;

import hcontrol.plugin.model.CultivationRealm;

import hcontrol.plugin.model.CultivatorProfile;
import org.bukkit.entity.Player;

/**
 * CULTIVATION SERVICE
 * Logic tu luyen, dot pha, thien kiep
 */
public class CultivationService {
    
    /**
     * Tu luyen (meditation) - tick based
     * @return tu vi nhan duoc
     */
    public long cultivate(CultivatorProfile profile, long ticks) {
        // He so tu luyen tong hop
        double multiplier = profile.getCultivationMultiplier();
        
        // Base cultivation per tick
        long baseCultivation = profile.getRealm().ordinal() + 1;
        
        // Tinh toan tu vi nhan duoc
        long gained = (long) (baseCultivation * ticks * multiplier);
        
        // Them tu vi
        profile.addCultivation(gained);
        profile.addCultivationTime(ticks);
        
        // Tang dao tam nhe khi tu luyen
        profile.addDaoHeart(0.01 * ticks);
        
        return gained;
    }
    
    /**
     * Tinh ti le dot pha thanh cong
     */
    public double calculateBreakthroughChance(CultivatorProfile profile) {
        if (!profile.canBreakthrough()) return 0.0;
        
        CultivationRealm current = profile.getRealm();
        CultivationRealm next = current.getNext();
        if (next == null) return 0.0;
        
        // Base chance theo realm
        double baseChance = switch (next) {
            case MORTAL -> 100.0;
            case QI_REFINING -> 95.0;
            case FOUNDATION -> 80.0;
            case GOLDEN_CORE -> 60.0;
            case NASCENT_SOUL -> 40.0;
            case SOUL_FORMATION -> 25.0;
            case VOID_REFINEMENT -> 15.0;
            case BODY_INTEGRATION -> 10.0;
            case MAHAYANA -> 5.0;
            case TRIBULATION -> 2.0;
            case IMMORTAL -> 1.0;
        };
        
        // Dao tam anh huong
        double daoHeartBonus = (profile.getDaoHeart() - 50) * 0.5; // +25% max
        
        // Noi thuong am
        double injuryPenalty = profile.getInnerInjury() * 0.5;
        
        // Linh can chat luong
        double qualityBonus = (profile.getRootQuality().getQualityMultiplier() - 1.0) * 10;
        
        double finalChance = baseChance + daoHeartBonus - injuryPenalty + qualityBonus;
        
        return Math.max(0.1, Math.min(99.9, finalChance));
    }
    
    /**
     * Thuc hien dot pha
     */
    public BreakthroughResult attemptBreakthrough(CultivatorProfile profile) {
        if (!profile.canBreakthrough()) {
            return new BreakthroughResult(false, "Chua du tu vi!", 0);
        }
        
        double chance = calculateBreakthroughChance(profile);
        double roll = Math.random() * 100;
        
        if (roll < chance) {
            // THANH CONG
            profile.breakthrough();
            
            // Reset trang thai
            profile.setDaoHeart(100.0);
            profile.setInnerInjury(0.0);
            profile.setTribulationPower(0.0);
            
            return new BreakthroughResult(true, "Dot pha thanh cong!", chance);
        } else {
            // THAT BAI
            
            // Mat 50% tu vi
            long lost = profile.getCultivation() / 2;
            profile.setCultivation(lost);
            
            // Nhan noi thuong
            double injury = 20 + Math.random() * 30; // 20-50%
            profile.addInnerInjury(injury);
            
            // Mat dao tam
            profile.addDaoHeart(-10 - Math.random() * 10);
            
            return new BreakthroughResult(false, 
                String.format("Dot pha that bai! Noi thuong %.0f%%, mat %.0f%% tu vi", 
                    injury, 50.0), 
                chance);
        }
    }
    
    /**
     * Chua tri noi thuong (meditation hoac dan duoc)
     */
    public void healInnerInjury(CultivatorProfile profile, double amount) {
        profile.setInnerInjury(profile.getInnerInjury() - amount);
    }
    
    /**
     * Tang dao tam (tu luyen hoac kinh nghiem)
     */
    public void improveDaoHeart(CultivatorProfile profile, double amount) {
        profile.addDaoHeart(amount);
    }
    
    /**
     * Kiem tra realm suppression (cao cap ap thap cap)
     */
    public double getRealmSuppression(CultivationRealm attacker, CultivationRealm defender) {
        int realmGap = attacker.ordinal() - defender.ordinal();
        
        if (realmGap >= 3) {
            // Chenh lech 3+ canh gioi = ap dao tuyet doi
            return 0.3; // defender chi con 30% suc manh
        } else if (realmGap == 2) {
            return 0.5; // 50%
        } else if (realmGap == 1) {
            return 0.7; // 70%
        } else if (realmGap == 0) {
            return 1.0; // binh thuong
        } else if (realmGap == -1) {
            return 1.3; // thap cap danh cao cap = bonus nho
        } else {
            return 1.5; // bonus lon hon (nghich thien)
        }
    }
    
    /**
     * Tinh toan thien kiep (dung cho tribulation realm)
     */
    public TribulationResult faceTribulation(CultivatorProfile profile) {
        // TODO: PHASE 9 - implement heavenly tribulation
        return new TribulationResult(false, "Chua implement");
    }
    
    // ===== RESULT CLASSES =====
    
    public static class BreakthroughResult {
        public final boolean success;
        public final String message;
        public final double chance;
        
        public BreakthroughResult(boolean success, String message, double chance) {
            this.success = success;
            this.message = message;
            this.chance = chance;
        }
    }
    
    public static class TribulationResult {
        public final boolean success;
        public final String message;
        
        public TribulationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
