package hcontrol.plugin.service;

import org.bukkit.entity.Player;

import hcontrol.plugin.model.AscensionProfile;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;

/**
 * ASCENSION SERVICE
 * Endgame progression system sau CHANTIEN level 10
 * 
 * Quy tắc:
 * - Chỉ mở khi realm == CHANTIEN && level == 10
 * - Không reset realm, không reset class
 * - Chỉ tăng modifier (ascensionPower)
 * - Cost tăng dần (soft cap)
 */
public class AscensionService {
    
    /**
     * Check điều kiện ascension
     * Chỉ mở khi: realm == CHANTIEN && level == 10
     */
    public boolean canAscend(PlayerProfile profile) {
        if (profile == null) {
            return false;
        }
        
        // Phải ở CHANTIEN level 10
        if (profile.getRealm() != CultivationRealm.CHANTIEN) {
            return false;
        }
        
        if (profile.getLevel() < profile.getRealm().getMaxLevelInRealm()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Thực hiện ascension
     * Tăng ascension level và trừ cultivation
     * 
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean ascend(PlayerProfile profile) {
        if (!canAscend(profile)) {
            return false;
        }
        
        AscensionProfile ascension = profile.getAscensionProfile();
        long requiredCultivation = ascension.getRequiredCultivation();
        
        // Check đủ cultivation
        if (profile.getCultivation() < requiredCultivation) {
            return false;
        }
        
        // Trừ cultivation và tăng level
        profile.setCultivation(profile.getCultivation() - requiredCultivation);
        ascension.increaseLevel();
        
        return true;
    }
    
    /**
     * Get required cultivation cho ascension tiếp theo
     */
    public long getRequiredCultivation(PlayerProfile profile) {
        if (profile == null) {
            return 0;
        }
        
        AscensionProfile ascension = profile.getAscensionProfile();
        return ascension.getRequiredCultivation();
    }
    
    /**
     * Get ascension power multiplier
     * Dùng cho CombatService
     */
    public double getAscensionPower(PlayerProfile profile) {
        if (profile == null) {
            return 1.0;
        }
        
        AscensionProfile ascension = profile.getAscensionProfile();
        return ascension.getAscensionPower();
    }
    
    /**
     * Send ascension info cho player
     */
    public void sendAscensionInfo(Player player, PlayerProfile profile) {
        if (player == null || profile == null) {
            return;
        }
        
        AscensionProfile ascension = profile.getAscensionProfile();
        
        player.sendMessage("§6§l=== ASCENSION INFO ===");
        player.sendMessage("§7Ascension Level: §e" + ascension.getAscensionLevel());
        player.sendMessage("§7Ascension Power: §e" + String.format("%.1f", ascension.getAscensionPower()) + "x");
        
        if (canAscend(profile)) {
            long required = getRequiredCultivation(profile);
            long current = profile.getCultivation();
            long needed = Math.max(0, required - current);
            
            player.sendMessage("§7Cultivation: §e" + current + "§7/§e" + required);
            if (needed > 0) {
                player.sendMessage("§7Cần thêm: §c" + needed + " §7cultivation");
            } else {
                player.sendMessage("§a✔ Đủ cultivation! Dùng §e/ascend §ađể thăng cấp!");
            }
        } else {
            player.sendMessage("§c✘ Chưa đủ điều kiện ascension!");
            player.sendMessage("§7Yêu cầu: §eCHÂN TIÊN §7level §e10");
        }
        
        player.sendMessage("§6§l======================");
    }
}
