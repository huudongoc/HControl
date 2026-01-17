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
        renderAllSkills(inv, player, profile);  // Hiển thị TẤT CẢ skills
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
        
        // Slot 0: All Skills label
        ItemStack allSkillsLabel = createItem(Material.ENCHANTED_BOOK,
            "§6§lTẤT CẢ SKILLS",
            "§a● Xanh: §7Đã học",
            "§e● Vàng: §7Có thể học",
            "§c● Đỏ: §7Chưa đủ điều kiện",
            "",
            "§7Click để học/sử dụng"
        );
        inv.setItem(9, allSkillsLabel);
        
        // Slot 45: Hotbar label
        ItemStack hotbarLabel = createItem(Material.BLAZE_ROD,
            "§6§lHotbar Bindings",
            "§7Skill đã gán vào phím 1-9",
            "§7Nhấn F để kích hoạt"
        );
        inv.setItem(45, hotbarLabel);
    }
    
    /**
     * Row 2-5: TẤT CẢ Skills (thay vì chỉ learned + available)
     * Hiển thị với màu khác nhau theo trạng thái
     */
    private void renderAllSkills(Inventory inv, Player player, PlayerProfile profile) {
        List<PlayerSkill> allSkills = registry.getAllSkills();
        
        int startSlot = 10; // Bắt đầu từ slot 10
        int slotIndex = 0;
        
        for (PlayerSkill skill : allSkills) {
            if (slotIndex >= 28) break; // Max 28 skills (4 rows x 7 columns)
            
            // Xác định trạng thái skill
            SkillStatus status = getSkillStatus(player, profile, skill);
            
            // Check cooldown nếu đã học
            boolean onCooldown = false;
            long remainingCD = 0;
            if (status == SkillStatus.LEARNED) {
                onCooldown = skillService.isOnCooldown(player.getUniqueId(), skill.getSkillId());
                remainingCD = skillService.getRemainingCooldown(player.getUniqueId(), skill.getSkillId());
            }
            
            // Tạo item với màu theo trạng thái
            ItemStack item = createSkillItemWithStatus(skill, status, onCooldown, remainingCD, profile);
            
            // Tính slot (skip cột 0 và 8)
            int row = slotIndex / 7;
            int col = slotIndex % 7 + 1; // Cột 1-7
            int slot = (row + 1) * 9 + col; // Row 1-4 (slot 10-17, 19-26, 28-35, 37-44)
            
            inv.setItem(slot, item);
            slotIndex++;
        }
    }
    
    /**
     * Enum trạng thái skill
     */
    private enum SkillStatus {
        LEARNED,      // Đã học (xanh)
        AVAILABLE,    // Có thể học (vàng)
        LOCKED        // Chưa đủ điều kiện (đỏ)
    }
    
    /**
     * Xác định trạng thái của skill
     * 
     * Logic level check:
     * - Nếu player realm > skill required realm → bỏ qua check level
     * - Nếu player realm == skill required realm → check level
     * - Nếu player realm < skill required realm → LOCKED
     */
    private SkillStatus getSkillStatus(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Đã học
        if (profile.hasLearnedSkill(skill.getSkillId())) {
            return SkillStatus.LEARNED;
        }
        
        int playerRealmOrd = profile.getRealm().ordinal();
        int skillRealmOrd = skill.getMinRealm().ordinal();
        
        // Player cảnh giới thấp hơn → LOCKED
        if (playerRealmOrd < skillRealmOrd) {
            return SkillStatus.LOCKED;
        }
        
        // Player cảnh giới CAO HƠN → AVAILABLE (bỏ qua level check)
        if (playerRealmOrd > skillRealmOrd) {
            return SkillStatus.AVAILABLE;
        }
        
        // Player CÙNG cảnh giới → check level
        if (profile.getRealmLevel() >= skill.getMinLevel()) {
            return SkillStatus.AVAILABLE;
        }
        
        return SkillStatus.LOCKED;
    }
    
    /**
     * Tạo skill item với màu theo trạng thái
     */
    private ItemStack createSkillItemWithStatus(PlayerSkill skill, SkillStatus status,
                                                 boolean onCooldown, long remainingCD,
                                                 PlayerProfile profile) {
        Material mat = getSkillMaterial(skill);
        
        // Thay đổi material theo trạng thái
        if (status == SkillStatus.LOCKED) {
            mat = Material.BARRIER; // Khóa
        } else if (onCooldown) {
            mat = Material.GRAY_DYE; // Cooldown
        }
        
        List<String> lore = new ArrayList<>();
        
        // Trạng thái badge
        String statusBadge = switch (status) {
            case LEARNED -> "§a§l✓ ĐÃ HỌC";
            case AVAILABLE -> "§e§l○ CÓ THỂ HỌC";
            case LOCKED -> "§c§l✗ CHƯA ĐỦ ĐIỀU KIỆN";
        };
        lore.add(statusBadge);
        lore.add("");
        
        // Description
        for (String line : skill.getDescription()) {
            lore.add("§7" + line);
        }
        lore.add("");
        
        // Stats
        lore.add("§eLoại: §f" + skill.getType().name());
        lore.add("§eCost: §b" + (int) skill.getCost().getLingQi() + " Linh Khí");
        lore.add("§eCooldown: §f" + skill.getCooldown() + "s");
        if (skill.getDamageMultiplier() > 0) {
            lore.add("§eDamage: §c" + String.format("%.0f%%", skill.getDamageMultiplier() * 100));
        }
        if (skill.getRange() > 0) {
            lore.add("§eRange: §f" + String.format("%.1f", skill.getRange()) + " blocks");
        }
        lore.add("");
        
        // Requirements - Logic màu đúng
        int playerRealmOrd = profile.getRealm().ordinal();
        int skillRealmOrd = skill.getMinRealm().ordinal();
        
        String realmColor = (playerRealmOrd >= skillRealmOrd) ? "§a" : "§c";
        
        // Level color: xanh nếu realm cao hơn HOẶC (cùng realm VÀ đủ level)
        String levelColor;
        if (playerRealmOrd > skillRealmOrd) {
            levelColor = "§a"; // Cảnh giới cao hơn → tự động đạt
        } else if (playerRealmOrd == skillRealmOrd && profile.getRealmLevel() >= skill.getMinLevel()) {
            levelColor = "§a"; // Cùng cảnh giới và đủ level
        } else {
            levelColor = "§c"; // Chưa đạt
        }
        
        lore.add("§7Yêu cầu:");
        lore.add("  " + realmColor + "▸ Cảnh giới: §f" + skill.getMinRealm().getDisplayName());
        lore.add("  " + levelColor + "▸ Level: §f" + skill.getMinLevel());
        lore.add("");
        
        // Actions based on status
        switch (status) {
            case LEARNED -> {
                if (onCooldown) {
                    lore.add("§c⏳ Cooldown: " + (remainingCD / 1000) + "s");
                } else {
                    lore.add("§a▶ Click để sử dụng");
                }
                lore.add("§e⚡ Shift+Click để bind hotbar");
            }
            case AVAILABLE -> {
                lore.add("§a📚 Click để học skill này");
            }
            case LOCKED -> {
                lore.add("§c🔒 Nâng cảnh giới/level để mở khóa");
            }
        }
        
        // Title color based on status
        String titleColor = switch (status) {
            case LEARNED -> onCooldown ? "§7" : "§a";
            case AVAILABLE -> "§e";
            case LOCKED -> "§c";
        };
        String title = titleColor + skill.getDisplayName();
        
        return createItem(mat, title, lore.toArray(new String[0]));
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
     * Lấy skill ID từ slot (cho tất cả skills)
     */
    public String getSkillIdFromSlot(Inventory inv, int slot, Player player, PlayerProfile profile) {
        // All skills area: slot 10-17, 19-26, 28-35, 37-44
        if (!isSkillSlot(slot)) {
            return null;
        }
        
        List<PlayerSkill> allSkills = registry.getAllSkills();
        int index = calculateSkillIndex(slot);
        
        if (index >= 0 && index < allSkills.size()) {
            return allSkills.get(index).getSkillId();
        }
        
        return null;
    }
    
    /**
     * Tính index skill từ slot
     */
    private int calculateSkillIndex(int slot) {
        // Tính row (0-3) và col (1-7)
        int row = (slot / 9) - 1;  // Row 1-4 → 0-3
        int col = (slot % 9) - 1;  // Col 1-7 → 0-6
        
        if (col < 0 || col > 6) return -1; // Không phải cột skill
        if (row < 0 || row > 3) return -1; // Không phải hàng skill
        
        return row * 7 + col;
    }
    
    /**
     * Kiểm tra slot có phải là skill area không (tất cả skills)
     */
    public boolean isSkillSlot(int slot) {
        // Rows 1-4, columns 1-7
        int row = slot / 9;
        int col = slot % 9;
        
        return row >= 1 && row <= 4 && col >= 1 && col <= 7;
    }
    
    /**
     * Kiểm tra slot có phải là learned skill area không (deprecated - dùng isSkillSlot)
     */
    public boolean isLearnedSkillSlot(int slot) {
        return isSkillSlot(slot);
    }
    
    /**
     * Kiểm tra slot có phải là available skill area không (deprecated - dùng isSkillSlot)
     */
    public boolean isAvailableSkillSlot(int slot) {
        return isSkillSlot(slot);
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
