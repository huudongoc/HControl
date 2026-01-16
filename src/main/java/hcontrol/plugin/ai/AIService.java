package hcontrol.plugin.ai;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PHASE 7 — AI SERVICE
 * Service quan ly AI cua tat ca mobs
 * Tick AI moi 1 giay (20 ticks)
 */
public class AIService {
    
    private final Plugin plugin;
    private final EntityManager entityManager;
    private final BrainRegistry brainRegistry;
    
    // Map UUID -> Brain instance
    private final Map<UUID, MobBrain> brains = new ConcurrentHashMap<>();
    
    // AI task
    private BukkitRunnable aiTask;
    
    public AIService(Plugin plugin, EntityManager entityManager, BrainRegistry brainRegistry) {
        this.plugin = plugin;
        this.entityManager = entityManager;
        this.brainRegistry = brainRegistry;
    }
    
    /**
     * Start AI tick task
     */
    public void start() {
        if (aiTask != null) {
            aiTask.cancel();
        }
        
        aiTask = new BukkitRunnable() {
            @Override
            public void run() {
                tickAllBrains();
            }
        };
        
        // Tick moi 1 giay (20 ticks)
        // TODO: Co the optimize bang cach tick nhanh hon (moi 10 ticks)
        aiTask.runTaskTimer(plugin, 20L, 20L);
    }
    
    /**
     * Stop AI tick task
     */
    public void stop() {
        if (aiTask != null) {
            aiTask.cancel();
            aiTask = null;
        }
        brains.clear();
    }
    
    /**
     * Tick tat ca brains
     */
    private void tickAllBrains() {
        for (EntityProfile profile : entityManager.getAll()) {
            LivingEntity entity = profile.getEntity();
            
            // Skip neu entity da chet hoac invalid
            if (entity == null || entity.isDead() || !entity.isValid()) {
                removeBrain(profile.getEntityUUID());
                continue;
            }
            
            // Lay hoac tao brain
            MobBrain brain = getOrCreateBrain(profile);
            
            // Tick brain
            try {
                brain.tick(profile, entity);
            } catch (Exception e) {
                plugin.getLogger().warning("Loi khi tick AI cho entity " + 
                    profile.getEntityUUID() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Lay hoac tao brain cho entity
     * 
     * @param profile EntityProfile
     * @return MobBrain
     */
    public MobBrain getOrCreateBrain(EntityProfile profile) {
        UUID uuid = profile.getEntityUUID();
        
        return brains.computeIfAbsent(uuid, k -> {
            // Tao brain moi
            return brainRegistry.createBrainForEntity(profile.getEntityType());
        });
    }
    
    /**
     * Get brain cua entity (co the null)
     * 
     * @param entityUUID UUID cua entity
     * @return MobBrain (null neu chua co)
     */
    public MobBrain getBrain(UUID entityUUID) {
        return brains.get(entityUUID);
    }
    
    /**
     * Set brain cho entity (ghi de)
     * 
     * @param entityUUID UUID cua entity
     * @param brain MobBrain moi
     */
    public void setBrain(UUID entityUUID, MobBrain brain) {
        brains.put(entityUUID, brain);
    }
    
    /**
     * Xoa brain cua entity
     * 
     * @param entityUUID UUID cua entity
     */
    public void removeBrain(UUID entityUUID) {
        brains.remove(entityUUID);
    }
    
    /**
     * Reset brain cua entity (clear aggro, state...)
     * 
     * @param entityUUID UUID cua entity
     */
    public void resetBrain(UUID entityUUID) {
        EntityProfile profile = entityManager.get(entityUUID);
        if (profile == null) {
            return;
        }
        
        MobBrain brain = brains.get(entityUUID);
        if (brain != null) {
            brain.reset(profile);
        }
    }
    
    /**
     * Notify brain khi entity bi danh
     * Goi tu CombatService hoac EntityDamageListener
     * 
     * @param victimUUID UUID cua entity bi danh
     * @param attacker Entity tan cong
     */
    public void onEntityDamaged(UUID victimUUID, LivingEntity attacker) {
        EntityProfile profile = entityManager.get(victimUUID);
        if (profile == null) {
            return;
        }
        
        MobBrain brain = getOrCreateBrain(profile);
        brain.onDamaged(profile, attacker);
    }
    
    /**
     * Get so luong entities co AI
     * 
     * @return So luong
     */
    public int getActiveBrainCount() {
        return brains.size();
    }
}
