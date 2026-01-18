package hcontrol.plugin.ai.behaviors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import hcontrol.plugin.ai.AggroTable;
import hcontrol.plugin.ai.MobBrain;
import hcontrol.plugin.entity.EntityProfile;

/**
 * WORLD BOSS PHASE BRAIN
 * AI cho world boss với phase-based mechanics
 * 
 * Phases:
 * - Phase 1 (100-70% HP): Normal attacks
 * - Phase 2 (70-40% HP): AoE attacks, summon minions
 * - Phase 3 (40-0% HP): Enrage, powerful skills
 */
public class WorldBossPhaseBrain implements MobBrain {
    
    private final AggroTable aggroTable;
    private final Random random = new Random();
    
    private int currentPhase = 1;
    private long lastPhaseChange = 0;
    private long lastSkillUse = 0;
    
    // Phase thresholds
    private static final double PHASE_2_THRESHOLD = 0.70;  // 70% HP
    private static final double PHASE_3_THRESHOLD = 0.40;  // 40% HP
    
    // Skill cooldowns (ticks)
    private static final long AOE_SKILL_COOLDOWN = 20L * 10;  // 10 giây
    private static final long ENRAGE_SKILL_COOLDOWN = 20L * 5;  // 5 giây
    
    public WorldBossPhaseBrain() {
        this.aggroTable = new AggroTable();
    }
    
    @Override
    public void tick(EntityProfile profile, LivingEntity entity) {
        // Check phase transitions
        checkPhaseTransition(profile, entity);
        
        // Update aggro (decay threat)
        aggroTable.decay(0.01);  // Decay 1% mỗi giây
        
        // Use skills based on phase
        usePhaseSkills(profile, entity);
    }
    
    /**
     * Check và chuyển phase nếu cần
     */
    private void checkPhaseTransition(EntityProfile profile, LivingEntity entity) {
        double healthPercent = profile.getCurrentHP() / profile.getMaxHP();
        int newPhase = 1;
        
        if (healthPercent <= PHASE_3_THRESHOLD) {
            newPhase = 3;
        } else if (healthPercent <= PHASE_2_THRESHOLD) {
            newPhase = 2;
        }
        
        if (newPhase != currentPhase) {
            changePhase(entity, newPhase);
        }
    }
    
    /**
     * Chuyển sang phase mới
     */
    private void changePhase(LivingEntity entity, int newPhase) {
        currentPhase = newPhase;
        lastPhaseChange = System.currentTimeMillis();
        
        // Visual effect
        Location loc = entity.getLocation();
        entity.getWorld().spawnParticle(Particle.TOTEM, loc, 100, 2.0, 2.0, 2.0, 0.1);
        entity.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.8f);
        
        // Announce
        String phaseName = switch (newPhase) {
            case 2 -> "§eAoE Phase";
            case 3 -> "§c§lENRAGE Phase";
            default -> "§7Normal Phase";
        };
        
        entity.getWorld().getPlayers().forEach(player -> {
            player.sendMessage("§c§l[WORLD BOSS] §7Chuyển sang " + phaseName + "!");
        });
        
        // Update BossEntity phase nếu có
        // Note: BossEntity phase sẽ được update từ BossEntity.nextPhase()
    }
    
    /**
     * Sử dụng skills theo phase
     */
    private void usePhaseSkills(EntityProfile profile, LivingEntity entity) {
        long currentTime = System.currentTimeMillis();
        long ticksSinceLastSkill = (currentTime - lastSkillUse) / 50;  // Convert to ticks
        
        switch (currentPhase) {
            case 1:
                // Phase 1: Normal attacks only
                break;
                
            case 2:
                // Phase 2: AoE attacks
                if (ticksSinceLastSkill >= AOE_SKILL_COOLDOWN) {
                    useAoESkill(profile, entity);
                    lastSkillUse = currentTime;
                }
                break;
                
            case 3:
                // Phase 3: Enrage skills
                if (ticksSinceLastSkill >= ENRAGE_SKILL_COOLDOWN) {
                    useEnrageSkill(profile, entity);
                    lastSkillUse = currentTime;
                }
                break;
        }
    }
    
    /**
     * AoE skill (Phase 2)
     */
    private void useAoESkill(EntityProfile profile, LivingEntity entity) {
        Location loc = entity.getLocation();
        
        // Visual effect
        entity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 5, 5.0, 2.0, 5.0, 0.1);
        entity.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        
        // Damage nearby players
        List<Player> nearby = new ArrayList<>(loc.getNearbyPlayers(10.0));
        for (Player player : nearby) {
            // TODO: Apply damage through CombatService
            player.sendMessage("§c§l[WORLD BOSS] §7Boss sử dụng AoE skill!");
        }
    }
    
    /**
     * Enrage skill (Phase 3)
     */
    private void useEnrageSkill(EntityProfile profile, LivingEntity entity) {
        Location loc = entity.getLocation();
        
        // Visual effect
        entity.getWorld().spawnParticle(Particle.LAVA, loc, 50, 3.0, 3.0, 3.0, 0.1);
        entity.getWorld().playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        
        // Damage nearby players
        List<Player> nearby = new ArrayList<>(loc.getNearbyPlayers(15.0));
        for (Player player : nearby) {
            // TODO: Apply damage through CombatService
            player.sendMessage("§c§l[WORLD BOSS] §7Boss sử dụng Enrage skill!");
        }
    }
    
    @Override
    public boolean shouldAttack(EntityProfile profile, LivingEntity target) {
        return true;  // Boss luôn tấn công
    }
    
    @Override
    public Player selectTarget(EntityProfile profile, LivingEntity entity, List<Player> nearbyPlayers) {
        if (nearbyPlayers.isEmpty()) {
            return null;
        }
        
        // Chọn target có aggro cao nhất
        UUID topAggro = aggroTable.getHighestThreat();
        if (topAggro != null) {
            for (Player player : nearbyPlayers) {
                if (player.getUniqueId().equals(topAggro)) {
                    return player;
                }
            }
        }
        
        // Fallback: chọn player gần nhất
        Player closest = null;
        double closestDist = Double.MAX_VALUE;
        Location bossLoc = entity.getLocation();
        
        for (Player player : nearbyPlayers) {
            double dist = player.getLocation().distance(bossLoc);
            if (dist < closestDist) {
                closestDist = dist;
                closest = player;
            }
        }
        
        return closest;
    }
    
    @Override
    public void onDamaged(EntityProfile profile, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Add aggro khi bị đánh
            double damage = profile.getMaxHP() - profile.getCurrentHP();  // Approximate
            aggroTable.addThreat(player.getUniqueId(), damage);
        }
    }
    
    @Override
    public double getAggroRange(EntityProfile profile) {
        return 50.0;  // World boss có aggro range lớn
    }
    
    @Override
    public double getCombatRange(EntityProfile profile) {
        return 10.0;  // Combat range
    }
    
    @Override
    public void reset(EntityProfile profile) {
        aggroTable.clear();
        currentPhase = 1;
        lastPhaseChange = 0;
        lastSkillUse = 0;
    }
    
    public AggroTable getAggroTable() {
        return aggroTable;
    }
    
    public int getCurrentPhase() {
        return currentPhase;
    }
}
