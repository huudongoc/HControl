package hcontrol.plugin.skill.custom;

/**
 * LOẠI MỤC TIÊU của skill
 */
public enum TargetType {
    
    SELF("Bản thân", "§aBản thân", 0),
    SINGLE("Đơn mục tiêu", "§eĐơn mục tiêu", 1),
    AOE("Phạm vi", "§cPhạm vi", 2),
    PROJECTILE("Phi đạn", "§dPhi đạn", 1),
    GROUND("Mặt đất", "§6Mặt đất", 2);
    
    private final String displayName;
    private final String coloredName;
    private final int pointMultiplier; // Nhân điểm cost
    
    TargetType(String displayName, String coloredName, int pointMultiplier) {
        this.displayName = displayName;
        this.coloredName = coloredName;
        this.pointMultiplier = pointMultiplier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColoredName() {
        return coloredName;
    }
    
    /**
     * AOE và GROUND cost nhiều điểm hơn
     */
    public int getPointMultiplier() {
        return pointMultiplier;
    }
    
    public String getDescription() {
        return switch (this) {
            case SELF -> "Tác dụng lên bản thân";
            case SINGLE -> "Tấn công một mục tiêu";
            case AOE -> "Ảnh hưởng nhiều mục tiêu trong phạm vi";
            case PROJECTILE -> "Bắn phi tiêu/đạn về phía trước";
            case GROUND -> "Tác dụng lên vùng đất";
        };
    }
}
