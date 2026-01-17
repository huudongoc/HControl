package hcontrol.plugin.ui.skill;

import hcontrol.plugin.master.skill.SkillCreatorSession;
import hcontrol.plugin.master.skill.SkillCreatorSession.CreatorStep;
import hcontrol.plugin.skill.custom.Element;
import hcontrol.plugin.skill.custom.SkillCategory;
import hcontrol.plugin.skill.custom.SkillPointCalculator;
import hcontrol.plugin.skill.custom.TargetType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI Sáng Tạo Công Pháp
 * Multi-step wizard với validation real-time
 */
public class SkillCreatorGUI {
    
    public static final String GUI_TITLE_PREFIX = "§5⚡ Sáng Tạo Công Pháp";
    
    /**
     * Mở GUI theo step hiện tại
     */
    public static void open(Player player, SkillCreatorSession session) {
        switch (session.getCurrentStep()) {
            case CHOOSE_CATEGORY -> openCategorySelection(player, session);
            case CHOOSE_ELEMENT -> openElementSelection(player, session);
            case CHOOSE_TARGET -> openTargetSelection(player, session);
            case ADJUST_STATS -> openStatsAdjustment(player, session);
            case ADD_EFFECTS -> openEffectsEditor(player, session);
            case CONFIRM -> openConfirmation(player, session);
        }
    }
    
    // ===== STEP 1: CHOOSE CATEGORY =====
    
    public static void openCategorySelection(Player player, SkillCreatorSession session) {
        session.setCurrentStep(CreatorStep.CHOOSE_CATEGORY);
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + " §7- Loại");
        
        // Border
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        // Title info
        inv.setItem(4, createInfoItem(
            Material.ENCHANTED_BOOK, 
            "§6⚡ CHỌN LOẠI CÔNG PHÁP",
            "§7Loại công pháp quyết định",
            "§7cách thức hoạt động cơ bản",
            "",
            "§eClick để chọn loại bên dưới"
        ));
        
        // Category options
        int[] slots = {20, 21, 22, 23, 24, 30};
        SkillCategory[] categories = SkillCategory.values();
        
        for (int i = 0; i < categories.length && i < slots.length; i++) {
            SkillCategory cat = categories[i];
            boolean selected = session.getCategory() == cat;
            
            ItemStack item = createItem(
                cat.getIcon(),
                cat.getColorCode() + (selected ? "✓ " : "") + cat.getDisplayName(),
                "§7" + cat.getDescription(),
                "",
                "§7Hệ số điểm: §e" + cat.getPointMultiplier() + "x",
                "",
                selected ? "§a✓ Đã chọn" : "§eClick để chọn"
            );
            
            if (selected) {
                addGlow(item);
            }
            
            inv.setItem(slots[i], item);
        }
        
        // Point display
        inv.setItem(49, createPointDisplay(session));
        
        // Navigation
        inv.setItem(45, createItem(Material.BARRIER, "§c✗ Hủy", "§7Hủy tạo công pháp"));
        inv.setItem(53, createItem(Material.ARROW, "§a→ Tiếp theo", "§7Chọn ngũ hành"));
        
        player.openInventory(inv);
    }
    
    // ===== STEP 2: CHOOSE ELEMENT =====
    
    public static void openElementSelection(Player player, SkillCreatorSession session) {
        session.setCurrentStep(CreatorStep.CHOOSE_ELEMENT);
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + " §7- Ngũ Hành");
        
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        // Title
        inv.setItem(4, createInfoItem(
            Material.NETHER_STAR,
            "§6☯ CHỌN NGŨ HÀNH",
            "§7Ngũ hành ảnh hưởng đến",
            "§7tương sinh tương khắc",
            "",
            "§7Kim sinh Thủy, Thủy sinh Mộc",
            "§7Mộc sinh Hỏa, Hỏa sinh Thổ",
            "§7Thổ sinh Kim",
            "",
            "§eChọn hệ hoặc để trống"
        ));
        
        // No element option
        boolean noElement = session.getElement() == null;
        inv.setItem(13, createItem(
            noElement ? Material.GLASS : Material.GRAY_STAINED_GLASS,
            "§7" + (noElement ? "✓ " : "") + "Không Hệ",
            "§7Công pháp không thuộc ngũ hành",
            "§7Không bị tương sinh tương khắc",
            "",
            noElement ? "§a✓ Đã chọn" : "§eClick để chọn"
        ));
        if (noElement) addGlow(inv.getItem(13));
        
        // Element options in a circle pattern
        Element[] elements = Element.values();
        int[] slots = {20, 22, 24, 30, 32}; // KIM, MOC, THUY, HOA, THO
        Material[] materials = {
            Material.IRON_INGOT,    // Kim
            Material.OAK_LEAVES,    // Mộc
            Material.WATER_BUCKET,  // Thủy
            Material.BLAZE_POWDER,  // Hỏa
            Material.DIRT           // Thổ
        };
        
        for (int i = 0; i < elements.length && i < slots.length; i++) {
            Element el = elements[i];
            boolean selected = session.getElement() == el;
            
            ItemStack item = createItem(
                materials[i],
                el.getColorCode() + (selected ? "✓ " : "") + el.getDisplayName(),
                "§7" + el.getRelationDescription(),
                "",
                "§7Particle: §f" + el.getParticle().name(),
                "",
                selected ? "§a✓ Đã chọn" : "§eClick để chọn"
            );
            
            if (selected) addGlow(item);
            inv.setItem(slots[i], item);
        }
        
        // Point display
        inv.setItem(49, createPointDisplay(session));
        
        // Navigation
        inv.setItem(45, createItem(Material.ARROW, "§c← Quay lại", "§7Chọn loại công pháp"));
        inv.setItem(53, createItem(Material.ARROW, "§a→ Tiếp theo", "§7Chọn mục tiêu"));
        
        player.openInventory(inv);
    }
    
    // ===== STEP 3: CHOOSE TARGET =====
    
    public static void openTargetSelection(Player player, SkillCreatorSession session) {
        session.setCurrentStep(CreatorStep.CHOOSE_TARGET);
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + " §7- Mục Tiêu");
        
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        // Title
        inv.setItem(4, createInfoItem(
            Material.TARGET,
            "§6🎯 CHỌN MỤC TIÊU",
            "§7Kiểu mục tiêu quyết định",
            "§7phạm vi tác dụng của skill",
            "",
            "§eChọn loại mục tiêu"
        ));
        
        // Target options
        TargetType[] targets = TargetType.values();
        int[] slots = {20, 21, 22, 23, 24};
        Material[] materials = {
            Material.PLAYER_HEAD,   // SELF
            Material.BOW,           // SINGLE
            Material.TNT,           // AOE
            Material.ARROW,         // PROJECTILE
            Material.GRASS_BLOCK    // GROUND
        };
        
        for (int i = 0; i < targets.length && i < slots.length; i++) {
            TargetType target = targets[i];
            boolean selected = session.getTargetType() == target;
            
            ItemStack item = createItem(
                materials[i],
                (selected ? "§a✓ " : "§e") + target.getDisplayName(),
                "§7" + target.getDescription(),
                "",
                "§7Hệ số điểm: §e+" + (target.getPointMultiplier() * 20) + "%",
                "",
                selected ? "§a✓ Đã chọn" : "§eClick để chọn"
            );
            
            if (selected) addGlow(item);
            inv.setItem(slots[i], item);
        }
        
        // Point display
        inv.setItem(49, createPointDisplay(session));
        
        // Navigation
        inv.setItem(45, createItem(Material.ARROW, "§c← Quay lại", "§7Chọn ngũ hành"));
        inv.setItem(53, createItem(Material.ARROW, "§a→ Tiếp theo", "§7Điều chỉnh stats"));
        
        player.openInventory(inv);
    }
    
    // ===== STEP 4: ADJUST STATS =====
    
    public static void openStatsAdjustment(Player player, SkillCreatorSession session) {
        session.setCurrentStep(CreatorStep.ADJUST_STATS);
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + " §7- Stats");
        
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        // Title with point warning
        boolean overLimit = session.isOverLimit();
        inv.setItem(4, createInfoItem(
            overLimit ? Material.REDSTONE_BLOCK : Material.EXPERIENCE_BOTTLE,
            overLimit ? "§c⚠ VƯỢT GIỚI HẠN!" : "§6⚙ ĐIỀU CHỈNH STATS",
            overLimit ? "§cGiảm stats để tiếp tục" : "§7Tinh chỉnh sức mạnh công pháp",
            "",
            "§7Điểm: " + (overLimit ? "§c" : "§e") + session.getCurrentPoints() + "§7/" + session.getPointCap(),
            "§7Sử dụng: " + (overLimit ? "§c" : "§a") + session.getPointPercentage() + "%"
        ));
        
        // Stats with +/- buttons
        double maxPower = SkillPointCalculator.getMaxPower(session.getPlayerRealm());
        double minCd = SkillPointCalculator.getMinCooldown(session.getPlayerRealm());
        
        // Row 1: Power, Cooldown, Mana
        createStatRow(inv, 19, "§c⚔ Sát Thương", (int)session.getBasePower(), 10, (int)maxPower, "power");
        createStatRow(inv, 22, "§b⏱ Hồi Chiêu", session.getCooldown(), minCd, 30, "cooldown");
        createStatRow(inv, 25, "§d💧 Linh Khí", (int)session.getManaCost(), 10, 500, "mana");
        
        // Row 2: Range, AOE, Projectiles
        createStatRow(inv, 28, "§e📏 Tầm Xa", session.getRange(), 1, 30, "range");
        
        if (session.getTargetType() == TargetType.AOE || session.getTargetType() == TargetType.GROUND) {
            createStatRow(inv, 31, "§6💫 Bán Kính", session.getAreaRadius(), 0, 15, "aoe");
        } else {
            inv.setItem(31, createItem(Material.GRAY_STAINED_GLASS_PANE, "§8Bán Kính (không khả dụng)"));
        }
        
        if (session.getTargetType() == TargetType.PROJECTILE) {
            createStatRow(inv, 34, "§a🎯 Số Đạn", session.getProjectileCount(), 1, 10, "projectile");
        } else {
            inv.setItem(34, createItem(Material.GRAY_STAINED_GLASS_PANE, "§8Số Đạn (không khả dụng)"));
        }
        
        // Duration for BUFF/CONTROL
        if (session.getCategory() == SkillCategory.BUFF || 
            session.getCategory() == SkillCategory.CONTROL ||
            session.getCategory() == SkillCategory.DEFENSE) {
            createStatRow(inv, 40, "§a⏳ Thời Gian", session.getDuration(), 0, 60, "duration");
        }
        
        // Point display (large)
        inv.setItem(49, createPointDisplay(session));
        
        // Navigation
        inv.setItem(45, createItem(Material.ARROW, "§c← Quay lại", "§7Chọn mục tiêu"));
        
        if (!overLimit) {
            inv.setItem(53, createItem(Material.ARROW, "§a→ Tiếp theo", "§7Thêm hiệu ứng"));
        } else {
            inv.setItem(53, createItem(Material.BARRIER, "§c✗ Không thể tiếp tục", "§7Giảm stats để tiếp tục"));
        }
        
        player.openInventory(inv);
    }
    
    private static void createStatRow(Inventory inv, int centerSlot, String name, double value, double min, double max, String statId) {
        // Minus button
        inv.setItem(centerSlot - 1, createItem(
            Material.RED_STAINED_GLASS_PANE,
            "§c◀ Giảm",
            "§7Click: -1",
            "§7Shift+Click: -10"
        ));
        
        // Value display
        String valueDisplay = value == (int)value ? String.valueOf((int)value) : String.format("%.1f", value);
        inv.setItem(centerSlot, createItem(
            Material.PAPER,
            name,
            "§7Giá trị: §f" + valueDisplay,
            "§7Min: §e" + (min == (int)min ? (int)min : min),
            "§7Max: §e" + (max == (int)max ? (int)max : max)
        ));
        
        // Plus button
        inv.setItem(centerSlot + 1, createItem(
            Material.LIME_STAINED_GLASS_PANE,
            "§a▶ Tăng",
            "§7Click: +1",
            "§7Shift+Click: +10"
        ));
    }
    
    // ===== STEP 5: ADD EFFECTS =====
    
    public static void openEffectsEditor(Player player, SkillCreatorSession session) {
        session.setCurrentStep(CreatorStep.ADD_EFFECTS);
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + " §7- Effects");
        
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        // Title
        inv.setItem(4, createInfoItem(
            Material.BREWING_STAND,
            "§6✨ THÊM HIỆU ỨNG",
            "§7Hiệu ứng phụ cho công pháp",
            "§7Mỗi effect tốn +20 điểm",
            "",
            "§7Effects: §e" + session.getEffects().size()
        ));
        
        // Current effects
        List<SkillCreatorSession.EffectEntry> effects = session.getEffects();
        for (int i = 0; i < Math.min(effects.size(), 7); i++) {
            SkillCreatorSession.EffectEntry effect = effects.get(i);
            inv.setItem(19 + i, createItem(
                Material.POTION,
                "§d" + effect.getDisplayName(),
                "§7Thời gian: " + (effect.getDuration() / 20) + "s",
                "§7Cấp độ: " + (effect.getAmplifier() + 1),
                "",
                "§cClick để xóa"
            ));
        }
        
        // Add effect button
        if (effects.size() < 7) {
            inv.setItem(30, createItem(
                Material.LIME_DYE,
                "§a+ Thêm Hiệu Ứng",
                "§7Click để thêm effect mới"
            ));
        }
        
        // Point display
        inv.setItem(49, createPointDisplay(session));
        
        // Navigation
        inv.setItem(45, createItem(Material.ARROW, "§c← Quay lại", "§7Điều chỉnh stats"));
        inv.setItem(53, createItem(Material.ARROW, "§a→ Tiếp theo", "§7Xác nhận & tạo"));
        
        player.openInventory(inv);
    }
    
    // ===== STEP 6: CONFIRMATION =====
    
    public static void openConfirmation(Player player, SkillCreatorSession session) {
        session.setCurrentStep(CreatorStep.CONFIRM);
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE_PREFIX + " §7- Xác Nhận");
        
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);
        
        // Summary
        Element element = session.getElement();
        String elementColor = element != null ? element.getColorCode() : "§f";
        
        inv.setItem(4, createInfoItem(
            Material.ENCHANTED_BOOK,
            elementColor + "📜 " + session.getSkillName(),
            "",
            "§7Loại: " + session.getCategory().getColoredName(),
            "§7Hệ: " + (element != null ? element.getColoredName() : "§7Không"),
            "§7Mục tiêu: " + session.getTargetType().getColoredName(),
            "",
            "§7Sát thương: §c" + (int)session.getBasePower(),
            "§7Hồi chiêu: §b" + session.getCooldown() + "s",
            "§7Linh khí: §d" + (int)session.getManaCost(),
            "§7Tầm xa: §e" + session.getRange() + " blocks"
        ));
        
        // Stats detail
        inv.setItem(20, createItem(
            Material.IRON_SWORD,
            "§c⚔ Stats Chiến Đấu",
            "§7Sát thương: §f" + (int)session.getBasePower(),
            "§7Bán kính: §f" + session.getAreaRadius() + " blocks",
            "§7Số đạn: §f" + session.getProjectileCount()
        ));
        
        inv.setItem(22, createItem(
            Material.CLOCK,
            "§b⏱ Stats Thời Gian",
            "§7Hồi chiêu: §f" + session.getCooldown() + "s",
            "§7Thời gian: §f" + session.getDuration() + "s"
        ));
        
        inv.setItem(24, createItem(
            Material.EXPERIENCE_BOTTLE,
            "§d💧 Stats Tiêu Hao",
            "§7Linh khí: §f" + (int)session.getManaCost(),
            "§7Điểm skill: §f" + session.getCurrentPoints()
        ));
        
        // Effects summary
        List<String> effectLines = new ArrayList<>();
        effectLines.add("§7Tổng: §e" + session.getEffects().size() + " effects");
        for (SkillCreatorSession.EffectEntry effect : session.getEffects()) {
            effectLines.add("§7- " + effect.getDisplayName());
        }
        inv.setItem(31, createItem(Material.BREWING_STAND, "§d✨ Hiệu Ứng", effectLines.toArray(new String[0])));
        
        // Cost display
        long cost = session.calculateCreationCost();
        inv.setItem(40, createItem(
            Material.GOLD_INGOT,
            "§6💰 Chi Phí Sáng Pháp",
            "",
            "§7Linh Thạch: §e" + cost,
            "",
            "§7Bao gồm:",
            "§7- Cơ bản: 500",
            "§7- Điểm skill: " + (session.getCurrentPoints() * 10)
        ));
        
        // Point check
        boolean overLimit = session.isOverLimit();
        inv.setItem(49, createPointDisplay(session));
        
        // Navigation
        inv.setItem(45, createItem(Material.ARROW, "§c← Quay lại", "§7Thêm hiệu ứng"));
        
        // Rename button
        inv.setItem(47, createItem(
            Material.NAME_TAG,
            "§e✏ Đổi Tên",
            "§7Tên hiện tại: §f" + session.getSkillName(),
            "",
            "§eClick để đổi tên"
        ));
        
        if (!overLimit) {
            inv.setItem(53, createItem(
                Material.NETHER_STAR,
                "§a⚡ SÁNG TẠO!",
                "§7Bắt đầu nghi thức sáng pháp",
                "",
                "§eChi phí: §6" + cost + " Linh Thạch"
            ));
            addGlow(inv.getItem(53));
        } else {
            inv.setItem(53, createItem(Material.BARRIER, "§c✗ Không thể tạo", "§7Vượt giới hạn đạo tắc"));
        }
        
        player.openInventory(inv);
    }
    
    // ===== HELPER METHODS =====
    
    private static ItemStack createPointDisplay(SkillCreatorSession session) {
        int points = session.getCurrentPoints();
        int cap = session.getPointCap();
        boolean over = points > cap;
        int percent = session.getPointPercentage();
        
        String barColor = over ? "§c" : (percent > 80 ? "§e" : "§a");
        String bar = createProgressBar(Math.min(percent, 100), 20);
        
        return createItem(
            over ? Material.REDSTONE_BLOCK : Material.EMERALD,
            over ? "§c⚠ VƯỢT GIỚI HẠN" : "§a✓ Điểm Đạo Tắc",
            "",
            barColor + bar,
            "§7Điểm: " + (over ? "§c" : "§e") + points + "§7/" + cap,
            "§7Realm: §e" + session.getPlayerRealm().getDisplayName(),
            "",
            over ? "§cGiảm stats để tiếp tục!" : "§7Trong giới hạn cho phép"
        );
    }
    
    private static String createProgressBar(int percent, int length) {
        int filled = percent * length / 100;
        StringBuilder bar = new StringBuilder("§a[");
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§7░");
            }
        }
        bar.append("§a]");
        return bar.toString();
    }
    
    private static void fillBorder(Inventory inv, Material material) {
        ItemStack border = createItem(material, " ");
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(45 + i, border);
        }
        for (int i = 9; i < 45; i += 9) {
            inv.setItem(i, border);
            inv.setItem(i + 8, border);
        }
    }
    
    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createInfoItem(Material material, String name, String... lore) {
        ItemStack item = createItem(material, name, lore);
        addGlow(item);
        return item;
    }
    
    private static void addGlow(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}
