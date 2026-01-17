package hcontrol.plugin.skill;

import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * PHASE 7.2 — MOB SKILL
 * Dinh nghia skill cua mob (data-driven)
 * 
 * VD: Zombie Bite - gay damage va poison
 */
public class MobSkill {
    
    private final String skillId;
    private final String displayName;
    private final SkillType type;
    
    // Cooldown (giay)
    private final int cooldown;
    
    // Damage modifier (1.0 = 100%, 1.5 = 150%)
    private final double damageMultiplier;
    
    // Range (blocks)
    private final double range;
    
    // Effects
    private final List<SkillEffect> effects;
    
    // Special properties
    private final int projectileCount;  // cho multishot/spread
    private final boolean piercing;     // xem co xuyen qua target khong
    private final double areaRadius;    // AOE radius (0 = single target)
    
    public MobSkill(String skillId, String displayName, SkillType type, 
                    int cooldown, double damageMultiplier, double range,
                    List<SkillEffect> effects, int projectileCount, 
                    boolean piercing, double areaRadius) {
        this.skillId = skillId;
        this.displayName = displayName;
        this.type = type;
        this.cooldown = cooldown;
        this.damageMultiplier = damageMultiplier;
        this.range = range;
        this.effects = effects;
        this.projectileCount = projectileCount;
        this.piercing = piercing;
        this.areaRadius = areaRadius;
    }
    
    // ===== GETTERS =====
    
    public String getSkillId() { return skillId; }
    public String getDisplayName() { return displayName; }
    public SkillType getType() { return type; }
    public int getCooldown() { return cooldown; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getRange() { return range; }
    public List<SkillEffect> getEffects() { return effects; }
    public int getProjectileCount() { return projectileCount; }
    public boolean isPiercing() { return piercing; }
    public double getAreaRadius() { return areaRadius; }
    
    /**
     * Builder pattern de tao skill de dang
     */
    public static class Builder {
        private String skillId;
        private String displayName;
        private SkillType type = SkillType.MELEE;
        private int cooldown = 5;
        private double damageMultiplier = 1.0;
        private double range = 3.0;
        private List<SkillEffect> effects = List.of();
        private int projectileCount = 1;
        private boolean piercing = false;
        private double areaRadius = 0;
        
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
        
        public Builder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }
        
        public Builder damageMultiplier(double multiplier) {
            this.damageMultiplier = multiplier;
            return this;
        }
        
        public Builder range(double range) {
            this.range = range;
            return this;
        }
        
        public Builder effects(List<SkillEffect> effects) {
            this.effects = effects;
            return this;
        }
        
        public Builder projectileCount(int count) {
            this.projectileCount = count;
            return this;
        }
        
        public Builder piercing(boolean piercing) {
            this.piercing = piercing;
            return this;
        }
        
        public Builder areaRadius(double radius) {
            this.areaRadius = radius;
            return this;
        }
        
        public MobSkill build() {
            return new MobSkill(skillId, displayName, type, cooldown, 
                damageMultiplier, range, effects, projectileCount, 
                piercing, areaRadius);
        }
    }
    
    /**
     * Skill Effect (poison, slow, stun...)
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
