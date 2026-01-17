package hcontrol.plugin.identity;

/**
 * PHASE 5 — DAO TYPE
 * Dao chu yeu cua player (khong chua logic)
 */
public enum DaoType {
    
    /**
     * Dao chính (orthodox cultivation)
     */
    RIGHTEOUS("§e正道", "Chính Đạo"),
    
    /**
     * Dao ma (demonic cultivation)
     */
    DEMONIC("§c魔道", "Ma Đạo"),
    
    /**
     * Dao quỷ (ghost cultivation)
     */
    GHOST("§8鬼道", "Quỷ Đạo"),
    
    /**
     * Dao kiếm (sword dao)
     */
    SWORD("§b剑道", "Kiếm Đạo"),
    
    /**
     * Dao đan (alchemy dao)
     */
    ALCHEMY("§6丹道", "Đan Đạo"),
    
    /**
     * Chưa xác định
     */
    NONE("§7无", "Chưa Định");
    
    private final String symbol;
    private final String displayName;
    
    DaoType(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
