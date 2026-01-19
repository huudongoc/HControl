package hcontrol.plugin.entity;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

/**
 * ZONE MANAGER
 * Quản lý các zone spawn config
 */
public class ZoneManager {
    
    private final Map<String, ZoneSpawnConfig> zones = new HashMap<>();
    
    /**
     * Thêm zone mới
     */
    public void addZone(ZoneSpawnConfig config) {
        zones.put(config.getZoneName().toLowerCase(), config);
    }
    
    /**
     * Xóa zone
     */
    public void removeZone(String zoneName) {
        zones.remove(zoneName.toLowerCase());
    }
    
    /**
     * Lấy zone theo tên
     */
    public ZoneSpawnConfig getZone(String zoneName) {
        return zones.get(zoneName.toLowerCase());
    }
    
    /**
     * Lấy zone chứa location
     */
    public ZoneSpawnConfig getZoneAt(Location loc) {
        for (ZoneSpawnConfig zone : zones.values()) {
            if (zone.contains(loc)) {
                return zone;
            }
        }
        return null;
    }
    
    /**
     * Lấy tất cả zones
     */
    public Collection<ZoneSpawnConfig> getAllZones() {
        return zones.values();
    }
    
    /**
     * Check zone có tồn tại không
     */
    public boolean hasZone(String zoneName) {
        return zones.containsKey(zoneName.toLowerCase());
    }
    
    /**
     * Lấy danh sách tên zones
     */
    public Set<String> getZoneNames() {
        return zones.keySet();
    }
}
