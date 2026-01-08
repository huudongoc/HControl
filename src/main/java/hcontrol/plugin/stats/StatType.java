package hcontrol.plugin.stats;

/**
 * PHASE 2 - TU TIEN STATS
 * Thuoc tinh tu tien thay vi RPG vanilla
 */
public enum StatType {
    // Primary stats (can ban - co the cong diem)
    CAN_COT("CC", "Can Cot"),
    LINH_LUC("LL", "Linh Luc"),
    THE_PHACH("TP", "The Phach"),
    NGO_TINH("NT", "Ngo Tinh"),
    KHI_VAN("KV", "Khi Van"),
    
    // Derived stats (tu dong tinh)
    SINH_MENH("SM", "Sinh Menh"),
    LINH_KHI("LK", "Linh Khi"),
    CONG_KICH("CK", "Cong Kich"),
    PHAP_LUC("PL", "Phap Luc"),
    PHONG_NGU("PN", "Phong Ngu"),
    BAO_KICH("BK", "Bao Kich"),
    THAN_PHAP("TF", "Than Phap");
    
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