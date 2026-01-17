package hcontrol.plugin.model;

/**
 * LINH CĂN PHẨM CHẤT
 * Chat luong linh can (quyet dinh toc do tu luyen)
 */
public enum RootQuality {
    PHAMNHAN("Pham Can", "§7", 0.5),         // pham nhan can
    SPIRITUAL("Linh Can", "§a", 1.0),      // linh can
    HEAVENLY("Thien Can", "§e", 1.5),      // thien linh can
    CHANTIEN("Tien Can", "§6§l", 2.5);     // tien linh can
    
    private final String displayName;
    private final String color;
    private final double qualityMultiplier;
    
    RootQuality(String displayName, String color, double qualityMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.qualityMultiplier = qualityMultiplier;
    }
    
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public double getQualityMultiplier() { return qualityMultiplier; }
    
    @Override
    public String toString() {
        return color + displayName;
    }
    
    /**
     * Random root quality
     * @deprecated Sử dụng SpiritualRootService.randomRootQuality() thay thế
     * Enum method này chỉ return PHAMNHAN, service có logic đầy đủ với tỷ lệ
     */
    @Deprecated
    public static RootQuality randomQuality() {
        // Fallback nếu service chưa sẵn sàng
        return PHAMNHAN;
    }
}
