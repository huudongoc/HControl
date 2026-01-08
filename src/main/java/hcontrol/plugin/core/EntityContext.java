package hcontrol.plugin.core;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityRegistry;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.module.boss.BossManager;

/**
 * ENTITY CONTEXT — PHASE 7
 * Quan ly tat ca service lien quan den Entity (Mob RPG)
 */
public class EntityContext {
    
    private final EntityManager entityManager;
    private final EntityRegistry entityRegistry;
    private final EntityService entityService;
    private final BossManager bossManager;
    
    public EntityContext() {
        this.entityManager = new EntityManager();
        this.entityRegistry = new EntityRegistry();
        this.entityService = new EntityService(entityManager, entityRegistry);
        this.bossManager = new BossManager();
    }
    
    // ========== GETTERS ==========
    
    public EntityManager getEntityManager() { return entityManager; }
    public EntityRegistry getEntityRegistry() { return entityRegistry; }
    public EntityService getEntityService() { return entityService; }
    public BossManager getBossManager() { return bossManager; }
}
