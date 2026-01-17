package hcontrol.plugin.model;

/**
 * PHASE 5 (TU TIEN) — CULTIVATION REALM
 * He thong canh gioi tu tien
 */
public enum CultivationRealm {
    // Ha Gioi (Lower Realm) - server chinh
    PHAMNHAN("Pham Nhan", 1.0, 10, 5, 0),              // 1-10 level, damage 5, 0 cultivation
    LUYENKHI("Luyen Khi", 1.2, 10, 10, 1000),      // 1-10 level, base damage 10
    TRUCCO("Truc Co", 1.5, 10, 25, 5000),        // 1-10 level, base damage 25
    KIMDAN("Kim Dan", 2.0, 10, 70, 20000),      // 1-10 level, base damage 70
    NGUYENANH("Nguyen Anh", 3.0, 10, 200, 100000),  // 1-10 level, base damage 200
    
    // Trung Gioi (Middle Realm) - unlock sau
    HOATHAN("Hoa Than", 4.0, 10, 600, 500000),     // 1-10 level, base damage 600
    LUYENHON("Luyen Hon", 5.5, 10, 1500, 2000000),  // 1-10 level
    HOPTHE("Hop The", 7.0, 10, 3500, 10000000), // 1-10 level
    DAITHUA("Dai Thua", 10.0, 10, 8000, 50000000),       // 1-10 level
    
    // Thuong Gioi (Upper Realm) - end game
    DOKIEP("Do Kiep", 15.0, 10, 20000, 200000000),     // 1-10 level
    CHANTIEN("Chan Tien", 25.0, 10, 50000, 1000000000);    // 1-10 level
    
    private final String displayName;
    private final double statMultiplier;  // nhan stat khi dat realm
    private final int maxLevelInRealm;    // max level trong canh gioi nay
    private final double baseDamage;      // base damage cua canh gioi
    private final long requiredCultivation;  // tu vi can de do kiep len realm nay
    
    CultivationRealm(String displayName, double statMultiplier, int maxLevelInRealm, double baseDamage, long requiredCultivation) {
        this.displayName = displayName;
        this.statMultiplier = statMultiplier;
        this.maxLevelInRealm = maxLevelInRealm;
        this.baseDamage = baseDamage;
        this.requiredCultivation = requiredCultivation;
    }
    
    public String getDisplayName() {
        return displayName;
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
    
    /**
     * Lay canh gioi truoc (lui xuong)
     */
    public CultivationRealm getPrevious() {
        int prevOrdinal = this.ordinal() - 1;
        if (prevOrdinal < 0) {
            return null; // da la realm thap nhat
        }
        return values()[prevOrdinal];
    }
    
    /**
     * Kiem tra co the do kiep (tribulation) dua tren level
     * BAT BUOC max level (10) cua realm hien tai
     */
    public boolean canBreakthrough(int currentLevel) {
        return currentLevel >= this.maxLevelInRealm;
    }
    
    public String getColor() {
        return switch (this) {
            case PHAMNHAN -> "§7";
            case LUYENKHI -> "§f";
            case TRUCCO -> "§a";
            case KIMDAN -> "§e";
            case NGUYENANH -> "§6";
            case HOATHAN -> "§d";
            case LUYENHON -> "§5";
            case HOPTHE -> "§b";
            case DAITHUA -> "§9";
            case DOKIEP -> "§c";
            case CHANTIEN -> "§6§l";
        };
    }
    
    /**
     * Lay ten viet tat de hien thi tren tablist
     */
    public String getShortName() {
        return switch (this) {
            case PHAMNHAN -> "PN";
            case LUYENKHI -> "LK";
            case TRUCCO -> "TC";
            case KIMDAN -> "KĐ";
            case NGUYENANH -> "NA";
            case HOATHAN -> "HT";
            case LUYENHON -> "LH";
            case HOPTHE -> "HT";
            case DAITHUA -> "ĐT";
            case DOKIEP -> "ĐK";
            case CHANTIEN -> "CT";
        };
    }
    
    @Override
    public String toString() {
        return getColor() + displayName;
    }
}
