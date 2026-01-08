package hcontrol.plugin.ui;

import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.LevelService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

/**
 * SCOREBOARD SERVICE
 * Hien thi bang thong tin ben phai man hinh
 */
public class ScoreboardService {
    
    private final PlayerManager playerManager;
    private final LevelService levelService;
    
    public ScoreboardService(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.levelService = hcontrol.plugin.core.CoreContext.getInstance().getLevelService();
    }
    
    /**
     * Tao scoreboard cho player
     */
    public void createScoreboard(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        // tao scoreboard moi
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("tutienstats", "dummy", "§6§l修仙 TU TIEN");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        updateScoreboard(player, profile, obj);
        player.setScoreboard(board);
    }
    
    /**
     * Update scoreboard
     */
    public void updateScoreboard(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("tutienstats");
        if (obj == null) {
            createScoreboard(player);
            return;
        }
        
        updateScoreboard(player, profile, obj);
    }
    
    /**
     * Update scoreboard content
     */
    private void updateScoreboard(Player player, PlayerProfile profile, Objective obj) {
        // xoa tat ca entry cu
        for (String entry : obj.getScoreboard().getEntries()) {
            obj.getScoreboard().resetScores(entry);
        }
        
        int line = 15;
        
        // realm + tier (KHONG hien thi level so)
        obj.getScore("§7§m━━━━━━━━━━━━━").setScore(line--);
        String tierName = getTierName(profile.getLevel());
        obj.getScore("§f⚡ " + profile.getRealm().getDisplayName() + " " + tierName).setScore(line--);
        
        // tu vi progress (KHONG hien thi level so)
        obj.getScore(" ").setScore(line--);
        int maxLevel = getMaxLevel(profile);
        double cultPercent = profile.getLevel() < maxLevel 
            ? (double) profile.getCultivation() / levelService.getRequiredCultivation(profile.getLevel() + 1, profile.getRealm()) * 100 
            : 100.0;
        obj.getScore("§fTu vi: §a" + String.format("%.1f%%", cultPercent)).setScore(line--);
        
        // Sinh Mang & Linh Khi
        obj.getScore("  ").setScore(line--);
        obj.getScore("§c❤ §fSinh Mang: §c" + String.format("%.0f", profile.getCurrentHP()) + "§7/§c" + profile.getStats().getMaxHP()).setScore(line--);
        obj.getScore("§9✦ §fLinh Khi: §9" + String.format("%.0f", profile.getCurrentLingQi()) + "§7/§9" + profile.getStats().getMaxLingQi()).setScore(line--);
        
        // Stats tu tien (5 stat chinh)
        obj.getScore("   ").setScore(line--);
        obj.getScore("§7─────────────").setScore(line--);
        obj.getScore("§fCC:§a" + profile.getStats().getRoot() + " §7| §fLL:§b" + profile.getStats().getSpirit()).setScore(line--);
        obj.getScore("§fTP:§c" + profile.getStats().getPhysique() + " §7| §fNT:§e" + profile.getStats().getComprehension()).setScore(line--);
        obj.getScore("§fKV:§6" + profile.getStats().getFortune()).setScore(line--);
        
        // stat points
        if (profile.getStatPoints() > 0) {
            obj.getScore("    ").setScore(line--);
            obj.getScore("§e✦ Diem thuoc tinh: §a" + profile.getStatPoints()).setScore(line--);
        }
    }
    
    /**
     * Lay max level dua vao realm
     */
    private int getMaxLevel(PlayerProfile profile) {
        return getMaxLevelForRealm(profile.getRealm());
    }
    
    private int getMaxLevelForRealm(hcontrol.plugin.model.CultivationRealm realm) {
        switch(realm) {
            case MORTAL: return 10;
            case QI_REFINING: return 9;
            case FOUNDATION: return 9;
            case GOLDEN_CORE: return 9;
            default: return 10;
        }
    }
    
    /**
     * Lay tier name tu level
     */
    private String getTierName(int level) {
        if (level <= 3) return "§7Hạ";
        if (level <= 6) return "§eTrung";
        if (level <= 9) return "§6Thượng";
        return "§cĐỉnh";
    }
    
    /**
     * Remove scoreboard
     */
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
