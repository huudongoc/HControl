package hcontrol.plugin.ui.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import io.papermc.paper.scoreboard.numbers.NumberFormat;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CultivationProgressService;
import hcontrol.plugin.service.DisplayFormatService;

/**
 * SCOREBOARD SERVICE
 * Hien thi bang thong tin ben phai man hinh
 * KHONG chua logic tinh toan - chi su dung DisplayFormatService va CultivationProgressService
 */
public class ScoreboardService {
    
    private final PlayerManager playerManager;
    private final DisplayFormatService displayFormatService;
    private final CultivationProgressService cultivationProgressService;
    
    public ScoreboardService(PlayerManager playerManager, DisplayFormatService displayFormatService, CultivationProgressService cultivationProgressService) {
        this.playerManager = playerManager;
        this.displayFormatService = displayFormatService;
        this.cultivationProgressService = cultivationProgressService;
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
        Objective obj = board.registerNewObjective("tutienstats", "dummy", "§6§l TU TIEN");
        obj.numberFormat(NumberFormat.blank());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        updateScoreboard(player, profile, obj);
        player.setScoreboard(board);
    }
    
    /**
     * Update scoreboard
     */
    public void updateScoreboard(Player player) {
        // Skip if player is null or offline
        if (player == null || !player.isOnline()) return;
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("tutienstats");
        
        // FIX: Unregister và recreate objective để đảm bảo không có entries cũ
        if (obj != null) {
            obj.unregister();
        }
        
        // Recreate objective
        obj = board.registerNewObjective("tutienstats", "dummy", "§6§l TU TIEN");
        obj.numberFormat(NumberFormat.blank());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        updateScoreboard(player, profile, obj);
    }
    
    /**
     * Update scoreboard content
     */
    private void updateScoreboard(Player player, PlayerProfile profile, Objective obj) {
        // Objective đã được recreate → không cần xóa entries
        
        int line = 15;
        obj.numberFormat(NumberFormat.blank());
        // realm + tier ( hien thi level so) - su dung DisplayFormatService
        obj.getScore("§7§m━━━━━━━━━━━━━").setScore(line--);

        String levelText = String.format("§7§lTang : %d", profile.getLevel()); // §7§l: mau xam, §l: in dam
        obj.getScore(levelText).setScore(line--);

        String realmTierText = displayFormatService.formatRealmTier(profile.getRealm(), profile.getLevel());

        obj.getScore("§f⚡ " + realmTierText).setScore(line--);
        
        // tu vi progress - su dung CultivationProgressService
        obj.getScore(" ").setScore(line--);
        double cultPercent = cultivationProgressService.getCultivationPercent(profile);
        String cultProgressText = displayFormatService.formatCultivationProgress(cultPercent);
        obj.getScore(cultProgressText).setScore(line--);
        
        // Sinh Mang & Linh Khi - FIXED: Dùng fixed entry names để tránh duplicate
        obj.getScore("  ").setScore(line--);
        
        // Format HP với giá trị cụ thể
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getStats().getMaxHP();
        String hpDisplay = String.format("§c❤ §fSinh Mang: §c%.0f§7/§c%.0f", currentHP, maxHP);
        obj.getScore(hpDisplay).setScore(line--);
        
        // Format Linh Khi
        double currentLQ = profile.getCurrentLingQi();
        double maxLQ = profile.getStats().getMaxLingQi();
        String lqDisplay = String.format("§9✦ §fLinh Khi: §9%.0f§7/§9%.0f", currentLQ, maxLQ);
        obj.getScore(lqDisplay).setScore(line--);
        
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
     * Remove scoreboard
     */
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
