package hcontrol.plugin.skill.custom;

import java.time.Instant;
import java.util.UUID;

/**
 * SKILL INSTANCE - Công pháp đã học của một người
 * 
 * 📌 Mỗi người học 1 template → tạo 1 instance riêng
 * 📌 Chứa mastery, refinement, stats cá nhân
 */
public class SkillInstance {
    
    // ===== REFERENCES =====
    private final String templateId;            // ID của SkillTemplate
    private final UUID learnerUuid;             // Người đã học
    private String learnerName;
    
    // ===== PROGRESSION =====
    private int mastery;                        // Độ thuần thục 0-100%
    private int refinement;                     // Cấp tinh luyện 1-9
    private long useCount;                      // Số lần sử dụng
    private Instant learnedAt;                  // Thời điểm học
    private Instant lastUsed;                   // Lần cuối sử dụng
    
    // ===== TAUGHT BY =====
    private UUID taughtByUuid;                  // Ai dạy (null nếu tự sáng tạo)
    private String taughtByName;
    
    public SkillInstance(String templateId, UUID learnerUuid, String learnerName) {
        this.templateId = templateId;
        this.learnerUuid = learnerUuid;
        this.learnerName = learnerName;
        this.mastery = 0;
        this.refinement = 1;
        this.useCount = 0;
        this.learnedAt = Instant.now();
        this.lastUsed = null;
        this.taughtByUuid = null;
        this.taughtByName = null;
    }
    
    // ===== MASTERY SYSTEM =====
    
    /**
     * Tăng mastery khi sử dụng skill
     * Tối đa 100%
     */
    public void addMastery(int amount) {
        this.mastery = Math.min(100, this.mastery + amount);
    }
    
    /**
     * Hệ số damage/hiệu quả dựa trên mastery
     * 0% mastery = 0.8x
     * 50% mastery = 1.0x
     * 100% mastery = 1.2x
     */
    public double getMasteryMultiplier() {
        return 0.8 + (mastery * 0.004); // 0.8 + 0.4 at 100%
    }
    
    // ===== REFINEMENT SYSTEM =====
    
    /**
     * Tăng cấp tinh luyện
     * Cấp 1 → 9 (Cửu Trọng)
     */
    public boolean increaseRefinement() {
        if (refinement >= 9) return false;
        refinement++;
        return true;
    }
    
    /**
     * Hệ số damage/hiệu quả dựa trên refinement
     * Cấp 1 = 1.0x
     * Cấp 9 = 1.8x
     */
    public double getRefinementMultiplier() {
        return 1.0 + ((refinement - 1) * 0.1); // 0.1 per level
    }
    
    /**
     * Tên cấp tinh luyện theo Tu Tiên
     */
    public String getRefinementName() {
        return switch (refinement) {
            case 1 -> "Sơ Nhập";
            case 2 -> "Tiểu Thành";
            case 3 -> "Trung Thành";
            case 4 -> "Đại Thành";
            case 5 -> "Viên Mãn";
            case 6 -> "Đăng Phong";
            case 7 -> "Nhập Vi";
            case 8 -> "Đại Viên Mãn";
            case 9 -> "Cửu Trọng Đỉnh Phong";
            default -> "???";
        };
    }
    
    // ===== USAGE TRACKING =====
    
    /**
     * Ghi nhận sử dụng skill
     */
    public void recordUsage() {
        this.useCount++;
        this.lastUsed = Instant.now();
        
        // Auto tăng mastery mỗi lần dùng (1-3 điểm)
        int masteryGain = 1 + (int) (Math.random() * 3);
        addMastery(masteryGain);
    }
    
    // ===== TOTAL MULTIPLIER =====
    
    /**
     * Tổng hệ số hiệu quả = mastery * refinement
     */
    public double getTotalMultiplier() {
        return getMasteryMultiplier() * getRefinementMultiplier();
    }
    
    /**
     * Cooldown giảm theo mastery
     * 100% mastery = -20% cooldown
     */
    public double getCooldownReduction() {
        return mastery * 0.002; // Max 20% reduction
    }
    
    /**
     * Mana cost giảm theo mastery
     * 100% mastery = -30% mana cost
     */
    public double getManaCostReduction() {
        return mastery * 0.003; // Max 30% reduction
    }
    
    // ===== GETTERS & SETTERS =====
    
    public String getTemplateId() { return templateId; }
    public UUID getLearnerUuid() { return learnerUuid; }
    public String getLearnerName() { return learnerName; }
    public void setLearnerName(String name) { this.learnerName = name; }
    
    public int getMastery() { return mastery; }
    public void setMastery(int mastery) { this.mastery = Math.max(0, Math.min(100, mastery)); }
    
    public int getRefinement() { return refinement; }
    public void setRefinement(int refinement) { this.refinement = Math.max(1, Math.min(9, refinement)); }
    
    public long getUseCount() { return useCount; }
    public void setUseCount(long useCount) { this.useCount = useCount; }
    
    public Instant getLearnedAt() { return learnedAt; }
    public void setLearnedAt(Instant learnedAt) { this.learnedAt = learnedAt; }
    
    public Instant getLastUsed() { return lastUsed; }
    
    public UUID getTaughtByUuid() { return taughtByUuid; }
    public String getTaughtByName() { return taughtByName; }
    
    public void setTaughtBy(UUID uuid, String name) {
        this.taughtByUuid = uuid;
        this.taughtByName = name;
    }
    
    public boolean wasTaught() {
        return taughtByUuid != null;
    }
    
    /**
     * Mô tả ngắn về instance
     */
    public String getStatusLine() {
        return String.format("§7Thuần thục: §a%d%% §7| Tinh luyện: §e%s §7(%dx)", 
            mastery, getRefinementName(), refinement);
    }
}
