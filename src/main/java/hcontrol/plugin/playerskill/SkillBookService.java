package hcontrol.plugin.playerskill;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 6 — SKILL BOOK SERVICE
 * Tạo và quản lý Skill Book items
 * 
 * Skill Book là item đặc biệt:
 * - Right-click để học skill
 * - Có độ hiếm (rarity)
 * - Drop từ mob/boss hoặc craft
 */
public class SkillBookService {
    
    private final Plugin plugin;
    private final PlayerSkillRegistry skillRegistry;
    private final NamespacedKey skillIdKey;
    
    // Rarity colors
    public enum BookRarity {
        COMMON("§f", "Thường", 1),
        UNCOMMON("§a", "Không Thường", 2),
        RARE("§9", "Hiếm", 3),
        EPIC("§5", "Sử Thi", 4),
        LEGENDARY("§6", "Huyền Thoại", 5),
        MYTHIC("§c", "Thần Thoại", 6);
        
        private final String color;
        private final String displayName;
        private final int stars;
        
        BookRarity(String color, String displayName, int stars) {
            this.color = color;
            this.displayName = displayName;
            this.stars = stars;
        }
        
        public String getColor() { return color; }
        public String getDisplayName() { return displayName; }
        public int getStars() { return stars; }
        
        public String getStarsDisplay() {
            return "§e" + "★".repeat(stars) + "§7" + "☆".repeat(6 - stars);
        }
    }
    
    public SkillBookService(Plugin plugin, PlayerSkillRegistry skillRegistry) {
        this.plugin = plugin;
        this.skillRegistry = skillRegistry;
        this.skillIdKey = new NamespacedKey(plugin, "skill_book_id");
    }
    
    /**
     * Tạo Skill Book item
     * @param skillId ID của skill
     * @param rarity Độ hiếm của sách
     * @return ItemStack skill book, null nếu skill không tồn tại
     */
    public ItemStack createSkillBook(String skillId, BookRarity rarity) {
        PlayerSkill skill = skillRegistry.getSkill(skillId);
        if (skill == null) {
            return null;
        }
        
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        
        if (meta == null) return null;
        
        // Set display name
        String title = rarity.getColor() + "§l【Bí Kíp】 " + skill.getDisplayName();
        meta.setDisplayName(title);
        
        // Set lore
        List<String> lore = new ArrayList<>();
        lore.add("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("");
        lore.add("§7Độ hiếm: " + rarity.getStarsDisplay());
        lore.add("§7Phẩm cấp: " + rarity.getColor() + rarity.getDisplayName());
        lore.add("");
        
        // Skill description
        lore.add("§e§lNội dung:");
        for (String line : skill.getDescription()) {
            lore.add("§7  " + line);
        }
        lore.add("");
        
        // Skill stats
        lore.add("§b§lThông tin:");
        lore.add("§7  Loại: §f" + skill.getType().name());
        lore.add("§7  Tiêu hao: §b" + (int) skill.getCost().getLingQi() + " Linh Khí");
        lore.add("§7  Hồi chiêu: §f" + skill.getCooldown() + "s");
        lore.add("§7  Sát thương: §c" + String.format("%.0f%%", skill.getDamageMultiplier() * 100));
        lore.add("");
        
        // Requirements
        lore.add("§c§lYêu cầu:");
        lore.add("§7  Cảnh giới: §f" + skill.getMinRealm().getDisplayName());
        lore.add("§7  Level: §f" + skill.getMinLevel());
        lore.add("");
        
        lore.add("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        lore.add("");
        lore.add("§a§l▶ Click chuột phải để học");
        lore.add("§7§o(Sách sẽ biến mất sau khi học)");
        
        meta.setLore(lore);
        
        // Add enchant glow effect
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        
        // Store skill ID in persistent data
        meta.getPersistentDataContainer().set(skillIdKey, PersistentDataType.STRING, skillId);
        
        book.setItemMeta(meta);
        return book;
    }
    
    /**
     * Tạo Skill Book với rarity tự động dựa trên skill
     */
    public ItemStack createSkillBook(String skillId) {
        PlayerSkill skill = skillRegistry.getSkill(skillId);
        if (skill == null) return null;
        
        // Auto determine rarity based on skill requirements
        BookRarity rarity = determineRarity(skill);
        return createSkillBook(skillId, rarity);
    }
    
    /**
     * Xác định rarity dựa trên skill
     */
    private BookRarity determineRarity(PlayerSkill skill) {
        int realmOrdinal = skill.getMinRealm().ordinal();
        
        if (realmOrdinal >= 8) return BookRarity.MYTHIC;      // Hóa Thần+
        if (realmOrdinal >= 6) return BookRarity.LEGENDARY;   // Nguyên Anh+
        if (realmOrdinal >= 4) return BookRarity.EPIC;        // Kim Đan+
        if (realmOrdinal >= 2) return BookRarity.RARE;        // Trúc Cơ+
        if (realmOrdinal >= 1) return BookRarity.UNCOMMON;    // Luyện Khí+
        return BookRarity.COMMON;                              // Phàm Nhân
    }
    
    /**
     * Kiểm tra item có phải là Skill Book không
     */
    public boolean isSkillBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        return meta.getPersistentDataContainer().has(skillIdKey, PersistentDataType.STRING);
    }
    
    /**
     * Lấy skill ID từ Skill Book
     */
    public String getSkillId(ItemStack item) {
        if (!isSkillBook(item)) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        return meta.getPersistentDataContainer().get(skillIdKey, PersistentDataType.STRING);
    }
    
    /**
     * Lấy NamespacedKey (để dùng trong listener)
     */
    public NamespacedKey getSkillIdKey() {
        return skillIdKey;
    }
}
