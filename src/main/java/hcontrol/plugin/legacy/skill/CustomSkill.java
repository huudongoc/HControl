package hcontrol.plugin.legacy.skill;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.SkillCost;
import hcontrol.plugin.skill.SkillType;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.*;

/**
 * CUSTOM SKILL - Công pháp do Sư Phụ tự tạo
 * Wraps PlayerSkill với thêm metadata về người tạo
 * 
 * ⚠️ DEPRECATED - LEGACY DATA MODEL
 * 
 * Class này chỉ dùng cho:
 * - Data migration từ CustomSkillManager cũ
 * - Import skill từ bản cũ
 * 
 * ❌ KHÔNG được dùng trong runtime logic
 * ✅ Architecture mới: SkillTemplateRegistry + SkillInstanceManager
 * 
 * @deprecated Sử dụng SkillTemplateRegistry và SkillInstanceManager thay thế
 * 
 * 📌 Package legacy/ - Cursor sẽ tự tránh code trong package này
 */
@Deprecated
public class CustomSkill {
    
    // Wrapped PlayerSkill
    private final PlayerSkill skill;
    
    // Creator info
    private final UUID creatorUuid;
    private String creatorName;
    private final Instant createdAt;
    
    // Custom skill settings
    private boolean transferable;           // Đệ tử có thể truyền tiếp không
    private int maxLearners;                // Số người tối đa học được
    private final Set<UUID> learners;       // Ai đã học skill này
    
    public CustomSkill(PlayerSkill skill, UUID creatorUuid, String creatorName) {
        this.skill = skill;
        this.creatorUuid = creatorUuid;
        this.creatorName = creatorName;
        this.createdAt = Instant.now();
        this.transferable = false;
        this.maxLearners = 10;
        this.learners = new HashSet<>();
    }
    
    // ===== DELEGATE TO PLAYERSKILL =====
    
    public String getSkillId() { return skill.getSkillId(); }
    public String getDisplayName() { return skill.getDisplayName(); }
    public SkillType getType() { return skill.getType(); }
    public SkillCost getCost() { return skill.getCost(); }
    public int getCooldown() { return skill.getCooldown(); }
    public double getDamageMultiplier() { return skill.getDamageMultiplier(); }
    public double getRange() { return skill.getRange(); }
    public CultivationRealm getMinRealm() { return skill.getMinRealm(); }
    public int getMinLevel() { return skill.getMinLevel(); }
    public List<PlayerSkill.SkillEffect> getEffects() { return skill.getEffects(); }
    public List<String> getDescription() { return skill.getDescription(); }
    public int getProjectileCount() { return skill.getProjectileCount(); }
    public double getAreaRadius() { return skill.getAreaRadius(); }
    
    public PlayerSkill getWrappedSkill() { return skill; }
    
    // ===== LEARNER MANAGEMENT =====
    
    public boolean addLearner(UUID learnerUuid) {
        if (learners.size() >= maxLearners) {
            return false;
        }
        return learners.add(learnerUuid);
    }
    
    public boolean removeLearner(UUID learnerUuid) {
        return learners.remove(learnerUuid);
    }
    
    public boolean hasLearned(UUID learnerUuid) {
        return learners.contains(learnerUuid);
    }
    
    public int getLearnerCount() {
        return learners.size();
    }
    
    public Set<UUID> getLearners() {
        return Collections.unmodifiableSet(learners);
    }
    
    public boolean canLearn() {
        return learners.size() < maxLearners;
    }
    
    // ===== GETTERS & SETTERS =====
    
    public UUID getCreatorUuid() {
        return creatorUuid;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public boolean isTransferable() {
        return transferable;
    }
    
    public void setTransferable(boolean transferable) {
        this.transferable = transferable;
    }
    
    public int getMaxLearners() {
        return maxLearners;
    }
    
    public void setMaxLearners(int maxLearners) {
        this.maxLearners = maxLearners;
    }
    
    public boolean isCustom() {
        return true;
    }
    
    /**
     * Builder for CustomSkill
     */
    public static class Builder {
        private String skillId;
        private String displayName;
        private SkillType type = SkillType.MELEE;
        private UUID creatorUuid;
        private String creatorName;
        
        private double damageMultiplier = 1.0;
        private double cost = 30;
        private int cooldown = 5;
        private double range = 5.0;
        private double areaRadius = 0;
        private int projectileCount = 1;
        
        private CultivationRealm minRealm = CultivationRealm.PHAMNHAN;
        private int minLevel = 1;
        
        private List<String> description = new ArrayList<>();
        private List<PlayerSkill.SkillEffect> effects = new ArrayList<>();
        
        private boolean transferable = false;
        private int maxLearners = 10;
        
        public Builder skillId(String skillId) {
            this.skillId = skillId;
            return this;
        }
        
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder type(SkillType type) {
            this.type = type;
            return this;
        }
        
        public Builder creator(UUID uuid, String name) {
            this.creatorUuid = uuid;
            this.creatorName = name;
            return this;
        }
        
        public Builder damageMultiplier(double dmg) {
            this.damageMultiplier = dmg;
            return this;
        }
        
        public Builder cost(double cost) {
            this.cost = cost;
            return this;
        }
        
        public Builder cooldown(int cd) {
            this.cooldown = cd;
            return this;
        }
        
        public Builder range(double range) {
            this.range = range;
            return this;
        }
        
        public Builder areaRadius(double radius) {
            this.areaRadius = radius;
            return this;
        }
        
        public Builder projectileCount(int count) {
            this.projectileCount = count;
            return this;
        }
        
        public Builder minRealm(CultivationRealm realm) {
            this.minRealm = realm;
            return this;
        }
        
        public Builder minLevel(int level) {
            this.minLevel = level;
            return this;
        }
        
        public Builder description(List<String> desc) {
            this.description = new ArrayList<>(desc);
            return this;
        }
        
        public Builder addEffect(PotionEffectType type, int duration, int amplifier) {
            this.effects.add(new PlayerSkill.SkillEffect(type, duration, amplifier));
            return this;
        }
        
        public Builder transferable(boolean t) {
            this.transferable = t;
            return this;
        }
        
        public Builder maxLearners(int max) {
            this.maxLearners = max;
            return this;
        }
        
        public CustomSkill build() {
            // Build PlayerSkill using its builder
            PlayerSkill.Builder psBuilder = new PlayerSkill.Builder(skillId)
                .displayName(displayName)
                .type(type)
                .cost(new SkillCost(cost))
                .cooldown(cooldown)
                .damageMultiplier(damageMultiplier)
                .range(range)
                .areaRadius(areaRadius)
                .projectileCount(projectileCount)
                .minRealm(minRealm)
                .minLevel(minLevel)
                .description(description);
            
            for (PlayerSkill.SkillEffect effect : effects) {
                psBuilder.addEffect(effect);
            }
            
            PlayerSkill skill = psBuilder.build();
            
            CustomSkill customSkill = new CustomSkill(skill, creatorUuid, creatorName);
            customSkill.setTransferable(transferable);
            customSkill.setMaxLearners(maxLearners);
            
            return customSkill;
        }
    }
}
