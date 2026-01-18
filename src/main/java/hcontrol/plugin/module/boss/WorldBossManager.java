package hcontrol.plugin.module.boss;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import hcontrol.plugin.Main;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.service.AscensionService;
import hcontrol.plugin.service.CombatService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * WORLD BOSS MANAGER
 * Quản lý toàn bộ world boss system
 * 
 * Responsibilities:
 * - Spawn scheduling
 * - Participation tracking
 * - Reward distribution
 * - Listener management
 */
public class WorldBossManager {
    
    private final Main plugin;
    private final BossManager bossManager;
    private final EntityManager entityManager;
    private final EntityService entityService;
    private final PlayerManager playerManager;
    private final AscensionService ascensionService;
    private final CombatService combatService;
    private final hcontrol.plugin.service.LevelService levelService;
    
    private WorldBossSpawnService spawnService;
    private WorldBossRewardService rewardService;
    private WorldBossListener listener;
    private WorldBossParticipation currentParticipation;
    
    // Map boss UUID -> participation
    private final Map<UUID, WorldBossParticipation> participationMap = new HashMap<>();
    
    public WorldBossManager(Main plugin, BossManager bossManager, 
                            EntityManager entityManager, EntityService entityService,
                            PlayerManager playerManager, AscensionService ascensionService,
                            CombatService combatService, hcontrol.plugin.service.LevelService levelService) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.entityManager = entityManager;
        this.entityService = entityService;
        this.playerManager = playerManager;
        this.ascensionService = ascensionService;
        this.combatService = combatService;
        this.levelService = levelService;
    }
    
    /**
     * Initialize world boss system
     */
    public void initialize() {
        // Create services
        this.spawnService = new WorldBossSpawnService(
            plugin, bossManager, entityManager, entityService, playerManager
        );
        this.spawnService.setWorldBossManager(this);  // Set reference để spawn service có thể set participation
        this.rewardService = new WorldBossRewardService(playerManager, ascensionService, levelService);
        
        // Create listener
        this.listener = new WorldBossListener(
            bossManager, entityManager, playerManager, 
            rewardService, combatService, this
        );
        
        // Register listener
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        
        // Start spawn service
        spawnService.start();
        
        plugin.getLogger().info("[World Boss] System đã khởi động!");
    }
    
    /**
     * Shutdown world boss system
     */
    public void shutdown() {
        if (spawnService != null) {
            spawnService.stop();
        }
        
        if (listener != null) {
            HandlerList.unregisterAll(listener);
        }
        
        participationMap.clear();
    }
    
    /**
     * Track damage từ player đánh boss
     */
    public void trackDamage(UUID playerUUID, UUID bossUUID, double damage) {
        WorldBossParticipation participation = participationMap.get(bossUUID);
        if (participation == null) {
            // Tạo participation mới
            participation = new WorldBossParticipation(bossUUID);
            participationMap.put(bossUUID, participation);
            currentParticipation = participation;
        }
        
        participation.recordDamage(playerUUID, damage);
        listener.trackDamage(playerUUID, bossUUID, damage);
    }
    
    /**
     * Get current participation
     */
    public WorldBossParticipation getCurrentParticipation() {
        if (currentParticipation == null) {
            // Tạo participation mới khi spawn boss
            BossEntity currentBoss = spawnService.getCurrentWorldBoss();
            if (currentBoss != null) {
                currentParticipation = new WorldBossParticipation(currentBoss.getEntity().getUniqueId());
                participationMap.put(currentBoss.getEntity().getUniqueId(), currentParticipation);
            }
        }
        return currentParticipation;
    }
    
    /**
     * Set current participation (khi spawn boss mới)
     */
    public void setCurrentParticipation(WorldBossParticipation participation) {
        this.currentParticipation = participation;
        if (participation != null) {
            participationMap.put(participation.getBossUUID(), participation);
        }
    }
    
    /**
     * Get spawn service
     */
    public WorldBossSpawnService getSpawnService() {
        return spawnService;
    }
    
    /**
     * Get reward service
     */
    public WorldBossRewardService getRewardService() {
        return rewardService;
    }
    
    /**
     * Force spawn boss (admin command)
     */
    public void forceSpawn(org.bukkit.Location location) {
        if (spawnService != null) {
            spawnService.forceSpawn(location);
        }
    }
}
