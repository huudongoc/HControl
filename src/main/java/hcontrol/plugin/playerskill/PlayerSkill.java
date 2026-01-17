package hcontrol.plugin.playerskill;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.skill.SkillType;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 6 — PLAYER SKILL
 * Definition cua skill cho player
 * Tuong tu MobSkill nhung co them cost & learning requirements
 */
public class PlayerSkill {
    
    private final String skillId;
    private final String displayName;
    private final SkillType type;
    private final SkillCost cost;
    private final int cooldown;  // seconds
    private final double damageMultiplier;
    private final double range;
    private final CultivationRealm minRealm;
    private final int minLevel;
    private final List<SkillEffect> effects;
    private final List<String> description;
    
    // Special properties
    private final int projectileCount;
    private final double areaRadius;
    
    private PlayerSkill(Builder builder) {
        this.skillId = builder.skillId;
        this.displayName = builder.displayName;
        this.type = builder.type;
        this.cost = builder.cost;
        this.cooldown = builder.cooldown;
        this.damageMultiplier = builder.damageMultiplier;
        this.range = builder.range;
        this.minRealm = builder.minRealm;
        this.minLevel = builder.minLevel;
        this.effects = builder.effects;
        this.description = builder.description;
        this.projectileCount = builder.projectileCount;
        this.areaRadius = builder.areaRadius;
    }
    
    // ========== GETTERS ==========
    
    public String getSkillId() { return skillId; }
    public String getDisplayName() { return displayName; }
    public SkillType getType() { return type; }
    public SkillCost getCost() { return cost; }
    public int getCooldown() { return cooldown; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getRange() { return range; }
    public CultivationRealm getMinRealm() { return minRealm; }
    public int getMinLevel() { return minLevel; }
    public List<SkillEffect> getEffects() { return new ArrayList<>(effects); }
    public List<String> getDescription() { return new ArrayList<>(description); }
    public int getProjectileCount() { return projectileCount; }
    public double getAreaRadius() { return areaRadius; }
    
    // ========== BUILDER ==========
    
    public static class Builder {
        private final String skillId;
        private String displayName;
        private SkillType type = SkillType.MELEE;
        private SkillCost cost = new SkillCost(0);
        private int cooldown = 5;
        private double damageMultiplier = 1.0;
        private double range = 5.0;
        private CultivationRealm minRealm = CultivationRealm.PHAMNHAN;
        private int minLevel = 1;
        private List<SkillEffect> effects = new ArrayList<>();
        private List<String> description = new ArrayList<>();
        private int projectileCount = 1;
        private double areaRadius = 5.0;
        
        public Builder(String skillId) {
            this.skillId = skillId;
            this.displayName = skillId;
        }
        
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder type(SkillType type) {
            this.type = type;
            return this;
        }
        
        public Builder cost(SkillCost cost) {
            this.cost = cost;
            return this;
        }
        
        public Builder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }
        
        public Builder damageMultiplier(double damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
            return this;
        }
        
        public Builder range(double range) {
            this.range = range;
            return this;
        }
        
        public Builder minRealm(CultivationRealm minRealm) {
            this.minRealm = minRealm;
            return this;
        }
        
        public Builder minLevel(int minLevel) {
            this.minLevel = minLevel;
            return this;
        }
        
        public Builder effects(List<SkillEffect> effects) {
            this.effects = new ArrayList<>(effects);
            return this;
        }
        
        public Builder addEffect(SkillEffect effect) {
            this.effects.add(effect);
            return this;
        }
        
        public Builder description(List<String> description) {
            this.description = new ArrayList<>(description);
            return this;
        }
        
        public Builder addDescription(String line) {
            this.description.add(line);
            return this;
        }
        
        public Builder projectileCount(int projectileCount) {
            this.projectileCount = projectileCount;
            return this;
        }
        
        public Builder areaRadius(double areaRadius) {
            this.areaRadius = areaRadius;
            return this;
        }
        
        public PlayerSkill build() {
            return new PlayerSkill(this);
        }
    }
    
    // ========== SKILL EFFECT ==========
    
    /**
     * Skill effect (potion effect)
     * Tuong tu MobSkill.SkillEffect
     */
    public static class SkillEffect {
        private final PotionEffectType effectType;
        private final int duration;  // ticks
        private final int amplifier;
        
        public SkillEffect(PotionEffectType effectType, int duration, int amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }
        
        public PotionEffectType getEffectType() { return effectType; }
        public int getDuration() { return duration; }
        public int getAmplifier() { return amplifier; }
    }
}
