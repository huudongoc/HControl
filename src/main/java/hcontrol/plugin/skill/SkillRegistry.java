package hcontrol.plugin.skill;

import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * PHASE 7.2 — SKILL REGISTRY
 * Dang ky skills cho moi mob type
 */
public class SkillRegistry {
    
    // Map: SkillId -> MobSkill
    private final Map<String, MobSkill> skills = new HashMap<>();
    
    // Map: EntityType -> List<SkillId>
    private final Map<EntityType, List<String>> mobSkills = new HashMap<>();
    
    public SkillRegistry() {
        registerDefaultSkills();
    }
    
    /**
     * Dang ky default skills cho vanilla mobs
     */
    private void registerDefaultSkills() {
        // ===== ZOMBIE SKILLS =====
        registerSkill(new MobSkill.Builder("zombie_bite")
            .displayName("§cZombie Bite")
            .type(SkillType.MELEE)
            .cooldown(8)
            .damageMultiplier(1.5)
            .range(3.0)
            .effects(List.of(
                new MobSkill.SkillEffect(PotionEffectType.POISON, 60, 0) // 3s poison
            ))
            .build());
        
        registerSkill(new MobSkill.Builder("zombie_rage")
            .displayName("§4Zombie Rage")
            .type(SkillType.BUFF)
            .cooldown(20)
            .damageMultiplier(0) // no damage, pure buff
            .range(0)
            .effects(List.of(
                new MobSkill.SkillEffect(PotionEffectType.SPEED, 100, 1),      // 5s speed II
                new MobSkill.SkillEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0)    // 5s strength I
            ))
            .build());
        
        registerMobSkill(EntityType.ZOMBIE, "zombie_bite", "zombie_rage");
        
        // ===== SKELETON SKILLS =====
        registerSkill(new MobSkill.Builder("skeleton_multishot")
            .displayName("§eMulti-shot")
            .type(SkillType.RANGED)
            .cooldown(10)
            .damageMultiplier(0.8)
            .range(32.0)
            .projectileCount(3)
            .build());
        
        registerSkill(new MobSkill.Builder("skeleton_poison_arrow")
            .displayName("§2Poison Arrow")
            .type(SkillType.RANGED)
            .cooldown(15)
            .damageMultiplier(1.0)
            .range(32.0)
            .effects(List.of(
                new MobSkill.SkillEffect(PotionEffectType.POISON, 80, 1) // 4s poison II
            ))
            .build());
        
        registerMobSkill(EntityType.SKELETON, "skeleton_multishot", "skeleton_poison_arrow");
        
        // ===== SPIDER SKILLS =====
        registerSkill(new MobSkill.Builder("spider_web")
            .displayName("§fWeb Trap")
            .type(SkillType.DEBUFF)
            .cooldown(12)
            .damageMultiplier(0.5)
            .range(5.0)
            .effects(List.of(
                new MobSkill.SkillEffect(PotionEffectType.SLOW, 100, 3) // 5s slowness IV
            ))
            .build());
        
        registerSkill(new MobSkill.Builder("spider_leap")
            .displayName("§aLeap Attack")
            .type(SkillType.MELEE)
            .cooldown(8)
            .damageMultiplier(1.3)
            .range(10.0)
            .build());
        
        registerMobSkill(EntityType.SPIDER, "spider_web", "spider_leap");
        
        // ===== CREEPER SKILLS =====
        registerSkill(new MobSkill.Builder("creeper_mini_explosion")
            .displayName("§cMini Blast")
            .type(SkillType.AOE)
            .cooldown(15)
            .damageMultiplier(2.0)
            .range(5.0)
            .areaRadius(3.0)
            .build());
        
        registerMobSkill(EntityType.CREEPER, "creeper_mini_explosion");
        
        // ===== WITCH SKILLS =====
        registerSkill(new MobSkill.Builder("witch_poison_cloud")
            .displayName("§5Poison Cloud")
            .type(SkillType.AOE)
            .cooldown(20)
            .damageMultiplier(0.5)
            .range(16.0)
            .areaRadius(4.0)
            .effects(List.of(
                new MobSkill.SkillEffect(PotionEffectType.POISON, 100, 1)
            ))
            .build());
        
        registerSkill(new MobSkill.Builder("witch_heal")
            .displayName("§dSelf Heal")
            .type(SkillType.HEAL)
            .cooldown(30)
            .damageMultiplier(0)
            .range(0)
            .build());
        
        registerMobSkill(EntityType.WITCH, "witch_poison_cloud", "witch_heal");
        
        // ===== BLAZE SKILLS =====
        registerSkill(new MobSkill.Builder("blaze_fireball_barrage")
            .displayName("§6Fireball Barrage")
            .type(SkillType.RANGED)
            .cooldown(12)
            .damageMultiplier(0.7)
            .range(32.0)
            .projectileCount(5)
            .build());
        
        registerMobSkill(EntityType.BLAZE, "blaze_fireball_barrage");
        
        // ===== ENDERMAN SKILLS =====
        registerSkill(new MobSkill.Builder("enderman_teleport_strike")
            .displayName("§dTeleport Strike")
            .type(SkillType.TELEPORT)
            .cooldown(10)
            .damageMultiplier(1.8)
            .range(16.0)
            .build());
        
        registerMobSkill(EntityType.ENDERMAN, "enderman_teleport_strike");
    }
    
    /**
     * Dang ky skill moi
     */
    public void registerSkill(MobSkill skill) {
        skills.put(skill.getSkillId(), skill);
    }
    
    /**
     * Dang ky skills cho mob type
     */
    public void registerMobSkill(EntityType entityType, String... skillIds) {
        mobSkills.put(entityType, Arrays.asList(skillIds));
    }
    
    /**
     * Lay skill theo ID
     */
    public MobSkill getSkill(String skillId) {
        return skills.get(skillId);
    }
    
    /**
     * Lay tat ca skills cua mob type
     */
    public List<MobSkill> getSkillsForMob(EntityType entityType) {
        List<String> skillIds = mobSkills.get(entityType);
        if (skillIds == null) {
            return List.of();
        }
        
        List<MobSkill> result = new ArrayList<>();
        for (String skillId : skillIds) {
            MobSkill skill = skills.get(skillId);
            if (skill != null) {
                result.add(skill);
            }
        }
        return result;
    }
    
    /**
     * Check mob co skills khong
     */
    public boolean hasSkills(EntityType entityType) {
        List<String> skillIds = mobSkills.get(entityType);
        return skillIds != null && !skillIds.isEmpty();
    }
}
