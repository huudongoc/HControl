package hcontrol.plugin.sect;

/**
 * SECT SYSTEM - Cấp bậc trong Môn Phái
 */
public enum SectRank {
    
    // Từ thấp đến cao
    OUTER_DISCIPLE("Đệ Tử Ngoại Môn", "§7", 0),
    INNER_DISCIPLE("Đệ Tử Nội Môn", "§f", 1),
    CORE_DISCIPLE("Chân Truyền Đệ Tử", "§a", 2),
    ELDER("Trưởng Lão", "§b", 3),
    VICE_LEADER("Phó Chưởng Môn", "§d", 4),
    LEADER("Chưởng Môn", "§6", 5);
    
    private final String displayName;
    private final String color;
    private final int level;
    
    SectRank(String displayName, String color, int level) {
        this.displayName = displayName;
        this.color = color;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getColoredName() {
        return color + displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Kiểm tra rank này có quyền cao hơn rank khác không
     */
    public boolean isHigherThan(SectRank other) {
        return this.level > other.level;
    }
    
    /**
     * Kiểm tra rank này có quyền mời người vào môn phái không
     */
    public boolean canInvite() {
        return this.level >= ELDER.level;
    }
    
    /**
     * Kiểm tra rank này có quyền kick người không
     */
    public boolean canKick() {
        return this.level >= ELDER.level;
    }
    
    /**
     * Kiểm tra rank này có quyền thăng cấp người khác không
     */
    public boolean canPromote() {
        return this.level >= VICE_LEADER.level;
    }
    
    /**
     * Kiểm tra rank này có quyền quản lý môn phái không
     */
    public boolean canManageSect() {
        return this.level >= LEADER.level;
    }
}
