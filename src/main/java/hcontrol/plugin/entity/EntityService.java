package hcontrol.plugin.entity;

import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * PHASE 7 — ENTITY SERVICE
 * Service logic cho entity (spawn, upgrade, boss...)
 */
public class EntityService {
    
    private final EntityManager entityManager;
    private final EntityRegistry entityRegistry;
    
    public EntityService(EntityManager entityManager, EntityRegistry entityRegistry) {
        this.entityManager = entityManager;
        this.entityRegistry = entityRegistry;
    }
    
    /**
     * Khoi tao profile cho entity moi spawn
     * Su dung template tu registry
     */
    public EntityProfile initializeEntity(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        EntityType type = entity.getType();
        
        // lay template
        EntityRegistry.EntityTemplate template = entityRegistry.getTemplate(type);
        
        // tao profile theo template
        EntityProfile profile = new EntityProfile(
            uuid,
            type,
            entity.getCustomName(), // co the null
            template.realm,
            template.level,
            template.maxHP,
            template.attack,
            template.defense
        );
        
        // add vao manager
        entityManager.add(profile);
        
        return profile;
    }
    
    /**
     * Spawn elite mob (manh hon binh thuong)
     * +50% stats, +1 realm
     */
    public EntityProfile spawnElite(LivingEntity entity) {
        EntityProfile profile = initializeEntity(entity);
        
        profile.setElite(true);
        profile.setMaxHP(profile.getMaxHP() * 1.5);
        profile.setCurrentHP(profile.getMaxHP());
        profile.setAttack(profile.getAttack() * 1.5);
        profile.setDefense(profile.getDefense() * 1.5);
        
        // tang 1 realm (neu co the)
        CultivationRealm currentRealm = profile.getRealm();
        CultivationRealm nextRealm = getNextRealm(currentRealm);
        if (nextRealm != null) {
            profile.setRealm(nextRealm);
        }
        
        return profile;
    }
    
    /**
     * Spawn boss mob (rat manh)
     * +200% stats, +2 realms
     */
    public EntityProfile spawnBoss(LivingEntity entity, String bossName) {
        EntityProfile profile = initializeEntity(entity);
        
        profile.setBoss(true);
        profile.setMaxHP(profile.getMaxHP() * 3.0);
        profile.setCurrentHP(profile.getMaxHP());
        profile.setAttack(profile.getAttack() * 3.0);
        profile.setDefense(profile.getDefense() * 3.0);
        
        // tang 2 realms
        CultivationRealm currentRealm = profile.getRealm();
        CultivationRealm nextRealm = getNextRealm(currentRealm);
        if (nextRealm != null) {
            profile.setRealm(nextRealm);
            CultivationRealm next2 = getNextRealm(nextRealm);
            if (next2 != null) {
                profile.setRealm(next2);
            }
        }
        
        return profile;
    }
    
    /**
     * Cleanup entity profile khi mob chet
     */
    public void onEntityDeath(UUID entityUUID) {
        entityManager.remove(entityUUID);
    }
    
    /**
     * Get next realm (de upgrade mob)
     */
    private CultivationRealm getNextRealm(CultivationRealm current) {
        CultivationRealm[] realms = CultivationRealm.values();
        for (int i = 0; i < realms.length - 1; i++) {
            if (realms[i] == current) {
                return realms[i + 1];
            }
        }
        return null; // da max
    }
}
