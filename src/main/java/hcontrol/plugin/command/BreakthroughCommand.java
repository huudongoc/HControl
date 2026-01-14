package hcontrol.plugin.command;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hcontrol.plugin.core.UIContext;
import hcontrol.plugin.model.BreakthroughResult;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.BreakthroughService;
import hcontrol.plugin.ui.tribulation.TribulationUI;

/**
 * TU TIEN - Breakthrough Command
 * Dot pha len canh gioi cao hon
 */
public class BreakthroughCommand implements CommandExecutor {
    
    private final PlayerManager playerManager;
    private final BreakthroughService breakthroughService;
    private final UIContext uiContext;
    
    public BreakthroughCommand(PlayerManager playerManager, BreakthroughService breakthroughService,
                               UIContext uiContext) {
        this.playerManager = playerManager;
        this.breakthroughService = breakthroughService;
        this.uiContext = uiContext;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChi player moi dung duoc lenh nay");
            return true;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cChua load profile!");
            return true;
        }
        
        CultivationRealm currentRealm = profile.getRealm();
        CultivationRealm nextRealm = currentRealm.getNext();
        
        // Check da max realm chua
        if (nextRealm == null) {
            player.sendMessage("§cBan da dat " + currentRealm.getDisplayName() + "! Khong the dot pha nua!");
            return true;
        }
        
        // Check dieu kien dot pha
        BreakthroughResult checkResult = breakthroughService.checkConditions(profile);
        
        if (checkResult == BreakthroughResult.ON_COOLDOWN) {
            handleCooldown(player);
            return true;
        }
        
        if (checkResult == BreakthroughResult.INSUFFICIENT_CULTIVATION) {
            handleInsufficientCultivation(player, profile, currentRealm, nextRealm);
            return true;
        }
        
        // PASS - hien thi ti le thanh cong va UI xac nhan
        double successRate = breakthroughService.calculateSuccessRate(profile, false);
        
        player.sendMessage((""));
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l    ⚡ ĐỘ KIẾP - THÔNG TIN");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        player.sendMessage("§7Cảnh giới: " + currentRealm.getDisplayName() + " §f→ §e" + nextRealm.getDisplayName());
        player.sendMessage("");
        player.sendMessage("§7Tỉ lệ thành công: " + getSuccessRateColor(successRate) + String.format("%.1f%%", successRate));
        player.sendMessage("§7Đạo tâm: " + getDaoHeartColor(profile.getDaoHeart()) + String.format("%.1f%%", profile.getDaoHeart()));
        player.sendMessage("§7Nội thương: " + getInjuryColor(profile.getInnerInjury()) + String.format("%.1f%%", profile.getInnerInjury()));
        player.sendMessage("");
        player.sendMessage("§c⚠ Thất bại sẽ:");
        player.sendMessage("§7  - Nhẹ: Lùi 1 tầng + nội thương");
        player.sendMessage("§7  - Nặng: Lùi về Đỉnh cảnh giới trước");
        player.sendMessage("§7  - Cực nặng: Tàn phế hoặc tử vong");
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        
        // Lay TribulationUI tu UIContext (lazy - khi command duoc thuc thi)
        TribulationUI tribulationUI = uiContext.getTribulationUI();
        if (tribulationUI != null) {
            tribulationUI.showConfirm(player);
        } else {
            player.sendMessage("§cLoi: TribulationUI chua duoc khoi tao!");
        }
        
        return true;
    }
    
    private String getSuccessRateColor(double rate) {
        if (rate >= 80) return "§a";
        if (rate >= 60) return "§e";
        if (rate >= 40) return "§6";
        if (rate >= 20) return "§c";
        return "§4";
    }
    
    private String getDaoHeartColor(double daoHeart) {
        if (daoHeart >= 80) return "§a";
        if (daoHeart >= 50) return "§e";
        return "§c";
    }
    
    private String getInjuryColor(double injury) {
        if (injury <= 20) return "§a";
        if (injury <= 50) return "§e";
        return "§c";
    }
    
    /**
     * Extract method: Xu ly cooldown (duplicate trong onCommand va handleBreakthroughResult)
     */
    private void handleCooldown(Player player) {
        long remaining = breakthroughService.getCooldownRemaining(player.getUniqueId());
        String timeStr = breakthroughService.formatCooldown(remaining);
        player.sendMessage("§c⏰ Dang trong thoi gian hoi phuc!");
        player.sendMessage("§7Con lai: §e" + timeStr);
    }
    
    /**
     * Extract method: Xu ly insufficient cultivation (duplicate trong onCommand va handleBreakthroughResult)
     */
    private void handleInsufficientCultivation(Player player, PlayerProfile profile,
                                               CultivationRealm currentRealm, CultivationRealm nextRealm) {
        long required = nextRealm.getRequiredCultivation();
        int maxLevel = currentRealm.getMaxLevelInRealm();
        
        player.sendMessage("§c❌ Chua du dieu kien de dot pha!");
        player.sendMessage("");
        
        // Check level
        if (profile.getLevel() < maxLevel) {
            player.sendMessage("§7✘ Level: §c" + profile.getLevel() + "§7/§e" + maxLevel + " §c(Chua max!)");
        } else {
            player.sendMessage("§7✔ Level: §a" + profile.getLevel() + "§7/§a" + maxLevel);
        }
        
        // Check cultivation
        if (profile.getCultivation() < required) {
            player.sendMessage("§7✘ Tu vi: §c" + profile.getCultivation() + "§7/§e" + required + " §c(Chua du!)");
        } else {
            player.sendMessage("§7✔ Tu vi: §a" + profile.getCultivation() + "§7/§a" + required);
        }
        
        // Check breakthrough unlock
        if (!profile.isBreakthroughUnlocked()) {
            player.sendMessage("§7✘ Mo khoa dot pha: §c✘ Chua mo khoa!");
            player.sendMessage("§7   §eCan: Nhiem vu dot pha / Giet boss tinh anh / Vuot thien kiep");
        } else {
            player.sendMessage("§7✔ Mo khoa dot pha: §a✔ Da mo khoa");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Can: §eLevel " + maxLevel + " §7+ §e" + required + " tu vi §7+ §eMo khoa dot pha");
    }
    
}
