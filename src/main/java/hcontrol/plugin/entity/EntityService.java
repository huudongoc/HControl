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
        
        // Neu chua dang ky -> tao template mac dinh dua tren maxHP cua entity
        if (template == null) {
            double entityMaxHP = entity.getMaxHealth();
            template = entityRegistry.getDefaultTemplate(entityMaxHP);
        }
        
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
     * +200% stats, realm tối thiểu NGUYENANH (boss tier)
     * 
     * ⚠️ FIX: Không dựa vào template của EntityType (có thể là LUYENKHI)
     * Set realm trực tiếp dựa trên entityMaxHP để đảm bảo boss có realm cao
     */
    public EntityProfile spawnBoss(LivingEntity entity, String bossName) {
        // Initialize profile (có thể realm thấp từ template)
        EntityProfile profile = initializeEntity(entity);
        
        // Tính stats sau khi boost (x3)
        double boostedMaxHP = profile.getMaxHP() * 3.0;
        double boostedAttack = profile.getAttack() * 3.0;
        double boostedDefense = profile.getDefense() * 3.0;
        
        // ⚠️ FIX: Set realm dựa trên boostedMaxHP, không dựa vào template
        // Boss tối thiểu phải ở NGUYENANH (boss tier)
        CultivationRealm bossRealm;
        if (boostedMaxHP >= 500) {
            // Boss rất mạnh - HOATHAN hoặc cao hơn
            bossRealm = CultivationRealm.HOATHAN;
        } else if (boostedMaxHP >= 200) {
            // Boss mạnh - NGUYENANH
            bossRealm = CultivationRealm.NGUYENANH;
        } else {
            // Boss yếu - tối thiểu NGUYENANH
            bossRealm = CultivationRealm.NGUYENANH;
        }
        
        // ✅ FIX: Tạo lại EntityProfile với customName = bossName
        // Vì customName là final, không thể set sau khi tạo
        EntityProfile bossProfile = new EntityProfile(
            entity.getUniqueId(),
            entity.getType(),
            bossName,  // Set customName = bossName
            bossRealm,
            bossRealm.getMaxLevelInRealm(),  // Boss luôn ở max level (10)
            boostedMaxHP,
            boostedAttack,
            boostedDefense
        );
        bossProfile.setBoss(true);
        bossProfile.setCurrentHP(boostedMaxHP);
        
        // Update trong manager
        entityManager.remove(profile.getEntityUUID());
        entityManager.add(bossProfile);
        
        return bossProfile;
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
