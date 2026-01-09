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
    private final TribulationUI tribulationUI;
    
    public BreakthroughCommand(PlayerManager playerManager, BreakthroughService breakthroughService,
                               UIContext uiContext, TribulationUI tribulationUI) {
        this.playerManager = playerManager;
        this.breakthroughService = breakthroughService;
        this.uiContext = uiContext;
        this.tribulationUI = tribulationUI;
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
    
    /**
     * Xu ly ket qua dot pha
     */
    private void handleBreakthroughResult(Player player, BreakthroughResult result, 
                                         CultivationRealm oldRealm, CultivationRealm newRealm,
                                         PlayerProfile profile) {
        
        if (result == BreakthroughResult.ON_COOLDOWN) {
            handleCooldown(player);
            return;
        }
        
        if (result == BreakthroughResult.INSUFFICIENT_CULTIVATION) {
            CultivationRealm currentRealm = profile.getRealm();
            CultivationRealm nextRealm = currentRealm.getNext();
            handleInsufficientCultivation(player, profile, currentRealm, nextRealm);
            return;
        }
        
        if (result == BreakthroughResult.SUCCESS) {
            // THANH CONG!
            playBreakthroughEffects(player, oldRealm, newRealm);
            
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e§l    ⚡ DOT PHA THANH CONG! ⚡");
            player.sendMessage("");
            player.sendMessage("§7    " + oldRealm.toString() + " §f→ " + newRealm.toString());
            player.sendMessage("");
            player.sendMessage("§7    Tu vi: §e0 §7(reset sau dot pha)");
            player.sendMessage("§7    Dao tam: §a100%");
            player.sendMessage("§7    Noi thuong: §a0%");
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // Update UI - su dung UIContext thay vi CoreContext.getInstance()
            uiContext.updateAllUI(player);
            return;
        }
        
        // THAT BAI!
        handleFailure(player, result, profile);
    }

    /**
     * Xu ly that bai
     */
    private void handleFailure(Player player, BreakthroughResult result, PlayerProfile profile) {
        // NOI THUONG (3 muc do - co the chua duoc)
        if (result.isInternalInjury()) {
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§c§l    ❌ DOT PHA THAT BAI - NOI THUONG!");
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage("§7" + result.getMessage());
            player.sendMessage("");
            player.sendMessage("§c⚠ Noi thuong: §e" + String.format("%.1f%%", profile.getInnerInjury()));
            player.sendMessage("§c⚠ Dao tam: §e" + String.format("%.1f%%", profile.getDaoHeart()));
            player.sendMessage("§c⚠ Tu vi bi tieu hao: §7" + profile.getCultivation());
            player.sendMessage("");
            player.sendMessage("§7Can dan duoc hoac be quan de chua tri.");
            player.sendMessage("§7Thoi gian hoi phuc: §e30 phut");
            player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // Sound va particle that bai
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
            spawnFailureParticles(player.getLocation());
            return;
        }
        
        // TAN PHE (3 muc do - RAT KHO CHUA)
        if (result.isCrippled()) {
            player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§4§l    ☠ CAN CO BI TON - TAO HOA NHAP MA!");
            player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage("§c" + result.getMessage());
            player.sendMessage("");
            player.sendMessage("§4§l⚠ MAT HET TU VI!");
            player.sendMessage("§c⚠ Noi thuong cuc nang: §4" + String.format("%.1f%%", profile.getInnerInjury()));
            player.sendMessage("§c⚠ Dao tam sup do: §4" + String.format("%.1f%%", profile.getDaoHeart()));
            player.sendMessage("§c⚠ Am nghiep: §5" + profile.getKarmaPoints());
            player.sendMessage("");
            player.sendMessage("§7Can dan cuc pham hoac bi phap dac biet.");
            player.sendMessage("§4Thoi gian hoi phuc: §c7 NGAY");
            player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            // Explosion + wither effect
            player.getWorld().createExplosion(player.getLocation(), 0F, false, false);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
            spawnCrippledParticles(player.getLocation());
            return;
        }
        
        // CHET
        if (result == BreakthroughResult.DEATH) {
            player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§4§l    ☠☠☠ TU SI TU VONG ☠☠☠");
            player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage("§7Dot pha cuong ep that bai...");
            player.sendMessage("§7Linh hon tan ra, tieu tan trong thien dia...");
            player.sendMessage("");
            player.sendMessage("§4§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
            player.setHealth(0); // Chet that
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);
            return;
        }
    }
    
    /**
     * Particle khi that bai noi thuong
     */
    private void spawnFailureParticles(org.bukkit.Location loc) {
        for (int i = 0; i < 30; i++) {
            loc.getWorld().spawnParticle(
                org.bukkit.Particle.SMOKE_LARGE,
                loc.clone().add(Math.random() - 0.5, Math.random(), Math.random() - 0.5),
                1, 0, 0, 0, 0
            );
        }
    }
    
    /**
     * Particle khi tan phe
     */
    private void spawnCrippledParticles(org.bukkit.Location loc) {
        for (int i = 0; i < 50; i++) {
            loc.getWorld().spawnParticle(
                org.bukkit.Particle.SMOKE_LARGE,
                loc.clone().add(Math.random() - 0.5, Math.random() * 2, Math.random() - 0.5),
                1, 0, 0, 0, 0
            );
            loc.getWorld().spawnParticle(
                org.bukkit.Particle.LAVA,
                loc.clone().add(Math.random() - 0.5, Math.random(), Math.random() - 0.5),
                1, 0, 0, 0, 0
            );
        }
    }
    
    /**
     * Particle effect that bai
     */
    private void playFailureEffects(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        
        // Smoke + explosion particles
        player.getWorld().spawnParticle(Particle.DUST_PLUME, loc, 50, 0.5, 0.5, 0.5, 0.05);
        player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 2);
        
        // Sound
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
    }
    
    /**
     * Hieu ung dot pha (particles + sounds)
     */
    private void playBreakthroughEffects(Player player, CultivationRealm oldRealm, CultivationRealm newRealm) {
        Location loc = player.getLocation();
        
        // Title
        player.sendTitle(
            "§6§l⚡ ĐỘT PHÁ THÀNH CÔNG ⚡",
            newRealm.toString(),
            10, 40, 20
        );
        
        // Particles (spiral len troi)
        for (int i = 0; i < 50; i++) {
            double angle = i * 0.3;
            double radius = 2.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = i * 0.1;
            
            Location particleLoc = loc.clone().add(x, y, z);
            player.getWorld().spawnParticle(
                Particle.END_ROD,
                particleLoc,
                1, 0, 0, 0, 0
            );
            player.getWorld().spawnParticle(
                Particle.FLAME,
                particleLoc,
                1, 0, 0, 0, 0
            );
        }
        
        // Flash particles
        player.getWorld().spawnParticle(
            Particle.FLASH,
            loc.clone().add(0, 1, 0),
            5, 1, 1, 1, 0
        );
        
        // Sounds
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
    }
}
