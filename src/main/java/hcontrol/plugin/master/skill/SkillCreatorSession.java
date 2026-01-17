package hcontrol.plugin.master.skill;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.skill.custom.Element;
import hcontrol.plugin.skill.custom.SkillCategory;
import hcontrol.plugin.skill.custom.SkillPointCalculator;
import hcontrol.plugin.skill.custom.SkillTemplate;
import hcontrol.plugin.skill.custom.TargetType;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Session lưu trạng thái đang tạo skill
 * Dựa trên kiến trúc SkillTemplate mới
 */
public class SkillCreatorSession {
    
    private final UUID playerUuid;
    private String playerName;
    private CultivationRealm playerRealm;
    
    // ===== STEP 1: IDENTITY =====
    private String skillName = "Công Pháp Mới";
    private String skillId = null; // Will be generated
    
    // ===== STEP 2: CLASSIFICATION =====
    private SkillCategory category = SkillCategory.ATTACK;
    private Element element = null;       // Nullable - không phải skill nào cũng có hệ
    private TargetType targetType = TargetType.SINGLE;
    
    // ===== STEP 3: STATS =====
    private double basePower = 50;         // 10 - max theo realm
    private double cooldown = 5;            // min theo realm - 30
    private double manaCost = 30;           // 10 - 500
    private double range = 5.0;             // 1 - 30
    private double areaRadius = 0;          // 0 - 15
    private int projectileCount = 1;        // 1 - 10
    private double duration = 0;            // 0 - 60 (for BUFF/CONTROL)
    
    // ===== STEP 4: EFFECTS =====
    private List<EffectEntry> effects = new ArrayList<>();
    
    // ===== STEP 5: METADATA =====
    private boolean transferable = true;
    private int maxLearners = 10;
    private List<String> description = new ArrayList<>();
    
    // ===== UI STATE =====
    private CreatorStep currentStep = CreatorStep.CHOOSE_CATEGORY;
    private boolean addingEffect = false;
    private PotionEffectType pendingEffectType = null;
    
    public SkillCreatorSession(UUID playerUuid, String playerName, CultivationRealm realm) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.playerRealm = realm;
        
        // Set max defaults based on realm
        updateMaxValues();
    }
    
    private void updateMaxValues() {
        // Default power based on realm
        double maxPower = SkillPointCalculator.getMaxPower(playerRealm);
        this.basePower = Math.min(maxPower * 0.3, 50); // Start at 30% of max
    }
    
    // ===== POINT CALCULATION (REAL-TIME) =====
    
    /**
     * Tính điểm skill hiện tại
     */
    public int getCurrentPoints() {
        return SkillPointCalculator.calculatePointsFromSession(
            category, element, targetType,
            basePower, cooldown, range,
            areaRadius, projectileCount, duration,
            effects.size()
        );
    }
    
    /**
     * Lấy giới hạn điểm theo realm
     */
    public int getPointCap() {
        return SkillPointCalculator.getPointCap(playerRealm);
    }
    
    /**
     * Kiểm tra có vượt giới hạn không
     */
    public boolean isOverLimit() {
        return getCurrentPoints() > getPointCap();
    }
    
    /**
     * % điểm đã sử dụng
     */
    public int getPointPercentage() {
        return (int) (getCurrentPoints() * 100.0 / getPointCap());
    }
    
    // ===== STAT ADJUSTMENTS =====
    
    public void adjustBasePower(double delta) {
        double max = SkillPointCalculator.getMaxPower(playerRealm);
        basePower = clamp(basePower + delta, 10, max);
    }
    
    public void adjustCooldown(double delta) {
        double min = SkillPointCalculator.getMinCooldown(playerRealm);
        cooldown = clamp(cooldown + delta, min, 30);
    }
    
    public void adjustManaCost(double delta) {
        manaCost = clamp(manaCost + delta, 10, 500);
    }
    
    public void adjustRange(double delta) {
        range = clamp(range + delta, 1, 30);
    }
    
    public void adjustAreaRadius(double delta) {
        areaRadius = clamp(areaRadius + delta, 0, 15);
    }
    
    public void adjustProjectileCount(int delta) {
        projectileCount = (int) clamp(projectileCount + delta, 1, 10);
    }
    
    public void adjustDuration(double delta) {
        duration = clamp(duration + delta, 0, 60);
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    // ===== EFFECTS =====
    
    public void addEffect(PotionEffectType type, int duration, int amplifier) {
        effects.add(new EffectEntry(type, duration, amplifier));
    }
    
    public void removeEffect(int index) {
        if (index >= 0 && index < effects.size()) {
            effects.remove(index);
        }
    }
    
    public List<EffectEntry> getEffects() {
        return effects;
    }
    
    // ===== BUILD TEMPLATE =====
    
    /**
     * Build SkillTemplate từ session data
     */
    public SkillTemplate buildTemplate(String generatedId) {
        SkillTemplate.Builder builder = new SkillTemplate.Builder(generatedId)
            .name(skillName)
            .creator(playerUuid, playerName)
            .category(category)
            .element(element)
            .targetType(targetType)
            .basePower(basePower)
            .cooldown(cooldown)
            .manaCost(manaCost)
            .range(range)
            .areaRadius(areaRadius)
            .projectileCount(projectileCount)
            .duration(duration)
            .requiredRealm(playerRealm)
            .requiredLevel(1)
            .skillPointCost(getCurrentPoints())
            .transferable(transferable)
            .maxLearners(maxLearners);
        
        // Add effects
        for (EffectEntry effect : effects) {
            builder.addEffect(
                effect.getType().getKey().getKey().toUpperCase(),
                effect.getDuration(),
                effect.getAmplifier()
            );
        }
        
        // Generate description
        builder.addDescription("§7Loại: " + category.getColoredName());
        if (element != null) {
            builder.addDescription("§7Hệ: " + element.getColoredName());
        }
        builder.addDescription("§7Mục tiêu: " + targetType.getColoredName());
        builder.addDescription("§7Sáng tạo bởi: §e" + playerName);
        
        return builder.build();
    }
    
    // ===== CREATION COST =====
    
    /**
     * Tính cost tạo skill (Linh Thạch)
     */
    public long calculateCreationCost() {
        return SkillPointCalculator.calculateCreationCost(getCurrentPoints());
    }
    
    // ===== GETTERS & SETTERS =====
    
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public CultivationRealm getPlayerRealm() { return playerRealm; }
    
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    
    public String getSkillId() { return skillId; }
    public void setSkillId(String skillId) { this.skillId = skillId; }
    
    public SkillCategory getCategory() { return category; }
    public void setCategory(SkillCategory category) { this.category = category; }
    
    public Element getElement() { return element; }
    public void setElement(Element element) { this.element = element; }
    
    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    
    public double getBasePower() { return basePower; }
    public double getCooldown() { return cooldown; }
    public double getManaCost() { return manaCost; }
    public double getRange() { return range; }
    public double getAreaRadius() { return areaRadius; }
    public int getProjectileCount() { return projectileCount; }
    public double getDuration() { return duration; }
    
    public boolean isTransferable() { return transferable; }
    public void setTransferable(boolean transferable) { this.transferable = transferable; }
    
    public int getMaxLearners() { return maxLearners; }
    public void setMaxLearners(int maxLearners) { this.maxLearners = maxLearners; }
    
    public CreatorStep getCurrentStep() { return currentStep; }
    public void setCurrentStep(CreatorStep currentStep) { this.currentStep = currentStep; }
    
    public boolean isAddingEffect() { return addingEffect; }
    public void setAddingEffect(boolean addingEffect) { this.addingEffect = addingEffect; }
    
    public PotionEffectType getPendingEffectType() { return pendingEffectType; }
    public void setPendingEffectType(PotionEffectType pendingEffectType) { 
        this.pendingEffectType = pendingEffectType; 
    }
    
    // ===== STEPS =====
    
    public enum CreatorStep {
        CHOOSE_CATEGORY,    // Bước 1: Chọn loại công pháp
        CHOOSE_ELEMENT,     // Bước 2: Chọn ngũ hành
        CHOOSE_TARGET,      // Bước 3: Chọn mục tiêu
        ADJUST_STATS,       // Bước 4: Điều chỉnh stats
        ADD_EFFECTS,        // Bước 5: Thêm effects
        CONFIRM             // Bước 6: Xác nhận & nghi thức
    }
    
    // ===== EFFECT ENTRY =====
    
    public static class EffectEntry {
        private final PotionEffectType type;
        private int duration;
        private int amplifier;
        
        public EffectEntry(PotionEffectType type, int duration, int amplifier) {
            this.type = type;
            this.duration = duration;
            this.amplifier = amplifier;
        }
        
        public PotionEffectType getType() { return type; }
        public int getDuration() { return duration; }
        public int getAmplifier() { return amplifier; }
        
        public String getDisplayName() {
            String name = type.getKey().getKey().replace("_", " ");
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
    }
}
