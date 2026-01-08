package hcontrol.plugin.ui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import hcontrol.plugin.ui.*;
import hcontrol.plugin.player.BreakthroughService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.model.BreakthroughResult;

/**
 * TRIBULATION INPUT LISTENER (DO KIEP)
 * Bat phim F/Q khi dang o trang thai tribulation confirm
 * Do kiep = len dai canh gioi (MORTAL -> QI_REFINING, etc.)
 */
public class TribulationInputListener implements Listener {

    private final UiStateService uiStateService;
    private final TribulationUI tribulationUI;
    private final BreakthroughService breakthroughService;
    private final PlayerManager playerManager;

    public TribulationInputListener(
        UiStateService uiStateService,
        TribulationUI tribulationUI,
        BreakthroughService breakthroughService,
        PlayerManager playerManager
    ) {
        this.uiStateService = uiStateService;
        this.tribulationUI = tribulationUI;
        this.breakthroughService = breakthroughService;
        this.playerManager = playerManager;
    }

    /**
     * F (Swap hand) = Xac nhan dot pha
     */
    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();

        if (uiStateService.getState(p.getUniqueId()) != UiState.TRIBULATION_CONFIRM) return;

        e.setCancelled(true);

        PlayerProfile profile = playerManager.get(p.getUniqueId());
        if (profile == null) return;

        // Check dieu kien dot pha
        BreakthroughResult checkResult = breakthroughService.checkConditions(profile);
        if (checkResult != null) {
            // Khong du dieu kien
            tribulationUI.cancelTribulation(p);
            p.sendMessage("§c✖ " + getResultMessage(checkResult));
            return;
        }

        // Bat dau thien kiep
        tribulationUI.startTribulation(p, () -> {
            // Xu ly ket qua dot pha
            BreakthroughResult result = breakthroughService.attemptBreakthrough(profile);
            
            switch (result) {
                case SUCCESS -> {
                    p.sendMessage("§a§l✔ ĐỘ KIẾP THÀNH CÔNG!");
                    p.sendMessage("§7Ngươi đã tiến vào §e" + profile.getRealm().getDisplayName());
                    p.sendTitle("§6§l⚡ ĐỘ KIẾP THÀNH CÔNG ⚡", "§e" + profile.getRealm().getDisplayName(), 10, 60, 20);
                }
                case INTERNAL_INJURY_MINOR, INTERNAL_INJURY_MODERATE, INTERNAL_INJURY_SEVERE -> {
                    p.sendMessage("§c✖ Độ kiếp thất bại - Nội thương!");
                    p.sendTitle("§c§l✖ THẤT BẠI", "§7Thiên kiếp quá mạnh...", 10, 60, 20);
                }
                case CRIPPLED_LIGHT, CRIPPLED_SEVERE, CRIPPLED_PERMANENT -> {
                    p.sendMessage("§4§l✖ Độ kiếp thất bại - TÀN PHẾ!");
                    p.sendTitle("§4§l✖ TÀN PHẾ", "§7Căn cơ bị thiên kiếp phá hủy...", 10, 60, 20);
                }
                case DEATH -> {
                    p.sendMessage("§4§l☠ Độ kiếp thất bại - HỒN PHI PHÁCH TÁN!");
                    p.setHealth(0);
                }
                default -> {
                    p.sendMessage("§c✖ Độ kiếp thất bại!");
                }
            }
        });
    }

    /**
     * Q (Drop) = Huy dot pha
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if (uiStateService.getState(p.getUniqueId()) != UiState.TRIBULATION_CONFIRM) return;

        e.setCancelled(true);
        tribulationUI.cancelTribulation(p);
    }
    
    /**
     * Auto clear state khi quit
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        uiStateService.clear(e.getPlayer().getUniqueId());
    }
    
    private String getResultMessage(BreakthroughResult result) {
        return switch (result) {
            case INSUFFICIENT_CULTIVATION -> "Chưa đủ tu vi hoặc level!";
            case ON_COOLDOWN -> "Đang trong thời gian chờ!";
            default -> "Không đủ điều kiện!";
        };
    }
}
