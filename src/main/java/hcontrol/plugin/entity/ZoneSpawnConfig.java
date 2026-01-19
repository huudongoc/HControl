package hcontrol.plugin.entity;

import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.*;

/**
 * ZONE SPAWN CONFIG
 * Cấu hình spawn cho một khu vực (zone)
 * - Mob types có thể spawn
 * - Realm của mobs
 * - Level range
 * - Skills mobs có thể có
 * - Loot table
 */
public class ZoneSpawnConfig {
    
    private final String zoneName;
    private final World world;
    private final Location minCorner;
    private final Location maxCorner;
    
    // Spawn config
    private final List<EntityType> allowedMobTypes;
    private CultivationRealm defaultRealm;
    private int minLevel;
    private int maxLevel;
    
    // Skills config (future)
    private final Set<String> allowedSkills;
    
    // Loot config (future)
    private final Map<String, Double> lootTable; // itemId -> dropChance
    
    public ZoneSpawnConfig(String zoneName, World world, Location minCorner, Location maxCorner) {
        this.zoneName = zoneName;
        this.world = world;
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.allowedMobTypes = new ArrayList<>();
        this.defaultRealm = CultivationRealm.LUYENKHI;
        this.minLevel = 1;
        this.maxLevel = 10;
        this.allowedSkills = new HashSet<>();
        this.lootTable = new HashMap<>();
    }
    
    /**
     * Check xem location có trong zone không
     */
    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(world)) return false;
        
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        
        return x >= minCorner.getX() && x <= maxCorner.getX() &&
               y >= minCorner.getY() && y <= maxCorner.getY() &&
               z >= minCorner.getZ() && z <= maxCorner.getZ();
    }
    
    // ========== GETTERS / SETTERS ==========
    
    public String getZoneName() { return zoneName; }
    public World getWorld() { return world; }
    public Location getMinCorner() { return minCorner; }
    public Location getMaxCorner() { return maxCorner; }
    
    public List<EntityType> getAllowedMobTypes() { return allowedMobTypes; }
    public void setAllowedMobTypes(List<EntityType> types) { 
        this.allowedMobTypes.clear();
        this.allowedMobTypes.addAll(types);
    }
    public void addAllowedMobType(EntityType type) {
        if (!allowedMobTypes.contains(type)) {
            allowedMobTypes.add(type);
        }
    }
    public void removeAllowedMobType(EntityType type) {
        allowedMobTypes.remove(type);
    }
    
    public CultivationRealm getDefaultRealm() { return defaultRealm; }
    public void setDefaultRealm(CultivationRealm realm) { this.defaultRealm = realm; }
    
    public int getMinLevel() { return minLevel; }
    public void setMinLevel(int minLevel) { this.minLevel = minLevel; }
    
    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
    
    public Set<String> getAllowedSkills() { return allowedSkills; }
    public void addAllowedSkill(String skillId) { allowedSkills.add(skillId); }
    public void removeAllowedSkill(String skillId) { allowedSkills.remove(skillId); }
    
    public Map<String, Double> getLootTable() { return lootTable; }
    public void setLootDrop(String itemId, double chance) { lootTable.put(itemId, chance); }
    public void removeLootDrop(String itemId) { lootTable.remove(itemId); }
    
    /**
     * Get random level trong range
     */
    public int getRandomLevel() {
        if (minLevel >= maxLevel) return minLevel;
        return new Random().nextInt(maxLevel - minLevel + 1) + minLevel;
    }
    
    /**
     * Get random mob type từ allowed list
     */
    public EntityType getRandomMobType() {
        if (allowedMobTypes.isEmpty()) return null;
        return allowedMobTypes.get(new Random().nextInt(allowedMobTypes.size()));
    }
}
