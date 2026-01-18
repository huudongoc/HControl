package hcontrol.plugin.listener;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkillService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Map;

/**
 * PHASE 6 — PLAYER SKILL HOTBAR LISTENER
 * Bắt sự kiện nhấn phím F (swap hand) để kích hoạt skill từ hotbar
 * 
 * CÁCH SỬ DỤNG:
 * 1. /skill bind <skillId> <slot> - Bind skill vào slot 1-9
 * 2. Chọn slot đó trong hotbar
 * 3. Nhấn phím F → Kích hoạt skill
 */
public class PlayerSkillHotbarListener implements Listener {
    
    private final PlayerManager playerManager;
    private final PlayerSkillService skillService;
    
    public PlayerSkillHotbarListener(PlayerManager playerManager, PlayerSkillService skillService) {
        this.playerManager = playerManager;
        this.skillService = skillService;
    }
    
    /**
     * Bắt sự kiện nhấn phím F (swap hand items)
     * Nếu slot hiện tại có skill đã bind → Cast skill thay vì swap hand
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        if (profile == null) {
            return; // Profile chưa load
        }
        
        // Lấy slot hiện tại (0-8 trong Bukkit API, nhưng hiển thị là 1-9)
        int currentSlot = player.getInventory().getHeldItemSlot() + 1; // Convert 0-8 → 1-9
        
        // Kiểm tra xem slot này có skill đã bind không
        Map<Integer, String> hotbar = profile.getSkillHotbar();
        String skillId = hotbar.get(currentSlot);
        
        if (skillId == null) {
            // Không có skill ở slot này → Cho phép swap hand bình thường
            return;
        }
        
        // Có skill đã bind → Cancel swap hand và cast skill
        event.setCancelled(true);
        
        // Cast skill
        boolean success = skillService.castSkill(player, skillId);
        
        // Nếu thành công → Hiển thị feedback đơn giản
        if (success) {
            // Feedback đã được xử lý trong skillService.castSkill()
        } else {
            // Lỗi đã được hiển thị trong skillService.castSkill()
        }
    }
}
