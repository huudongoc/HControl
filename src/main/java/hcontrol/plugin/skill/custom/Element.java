package hcontrol.plugin.skill.custom;

import org.bukkit.Color;
import org.bukkit.Particle;

/**
 * NGŨ HÀNH (Five Elements)
 * Hệ thống tương sinh tương khắc
 */
public enum Element {
    
    KIM("Kim", "§f", Color.WHITE, Particle.END_ROD, "Metal"),
    MOC("Mộc", "§a", Color.GREEN, Particle.VILLAGER_HAPPY, "Wood"),
    THUY("Thủy", "§b", Color.AQUA, Particle.DRIP_WATER, "Water"),
    HOA("Hỏa", "§c", Color.RED, Particle.FLAME, "Fire"),
    THO("Thổ", "§6", Color.ORANGE, Particle.BLOCK_CRACK, "Earth");
    
    private final String displayName;
    private final String colorCode;
    private final Color color;
    private final Particle particle;
    private final String englishName;
    
    Element(String displayName, String colorCode, Color color, Particle particle, String englishName) {
        this.displayName = displayName;
        this.colorCode = colorCode;
        this.color = color;
        this.particle = particle;
        this.englishName = englishName;
    }
    
    // ===== NGŨ HÀNH TƯƠNG SINH =====
    // Kim sinh Thủy, Thủy sinh Mộc, Mộc sinh Hỏa, Hỏa sinh Thổ, Thổ sinh Kim
    
    /**
     * Hệ này sinh ra hệ nào
     */
    public Element generates() {
        return switch (this) {
            case KIM -> THUY;   // Kim sinh Thủy
            case THUY -> MOC;   // Thủy sinh Mộc
            case MOC -> HOA;    // Mộc sinh Hỏa
            case HOA -> THO;    // Hỏa sinh Thổ
            case THO -> KIM;    // Thổ sinh Kim
        };
    }
    
    /**
     * Hệ này được sinh bởi hệ nào
     */
    public Element generatedBy() {
        return switch (this) {
            case KIM -> THO;    // Thổ sinh Kim
            case THUY -> KIM;   // Kim sinh Thủy
            case MOC -> THUY;   // Thủy sinh Mộc
            case HOA -> MOC;    // Mộc sinh Hỏa
            case THO -> HOA;    // Hỏa sinh Thổ
        };
    }
    
    // ===== NGŨ HÀNH TƯƠNG KHẮC =====
    // Kim khắc Mộc, Mộc khắc Thổ, Thổ khắc Thủy, Thủy khắc Hỏa, Hỏa khắc Kim
    
    /**
     * Hệ này khắc chế hệ nào
     */
    public Element controls() {
        return switch (this) {
            case KIM -> MOC;    // Kim khắc Mộc
            case MOC -> THO;    // Mộc khắc Thổ
            case THO -> THUY;   // Thổ khắc Thủy
            case THUY -> HOA;   // Thủy khắc Hỏa
            case HOA -> KIM;    // Hỏa khắc Kim
        };
    }
    
    /**
     * Hệ này bị khắc bởi hệ nào
     */
    public Element controlledBy() {
        return switch (this) {
            case KIM -> HOA;    // Hỏa khắc Kim
            case MOC -> KIM;    // Kim khắc Mộc
            case THO -> MOC;    // Mộc khắc Thổ
            case THUY -> THO;   // Thổ khắc Thủy
            case HOA -> THUY;   // Thủy khắc Hỏa
        };
    }
    
    // ===== DAMAGE MODIFIERS =====
    
    /**
     * Hệ số sát thương khi tấn công hệ khác
     * @param target Hệ của mục tiêu
     * @return Multiplier (1.0 = bình thường, 1.5 = khắc, 0.7 = bị khắc)
     */
    public double getDamageModifier(Element target) {
        if (target == null) return 1.0;
        
        if (this.controls() == target) {
            return 1.5; // Khắc chế: +50% damage
        }
        if (this.controlledBy() == target) {
            return 0.7; // Bị khắc: -30% damage
        }
        if (this.generates() == target) {
            return 0.9; // Sinh ra: -10% (tương sinh không tốt cho tấn công)
        }
        if (this.generatedBy() == target) {
            return 1.1; // Được sinh: +10%
        }
        return 1.0; // Cùng hệ hoặc không liên quan
    }
    
    // ===== GETTERS =====
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public String getColoredName() {
        return colorCode + displayName;
    }
    
    public Color getColor() {
        return color;
    }
    
    public Particle getParticle() {
        return particle;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    /**
     * Mô tả quan hệ ngũ hành
     */
    public String getRelationDescription() {
        return colorCode + displayName + " §7sinh " + generates().getColoredName() + 
               "§7, khắc " + controls().getColoredName();
    }
}
