package hcontrol.plugin.listener;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.ui.EntityNameplateService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * PHASE 7 — ENTITY LIFECYCLE LISTENER
 * Track mob spawn/death de quan ly EntityProfile + nameplate
 */
public class EntityLifecycleListener implements Listener {
    
    private final EntityManager entityManager;
    private final EntityService entityService;
    private final EntityNameplateService nameplateService;
    
    public EntityLifecycleListener(EntityManager entityManager, 
                                  EntityService entityService,
                                  EntityNameplateService nameplateService) {
        this.entityManager = entityManager;
        this.entityService = entityService;
        this.nameplateService = nameplateService;
    }
    
    /**
     * Khi mob spawn -> tao profile + enable nameplate
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        
        // chi track living entities
        if (!(entity instanceof LivingEntity)) return;
        
        // KHONG track player (player co NameplateService rieng)
        if (entity instanceof org.bukkit.entity.Player) return;
        
        // khoi tao profile theo registry template
        EntityProfile profile = entityService.initializeEntity(entity);
        
        // Enable nameplate cho TAT CA mob (hien thi HP + realm)
        if (profile != null) {
            nameplateService.enableNameplate(entity);
        }
        
        // TODO: Check custom spawn qua metadata/tags
        // if (entity.getScoreboardTags().contains("elite")) {
        //     entityService.spawnElite(entity);
        // }
    }
    
    /**
     * Khi mob chet -> cleanup profile + nameplate
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // disable nameplate truoc
        nameplateService.disableNameplate(entity);
        
        // remove profile khoi manager
        entityService.onEntityDeath(entity.getUniqueId());
        
        // TODO PHASE 8: Drop custom items dua tren realm/level
        // TODO PHASE 10: Player kill mob -> exp, karma, reputation
    }
}
