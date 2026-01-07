package hcontrol.plugin.service;

import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.stats.StatType;

/**
 * PHASE 2 — Stat allocation service
 * Logic cua stat system, khong phai command
 */
public class StatService {

    /**
     * Parse stat type tu string (ho tro ca shortName)
     */
    public StatType parseStatType(String input) {
        String typeName = input.toUpperCase();
        
        for (StatType type : StatType.values()) {
            if (type.name().equals(typeName) || type.getShortName().equals(typeName)) {
                return type;
            }
        }
        
        return null; // khong tim thay
    }

    /**
     * Allocate stat points vao primary stat
     * @return true neu thanh cong, false neu that bai
     */
    public boolean allocateStatPoints(PlayerProfile profile, StatType type, int amount) {
        // validate stat type
        if (type == null || !type.isPrimary()) {
            return false;
        }
        
        // validate amount
        if (amount <= 0) {
            return false;
        }
        
        // validate stat points
        if (profile.getStatPoints() < amount) {
            return false;
        }
        
        // allocate
        profile.getStats().addPrimaryStat(type, amount);
        profile.removeStatPoints(amount);
        
        // update scoreboard ngay
        var player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            var scoreboardService = hcontrol.plugin.core.CoreContext.getInstance().getScoreboardService();
            if (scoreboardService != null) {
                scoreboardService.updateScoreboard(player);
            }
        }
        
        return true;
    }

    /**
     * Get formatted stat info
     */
    public String getStatInfo(PlayerProfile profile) {
        return profile.getStats().toDetailString();
    }
}
