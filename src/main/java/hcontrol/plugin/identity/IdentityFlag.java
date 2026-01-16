package hcontrol.plugin.identity;

/**
 * PHASE 5 — IDENTITY FLAG
 * Dac tinh dac biet cua player
 * Flag dung de RULE, khong dung truc tiep
 */
public enum IdentityFlag {
    
    /**
     * Thân thể đột biến
     * VD: có thể dùng skill đặc biệt
     */
    MUTATED_BODY("§5变异", "Thân Đột Biến"),
    
    /**
     * Thiên tuyển chi tử
     * VD: giảm tribulation difficulty
     */
    HEAVEN_CHOSEN("§e天选", "Thiên Tuyển"),
    
    /**
     * Ma tâm
     * VD: có thể dùng demonic skills
     */
    DEMON_HEART("§c魔心", "Ma Tâm"),
    
    /**
     * Linh hồn bị phong ấn
     * VD: không thể dùng 1 số skill
     */
    SEALED_SOUL("§8封魂", "Phong Hồn"),
    
    /**
     * Bloodline đặc biệt
     * VD: stat bonus
     */
    SPECIAL_BLOODLINE("§6血脉", "Huyết Mạch"),
    
    /**
     * Cursed
     * VD: debuff effects
     */
    CURSED("§4诅咒", "Nguyền Rủa");
    
    private final String symbol;
    private final String displayName;
    
    IdentityFlag(String symbol, String displayName) {
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
