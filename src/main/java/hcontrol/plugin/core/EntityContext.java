package hcontrol.plugin.core;

import org.bukkit.plugin.Plugin;

import hcontrol.plugin.ai.AIService;
import hcontrol.plugin.ai.BrainRegistry;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityRegistry;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.module.boss.BossManager;
import hcontrol.plugin.module.boss.WorldBossManager;
import hcontrol.plugin.service.AscensionService;
import hcontrol.plugin.service.CombatService;
import hcontrol.plugin.skill.SkillCooldownManager;
import hcontrol.plugin.skill.SkillExecutor;
import hcontrol.plugin.skill.SkillRegistry;

/**
 * ENTITY CONTEXT — PHASE 7
 * Quan ly tat ca service lien quan den Entity (Mob RPG) + AI System + Skill System
 */
public class EntityContext {
    
    private final EntityManager entityManager;
    private final EntityRegistry entityRegistry;
    private final EntityService entityService;
    private final BossManager bossManager;
    
    // PHASE 7: AI System
    private final BrainRegistry brainRegistry;
    private AIService aiService;  // init sau trong lifecycle
    
    // PHASE 7.2: Skill System
    private final SkillRegistry skillRegistry;
    private final SkillCooldownManager cooldownManager;
    private SkillExecutor skillExecutor;  // init sau (can CombatService)
    
    // WORLD BOSS SYSTEM - ENDGAME
    private WorldBossManager worldBossManager;  // init sau (can nhiều dependencies)
    
    public EntityContext() {
        this.entityManager = new EntityManager();
        this.entityRegistry = new EntityRegistry();
        this.entityService = new EntityService(entityManager, entityRegistry);
        this.bossManager = new BossManager();
        this.brainRegistry = new BrainRegistry();
        this.skillRegistry = new SkillRegistry();
        this.cooldownManager = new SkillCooldownManager();
    }
    
    /**
     * Init AI service (goi trong lifecycle callbacks)
     * Can Plugin instance nen khong the init trong constructor
     */
    public void initAI(Plugin plugin) {
        this.aiService = new AIService(plugin, entityManager, brainRegistry);
    }
    
    /**
     * Init Skill system (can CombatService)
     */
    public void initSkills(CombatService combatService) {
        this.skillExecutor = new SkillExecutor(combatService, cooldownManager);
    }
    
    /**
     * Init World Boss system (can nhiều dependencies)
     */
    public void initWorldBoss(hcontrol.plugin.Main plugin, 
                              hcontrol.plugin.player.PlayerManager playerManager,
                              AscensionService ascensionService,
                              CombatService combatService) {
        this.worldBossManager = new WorldBossManager(
            plugin, bossManager, entityManager, entityService,
            playerManager, ascensionService, combatService
        );
        this.worldBossManager.initialize();
    }
    
    // ========== GETTERS ==========
    
    public EntityManager getEntityManager() { return entityManager; }
    public EntityRegistry getEntityRegistry() { return entityRegistry; }
    public EntityService getEntityService() { return entityService; }
    public BossManager getBossManager() { return bossManager; }
    public BrainRegistry getBrainRegistry() { return brainRegistry; }
    public AIService getAIService() { return aiService; }
    public SkillRegistry getSkillRegistry() { return skillRegistry; }
    public SkillCooldownManager getCooldownManager() { return cooldownManager; }
    public SkillExecutor getSkillExecutor() { return skillExecutor; }
    public WorldBossManager getWorldBossManager() { return worldBossManager; }
}
