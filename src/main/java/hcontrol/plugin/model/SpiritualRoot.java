package hcontrol.plugin.model;

/**
 * LINH CĂN - Spiritual Root
 * Thuoc tinh can ban cua tu si (khong the thay doi)
 * CANG HIEM -> TU LUYEN NHANH + DAME CAO
 */
public enum SpiritualRoot {
    // NGU HANH (PHO BIEN) - Rarity 1
    METAL("Kim", "§f", 1.0, 1, 1.0),
    WOOD("Moc", "§a", 1.0, 1, 1.0),
    WATER("Thuy", "§9", 1.0, 1, 1.0),
    FIRE("Hoa", "§c", 1.0, 1, 1.0),
    EARTH("Tho", "§6", 1.0, 1, 1.0),
    
    // BIEN DI (HIEM) - Rarity 2 - ty le 1/9999
    THUNDER("Loi", "§e", 1.2, 2, 1.15),    // +20% tu luyen, +15% damage
    WIND("Phong", "§b", 1.2, 2, 1.15),
    ICE("Bang", "§3", 1.2, 2, 1.15),
    
    // HUYEN THOAI (CUC HIEM) - Rarity 3 - ty le 1/99999
    YIN_YANG("Am Duong", "§5§l", 1.25, 3, 1.20),  // +25% tu luyen, +20% damage
    CHAOS("Hon Don", "§4§l", 1.3, 3, 1.25);      // +30% tu luyen, +25% damage
    
    private final String displayName;
    private final String color;
    private final double cultivationBonus; // he so tu luyen
    private final int rarity;              // 1=pho bien, 2=hiem, 3=huyen thoai
    private final double damageBonus;      // nhan dame (cang hiem +0.5)
    
    SpiritualRoot(String displayName, String color, double cultivationBonus, int rarity, double damageBonus) {
        this.displayName = displayName;
        this.color = color;
        this.cultivationBonus = cultivationBonus;
        this.rarity = rarity;
        this.damageBonus = damageBonus;
    }
    
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public double getCultivationBonus() { return cultivationBonus; }
    public int getRarity() { return rarity; }
    public double getDamageBonus() { return damageBonus; }
    
    /**
     * Ten day du co mau sac + danh dau rarity
     */
    @Override
    public String toString() {
        String rarityMark = switch (rarity) {
            case 1 -> "";           // pho bien khong danh dau
            case 2 -> "§e★§r ";     // hiem - 1 sao vang
            case 3 -> "§6★★★§r ";   // huyen thoai - 3 sao vang
            default -> "";
        };
        return rarityMark + color + displayName + " Can§r";
    }
    
    /**
     * Random spiritual root
     * @deprecated Sử dụng SpiritualRootService.randomSpiritualRoot() thay thế
     * Enum method này chỉ return WOOD, service có logic đầy đủ với rarity
     */
    @Deprecated
    public static SpiritualRoot randomSpiritualRoot() {
        // Fallback nếu service chưa sẵn sàng
        return WOOD;
    }
}
