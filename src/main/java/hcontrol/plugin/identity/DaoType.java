package hcontrol.plugin.identity;

/**
 * PHASE 5 — DAO TYPE
 * Dao chu yeu cua player (khong chua logic)
 */
public enum DaoType {
    
    /**
     * Dao chính (orthodox cultivation)
     */
    RIGHTEOUS("§e✦", "Chính Đạo"),
    
    /**
     * Dao ma (demonic cultivation)
     */
    DEMONIC("§c☠", "Ma Đạo"),
    
    /**
     * Dao quỷ (ghost cultivation)
     */
    GHOST("§8●", "Quỷ Đạo"),
    
    /**
     * Dao kiếm (sword dao)
     */
    SWORD("§b⚔", "Kiếm Đạo"),
    
    /**
     * Dao đan (alchemy dao)
     */
    ALCHEMY("§6◆", "Đan Đạo"),
    
    /**
     * Chưa xác định
     */
    NONE("§7○", "Chưa Định");
    
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
