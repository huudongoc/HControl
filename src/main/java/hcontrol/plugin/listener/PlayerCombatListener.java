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
        event.setCancelled(true);
        
        // Apply damage vao tu tien HP
        double currentHP = profile.getCurrentHP();
        double newHP = Math.max(0, currentHP - vanillaDamage);
        profile.setCurrentHP(newHP);
        
        // Sync vanilla health (scale)
        var healthService = hcontrol.plugin.core.CoreContext.getInstance().getPlayerHealthService();
        healthService.updateCurrentHealth(player, profile);
        
        // Check chet
        if (newHP <= 0) {
            player.setHealth(0);
        }
        
        // Feedback message (optional)
        String causeMsg = switch(event.getCause()) {
            case FALL -> "§cRơi từ cao";
            case FIRE, FIRE_TICK, LAVA -> "§6Lửa";
            case DROWNING -> "§9Đuối nước";
            case SUFFOCATION -> "§7Ngạt";
            case VOID -> "§5Hư không";
            default -> "§cSát thương";
        };
        
        player.sendActionBar(String.format("§c-%.1f HP §7| %s §7| §c%.0f§7/§e%d", 
            vanillaDamage, causeMsg, newHP, profile.getStats().getMaxHP()));
    }
    
    /**
     * TAT HUNGER VANILLA - sau nay lam thanh co che tu tien
     * Tu si khong can an uong, chi can linh khi
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
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
            event.setCancelled(true);
            // TODO PHASE 4: Tu tien HP chi hoi tu dan duoc, thien dinh, linh thach...
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
