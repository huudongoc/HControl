package hcontrol.plugin.ai;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import hcontrol.plugin.entity.EntityProfile;

/**
 * PHASE 7 — PASSIVE BRAIN
 * Mob khong tan cong, chi chay tron khi bi danh
 * VD: Pig, Cow, Chicken, peaceful animals
 */
public class PassiveBrain implements MobBrain {
    
    private static final double FLEE_RANGE = 8.0;  // chay tron khi player gan 8 blocks
    private static final double FLEE_SPEED = 1.5;  // toc do chay tron
    
    @Override
    public void tick(EntityProfile profile, LivingEntity entity) {
        // Passive mob khong chu dong tan cong
        // Chi wander hoac chay tron
        
        // TODO PHASE 7.1: Implement wander behavior (di chuyen ngau nhien)
    }
    
    @Override
    public boolean shouldAttack(EntityProfile profile, LivingEntity target) {
        // Passive mob khong bao gio tan cong
        return false;
    }
    
    @Override
    public Player selectTarget(EntityProfile profile, LivingEntity entity, List<Player> nearbyPlayers) {
        // Passive mob khong co target
        return null;
    }
    
    @Override
    public void onDamaged(EntityProfile profile, LivingEntity attacker) {
        // Khi bi danh, chay tron
        if (profile.getEntity() != null) {
            LivingEntity entity = profile.getEntity();
            Location entityLoc = entity.getLocation();
            Location attackerLoc = attacker.getLocation();
            
            // Tinh huong chay tron (nguoc voi attacker)
            Vector fleeDirection = entityLoc.toVector()
                .subtract(attackerLoc.toVector())
                .normalize()
                .multiply(FLEE_SPEED);
            
            entity.setVelocity(fleeDirection);
        }
    }
    
    @Override
    public double getAggroRange(EntityProfile profile) {
        // Passive mob khong co aggro range
        return 0;
    }
    
    @Override
    public double getCombatRange(EntityProfile profile) {
        // Passive mob khong combat
        return 0;
    }
    
    @Override
    public void reset(EntityProfile profile) {
        // Khong can reset gi
    }
}
