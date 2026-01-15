package hcontrol.plugin.listener;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.ui.entity.EntityNameplateService;
import hcontrol.plugin.entity.EntityProfile;

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
     * ✅ FIX: Khi mob spawn -> CHỈ mark, KHÔNG làm gì nặng
     * EntitySpawnEvent có thể spawn 10-100 entity/tick
     * KHÔNG BAO GIỜ làm logic nặng ở đây
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
        
        // ✅ FIX: CHỈ mark để global task xử lý (KHÔNG gọi enableNameplate())
        if (profile != null) {
            nameplateService.markForInit(entity); // ✅ CHỈ mark, O(1)
        }
        
        // TODO: Check custom spawn qua metadata/tags
        // if (entity.getScoreboardTags().contains("elite")) {
        //     entityService.spawnElite(entity);
        // }
    }
    
    /**
     * Khi mob chet -> cleanup profile + nameplate
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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