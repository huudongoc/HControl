package hcontrol.plugin.identity;

/**
 * PHASE 5 — SECT ID
 * Mon phai cua player (khong chua logic)
 */
public enum SectId {
    
    /**
     * Không thuộc môn phái
     */
    NONE("§7无门", "Tán Tu"),
    
    /**
     * Thanh Vân Tông
     */
    QINGYUN("§a青云宗", "Thanh Vân Tông"),
    
    /**
     * Thiên Âm Tự
     */
    TIANYIN("§e天音寺", "Thiên Âm Tự"),
    
    /**
     * Quỷ Vương Tông
     */
    GHOST_KING("§8鬼王宗", "Quỷ Vương Tông"),
    
    /**
     * Hợp Hoan Phái
     */
    HEHUAN("§d合欢派", "Hợp Hoan Phái"),
    
    /**
     * Thiên Đế Các
     */
    TIANDI("§6天帝阁", "Thiên Đế Các");
    
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
