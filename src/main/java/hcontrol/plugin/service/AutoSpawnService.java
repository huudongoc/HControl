package hcontrol.plugin.service;

import hcontrol.plugin.entity.EntityService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AUTO SPAWN SERVICE
 * Hệ thống spawn tự động quanh player hoặc trong khu vực được set
 */
public class AutoSpawnService {
    
    private final Plugin plugin;
    private final EntityService entityService;
    
    // Khu vực spawn (admin set bằng lệnh)
    private SpawnArea spawnArea;
    
    // Task spawn định kỳ
    private BukkitTask spawnTask;
    
    // Cấu hình spawn
    private int spawnInterval = 100; // ticks (5 giây)
    private int spawnRadius = 50; // bán kính spawn quanh player
    private int maxMobsPerPlayer = 10; // số mob tối đa quanh mỗi player
    private int maxMobsInArea = 50; // số mob tối đa trong khu vực
    
    // Danh sách entity types có thể spawn
    private final List<EntityType> spawnableTypes = Arrays.asList(
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.SPIDER,
        EntityType.CREEPER,
        EntityType.ENDERMAN,
        EntityType.BLAZE,
        EntityType.WITCH
    );
    
    public AutoSpawnService(Plugin plugin, EntityService entityService) {
        this.plugin = plugin;
        this.entityService = entityService;
    }
    
    /**
     * Bắt đầu hệ thống spawn tự động
     */
    public void start() {
        if (spawnTask != null && !spawnTask.isCancelled()) {
            return; // Đã chạy rồi
        }
        
        spawnTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                performSpawn();
            } catch (Exception e) {
                Bukkit.getLogger().warning("[AutoSpawn] Lỗi khi spawn: " + e.getMessage());
                e.printStackTrace();
            }
        }, spawnInterval, spawnInterval);
        
        Bukkit.getLogger().info("[AutoSpawn] Đã bắt đầu hệ thống spawn tự động");
    }
    
    /**
     * Dừng hệ thống spawn tự động
     */
    public void stop() {
        if (spawnTask != null && !spawnTask.isCancelled()) {
            spawnTask.cancel();
            spawnTask = null;
        }
        Bukkit.getLogger().info("[AutoSpawn] Đã dừng hệ thống spawn tự động");
    }
    
    /**
     * Thực hiện spawn mobs
     */
    private void performSpawn() {
        if (spawnArea != null) {
            // Spawn trong khu vực được set
            spawnInArea();
        } else {
            // Spawn quanh các player online
            spawnAroundPlayers();
        }
    }
    
    /**
     * Spawn mobs quanh các player online
     */
    private void spawnAroundPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            
            Location playerLoc = player.getLocation();
            World world = playerLoc.getWorld();
            if (world == null) continue;
            
            // Đếm số mob hiện tại quanh player
            int nearbyMobs = countNearbyMobs(playerLoc, spawnRadius);
            if (nearbyMobs >= maxMobsPerPlayer) continue;
            
            // Spawn mobs
            int toSpawn = maxMobsPerPlayer - nearbyMobs;
            for (int i = 0; i < toSpawn; i++) {
                spawnMobNearLocation(world, playerLoc, spawnRadius);
            }
        }
    }
    
    /**
     * Spawn mobs trong khu vực được set
     */
    private void spawnInArea() {
        World world = spawnArea.getWorld();
        if (world == null) return;
        
        // Đếm số mob hiện tại trong khu vực
        int mobsInArea = countMobsInArea();
        if (mobsInArea >= maxMobsInArea) return;
        
        // Spawn mobs
        int toSpawn = Math.min(5, maxMobsInArea - mobsInArea); // Spawn tối đa 5 mobs mỗi lần
        for (int i = 0; i < toSpawn; i++) {
            spawnMobInArea();
        }
    }
    
    /**
     * Spawn một mob gần location
     */
    private void spawnMobNearLocation(World world, Location center, int radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // Random vị trí trong bán kính
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble(10, radius);
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
        
        Location spawnLoc = new Location(world, x, y, z);
        
        // Kiểm tra vị trí hợp lệ
        if (!isValidSpawnLocation(spawnLoc)) return;
        
        // Random entity type
        EntityType type = spawnableTypes.get(random.nextInt(spawnableTypes.size()));
        
        // Spawn entity
        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLoc, type, 
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);
        
        if (entity != null) {
            // Initialize profile (sẽ được gọi tự động bởi EntityLifecycleListener)
            entityService.initializeEntity(entity);
        }
    }
    
    /**
     * Spawn một mob trong khu vực
     */
    private void spawnMobInArea() {
        World world = spawnArea.getWorld();
        if (world == null) return;
        
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // Random vị trí trong khu vực
        double x = random.nextDouble(spawnArea.getMinX(), spawnArea.getMaxX());
        double z = random.nextDouble(spawnArea.getMinZ(), spawnArea.getMaxZ());
        double y = world.getHighestBlockYAt((int)x, (int)z) + 1;
        
        Location spawnLoc = new Location(world, x, y, z);
        
        // Kiểm tra vị trí hợp lệ
        if (!isValidSpawnLocation(spawnLoc)) return;
        
        // Random entity type
        EntityType type = spawnableTypes.get(random.nextInt(spawnableTypes.size()));
        
        // Spawn entity
        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLoc, type, 
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);
        
        if (entity != null) {
            // Initialize profile
            entityService.initializeEntity(entity);
        }
    }
    
    /**
     * Đếm số mob quanh location
     */
    private int countNearbyMobs(Location center, int radius) {
        int count = 0;
        for (LivingEntity entity : center.getWorld().getLivingEntities()) {
            if (entity instanceof Player) continue;
            if (entity.getLocation().distance(center) <= radius) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Đếm số mob trong khu vực
     */
    private int countMobsInArea() {
        if (spawnArea == null) return 0;
        
        World world = spawnArea.getWorld();
        if (world == null) return 0;
        
        int count = 0;
        for (LivingEntity entity : world.getLivingEntities()) {
            if (entity instanceof Player) continue;
            Location loc = entity.getLocation();
            if (spawnArea.contains(loc)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Kiểm tra vị trí spawn hợp lệ
     */
    private boolean isValidSpawnLocation(Location loc) {
        if (loc.getBlock().getType().isSolid()) return false;
        if (loc.getBlock().getRelative(0, -1, 0).getType().isAir()) return false;
        if (loc.getBlock().getRelative(0, 1, 0).getType().isSolid()) return false;
        return true;
    }
    
    // ===== GETTERS & SETTERS =====
    
    public void setSpawnArea(SpawnArea area) {
        this.spawnArea = area;
    }
    
    public SpawnArea getSpawnArea() {
        return spawnArea;
    }
    
    public void clearSpawnArea() {
        this.spawnArea = null;
    }
    
    public void setSpawnInterval(int ticks) {
        this.spawnInterval = ticks;
        // Restart task với interval mới
        if (spawnTask != null && !spawnTask.isCancelled()) {
            stop();
            start();
        }
    }
    
    public void setSpawnRadius(int radius) {
        this.spawnRadius = radius;
    }
    
    public void setMaxMobsPerPlayer(int max) {
        this.maxMobsPerPlayer = max;
    }
    
    public void setMaxMobsInArea(int max) {
        this.maxMobsInArea = max;
    }
    
    /**
     * Inner class: Khu vực spawn
     */
    public static class SpawnArea {
        private final World world;
        private final double minX, maxX;
        private final double minZ, maxZ;
        private final int minY, maxY;
        
        public SpawnArea(World world, double minX, double maxX, double minZ, double maxZ, int minY, int maxY) {
            this.world = world;
            this.minX = Math.min(minX, maxX);
            this.maxX = Math.max(minX, maxX);
            this.minZ = Math.min(minZ, maxZ);
            this.maxZ = Math.max(minZ, maxZ);
            this.minY = Math.min(minY, maxY);
            this.maxY = Math.max(minY, maxY);
        }
        
        public SpawnArea(Location loc1, Location loc2) {
            this(loc1.getWorld(),
                 Math.min(loc1.getX(), loc2.getX()),
                 Math.max(loc1.getX(), loc2.getX()),
                 Math.min(loc1.getZ(), loc2.getZ()),
                 Math.max(loc1.getZ(), loc2.getZ()),
                 Math.min(loc1.getBlockY(), loc2.getBlockY()),
                 Math.max(loc1.getBlockY(), loc2.getBlockY()));
        }
        
        public boolean contains(Location loc) {
            if (loc.getWorld() != world) return false;
            return loc.getX() >= minX && loc.getX() <= maxX &&
                   loc.getZ() >= minZ && loc.getZ() <= maxZ &&
                   loc.getY() >= minY && loc.getY() <= maxY;
        }
        
        public World getWorld() { return world; }
        public double getMinX() { return minX; }
        public double getMaxX() { return maxX; }
        public double getMinZ() { return minZ; }
        public double getMaxZ() { return maxZ; }
        public int getMinY() { return minY; }
        public int getMaxY() { return maxY; }
    }
}
