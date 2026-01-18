package hcontrol.plugin.listener;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillService;
import hcontrol.plugin.playerskill.SkillBookService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * PHASE 6 — SKILL BOOK LISTENER
 * Xử lý sự kiện right-click Skill Book để học skill
 */
public class SkillBookListener implements Listener {
    
    private final PlayerManager playerManager;
    private final PlayerSkillService skillService;
    private final SkillBookService skillBookService;
    
    public SkillBookListener(PlayerManager playerManager, 
                             PlayerSkillService skillService,
                             SkillBookService skillBookService) {
        this.playerManager = playerManager;
        this.skillService = skillService;
        this.skillBookService = skillBookService;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Chỉ xử lý right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Chỉ xử lý main hand (tránh duplicate)
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Kiểm tra có phải skill book không
        if (!skillBookService.isSkillBook(item)) {
            return;
        }
        
        // Cancel event để không mở book GUI
        event.setCancelled(true);
        
        // Lấy skill ID từ book
        String skillId = skillBookService.getSkillId(item);
        if (skillId == null) {
            player.sendMessage("§c[Lỗi] Bí kíp này bị hỏng!");
            return;
        }
        
        // Lấy profile
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return;
        }
        
        // Kiểm tra đã học chưa
        if (profile.hasLearnedSkill(skillId)) {
            player.sendMessage("§e⚠ Ngươi đã lĩnh ngộ bí kíp này rồi!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        // Lấy thông tin skill
        PlayerSkill skill = skillService.getRegistry().getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§c[Lỗi] Skill không tồn tại: " + skillId);
            return;
        }
        
        // Học skill (kiểm tra requirements trong service)
        boolean success = skillService.learnSkill(player, skillId);
        
        if (success) {
            // Consume book (giảm 1)
            item.setAmount(item.getAmount() - 1);
            
            // Effects
            playLearnEffect(player, skill);
        }
    }
    
    /**
     * Hiệu ứng khi học skill thành công
     */
    private void playLearnEffect(Player player, PlayerSkill skill) {
        // Sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 0.8f);
        
        // Particle
        player.getWorld().spawnParticle(
            org.bukkit.Particle.ENCHANTMENT_TABLE,
            player.getLocation().add(0, 1, 0),
            50, 0.5, 0.5, 0.5, 0.5
        );
        
        // Title
        player.sendTitle(
            "§a§l✦ LĨNH NGỘ ✦",
            "§7Đã học: " + skill.getDisplayName(),
            10, 40, 20
        );
        
        // Chat message (đã được gửi trong learnSkill)
        player.sendMessage("");
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§a§l  ✦ LĨNH NGỘ BÍ KÍP ✦");
        player.sendMessage("");
        player.sendMessage("§7  Ngươi đã lĩnh ngộ được:");
        player.sendMessage("§e  " + skill.getDisplayName());
        player.sendMessage("");
        player.sendMessage("§7  Dùng §e/skill §7để xem chi tiết");
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
}
