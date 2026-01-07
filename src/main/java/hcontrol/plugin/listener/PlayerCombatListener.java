package hcontrol.plugin.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CombatService;

/**
 * PHASE 3 — COMBAT LISTENER
 * Cancel vanilla damage + apply custom combat
 */
public class PlayerCombatListener implements Listener {

    private final PlayerManager playerManager;
    private final CombatService combatService;
    
    public PlayerCombatListener(PlayerManager playerManager, CombatService combatService) {
        this.playerManager = playerManager;
        this.combatService = combatService;
    }
    
    /**
     * Handle player attack + mob attack player
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // cancel vanilla damage
        event.setCancelled(true);
        
        // CASE 1: Player attack (player/mob)
        if (event.getDamager() instanceof Player attacker) {
            if (!(event.getEntity() instanceof LivingEntity target)) {
                return;
            }
            
            PlayerProfile attackerProfile = playerManager.get(attacker.getUniqueId());
            if (attackerProfile == null) {
                return; // chua load profile
            }
            
            // reset attack cooldown (danh nhanh hon vanilla)
            attacker.resetCooldown();
            
            // set attack speed based on AGI (nhanh hon = AGI cao)
            updateAttackSpeed(attacker, attackerProfile);
            
            // player danh target
            combatService.handlePlayerAttack(attacker, target, attackerProfile);
            return;
        }
        
        // CASE 2: Mob attack player
        if (event.getEntity() instanceof Player player) {
            if (!(event.getDamager() instanceof LivingEntity mob)) {
                return;
            }
            
            PlayerProfile playerProfile = playerManager.get(player.getUniqueId());
            if (playerProfile == null) {
                return;
            }
            
            // mob danh player
            combatService.handleMobAttackPlayer(mob, player, playerProfile);
        }
    }
    
    /**
     * Cancel tat ca damage khac (fall, fire, drown...)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        // chi cancel damage cua player (giu vanilla cho mob)
        if (event.getEntity() instanceof Player) {
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK &&
                event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
                // fall, fire, drown... van giu vanilla
                // sau nay co the lam custom
            }
        }
    }
    
    /**
     * Update attack speed based on AGI stat
     */
    private void updateAttackSpeed(Player player, PlayerProfile profile) {
        // attack speed vanilla = 4.0 (0.25s cooldown)
        // tang them 0.1 moi 10 AGI
        int agility = profile.getStats().getAgility();
        double attackSpeed = 4.0 + (agility / 10.0) * 0.5; // max ~6.5 voi 50 AGI
        
        var attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(attackSpeed);
        }
    }
}
