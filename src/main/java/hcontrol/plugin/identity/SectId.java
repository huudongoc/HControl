package hcontrol.plugin.identity;

/**
 * PHASE 5 — SECT ID
 * Mon phai cua player (khong chua logic)
 */
public enum SectId {
    
    /**
     * Không thuộc môn phái
     */
    NONE("§7○", "Tán Tu"),
    
    /**
     * Thanh Vân Tông
     */
    QINGYUN("§a✦", "Thanh Vân Tông"),
    
    /**
     * Thiên Âm Tự
     */
    TIANYIN("§e◆", "Thiên Âm Tự"),
    
    /**
     * Quỷ Vương Tông
     */
    GHOST_KING("§8☠", "Quỷ Vương Tông"),
    
    /**
     * Hợp Hoan Phái
     */
    HEHUAN("§d●", "Hợp Hoan Phái"),
    
    /**
     * Thiên Đế Các
     */
    TIANDI("§6★", "Thiên Đế Các");
    
    private final String symbol;
    private final String displayName;
    
    SectId(String symbol, String displayName) {
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
