package hcontrol.plugin.skill.custom;

import hcontrol.plugin.model.CultivationRealm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SKILL TEMPLATE - Bản thiết kế công pháp
 * 
 * 📌 Chỉ chứa DATA, không chứa Bukkit logic
 * 📌 Một template → nhiều người học
 * 📌 Immutable sau khi tạo
 */
public class SkillTemplate {
    
    // ===== IDENTITY =====
    private final String id;                    // unique ID (e.g., "blazing_sword")
    private final String name;                  // Tên hiển thị (e.g., "Hỏa Diệm Kiếm")
    private final UUID creatorUuid;             // Người sáng tạo
    private final String creatorName;
    private final Instant createdAt;
    
    // ===== CLASSIFICATION =====
    private final SkillCategory category;       // ATTACK, DEFENSE, CONTROL, BUFF, HEAL, MOVEMENT
    private final Element element;              // Ngũ hành (nullable cho skill không hệ)
    private final TargetType targetType;        // SELF, SINGLE, AOE, PROJECTILE, GROUND
    
    // ===== STATS =====
    private final double basePower;             // Sát thương/hiệu quả cơ bản
    private final double cooldown;              // Giây
    private final double manaCost;              // Linh khí
    private final double range;                 // Tầm xa (blocks)
    private final double areaRadius;            // Bán kính AOE (nếu có)
    private final int projectileCount;          // Số đạn (nếu PROJECTILE)
    private final double duration;              // Thời gian hiệu lực (nếu BUFF/CONTROL)
    
    // ===== REQUIREMENTS =====
    private final CultivationRealm requiredRealm;
    private final int requiredLevel;
    private final int skillPointCost;           // Điểm skill để học
    
    // ===== EFFECTS =====
    private final List<SkillEffect> effects;    // Hiệu ứng phụ
    
    // ===== METADATA =====
    private final List<String> description;
    private final boolean transferable;         // Có thể truyền cho đệ tử không
    private final int maxLearners;              // Số người tối đa học được
    
    private SkillTemplate(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.creatorUuid = builder.creatorUuid;
        this.creatorName = builder.creatorName;
        this.createdAt = Instant.now();
        
        this.category = builder.category;
        this.element = builder.element;
        this.targetType = builder.targetType;
        
        this.basePower = builder.basePower;
        this.cooldown = builder.cooldown;
        this.manaCost = builder.manaCost;
        this.range = builder.range;
        this.areaRadius = builder.areaRadius;
        this.projectileCount = builder.projectileCount;
        this.duration = builder.duration;
        
        this.requiredRealm = builder.requiredRealm;
        this.requiredLevel = builder.requiredLevel;
        this.skillPointCost = builder.skillPointCost;
        
        this.effects = new ArrayList<>(builder.effects);
        this.description = new ArrayList<>(builder.description);
        this.transferable = builder.transferable;
        this.maxLearners = builder.maxLearners;
    }
    
    // ===== GETTERS =====
    
    public String getId() { return id; }
    public String getName() { return name; }
    public UUID getCreatorUuid() { return creatorUuid; }
    public String getCreatorName() { return creatorName; }
    public Instant getCreatedAt() { return createdAt; }
    
    public SkillCategory getCategory() { return category; }
    public Element getElement() { return element; }
    public TargetType getTargetType() { return targetType; }
    
    public double getBasePower() { return basePower; }
    public double getCooldown() { return cooldown; }
    public double getManaCost() { return manaCost; }
    public double getRange() { return range; }
    public double getAreaRadius() { return areaRadius; }
    public int getProjectileCount() { return projectileCount; }
    public double getDuration() { return duration; }
    
    public CultivationRealm getRequiredRealm() { return requiredRealm; }
    public int getRequiredLevel() { return requiredLevel; }
    public int getSkillPointCost() { return skillPointCost; }
    
    public List<SkillEffect> getEffects() { return new ArrayList<>(effects); }
    public List<String> getDescription() { return new ArrayList<>(description); }
    public boolean isTransferable() { return transferable; }
    public int getMaxLearners() { return maxLearners; }
    
    public boolean hasElement() { return element != null; }
    
    /**
     * Lấy tên đầy đủ với màu sắc
     */
    public String getColoredName() {
        String prefix = element != null ? element.getColorCode() : category.getColorCode();
        return prefix + name;
    }
    
    // ===== SKILL EFFECT =====
    
    public static class SkillEffect {
        private final String effectType;    // e.g., "SLOW", "POISON", "BURN"
        private final int duration;         // ticks
        private final int amplifier;        // level
        
        public SkillEffect(String effectType, int duration, int amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }
        
        public String getEffectType() { return effectType; }
        public int getDuration() { return duration; }
        public int getAmplifier() { return amplifier; }
    }
    
    // ===== BUILDER =====
    
    public static class Builder {
        private String id;
        private String name;
        private UUID creatorUuid;
        private String creatorName;
        
        private SkillCategory category = SkillCategory.ATTACK;
        private Element element = null;
        private TargetType targetType = TargetType.SINGLE;
        
        private double basePower = 50;
        private double cooldown = 5;
        private double manaCost = 30;
        private double range = 5;
        private double areaRadius = 0;
        private int projectileCount = 1;
        private double duration = 0;
        
        private CultivationRealm requiredRealm = CultivationRealm.PHAMNHAN;
        private int requiredLevel = 1;
        private int skillPointCost = 0;
        
        private List<SkillEffect> effects = new ArrayList<>();
        private List<String> description = new ArrayList<>();
        private boolean transferable = true;
        private int maxLearners = 10;
        
        public Builder(String id) {
            this.id = id;
            this.name = id;
        }
        
        public Builder name(String name) { this.name = name; return this; }
        public Builder creator(UUID uuid, String name) { 
            this.creatorUuid = uuid; 
            this.creatorName = name; 
            return this; 
        }
        
        public Builder category(SkillCategory category) { this.category = category; return this; }
        public Builder element(Element element) { this.element = element; return this; }
        public Builder targetType(TargetType targetType) { this.targetType = targetType; return this; }
        
        public Builder basePower(double power) { this.basePower = power; return this; }
        public Builder cooldown(double cd) { this.cooldown = cd; return this; }
        public Builder manaCost(double cost) { this.manaCost = cost; return this; }
        public Builder range(double range) { this.range = range; return this; }
        public Builder areaRadius(double radius) { this.areaRadius = radius; return this; }
        public Builder projectileCount(int count) { this.projectileCount = count; return this; }
        public Builder duration(double duration) { this.duration = duration; return this; }
        
        public Builder requiredRealm(CultivationRealm realm) { this.requiredRealm = realm; return this; }
        public Builder requiredLevel(int level) { this.requiredLevel = level; return this; }
        public Builder skillPointCost(int cost) { this.skillPointCost = cost; return this; }
        
        public Builder addEffect(String type, int duration, int amplifier) {
            this.effects.add(new SkillEffect(type, duration, amplifier));
            return this;
        }
        
        public Builder description(List<String> desc) { 
            this.description = new ArrayList<>(desc); 
            return this; 
        }
        public Builder addDescription(String line) { 
            this.description.add(line); 
            return this; 
        }
        
        public Builder transferable(boolean t) { this.transferable = t; return this; }
        public Builder maxLearners(int max) { this.maxLearners = max; return this; }
        
        public SkillTemplate build() {
            return new SkillTemplate(this);
        }
    }
}
