package hcontrol.plugin.ai;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;

/**
 * PHASE 7 — BRAIN REGISTRY
 * Registry de map EntityType -> BrainType
 * Dinh nghia brain cho moi loai mob
 */
public class BrainRegistry {
    
    private final Map<EntityType, BrainType> brainTypes = new HashMap<>();
    
    public BrainRegistry() {
        registerDefaultBrains();
    }
    
    /**
     * Dang ky brain mac dinh cho cac vanilla mobs
     */
    private void registerDefaultBrains() {
        // ===== PASSIVE MOBS =====
        registerBrain(EntityType.PIG, BrainType.PASSIVE);
        registerBrain(EntityType.COW, BrainType.PASSIVE);
        registerBrain(EntityType.SHEEP, BrainType.PASSIVE);
        registerBrain(EntityType.CHICKEN, BrainType.PASSIVE);
        registerBrain(EntityType.RABBIT, BrainType.PASSIVE);
        registerBrain(EntityType.BAT, BrainType.PASSIVE);
        registerBrain(EntityType.SQUID, BrainType.PASSIVE);
        registerBrain(EntityType.VILLAGER, BrainType.PASSIVE);
        
        // ===== AGGRESSIVE MOBS =====
        registerBrain(EntityType.ZOMBIE, BrainType.AGGRESSIVE);
        registerBrain(EntityType.SKELETON, BrainType.AGGRESSIVE);
        registerBrain(EntityType.SPIDER, BrainType.AGGRESSIVE);
        registerBrain(EntityType.CREEPER, BrainType.AGGRESSIVE);
        registerBrain(EntityType.SLIME, BrainType.AGGRESSIVE);
        registerBrain(EntityType.MAGMA_CUBE, BrainType.AGGRESSIVE);
        registerBrain(EntityType.BLAZE, BrainType.AGGRESSIVE);
        registerBrain(EntityType.GHAST, BrainType.AGGRESSIVE);
        registerBrain(EntityType.WITCH, BrainType.AGGRESSIVE);
        registerBrain(EntityType.PHANTOM, BrainType.AGGRESSIVE);
        registerBrain(EntityType.DROWNED, BrainType.AGGRESSIVE);
        registerBrain(EntityType.HUSK, BrainType.AGGRESSIVE);
        registerBrain(EntityType.STRAY, BrainType.AGGRESSIVE);
        registerBrain(EntityType.VEX, BrainType.AGGRESSIVE);
        registerBrain(EntityType.VINDICATOR, BrainType.AGGRESSIVE);
        registerBrain(EntityType.EVOKER, BrainType.AGGRESSIVE);
        registerBrain(EntityType.PILLAGER, BrainType.AGGRESSIVE);
        registerBrain(EntityType.RAVAGER, BrainType.AGGRESSIVE);
        
        // ===== NEUTRAL MOBS (dung Guard brain) =====
        registerBrain(EntityType.WOLF, BrainType.GUARD);
        registerBrain(EntityType.IRON_GOLEM, BrainType.GUARD);
        registerBrain(EntityType.POLAR_BEAR, BrainType.GUARD);
        registerBrain(EntityType.LLAMA, BrainType.GUARD);
        registerBrain(EntityType.PANDA, BrainType.GUARD);
        registerBrain(EntityType.BEE, BrainType.GUARD);
        registerBrain(EntityType.ENDERMAN, BrainType.GUARD);
        registerBrain(EntityType.PIGLIN, BrainType.GUARD);
        registerBrain(EntityType.ZOMBIFIED_PIGLIN, BrainType.GUARD);
        
        // ===== ELITE/BOSS (mac dinh aggressive, sau nang cap len elite) =====
        registerBrain(EntityType.WITHER_SKELETON, BrainType.ELITE);
        registerBrain(EntityType.ELDER_GUARDIAN, BrainType.ELITE);
        registerBrain(EntityType.SHULKER, BrainType.ELITE);
        
        // ===== BOSS =====
        registerBrain(EntityType.ENDER_DRAGON, BrainType.BOSS);
        registerBrain(EntityType.WITHER, BrainType.BOSS);
        registerBrain(EntityType.WARDEN, BrainType.BOSS);
    }
    
    /**
     * Dang ky brain type cho entity type
     * 
     * @param entityType EntityType
     * @param brainType BrainType
     */
    public void registerBrain(EntityType entityType, BrainType brainType) {
        brainTypes.put(entityType, brainType);
    }
    
    /**
     * Lay brain type cua entity type
     * 
     * @param entityType EntityType
     * @return BrainType (mac dinh AGGRESSIVE neu khong tim thay)
     */
    public BrainType getBrainType(EntityType entityType) {
        return brainTypes.getOrDefault(entityType, BrainType.AGGRESSIVE);
    }
    
    /**
     * Tao instance brain tu brain type
     * 
     * @param brainType BrainType
     * @return MobBrain instance
     */
    public MobBrain createBrain(BrainType brainType) {
        return switch (brainType) {
            case PASSIVE -> new PassiveBrain();
            case AGGRESSIVE -> new AggressiveBrain();
            case GUARD, NEUTRAL -> new GuardBrain();
            case ELITE -> new AggressiveBrain();  // TODO: EliteBrain
            case BOSS -> new AggressiveBrain();   // TODO: BossBrain
        };
    }
    
    /**
     * Tao brain cho entity type
     * 
     * @param entityType EntityType
     * @return MobBrain instance
     */
    public MobBrain createBrainForEntity(EntityType entityType) {
        BrainType type = getBrainType(entityType);
        return createBrain(type);
    }
}
