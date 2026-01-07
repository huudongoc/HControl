package hcontrol.plugin.stats;

/**
 * PHASE 2 - TU TIEN STATS
 * Thuoc tinh tu tien thay vi RPG vanilla
 */
public enum StatType {
    // Primary stats (can ban - co the cong diem)
    ROOT("ROOT", "Can Cot"),
    SPIRIT("SPR", "Linh Luc"),
    PHYSIQUE("PHY", "The Phach"),
    COMPREHENSION("COM", "Ngo Tinh"),
    FORTUNE("FOR", "Khi Van"),
    
    // Derived stats (tu dong tinh)
    MAX_HP("HP", "Sinh Menh"),
    MAX_LINGQI("QI", "Linh Khi"),
    ATTACK("ATK", "Cong Kich"),
    MAGIC_ATTACK("MATK", "Phap Luc"),
    DEFENSE("DEF", "Phong Ngu"),
    CRIT_RATE("CRIT", "Bao Kich"),
    DODGE("DODGE", "Than Phap");
    
    private final String shortName;
    private final String displayName;
    
    StatType(String shortName, String displayName) {
        this.shortName = shortName;
        this.displayName = displayName;
    }
    
    public String getShortName() {
        return shortName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isPrimary() {
        return this.ordinal() < 5; // 5 stat dau la primary
    }
    
    public boolean isDerived() {
        return !isPrimary();
    }
}