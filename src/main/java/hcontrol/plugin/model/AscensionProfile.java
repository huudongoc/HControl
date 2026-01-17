package hcontrol.plugin.model;

/**
 * ASCENSION PROFILE
 * Endgame progression sau CHANTIEN level 10
 * 
 * Quy tắc:
 * - Chỉ mở khi realm == CHANTIEN && level == 10
 * - Không reset realm, không reset class
 * - Chỉ tăng modifier (ascensionPower)
 * - Cost tăng dần (soft cap)
 */
public class AscensionProfile {
    
    private int ascensionLevel;        // 0, 1, 2, 3... (0 = chua ascension)
    private double ascensionPower;      // bonus multiplier (1.0 + level * 0.05)
    
    public AscensionProfile() {
        this.ascensionLevel = 0;
        this.ascensionPower = 1.0;
    }
    
    public AscensionProfile(int ascensionLevel) {
        this.ascensionLevel = Math.max(0, ascensionLevel);
        this.ascensionPower = calculatePower(this.ascensionLevel);
    }
    
    /**
     * Tính ascension power từ level
     * Formula: 1.0 + (level * 0.05)
     * - Level 0: 1.0x (no bonus)
     * - Level 1: 1.05x (+5%)
     * - Level 10: 1.5x (+50%)
     * - Level 20: 2.0x (+100%)
     */
    private double calculatePower(int level) {
        return 1.0 + (level * 0.05);
    }
    
    /**
     * Tăng ascension level
     * Tự động tính lại power
     */
    public void increaseLevel() {
        this.ascensionLevel++;
        this.ascensionPower = calculatePower(this.ascensionLevel);
    }
    
    /**
     * Tính cost để lên ascension level tiếp theo
     * Formula: base * (1.5 ^ level)
     * - Level 0->1: 1,000,000 cultivation
     * - Level 1->2: 1,500,000 cultivation
     * - Level 2->3: 2,250,000 cultivation
     * - Soft cap: cost tăng dần
     */
    public long getRequiredCultivation() {
        if (ascensionLevel < 0) return 0;
        
        long base = 1_000_000L;  // 1M cultivation cho level đầu
        double multiplier = Math.pow(1.5, ascensionLevel);
        return (long)(base * multiplier);
    }
    
    // ========== GETTERS ==========
    
    public int getAscensionLevel() {
        return ascensionLevel;
    }
    
    public double getAscensionPower() {
        return ascensionPower;
    }
    
    /**
     * Check có đang ở ascension không
     */
    public boolean isAscended() {
        return ascensionLevel > 0;
    }
    
    /**
     * Set ascension level (dùng cho load/save)
     */
    public void setAscensionLevel(int level) {
        this.ascensionLevel = Math.max(0, level);
        this.ascensionPower = calculatePower(this.ascensionLevel);
    }
}
