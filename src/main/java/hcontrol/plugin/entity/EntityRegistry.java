package hcontrol.plugin.entity;

import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * PHASE 7 — ENTITY REGISTRY
 * Dang ky mob types voi realm/stats mac dinh
 * Vanilla mobs + Custom mods sau nay
 */
public class EntityRegistry {
    
    // EntityType -> EntityTemplate (stats mac dinh)
    private final Map<EntityType, EntityTemplate> templates = new HashMap<>();
    
    public EntityRegistry() {
        registerVanillaMobs();
    }
    
    /**
     * Dang ky vanilla mobs voi realm mac dinh
     */
    private void registerVanillaMobs() {
        // QI_REFINING (yeu nhat)
        register(EntityType.ZOMBIE, CultivationRealm.QI_REFINING, 1, 20, 3, 0);
        register(EntityType.SKELETON, CultivationRealm.QI_REFINING, 1, 15, 4, 0);
        register(EntityType.SPIDER, CultivationRealm.QI_REFINING, 1, 16, 2, 0);
        register(EntityType.CREEPER, CultivationRealm.QI_REFINING, 2, 20, 25, 0); // explosion dame cao
        
        // FOUNDATION (trung binh)
        register(EntityType.ENDERMAN, CultivationRealm.FOUNDATION, 3, 40, 7, 5);
        register(EntityType.BLAZE, CultivationRealm.FOUNDATION, 4, 20, 6, 3);
        register(EntityType.WITCH, CultivationRealm.FOUNDATION, 4, 26, 5, 4);
        
        // GOLDEN_CORE (manh)
        register(EntityType.WITHER_SKELETON, CultivationRealm.GOLDEN_CORE, 5, 50, 8, 6);
        register(EntityType.PIGLIN_BRUTE, CultivationRealm.GOLDEN_CORE, 5, 50, 9, 7);
        
        // NASCENT_SOUL (boss tier)
        register(EntityType.ELDER_GUARDIAN, CultivationRealm.NASCENT_SOUL, 6, 80, 10, 8);
        
        // SOUL_FORMATION (world boss)
        register(EntityType.WITHER, CultivationRealm.SOUL_FORMATION, 8, 300, 15, 10);
        register(EntityType.ENDER_DRAGON, CultivationRealm.SOUL_FORMATION, 9, 200, 12, 12);
    }
    
    /**
     * Dang ky mob template
     */
    public void register(EntityType type, CultivationRealm realm, int level, 
                        double maxHP, double attack, double defense) {
        templates.put(type, new EntityTemplate(realm, level, maxHP, attack, defense));
    }
    
    /**
     * Lay template cho entity type
     */
    public EntityTemplate getTemplate(EntityType type) {
        return templates.getOrDefault(type, getDefaultTemplate());
    }
    
    /**
     * Check co dang ky chua
     */
    public boolean isRegistered(EntityType type) {
        return templates.containsKey(type);
    }
    
    /**
     * Template mac dinh (cho mob chua dang ky)
     */
    private EntityTemplate getDefaultTemplate() {
        return new EntityTemplate(CultivationRealm.QI_REFINING, 1, 20, 2, 0);
    }
    
    /**
     * Inner class: Template de tao EntityProfile
     */
    public static class EntityTemplate {
        public final CultivationRealm realm;
        public final int level;
        public final double maxHP;
        public final double attack;
        public final double defense;
        
        public EntityTemplate(CultivationRealm realm, int level, double maxHP, 
                            double attack, double defense) {
            this.realm = realm;
            this.level = level;
            this.maxHP = maxHP;
            this.attack = attack;
            this.defense = defense;
        }
    }
}
