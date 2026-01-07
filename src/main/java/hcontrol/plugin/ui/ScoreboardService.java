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
        this.levelService = new LevelService();
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
        
        // realm
        obj.getScore("§7§m━━━━━━━━━━━━━").setScore(line--);
        obj.getScore("§f⚡ " + profile.getRealm().toString()).setScore(line--);
        obj.getScore("§7Canh gioi: " + profile.getRealm().getDisplayName()).setScore(line--);
        
        // level & exp
        obj.getScore(" ").setScore(line--);
        int maxLevel = getMaxLevel(profile);
        obj.getScore("§fCap do: §e" + profile.getLevel() + "§7/§e" + maxLevel).setScore(line--);
        
        double expPercent = profile.getLevel() < maxLevel 
            ? (double) profile.getCultivation() / levelService.getRequiredCultivation(profile.getLevel() + 1, profile.getRealm()) * 100 
            : 100.0;
        obj.getScore("§fKinh nghiem: §a" + String.format("%.1f%%", expPercent)).setScore(line--);
        
        // HP & Linh Khi
        obj.getScore("  ").setScore(line--);
        obj.getScore("§c❤ §fHP: §c" + profile.getCurrentHP() + "§7/§c" + profile.getStats().getMaxHP()).setScore(line--);
        obj.getScore("§9✦ §fLinh Khi: §9" + profile.getCurrentLingQi() + "§7/§9" + profile.getStats().getMaxLingQi()).setScore(line--);
        
        // LOAI BO combat stats RPG cu - chi hien thi defense
        obj.getScore("   ").setScore(line--);
        obj.getScore("§7§m━━━━━━━━━━━━━").setScore(line--);
        obj.getScore("§fPhong Thu: §b" + String.format("%.0f", profile.getStats().getDefense())).setScore(line--);
        
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
        var nextRealm = profile.getRealm().getNext();
        if (nextRealm == null) return 9999; // max realm
        return nextRealm.getRequiredLevel() - 1;
    }
    
    /**
     * Remove scoreboard
     */
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
