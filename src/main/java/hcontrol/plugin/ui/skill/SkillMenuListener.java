package hcontrol.plugin.ui.skill;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkillService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

/**
 * PHASE 6 — SKILL MENU LISTENER
 * Xử lý click events trong Skill Menu GUI
 */
public class SkillMenuListener implements Listener {
    
    private final PlayerManager playerManager;
    private final PlayerSkillService skillService;
    private final SkillMenuGUI menuGUI;
    private final Plugin plugin;
    
    public SkillMenuListener(PlayerManager playerManager, PlayerSkillService skillService, 
                             SkillMenuGUI menuGUI, Plugin plugin) {
        this.playerManager = playerManager;
        this.skillService = skillService;
        this.menuGUI = menuGUI;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check nếu là Skill Menu
        if (event.getView().getTitle() == null || 
            !event.getView().getTitle().equals(SkillMenuGUI.MENU_TITLE)) {
            return;
        }
        
        // Cancel move items
        event.setCancelled(true);
        
        // Check player
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        
        int slot = event.getRawSlot();
        boolean shiftClick = event.isShiftClick();
        Inventory inv = event.getInventory();
        
        // Lấy skill ID từ slot
        String skillId = menuGUI.getSkillIdFromSlot(inv, slot, player, profile);
        
        if (skillId == null) {
            // Check nếu click vào hotbar slot để unbind
            if (menuGUI.isHotbarSlot(slot)) {
                int hotbarNum = menuGUI.getHotbarNumber(slot);
                if (hotbarNum > 0 && profile.getSkillHotbar().containsKey(hotbarNum)) {
                    skillService.unbindSkill(player, hotbarNum);
                    // Refresh menu
                    refreshMenu(player, profile);
                }
            }
            return;
        }
        
        // ===== LEARNED SKILL SLOT =====
        if (menuGUI.isLearnedSkillSlot(slot)) {
            if (shiftClick) {
                // Shift+Click → Mở dialog bind hotbar
                openBindDialog(player, skillId);
            } else {
                // Click → Cast skill
                player.closeInventory();
                skillService.castSkill(player, skillId);
            }
            return;
        }
        
        // ===== AVAILABLE SKILL SLOT =====
        if (menuGUI.isAvailableSkillSlot(slot)) {
            // Click → Learn skill
            boolean success = skillService.learnSkill(player, skillId);
            if (success) {
                // Refresh menu
                refreshMenu(player, profile);
            }
            return;
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Prevent dragging items in skill menu
        if (event.getView().getTitle() != null && 
            event.getView().getTitle().equals(SkillMenuGUI.MENU_TITLE)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Refresh menu sau khi có thay đổi
     */
    private void refreshMenu(Player player, PlayerProfile profile) {
        // Delay 1 tick để tránh conflict
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.closeInventory();
            menuGUI.openMenu(player, profile);
        }, 1L);
    }
    
    /**
     * Mở dialog để bind skill vào hotbar
     */
    private void openBindDialog(Player player, String skillId) {
        player.closeInventory();
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lBind Skill vào Hotbar");
        player.sendMessage("");
        player.sendMessage("§7Nhập slot (1-9) trong chat:");
        player.sendMessage("§e/skill bind " + skillId + " <slot>");
        player.sendMessage("");
        player.sendMessage("§7Hoặc dùng command trực tiếp.");
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
}
