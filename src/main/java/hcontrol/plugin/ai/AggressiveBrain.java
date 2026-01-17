package hcontrol.plugin.ai;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.skill.MobSkill;
import hcontrol.plugin.skill.SkillExecutor;
import hcontrol.plugin.skill.SkillRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * PHASE 7 — AGGRESSIVE BRAIN
 * Mob tan cong ngay khi thay player trong range
 * VD: Zombie, Skeleton, Spider, hostile mobs
 * PHASE 7.2 — Dung skills khi combat
 */
public class AggressiveBrain implements MobBrain {
    
    private final AggroTable aggroTable = new AggroTable();
    private final Random random = new Random();
    
    @Override
    public void tick(EntityProfile profile, LivingEntity entity) {
        // Decay aggro theo thoi gian (5% moi giay)
        aggroTable.decay(0.05);
        
        // Neu mob da chet, clear aggro
        if (!entity.isValid() || entity.isDead()) {
            aggroTable.clear();
            return;
        }
        
        // Tim players gan
        Location loc = entity.getLocation();
        double aggroRange = getAggroRange(profile);
        
        List<Player> nearbyPlayers = entity.getWorld().getPlayers().stream()
            .filter(p -> p.getLocation().distance(loc) <= aggroRange)
            .filter(p -> !p.isDead())
            .filter(p -> p.getGameMode() == org.bukkit.GameMode.SURVIVAL || 
                         p.getGameMode() == org.bukkit.GameMode.ADVENTURE)
            .toList();
        
        if (nearbyPlayers.isEmpty()) {
            return;
        }
        
        // Chon target dua tren aggro
        Player target = selectTarget(profile, entity, nearbyPlayers);
        
        if (target != null && shouldAttack(profile, target)) {
            // Set entity target (Bukkit AI)
            if (entity instanceof org.bukkit.entity.Mob mob) {
                mob.setTarget(target);
            }
            
            // PHASE 7.2: Try use skill (20% chance)
            if (random.nextDouble() < 0.2) {
                tryUseSkill(entity, profile, target);
            }
        }
    }
    
    /**
     * PHASE 7.2: Thu dung skill
     */
    private void tryUseSkill(LivingEntity entity, EntityProfile profile, Player target) {
        try {
            CoreContext ctx = CoreContext.getInstance();
            SkillRegistry skillRegistry = ctx.getSkillRegistry();
            SkillExecutor skillExecutor = ctx.getSkillExecutor();
            
            if (skillRegistry == null || skillExecutor == null) {
                return;
            }
            
            // Lay skills cua mob
            List<MobSkill> skills = skillRegistry.getSkillsForMob(entity.getType());
            if (skills.isEmpty()) {
                return;
            }
            
            // Chon random skill
            MobSkill skill = skills.get(random.nextInt(skills.size()));
            
            // Execute skill
            skillExecutor.executeSkill(entity, profile, target, skill);
            
        } catch (Exception e) {
            // Ignore - skill system may not be initialized
        }
    }
    
    @Override
    public boolean shouldAttack(EntityProfile profile, LivingEntity target) {
        if (target == null || target.isDead()) {
            return false;
        }
        
        // Kiem tra distance
        LivingEntity entity = profile.getEntity();
        if (entity == null) {
            return false;
        }
        
        double distance = entity.getLocation().distance(target.getLocation());
        double combatRange = getCombatRange(profile);
        
        return distance <= combatRange;
    }
    
    @Override
    public Player selectTarget(EntityProfile profile, LivingEntity entity, List<Player> nearbyPlayers) {
        if (nearbyPlayers.isEmpty()) {
            return null;
        }
        
        // UU TIEN 1: Lay player co threat cao nhat
        UUID highestThreatUUID = aggroTable.getHighestThreat();
        if (highestThreatUUID != null) {
            Player highestThreatPlayer = Bukkit.getPlayer(highestThreatUUID);
            if (highestThreatPlayer != null && 
                highestThreatPlayer.isOnline() && 
                !highestThreatPlayer.isDead() &&
                nearbyPlayers.contains(highestThreatPlayer)) {
                return highestThreatPlayer;
            }
        }
        
        // UU TIEN 2: Lay player gan nhat
        Location loc = entity.getLocation();
        Player nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Player player : nearbyPlayers) {
            double distance = player.getLocation().distance(loc);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = player;
            }
        }
        
        // Them aggro cho player gan nhat
        if (nearest != null) {
            aggroTable.addThreat(nearest.getUniqueId(), 10.0);
        }
        
        return nearest;
    }
    
    @Override
    public void onDamaged(EntityProfile profile, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Them rat nhieu threat khi bi danh
            aggroTable.addThreat(player.getUniqueId(), 100.0);
        }
    }
    
    @Override
    public double getAggroRange(EntityProfile profile) {
        // Aggro range tang theo realm
        int realmLevel = profile.getRealm().ordinal();
        return 16.0 + (realmLevel * 2.0);  // 16-32 blocks
    }
    
    @Override
    public double getCombatRange(EntityProfile profile) {
        // Combat range la 3 blocks (melee range)
        return 3.0;
    }
    
    @Override
    public void reset(EntityProfile profile) {
        aggroTable.clear();
    }
    
    /**
     * Get aggro table (de external systems co the add threat)
     */
    public AggroTable getAggroTable() {
        return aggroTable;
    }
}
