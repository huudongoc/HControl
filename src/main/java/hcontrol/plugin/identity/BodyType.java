package hcontrol.plugin.identity;

/**
 * PHASE 5 — BODY TYPE
 * The chat cua player (khong chua logic)
 */
public enum BodyType {
    
    /**
     * Thân thể phàm nhân
     */
    MORTAL("§7凡体", "Phàm Thể"),
    
    /**
     * Thân thể linh tính
     */
    SPIRITUAL("§b灵体", "Linh Thể"),
    
    /**
     * Thân thể kim cang
     */
    VAJRA("§6金刚体", "Kim Cương Thể"),
    
    /**
     * Thân thể băng huyết
     */
    ICE_BLOOD("§3冰血体", "Băng Huyết Thể"),
    
    /**
     * Thân thể hỏa linh
     */
    FIRE_SPIRIT("§c火灵体", "Hỏa Linh Thể"),
    
    /**
     * Thân thể đột biến
     */
    MUTATED("§5变异体", "Đột Biến Thể");
    
    private final String symbol;
    private final String displayName;
    
    BodyType(String symbol, String displayName) {
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
