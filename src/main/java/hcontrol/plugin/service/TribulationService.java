package hcontrol.plugin.service;

import hcontrol.plugin.Main;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.model.BreakthroughResult;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.tribulation.TribulationTask;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * TRIBULATION SERVICE
 * Quan ly logic thien kiep (tribulation)
 */
public class TribulationService {

    private final Main plugin;
    private final BreakthroughService breakthroughService;

    public TribulationService(Main plugin, BreakthroughService breakthroughService) {
        this.plugin = plugin;
        this.breakthroughService = breakthroughService;
    }

    /**
     * Bat dau thien kiep (4 phase animation)
     * @param player nguoi choi
     * @param profile profile cua player
     * @param isForced co force khong (bypass dieu kien)
     * @param onComplete callback khi ket thuc (success, result)
     */
    public void startTribulation(Player player, PlayerProfile profile, boolean isForced, 
                                  Consumer<BreakthroughResult> onComplete) {
        
        // Thong bao bat dau
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l    ⚡ THIÊN KIẾP GIÁNG LÂM ⚡");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        player.sendMessage("§7Thiên địa cảm ứng...");
        player.sendMessage("§7Linh khí hội tụ...");
        player.sendMessage("§c⚡ Thiên lôi sắp giáng! ⚡");
        player.sendMessage("");

        // LAY REALM INFO
        CultivationRealm fromRealm = profile.getRealm();
        CultivationRealm toRealm = CultivationRealm.values()[fromRealm.ordinal() + 1]; // realm tiep theo

        // BAT DAU TRIBULATION TASK (REFACTORED)
        new TribulationTask(plugin, player, fromRealm, toRealm, (success) -> {
            // CALLBACK KHI KET THUC THIEN KIEP
            if (success) {
                // THANH CONG THIEN KIEP - BREAKTHROUGH PHẢI THÀNH CÔNG 100%
                // Không roll random nữa vì đã vượt qua thiên kiếp
                BreakthroughResult result = breakthroughService.attemptBreakthroughAfterTribulation(profile);
                
                if (onComplete != null) {
                    onComplete.accept(result);
                }
            } else {
                // THAT BAI THIEN KIEP
                handleTribulationFailure(player, profile);
                
                if (onComplete != null) {
                    onComplete.accept(BreakthroughResult.TRIBULATION_FAILED);
                }
            }
        }).runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Xu ly that bai thien kiep
     */
    private void handleTribulationFailure(Player player, PlayerProfile profile) {
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§c§l    ☠ THIÊN KIẾP THẤT BẠI ☠");
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        player.sendMessage("§7Linh hồn bị thiên lôi tổn thương...");
        player.sendMessage("§cNội thương cực nặng!");
        player.sendMessage("");

        // Ap dung noi thuong
        profile.addInnerInjury(50.0);
        profile.setDaoHeart(Math.max(0, profile.getDaoHeart() - 30.0));
    }
}
