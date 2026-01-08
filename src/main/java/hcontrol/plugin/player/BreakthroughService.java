package hcontrol.plugin.player;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.model.BreakthroughResult;
import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BREAKTHROUGH SERVICE
 * Logic dot pha - core cua tu tien system
 */
public class BreakthroughService {
    
    // Cooldown dot pha (UUID -> timestamp)
    private final Map<UUID, Long> breakthroughCooldowns = new HashMap<>();
    
    // Cooldown time (5 phut)
    private static final long COOLDOWN_MS = 5 * 60 * 1000L;
    
    // Cooldown sau that bai (30 phut)
    private static final long FAILURE_COOLDOWN_MS = 30 * 60 * 1000L;
    
    /**
     * 1. KIEM TRA DIEU KIEN DOT PHA
     */
    public BreakthroughResult checkConditions(PlayerProfile profile) {
        // Check da max realm chua
        if (profile.getRealm().getNext() == null) {
            return BreakthroughResult.INSUFFICIENT_CULTIVATION;
        }
        
        // Check cooldown
        if (isOnCooldown(profile.getUuid())) {
            return BreakthroughResult.ON_COOLDOWN;
        }
        
        // Check tu vi du chua
        if (!profile.canBreakthrough()) {
            return BreakthroughResult.INSUFFICIENT_CULTIVATION;
        }
        
        return null; // pass
    }
    
    /**
     * 2. TINH TI LE THANH CONG
     */
    public double calculateSuccessRate(PlayerProfile profile, boolean isForced) {
        CultivationRealm current = profile.getRealm();
        CultivationRealm next = current.getNext();
        if (next == null) return 0.0;
        
        // Base chance theo realm (cang cao cang kho)
        double baseChance = getBaseChance(next);
        
        // Dao tam bonus (+0.5% moi diem dao tam)
        double daoHeartBonus = (profile.getDaoHeart() - 50) * 0.5;
        
        // Noi thuong penalty (-1% moi diem noi thuong)
        double injuryPenalty = profile.getInnerInjury() * 1.0;
        
        // Linh can chat luong bonus
        double qualityBonus = (profile.getRootQuality().getQualityMultiplier() - 1.0) * 10;
        
        // Nghiep luc (karma)
        double karmaBonus = profile.getKarmaPoints() * 0.1;
        
        // Cuong ep dot pha -> giam 80% ti le
        double forcePenalty = isForced ? -80.0 : 0.0;
        
        // Tong ti le
        double totalChance = baseChance 
                           + daoHeartBonus 
                           - injuryPenalty 
                           + qualityBonus 
                           + karmaBonus
                           + forcePenalty;
        
        // Clamp 0.1% - 99.9%
        return Math.max(0.1, Math.min(99.9, totalChance));
    }
    
    /**
     * Base chance theo realm
     */
    private double getBaseChance(CultivationRealm realm) {
        return switch (realm) {
            case MORTAL -> 100.0;
            case QI_REFINING -> 95.0;
            case FOUNDATION -> 80.0;
            case GOLDEN_CORE -> 60.0;      // bat dau kho
            case NASCENT_SOUL -> 40.0;
            case SOUL_FORMATION -> 25.0;
            case VOID_REFINEMENT -> 15.0;
            case BODY_INTEGRATION -> 10.0;
            case MAHAYANA -> 5.0;
            case TRIBULATION -> 2.0;       // cuc kho
            case IMMORTAL -> 1.0;          // gan nhu khong the
        };
    }
    
    /**
     * 3. THUC HIEN DOT PHA BINH THUONG
     */
    public BreakthroughResult attemptBreakthrough(PlayerProfile profile) {
        return attemptBreakthrough(profile, false);
    }
    
    /**
     * 4. THUC HIEN DOT PHA (CO THE CUONG EP)
     */
    public BreakthroughResult attemptBreakthrough(PlayerProfile profile, boolean isForced) {
        // Kiem tra dieu kien (bo qua neu cuong ep)
        if (!isForced) {
            BreakthroughResult check = checkConditions(profile);
            if (check != null) return check;
        }
        
        // Tinh ti le
        double successRate = calculateSuccessRate(profile, isForced);
        double roll = Math.random() * 100;
        
        if (roll < successRate) {
            // THANH CONG
            return handleSuccess(profile);
        } else {
            // THAT BAI
            return handleFailure(profile, successRate, isForced);
        }
    }
    
    /**
     * 5. XU LY THANH CONG
     */
    private BreakthroughResult handleSuccess(PlayerProfile profile) {
        // Dot pha len canh gioi moi
        profile.breakthrough();
        
        // Reset trang thai
        profile.setDaoHeart(100.0);
        profile.setInnerInjury(0.0);
        profile.setTribulationPower(0.0);
        
        // Sync vanilla health (realm va level thay doi -> maxHP thay doi)
        Player player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            var healthService = CoreContext.getInstance().getPlayerHealthService();
            healthService.syncHealth(player, profile);
        }
        
        // Set cooldown ngan (5 phut)
        setCooldown(profile.getUuid(), COOLDOWN_MS);
        
        return BreakthroughResult.SUCCESS;
    }
    
    /**
     * 6. XU LY THAT BAI (3 LOAI: NOI THUONG / TAN PHE / CHET)
     */
    private BreakthroughResult handleFailure(PlayerProfile profile, double successRate, boolean isForced) {
        CultivationRealm realm = profile.getRealm();
        
        // Ti le 3 loai that bai theo canh gioi
        double injuryRate = getInjuryRate(realm);
        double crippledRate = getCrippledRate(realm);
        double deathRate = getDeathRate(realm);
        
        // Cuong ep -> tang risk gap doi
        if (isForced) {
            injuryRate *= 0.5;  // giam nua (vi se chuyen sang tan phe/chet)
            crippledRate *= 2.0;  // tang gap doi
            deathRate *= 2.0;     // tang gap doi
        }
        
        // Roll xac suat loai that bai
        double roll = Math.random() * 100;
        
        if (roll < deathRate) {
            // CHET (hiem nhat)
            return BreakthroughResult.DEATH;
        } else if (roll < deathRate + crippledRate) {
            // TAN PHE
            return handleCrippled(profile, successRate);
        } else {
            // NOI THUONG (pho bien nhat)
            return handleInternalInjury(profile, successRate);
        }
    }
    
    /**
     * 7. TI LE 3 LOAI THAT BAI THEO CANH GIOI
     */
    private double getInjuryRate(CultivationRealm realm) {
        return switch (realm) {
            case MORTAL -> 70.0;
            case QI_REFINING -> 60.0;
            case FOUNDATION -> 50.0;
            case GOLDEN_CORE -> 40.0;
            default -> 35.0;
        };
    }
    
    private double getCrippledRate(CultivationRealm realm) {
        return switch (realm) {
            case MORTAL -> 25.0;
            case QI_REFINING -> 30.0;
            case FOUNDATION -> 35.0;
            case GOLDEN_CORE -> 40.0;
            default -> 45.0;
        };
    }
    
    private double getDeathRate(CultivationRealm realm) {
        return switch (realm) {
            case MORTAL -> 5.0;
            case QI_REFINING -> 10.0;
            case FOUNDATION -> 15.0;
            case GOLDEN_CORE -> 20.0;
            default -> 25.0;
        };
    }
    
    /**
     * 8. XU LY NOI THUONG (PHAM VI 10-60% TUY MUC DO)
     */
    private BreakthroughResult handleInternalInjury(PlayerProfile profile, double successRate) {
        BreakthroughResult result;
        double injuryAmount;
        long cultivationLoss;
        
        if (successRate > 70) {
            // Nhe - lui 1 tang
            result = BreakthroughResult.INTERNAL_INJURY_MINOR;
            injuryAmount = 10 + Math.random() * 10;  // 10-20%
            cultivationLoss = profile.getCultivation() * 30 / 100;  // mat 30% tu vi
            profile.addDaoHeart(-5);  // mat dao tam nhe
            
            // Lui 1 tang
            if (profile.getLevel() > 1) {
                profile.setLevel(profile.getLevel() - 1);
            }
        } else if (successRate > 40) {
            // Trung binh - lui 2-3 tang
            result = BreakthroughResult.INTERNAL_INJURY_MODERATE;
            injuryAmount = 20 + Math.random() * 20;  // 20-40%
            cultivationLoss = profile.getCultivation() * 50 / 100;  // mat 50%
            profile.addDaoHeart(-15);
            profile.addTribulationPower(10);  // thien kiep luc tang
            
            // Lui 2-3 tang
            int newLevel = Math.max(1, profile.getLevel() - 2);
            profile.setLevel(newLevel);
        } else {
            // Nang - lui ve Dinh canh gioi truoc
            result = BreakthroughResult.INTERNAL_INJURY_SEVERE;
            injuryAmount = 40 + Math.random() * 20;  // 40-60%
            cultivationLoss = profile.getCultivation() * 70 / 100;  // mat 70%
            profile.addDaoHeart(-30);
            profile.addTribulationPower(25);
            profile.addKarmaPoints(-10);  // am nghiep
            
            // Lui ve Dinh (level 10) canh gioi truoc
            CultivationRealm previousRealm = profile.getRealm().getPrevious();
            if (previousRealm != null) {
                profile.setRealm(previousRealm);
                profile.setLevel(previousRealm.getMaxLevelInRealm()); // Level 10
            } else {
                // Neu khong co realm truoc, chi lui ve tang 1
                profile.setLevel(1);
            }
        }
        
        // Apply penalty
        profile.addInnerInjury(injuryAmount);
        profile.setCultivation(profile.getCultivation() - cultivationLoss);
        
        // Sync vanilla health (realm/level thay doi -> maxHP thay doi)
        Player player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            var healthService = CoreContext.getInstance().getPlayerHealthService();
            healthService.syncHealth(player, profile);
        }
        
        // Set cooldown (30 phut)
        setCooldown(profile.getUuid(), FAILURE_COOLDOWN_MS);
        
        return result;
    }
    
    /**
     * 9. XU LY TAN PHE (3 MUC DO: NHE / NANG / VINH VIEN)
     */
    private BreakthroughResult handleCrippled(PlayerProfile profile, double successRate) {
        BreakthroughResult result;
        
        // Xac dinh muc do tan phe (ti le thap that bai -> tan phe nang hon)
        double severityRoll = Math.random() * 100;
        
        if (successRate > 50 || severityRoll < 50) {
            // Tan phe nhe - giam tran level
            result = BreakthroughResult.CRIPPLED_LIGHT;
            // TODO: implement level cap reduction (luu trong profile)
        } else if (successRate > 30 || severityRoll < 80) {
            // Tan phe nang - khong dot pha nua
            result = BreakthroughResult.CRIPPLED_SEVERE;
            // TODO: implement breakthrough ban (flag trong profile)
        } else {
            // Tan phe vinh vien - tro thanh pham nhan
            result = BreakthroughResult.CRIPPLED_PERMANENT;
            // TODO: implement permanent cripple (reset realm ve MORTAL?)
        }
        
        // Penalty chung cho tat ca tan phe
        profile.setCultivation(0);  // mat het tu vi
        profile.addInnerInjury(60 + Math.random() * 30);  // 60-90% noi thuong
        profile.setDaoHeart(10 + Math.random() * 10);  // dao tam chi con 10-20%
        profile.addKarmaPoints(-50);  // am nghiep nang
        
        // Sync vanilla health (tan phe -> maxHP co the thay doi)
        Player player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            var healthService = CoreContext.getInstance().getPlayerHealthService();
            healthService.syncHealth(player, profile);
        }
        
        // Cooldown cuc dai (7 ngay)
        setCooldown(profile.getUuid(), 7 * 24 * 60 * 60 * 1000L);
        
        return result;
    }
    
    /**
     * COOLDOWN MANAGEMENT
     */
    /**
     * Set cooldown vao PlayerProfile (persistent)
     */
    private void setCooldown(UUID uuid, long durationMs) {
        // Khong luu vao HashMap nua
        // BreakthroughCommand se goi profile.setBreakthroughCooldownEnd(...)
    }
    
    private boolean isOnCooldown(UUID uuid) {
        Long cooldownEnd = breakthroughCooldowns.get(uuid);
        if (cooldownEnd == null) return false;
        
        if (System.currentTimeMillis() < cooldownEnd) {
            return true;
        }
        
        // Het cooldown
        breakthroughCooldowns.remove(uuid);
        return false;
    }
    
    public long getCooldownRemaining(UUID uuid) {
        Long cooldownEnd = breakthroughCooldowns.get(uuid);
        if (cooldownEnd == null) return 0;
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Format cooldown time
     */
    public String formatCooldown(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return hours + " gio " + (minutes % 60) + " phut";
        } else if (minutes > 0) {
            return minutes + " phut " + (seconds % 60) + " giay";
        } else {
            return seconds + " giay";
        }
    }
}
