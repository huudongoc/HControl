package hcontrol.plugin.ui.skill;

import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillRegistry;
import hcontrol.plugin.playerskill.PlayerSkillService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PHASE 6 — SKILL MENU GUI
 * Inventory GUI để hiển thị và quản lý skills
 * 
 * Layout (54 slots - Double Chest):
 * Row 1: Header + Info
 * Row 2-3: Learned Skills (click để cast, shift+click để bind)
 * Row 4-5: Available Skills (click để learn)
 * Row 6: Hotbar Bindings (1-9)
 */
public class SkillMenuGUI {
    
    public static final String MENU_TITLE = "§6§l⚔ SKILL MENU ⚔";
    public static final int MENU_SIZE = 54; // 6 rows
    
    private final PlayerSkillService skillService;
    private final PlayerSkillRegistry registry;
    
    public SkillMenuGUI(PlayerSkillService skillService) {
        this.skillService = skillService;
        this.registry = skillService.getRegistry();
    }
    
    /**
     * Mở menu cho player
     */
    public void openMenu(Player player, PlayerProfile profile) {
        Inventory inv = Bukkit.createInventory(null, MENU_SIZE, MENU_TITLE);
        
        // Render các phần của menu
        renderHeader(inv, profile);
        renderLearnedSkills(inv, player, profile);
        renderAvailableSkills(inv, player, profile);
        renderHotbar(inv, profile);
        renderDecoration(inv);
        
        player.openInventory(inv);
    }
    
    // ========== RENDER SECTIONS ==========
    
    /**
     * Row 1: Header với info player
     */
    private void renderHeader(Inventory inv, PlayerProfile profile) {
        // Slot 4: Player Info
        ItemStack info = createItem(Material.PLAYER_HEAD, 
            "§e§l" + profile.getRealm().getDisplayName(),
            "§7Level: §f" + profile.getRealmLevel(),
            "§7Môn phái: §f" + profile.getIdentity().getSect().getDisplayName(),
            "",
            "§bLinh Khí: §f" + (int) profile.getCurrentLingQi() + "/" + profile.getMaxLingQi(),
            "",
            "§7§oClick vào skill để xem thông tin",
            "§7§oShift+Click để bind vào hotbar"
        );
        inv.setItem(4, info);
        
        // Slot 0: Learned Skills label
        ItemStack learnedLabel = createItem(Material.BOOK,
            "§a§lSkills Đã Học",
            "§7Click để sử dụng skill",
            "§7Shift+Click để bind hotbar"
        );
        inv.setItem(9, learnedLabel);
        
        // Slot 27: Available Skills label
        ItemStack availableLabel = createItem(Material.WRITABLE_BOOK,
            "§e§lSkills Có Thể Học",
            "§7Click để học skill mới"
        );
        inv.setItem(27, availableLabel);
        
        // Slot 45: Hotbar label
        ItemStack hotbarLabel = createItem(Material.BLAZE_ROD,
            "§6§lHotbar Bindings",
            "§7Skill đã gán vào phím 1-9",
            "§7Nhấn F để kích hoạt"
        );
        inv.setItem(45, hotbarLabel);
    }
    
    /**
     * Row 2-3: Learned Skills
     */
    private void renderLearnedSkills(Inventory inv, Player player, PlayerProfile profile) {
        List<PlayerSkill> learnedSkills = skillService.getLearnedSkills(player);
        
        int startSlot = 10; // Bắt đầu từ slot 10
        int maxSlots = 16;  // 16 slots cho learned skills (2 rows - label)
        
        for (int i = 0; i < Math.min(learnedSkills.size(), maxSlots); i++) {
            PlayerSkill skill = learnedSkills.get(i);
            
            // Check cooldown
            boolean onCooldown = skillService.isOnCooldown(player.getUniqueId(), skill.getSkillId());
            long remainingCD = skillService.getRemainingCooldown(player.getUniqueId(), skill.getSkillId());
            
            ItemStack item = createSkillItem(skill, true, onCooldown, remainingCD, profile);
            
            int slot = startSlot + i;
            // Skip sang hàng tiếp nếu đến cuối
            if ((slot - 9) % 9 == 8) {
                slot += 2; // Skip 2 slots (cuối hàng + đầu hàng tiếp)
            }
            
            inv.setItem(slot, item);
        }
    }
    
    /**
     * Row 4-5: Available Skills
     */
    private void renderAvailableSkills(Inventory inv, Player player, PlayerProfile profile) {
        List<PlayerSkill> availableSkills = skillService.getAvailableSkills(player);
        
        int startSlot = 28; // Bắt đầu từ slot 28
        int maxSlots = 16;  // 16 slots
        
        for (int i = 0; i < Math.min(availableSkills.size(), maxSlots); i++) {
            PlayerSkill skill = availableSkills.get(i);
            ItemStack item = createSkillItem(skill, false, false, 0, profile);
            
            int slot = startSlot + i;
            if ((slot - 27) % 9 == 8) {
                slot += 2;
            }
            
            inv.setItem(slot, item);
        }
    }
    
    /**
     * Row 6: Hotbar Bindings (slot 1-9)
     */
    private void renderHotbar(Inventory inv, PlayerProfile profile) {
        Map<Integer, String> hotbar = profile.getSkillHotbar();
        
        for (int i = 1; i <= 9; i++) {
            String skillId = hotbar.get(i);
            int slot = 44 + i; // Slot 45-53 (0-indexed)
            
            if (skillId != null) {
                PlayerSkill skill = registry.getSkill(skillId);
                if (skill != null) {
                    ItemStack item = createItem(getSkillMaterial(skill),
                        "§6[" + i + "] §f" + skill.getDisplayName(),
                        "§7" + skill.getDescription().get(0),
                        "",
                        "§eClick để gỡ khỏi hotbar"
                    );
                    inv.setItem(slot, item);
                    continue;
                }
            }
            
            // Empty slot
            ItemStack empty = createItem(Material.GRAY_STAINED_GLASS_PANE,
                "§7[" + i + "] Trống",
                "§7Shift+Click skill để bind"
            );
            inv.setItem(slot, empty);
        }
    }
    
    /**
     * Decoration - borders
     */
    private void renderDecoration(Inventory inv) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        
        // Column 0 và 8 (trừ label slots)
        for (int row = 0; row < 6; row++) {
            int leftSlot = row * 9;
            int rightSlot = row * 9 + 8;
            
            // Skip label slots
            if (leftSlot != 9 && leftSlot != 27 && leftSlot != 45) {
                if (inv.getItem(leftSlot) == null) inv.setItem(leftSlot, border);
            }
            if (inv.getItem(rightSlot) == null) inv.setItem(rightSlot, border);
        }
        
        // Row 0 (header row)
        for (int i = 0; i < 9; i++) {
            if (i != 4 && inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Tạo skill item với đầy đủ thông tin
     */
    private ItemStack createSkillItem(PlayerSkill skill, boolean learned, 
                                       boolean onCooldown, long remainingCD,
                                       PlayerProfile profile) {
        Material mat = getSkillMaterial(skill);
        
        // Nếu cooldown → dùng màu xám
        if (onCooldown) {
            mat = Material.GRAY_DYE;
        }
        
        List<String> lore = new ArrayList<>();
        
        // Description
        for (String line : skill.getDescription()) {
            lore.add("§7" + line);
        }
        lore.add("");
        
        // Stats
        lore.add("§eLoại: §f" + skill.getType().name());
        lore.add("§eCost: §b" + (int) skill.getCost().getLingQi() + " Linh Khí");
        lore.add("§eCooldown: §f" + skill.getCooldown() + "s");
        lore.add("§eDamage: §c" + String.format("%.0f%%", skill.getDamageMultiplier() * 100));
        lore.add("§eRange: §f" + String.format("%.1f", skill.getRange()) + " blocks");
        lore.add("");
        
        // Requirements
        lore.add("§7Yêu cầu: §f" + skill.getMinRealm().getDisplayName() + " Lv" + skill.getMinLevel());
        lore.add("");
        
        // Actions
        if (learned) {
            if (onCooldown) {
                lore.add("§c⏳ Cooldown: " + (remainingCD / 1000) + "s");
            } else {
                lore.add("§a▶ Click để sử dụng");
            }
            lore.add("§e⚡ Shift+Click để bind hotbar");
        } else {
            lore.add("§a📚 Click để học skill này");
        }
        
        // Title color based on state
        String titleColor = learned ? (onCooldown ? "§7" : "§a") : "§e";
        String title = titleColor + skill.getDisplayName();
        if (learned) title += " §a✓";
        
        return createItem(mat, title, lore.toArray(new String[0]));
    }
    
    /**
     * Material cho từng loại skill
     */
    private Material getSkillMaterial(PlayerSkill skill) {
        return switch (skill.getType()) {
            case MELEE -> Material.IRON_SWORD;
            case RANGED -> Material.BOW;
            case AOE -> Material.TNT;
            case BUFF -> Material.GOLDEN_APPLE;
            case DEBUFF -> Material.POISONOUS_POTATO;
            case HEAL -> Material.GLISTERING_MELON_SLICE;
            case TELEPORT -> Material.ENDER_PEARL;
            case SUMMON -> Material.ZOMBIE_SPAWN_EGG;
        };
    }
    
    /**
     * Helper tạo ItemStack
     */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(List.of(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Lấy skill ID từ item (nếu có)
     */
    public String getSkillIdFromSlot(Inventory inv, int slot, Player player, PlayerProfile profile) {
        // Learned skills: slot 10-17, 19-26
        if ((slot >= 10 && slot <= 17) || (slot >= 19 && slot <= 26)) {
            List<PlayerSkill> learned = skillService.getLearnedSkills(player);
            int index = calculateSkillIndex(slot, 10);
            if (index >= 0 && index < learned.size()) {
                return learned.get(index).getSkillId();
            }
        }
        
        // Available skills: slot 28-35, 37-44
        if ((slot >= 28 && slot <= 35) || (slot >= 37 && slot <= 44)) {
            List<PlayerSkill> available = skillService.getAvailableSkills(player);
            int index = calculateSkillIndex(slot, 28);
            if (index >= 0 && index < available.size()) {
                return available.get(index).getSkillId();
            }
        }
        
        return null;
    }
    
    /**
     * Tính index skill từ slot
     */
    private int calculateSkillIndex(int slot, int startSlot) {
        int row = (slot - startSlot) / 9;
        int col = (slot - startSlot) % 9;
        if (col > 7) return -1; // Skip slot 8
        return row * 7 + col;
    }
    
    /**
     * Kiểm tra slot có phải là learned skill area không
     */
    public boolean isLearnedSkillSlot(int slot) {
        return (slot >= 10 && slot <= 17) || (slot >= 19 && slot <= 26);
    }
    
    /**
     * Kiểm tra slot có phải là available skill area không
     */
    public boolean isAvailableSkillSlot(int slot) {
        return (slot >= 28 && slot <= 35) || (slot >= 37 && slot <= 44);
    }
    
    /**
     * Kiểm tra slot có phải là hotbar slot không
     */
    public boolean isHotbarSlot(int slot) {
        return slot >= 45 && slot <= 53;
    }
    
    /**
     * Lấy hotbar number từ slot (1-9)
     */
    public int getHotbarNumber(int slot) {
        if (isHotbarSlot(slot)) {
            return slot - 44;
        }
        return -1;
    }
}
