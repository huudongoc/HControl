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
import hcontrol.plugin.service.DisableDameService;

/**
 * PHASE 3 — COMBAT LISTENER
 * Cancel vanilla damage + apply custom combat
 */
public class PlayerCombatListener implements Listener {

    private final PlayerManager playerManager;
    private final CombatService combatService;
    private final DisableDameService disableDameService;
    
    public PlayerCombatListener(PlayerManager playerManager, CombatService combatService, DisableDameService disableDameService) {
        this.playerManager = playerManager;
        this.combatService = combatService;
        this.disableDameService = disableDameService;
    }
    
    /**
     * Handle player attack + mob attack player
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // cancel vanilla damage
        disableDameService.cancelDamageByEntity(event);
        
        // CASE 1: Player attack (player/mob)
        if (event.getDamager() instanceof Player attacker) {
            if (!(event.getEntity() instanceof LivingEntity target)) {
                return;
            }
            
            // CHECK: Target da chet - khong cho tan cong
            if (target.isDead()) {
                event.setCancelled(true);
                return; // Target da chet, cancel event
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
            
            // CHECK: Player da chet - khong cho mob tan cong
            if (player.isDead()) {
                event.setCancelled(true);
                return; // Player da chet, cancel event
            }
            
            PlayerProfile playerProfile = playerManager.get(player.getUniqueId());
            if (playerProfile == null) {
                return;
            }
            
            // CHECK: Player HP = 0 - da chet
            if (playerProfile.getCurrentHP() <= 0) {
                event.setCancelled(true);
                return; // Player da chet trong profile, cancel event
            }
            
            // mob danh player
            combatService.handleMobAttackPlayer(mob, player, playerProfile);
        }
    }
    
    /**
     * Cancel tat ca damage khac (fall, fire, drown...)
     * VA sync vanilla damage -> tu tien HP
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageEvent event) {
        // chi xu ly player damage
        if (!(event.getEntity() instanceof Player player)) return;
        
        // BO QUA neu la EntityDamageByEntityEvent - da xu ly o tren
        if (event instanceof EntityDamageByEntityEvent) return;
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        // Lay damage tu event (fall, fire, drown, void...)
        double vanillaDamage = event.getFinalDamage();
        
        // Cancel vanilla damage
        disableDameService.cancelAllDamage(event);
        
        // Delegate damage calculation to CombatService
        String damageMessage = combatService.handleEnvironmentalDamage(player, profile, vanillaDamage, event.getCause());
        player.sendActionBar(damageMessage);
    }
    
    /**
     * TAT HUNGER VANILLA - sau nay lam thanh co che tu tien
     * Tu si khong can an uong, chi can linh khi
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            disableDameService.cancelFoodChange(event);
            // TODO PHASE 4: Replace voi co che Linh Khi / Tu Luyen
            // - An dan duoc: tang Linh Khi
            // - Thien dinh: hoi phuc Linh Khi
            // - Doi = het Linh Khi
        }
    }
    
    /**
     * TAT VANILLA HEALTH REGENERATION
     * Tu tien HP khong hoi phuc tu dong tu hunger
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(org.bukkit.event.entity.EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        // Chi tat regen tu hunger/saturation, giu lai healing khac (dan duoc, skill...)
        if (event.getRegainReason() == org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED) {
            disableDameService.cancelHealthRegen(event);
            // TODO PHASE 4: Tu tien HP chi hoi tu dan duoc, thien dinh, linh thach...
        }
    }
    
    /**
     * Update attack speed based on AGI stat
     */
    private void updateAttackSpeed(Player player, PlayerProfile profile) {
        // attack speed vanilla = 1.0 (1 tick cooldown)
        // tang them 0.1 moi 10 AGI
        int agility = profile.getStats().getAgility();
        double attackSpeed = 1.0 + (agility / 10.0) * 0.5; // max ~1.5 voi 50 AGI
        
        var attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(attackSpeed);
        }
    }
}
