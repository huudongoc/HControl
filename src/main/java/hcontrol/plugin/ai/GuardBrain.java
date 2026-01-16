package hcontrol.plugin.ai;

import hcontrol.plugin.entity.EntityProfile;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * PHASE 7 — GUARD BRAIN
 * Mob chi tan cong khi:
 * - Bi danh
 * - Player qua gan (invade personal space)
 * 
 * VD: Iron Golem, Villager Guard, Neutral creatures
 */
public class GuardBrain implements MobBrain {
    
    private final AggroTable aggroTable = new AggroTable();
    private Location spawnLocation;  // vi tri spawn de guard quay ve
    private static final double PERSONAL_SPACE = 3.0;  // player gan hon 3 blocks = aggro
    private static final double RETURN_RANGE = 32.0;  // xa spawn point hon 32 blocks = quay ve
    
    @Override
    public void tick(EntityProfile profile, LivingEntity entity) {
        // Luu spawn location lan dau
        if (spawnLocation == null) {
            spawnLocation = entity.getLocation().clone();
        }
        
        // Decay aggro nhanh hon aggressive mob (10% moi giay)
        aggroTable.decay(0.10);
        
        // Neu khong co aggro, quay ve spawn point
        if (aggroTable.isEmpty()) {
            Location current = entity.getLocation();
            double distanceFromSpawn = current.distance(spawnLocation);
            
            if (distanceFromSpawn > RETURN_RANGE) {
                // Qua xa spawn point, teleport ve
                entity.teleport(spawnLocation);
                return;
            }
            
            // TODO PHASE 7.1: Di chuyen ve spawn location tu tu
            return;
        }
        
        // Co aggro, tim target
        Location loc = entity.getLocation();
        double combatRange = getCombatRange(profile);
        
        List<Player> nearbyPlayers = entity.getWorld().getPlayers().stream()
            .filter(p -> p.getLocation().distance(loc) <= combatRange)
            .filter(p -> !p.isDead())
            .filter(p -> p.getGameMode() == org.bukkit.GameMode.SURVIVAL || 
                         p.getGameMode() == org.bukkit.GameMode.ADVENTURE)
            .toList();
        
        Player target = selectTarget(profile, entity, nearbyPlayers);
        
        if (target != null && shouldAttack(profile, target)) {
            if (entity instanceof org.bukkit.entity.Mob mob) {
                mob.setTarget(target);
            }
        }
    }
    
    @Override
    public boolean shouldAttack(EntityProfile profile, LivingEntity target) {
        if (target == null || target.isDead()) {
            return false;
        }
        
        // Chi tan cong neu trong combat range
        LivingEntity entity = profile.getEntity();
        if (entity == null) {
            return false;
        }
        
        double distance = entity.getLocation().distance(target.getLocation());
        return distance <= getCombatRange(profile);
    }
    
    @Override
    public Player selectTarget(EntityProfile profile, LivingEntity entity, List<Player> nearbyPlayers) {
        // Tim player co threat cao nhat
        for (Player player : nearbyPlayers) {
            double threat = aggroTable.getThreat(player.getUniqueId());
            if (threat > 0) {
                return player;
            }
        }
        
        // Kiem tra player co vao personal space khong
        Location loc = entity.getLocation();
        for (Player player : nearbyPlayers) {
            double distance = player.getLocation().distance(loc);
            if (distance < PERSONAL_SPACE) {
                // Player qua gan, them aggro
                aggroTable.addThreat(player.getUniqueId(), 50.0);
                return player;
            }
        }
        
        return null;
    }
    
    @Override
    public void onDamaged(EntityProfile profile, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Them threat khi bi danh
            aggroTable.addThreat(player.getUniqueId(), 100.0);
        }
    }
    
    @Override
    public double getAggroRange(EntityProfile profile) {
        // Guard chi aggro trong personal space
        return PERSONAL_SPACE;
    }
    
    @Override
    public double getCombatRange(EntityProfile profile) {
        // Combat range lon hon aggro range (de duoi theo attacker)
        return 16.0;
    }
    
    @Override
    public void reset(EntityProfile profile) {
        aggroTable.clear();
        spawnLocation = null;
    }
}
