package hcontrol.plugin.skill.custom;

import org.bukkit.Material;

/**
 * LOẠI CÔNG PHÁP
 * Thay thế SkillType để phù hợp hơn với Tu Tiên
 */
public enum SkillCategory {
    
    ATTACK("Công Kích", "§c", Material.IRON_SWORD, 
           "Kỹ năng gây sát thương"),
    
    DEFENSE("Phòng Ngự", "§9", Material.SHIELD,
            "Kỹ năng phòng thủ, tạo khiên"),
    
    CONTROL("Khống Chế", "§e", Material.COBWEB,
            "Làm chậm, choáng, trói buộc"),
    
    BUFF("Tăng Cường", "§a", Material.GOLDEN_APPLE,
         "Buff stats cho bản thân/đồng đội"),
    
    HEAL("Hồi Phục", "§d", Material.GLISTERING_MELON_SLICE,
         "Hồi máu, giải độc"),
    
    MOVEMENT("Di Chuyển", "§b", Material.ENDER_PEARL,
             "Dịch chuyển, tăng tốc");
    
    private final String displayName;
    private final String colorCode;
    private final Material icon;
    private final String description;
    
    SkillCategory(String displayName, String colorCode, Material icon, String description) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.icon = icon;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public String getColoredName() {
        return colorCode + displayName;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Hệ số điểm cho loại skill
     * Attack/Control tốn nhiều điểm hơn
     */
    public double getPointMultiplier() {
        return switch (this) {
            case ATTACK -> 1.2;
            case DEFENSE -> 0.9;
            case CONTROL -> 1.1;
            case BUFF -> 0.8;
            case HEAL -> 0.85;
            case MOVEMENT -> 1.0;
        };
    }
}
