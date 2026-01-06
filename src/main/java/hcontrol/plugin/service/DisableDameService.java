package hcontrol.plugin.service;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * PHASE 3 — DISABLE DAMAGE SERVICE
 * Cancel tất cả damage/heal vanilla
 * Chuẩn bị cho custom combat system
 */
public class DisableDameService {
    
    /**
     * Cancel damage từ entity khác (PvP, PvE)
     */
    public void cancelDamageByEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
    }
    
    /**
     * Cancel tất cả damage (fall, fire, drowning...)
     */
    public void cancelAllDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    /**
     * Cancel vanilla health regen
     */
    public void cancelHealthRegen(EntityRegainHealthEvent event) {
        event.setCancelled(true);
    }
    
    /**
     * Cancel food level change (hunger)
     */
    public void cancelFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}