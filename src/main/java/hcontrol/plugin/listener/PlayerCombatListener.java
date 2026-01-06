package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import hcontrol.plugin.service.DisableDameService;

/**
 * PHASE 3 — COMBAT LISTENER
 * Cancel tất cả damage/heal vanilla
 * Chuẩn bị cho custom combat system
 */
public class PlayerCombatListener implements Listener {

    private final DisableDameService disableDameService;
    
    public PlayerCombatListener(DisableDameService disableDameService) {
        this.disableDameService = disableDameService;
    }
    
    /**
     * Cancel entity damage by entity (PvP, PvE)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        disableDameService.cancelDamageByEntity(event);
    }
    
    /**
     * Cancel tất cả damage (fall, fire, drowning...)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        disableDameService.cancelAllDamage(event);
    }
    
    /**
     * Cancel vanilla health regen
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        disableDameService.cancelHealthRegen(event);
    }
    
    /**
     * Cancel food level change (hunger)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        disableDameService.cancelFoodChange(event);
    }
}
