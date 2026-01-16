package hcontrol.plugin.skill;

import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CombatService;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * PHASE 7.2 — SKILL EXECUTOR
 * Thuc thi skill logic
 */
public class SkillExecutor {
    
    private final CombatService combatService;
    private final SkillCooldownManager cooldownManager;
    
    public SkillExecutor(CombatService combatService, SkillCooldownManager cooldownManager) {
        this.combatService = combatService;
        this.cooldownManager = cooldownManager;
    }
    
    /**
     * Execute skill
     * 
     * @param caster Entity cast skill
     * @param casterProfile Profile cua caster
     * @param target Target (co the null voi buff/heal skills)
     * @param skill Skill de execute
     * @return true neu thanh cong
     */
    public boolean executeSkill(LivingEntity caster, EntityProfile casterProfile, 
                                 LivingEntity target, MobSkill skill) {
        // Kiem tra cooldown
        if (cooldownManager.isOnCooldown(caster.getUniqueId(), skill.getSkillId())) {
            return false;
        }
        
        // Kiem tra range (neu co target)
        if (target != null) {
            double distance = caster.getLocation().distance(target.getLocation());
            if (distance > skill.getRange()) {
                return false;
            }
        }
        
        // Execute theo type
        boolean success = switch (skill.getType()) {
            case MELEE -> executeMelee(caster, casterProfile, target, skill);
            case RANGED -> executeRanged(caster, casterProfile, target, skill);
            case AOE -> executeAOE(caster, casterProfile, skill);
            case BUFF -> executeBuff(caster, skill);
            case DEBUFF -> executeDebuff(target, skill);
            case SUMMON -> executeSummon(caster, skill);
            case TELEPORT -> executeTeleport(caster, target, skill);
            case HEAL -> executeHeal(caster, casterProfile, skill);
        };
        
        if (success) {
            // Set cooldown
            cooldownManager.setCooldown(caster.getUniqueId(), skill.getSkillId(), skill.getCooldown());
            
            // Visual/sound effects
            playSkillEffects(caster, skill);
        }
        
        return success;
    }
    
    private boolean executeMelee(LivingEntity caster, EntityProfile casterProfile, 
                                  LivingEntity target, MobSkill skill) {
        if (target == null) return false;
        
        // Apply custom damage through CombatService
        double baseDamage = casterProfile.getAttack() * skill.getDamageMultiplier();
        
        if (target instanceof Player player) {
            // Mob attack player (already handled by CombatService)
            // Just apply skill effects
            applyEffects(target, skill);
        }
        
        return true;
    }
    
    private boolean executeRanged(LivingEntity caster, EntityProfile casterProfile, 
                                   LivingEntity target, MobSkill skill) {
        if (target == null) return false;
        
        Location casterLoc = caster.getEyeLocation();
        Vector direction = target.getEyeLocation().toVector()
            .subtract(casterLoc.toVector())
            .normalize();
        
        int projectiles = skill.getProjectileCount();
        
        for (int i = 0; i < projectiles; i++) {
            // Spread projectiles neu nhieu hon 1
            Vector spread = direction.clone();
            if (projectiles > 1) {
                double spreadAngle = (i - projectiles / 2.0) * 0.1;
                spread.rotateAroundY(spreadAngle);
            }
            
            // Spawn arrow
            Arrow arrow = caster.getWorld().spawnArrow(
                casterLoc, 
                spread, 
                1.6f, 
                12.0f
            );
            arrow.setShooter(caster);
            
            // Apply skill effects to arrow metadata
            if (!skill.getEffects().isEmpty()) {
                arrow.setMetadata("skill_effects", 
                    new org.bukkit.metadata.FixedMetadataValue(
                        org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
                        skill.getSkillId()
                    )
                );
            }
        }
        
        return true;
    }
    
    private boolean executeAOE(LivingEntity caster, EntityProfile casterProfile, MobSkill skill) {
        Location center = caster.getLocation();
        double radius = skill.getAreaRadius();
        
        // Find all players in radius
        List<Player> nearbyPlayers = center.getWorld().getPlayers().stream()
            .filter(p -> p.getLocation().distance(center) <= radius)
            .filter(p -> !p.isDead())
            .toList();
        
        for (Player player : nearbyPlayers) {
            // Apply damage and effects
            applyEffects(player, skill);
        }
        
        // Spawn visual effect
        center.getWorld().spawnParticle(
            Particle.EXPLOSION_LARGE, 
            center, 
            (int)(radius * 10), 
            radius, radius, radius
        );
        
        return true;
    }
    
    private boolean executeBuff(LivingEntity caster, MobSkill skill) {
        applyEffects(caster, skill);
        return true;
    }
    
    private boolean executeDebuff(LivingEntity target, MobSkill skill) {
        if (target == null) return false;
        applyEffects(target, skill);
        return true;
    }
    
    private boolean executeSummon(LivingEntity caster, MobSkill skill) {
        // TODO PHASE 7.3: Summon minions
        return false;
    }
    
    private boolean executeTeleport(LivingEntity caster, LivingEntity target, MobSkill skill) {
        if (target == null) return false;
        
        // Teleport behind target
        Location targetLoc = target.getLocation();
        Vector direction = targetLoc.getDirection().multiply(-2); // 2 blocks behind
        Location teleportLoc = targetLoc.add(direction);
        
        caster.teleport(teleportLoc);
        
        // Spawn teleport particles
        caster.getWorld().spawnParticle(Particle.PORTAL, caster.getLocation(), 50);
        
        return true;
    }
    
    private boolean executeHeal(LivingEntity caster, EntityProfile casterProfile, MobSkill skill) {
        double healAmount = casterProfile.getMaxHP() * 0.3; // heal 30% max HP
        double newHP = Math.min(casterProfile.getCurrentHP() + healAmount, casterProfile.getMaxHP());
        casterProfile.setCurrentHP(newHP);
        
        // Visual
        caster.getWorld().spawnParticle(Particle.HEART, caster.getLocation().add(0, 1, 0), 10);
        
        return true;
    }
    
    /**
     * Apply potion effects
     */
    private void applyEffects(LivingEntity target, MobSkill skill) {
        for (MobSkill.SkillEffect effect : skill.getEffects()) {
            PotionEffect potionEffect = new PotionEffect(
                effect.getEffectType(),
                effect.getDuration(),
                effect.getAmplifier(),
                false,
                true
            );
            target.addPotionEffect(potionEffect);
        }
    }
    
    /**
     * Play skill visual/sound effects
     */
    private void playSkillEffects(LivingEntity caster, MobSkill skill) {
        Location loc = caster.getLocation();
        
        // Particle dựa trên skill type
        Particle particle = switch (skill.getType()) {
            case MELEE -> Particle.CRIT;
            case RANGED -> Particle.FLAME;
            case AOE -> Particle.EXPLOSION_LARGE;
            case BUFF -> Particle.ENCHANTMENT_TABLE;
            case DEBUFF -> Particle.SMOKE_LARGE;
            case SUMMON -> Particle.PORTAL;
            case TELEPORT -> Particle.END_ROD;
            case HEAL -> Particle.HEART;
        };
        
        caster.getWorld().spawnParticle(particle, loc.add(0, 1, 0), 20);
        
        // Sound
        Sound sound = switch (skill.getType()) {
            case MELEE -> Sound.ENTITY_PLAYER_ATTACK_STRONG;
            case RANGED -> Sound.ENTITY_ARROW_SHOOT;
            case AOE -> Sound.ENTITY_GENERIC_EXPLODE;
            case BUFF -> Sound.ENTITY_PLAYER_LEVELUP;
            case DEBUFF -> Sound.ENTITY_SPLASH_POTION_BREAK;
            case SUMMON -> Sound.ENTITY_ENDERMAN_TELEPORT;
            case TELEPORT -> Sound.ENTITY_ENDERMAN_TELEPORT;
            case HEAL -> Sound.ENTITY_PLAYER_LEVELUP;
        };
        
        caster.getWorld().playSound(loc, sound, 1.0f, 1.0f);
    }
}
