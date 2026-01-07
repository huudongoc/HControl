package hcontrol.plugin.model;

/**
 * PHASE 5 (TU TIEN) — CULTIVATION REALM
 * He thong canh gioi tu tien
 */
public enum CultivationRealm {
    // Ha Gioi (Lower Realm) - server chinh
    MORTAL("Pham Nhan", 0, 1.0, 10, 5, 0),          // 1-10 level, damage 5, 0 cultivation to breakthrough
    QI_REFINING("Luyen Khi", 1, 1.2, 9, 10, 1000),      // 1-9 level, base damage 10, 1k cultivation
    FOUNDATION("Truc Co", 10, 1.5, 9, 25, 5000),        // 1-9 level, base damage 25
    GOLDEN_CORE("Kim Dan", 30, 2.0, 9, 70, 20000),       // 1-9 level, base damage 70
    NASCENT_SOUL("Nguyen Anh", 60, 3.0, 7, 200, 100000),  // 1-7 level, base damage 200
    
    // Trung Gioi (Middle Realm) - unlock sau
    SOUL_FORMATION("Hoa Than", 100, 4.0, 7, 600, 500000),    // 1-7 level, base damage 600
    VOID_REFINEMENT("Luyen Ho", 150, 5.5, 5, 1500, 2000000),  // 1-5 level
    BODY_INTEGRATION("Hop The", 200, 7.0, 5, 3500, 10000000),  // 1-5 level
    MAHAYANA("Dai Thua", 300, 10.0, 3, 8000, 50000000),        // 1-3 level
    
    // Thuong Gioi (Upper Realm) - end game
    TRIBULATION("Do Kiep", 500, 15.0, 3, 20000, 200000000),     // 1-3 level
    IMMORTAL("Chan Tien", 1000, 25.0, 1, 50000, 1000000000);     // chi 1 level
    
    private final String displayName;
    private final int requiredLevel;      // level can thiet de breakthrough (cu)
    private final double statMultiplier;  // nhan stat khi dat realm
    private final int maxLevelInRealm;    // max level trong canh gioi nay (NEW)
    private final double baseDamage;      // base damage cua canh gioi (NEW)
    private final long requiredCultivation;  // tu vi can de dot pha len realm nay (NEW)
    
    CultivationRealm(String displayName, int requiredLevel, double statMultiplier, int maxLevelInRealm, double baseDamage, long requiredCultivation) {
        this.displayName = displayName;
        this.requiredLevel = requiredLevel;
        this.statMultiplier = statMultiplier;
        this.maxLevelInRealm = maxLevelInRealm;
        this.baseDamage = baseDamage;
        this.requiredCultivation = requiredCultivation;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getRequiredLevel() {
        return requiredLevel;
    }
    
    public double getStatMultiplier() {
        return statMultiplier;
    }
    
    public int getMaxLevelInRealm() {
        return maxLevelInRealm;
    }
    
    public double getBaseDamage() {
        return baseDamage;
    }
    
    public long getRequiredCultivation() {
        return requiredCultivation;
    }
    
    public CultivationRealm getNext() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal >= values().length) {
            return null; // da max realm
        }
        return values()[nextOrdinal];
    }
    
    public boolean canBreakthrough(int currentLevel) {
        CultivationRealm next = getNext();
        return next != null && currentLevel >= next.getRequiredLevel();
    }
    
    public String getColor() {
        return switch (this) {
            case MORTAL -> "§7";
            case QI_REFINING -> "§f";
            case FOUNDATION -> "§a";
            case GOLDEN_CORE -> "§e";
            case NASCENT_SOUL -> "§6";
            case SOUL_FORMATION -> "§d";
            case VOID_REFINEMENT -> "§5";
            case BODY_INTEGRATION -> "§b";
            case MAHAYANA -> "§9";
            case TRIBULATION -> "§c";
            case IMMORTAL -> "§6§l";
        };
    }
    
    @Override
    public String toString() {
        return getColor() + displayName;
    }
}
