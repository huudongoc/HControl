package hcontrol.plugin.module.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import hcontrol.plugin.Main;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;

import java.util.*;

/**
 * WORLD BOSS SPAWN SERVICE
 * Scheduler để spawn world boss theo lịch
 * 
 * Features:
 * - Scheduled spawns (mỗi X giờ)
 * - Scale theo average ascension level của players online
 * - Announcement system
 * - Spawn location management
 */
public class WorldBossSpawnService {
    
    private final Main plugin;
    private final BossManager bossManager;
    private final EntityManager entityManager;
    private final EntityService entityService;
    private final PlayerManager playerManager;
    private WorldBossManager worldBossManager;  // Reference để set participation
    
    private BossEntity currentWorldBoss;
    private BukkitTask spawnTask;
    private BukkitTask announcementTask;
    
    // Config
    private static final long SPAWN_INTERVAL_TICKS = 20L * 60 * 60 * 2; // 2 giờ
    private static final long ANNOUNCEMENT_DELAY_TICKS = 20L * 60 * 10; // 10 phút trước khi spawn
    private static final Location DEFAULT_SPAWN_LOCATION = new Location(
        Bukkit.getWorlds().get(0), 0, 100, 0
    );
    
    public WorldBossSpawnService(Main plugin, BossManager bossManager, 
                                 EntityManager entityManager, EntityService entityService,
                                 PlayerManager playerManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.entityManager = entityManager;
        this.entityService = entityService;
        this.playerManager = playerManager;
    }
    
    /**
     * Set WorldBossManager reference (để set participation)
     */
    public void setWorldBossManager(WorldBossManager worldBossManager) {
        this.worldBossManager = worldBossManager;
    }
    
    /**
     * Start scheduler
     */
    public void start() {
        // Spawn boss đầu tiên sau 5 phút
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnWorldBoss();
            }
        }.runTaskLater(plugin, 20L * 60 * 5);
        
        // Schedule spawns định kỳ
        spawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentWorldBoss == null || currentWorldBoss.isDead()) {
                    spawnWorldBoss();
                }
            }
        }.runTaskTimer(plugin, SPAWN_INTERVAL_TICKS, SPAWN_INTERVAL_TICKS);
        
        plugin.getLogger().info("[World Boss] Scheduler đã khởi động!");
    }
    
    /**
     * Stop scheduler
     */
    public void stop() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        if (announcementTask != null) {
            announcementTask.cancel();
        }
    }
    
    /**
     * Spawn world boss
     * Scale theo average ascension level của players online
     */
    public void spawnWorldBoss() {
        // Check xem có boss đang active không
        if (currentWorldBoss != null && !currentWorldBoss.isDead()) {
            plugin.getLogger().info("[World Boss] Boss đang active, bỏ qua spawn!");
            return;
        }
        
        // Tính average ascension level
        int avgAscensionLevel = calculateAverageAscensionLevel();
        
        // Chọn spawn location
        Location spawnLoc = getSpawnLocation();
        
        // Spawn entity
        World world = spawnLoc.getWorld();
        if (world == null) {
            plugin.getLogger().warning("[World Boss] Không tìm thấy world!");
            return;
        }
        
        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLoc, EntityType.WITHER);
        
        // Tính stats dựa trên ascension level
        double baseHP = 1000.0 + (avgAscensionLevel * 500.0);  // 1000 + (level * 500)
        double baseAttack = 50.0 + (avgAscensionLevel * 10.0);  // 50 + (level * 10)
        double baseDefense = 20.0 + (avgAscensionLevel * 5.0);  // 20 + (level * 5)
        
        // Set entity stats
        entity.setMaxHealth(baseHP);
        entity.setHealth(baseHP);
        
        // Tạo EntityProfile với stats đã scale
        EntityProfile entityProfile = new EntityProfile(
            entity.getUniqueId(),
            EntityType.WITHER,
            "World Boss",
            CultivationRealm.CHANTIEN,  // World boss luôn ở CHANTIEN
            10,  // Max level
            baseHP,
            baseAttack,
            baseDefense
        );
        entityProfile.setCurrentHP(baseHP);
        entityManager.add(entityProfile);
        
        // Tạo BossEntity
        String bossName = "Thiên Kiếp Long Vương";  // Có thể random hoặc config
        BossEntity boss = new BossEntity(entity, bossName, BossType.WORLD_BOSS);
        bossManager.registerBoss(boss);
        currentWorldBoss = boss;
        
        // Tạo participation tracking cho boss mới
        if (worldBossManager != null) {
            WorldBossParticipation participation = new WorldBossParticipation(entity.getUniqueId());
            worldBossManager.setCurrentParticipation(participation);
        }
        
        // Announce
        String announcement = String.format(
            "§c§l[WORLD BOSS] §e%s §7đã xuất hiện tại §e%s§7!",
            bossName,
            formatLocation(spawnLoc)
        );
        for (org.bukkit.entity.Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage(announcement);
            onlinePlayer.sendMessage("§7Boss Level: §e" + avgAscensionLevel + " §7(Ascension)");
            onlinePlayer.sendMessage("§7HP: §c" + String.format("%.0f", baseHP) + 
                " §7| ATK: §c" + String.format("%.0f", baseAttack) + 
                " §7| DEF: §c" + String.format("%.0f", baseDefense));
        }
        
        plugin.getLogger().info("[World Boss] Đã spawn: " + bossName + 
            " (Ascension Level: " + avgAscensionLevel + ")");
    }
    
    /**
     * Tính average ascension level của players online
     */
    private int calculateAverageAscensionLevel() {
        List<PlayerProfile> onlineProfiles = new ArrayList<>();
        
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = playerManager.get(player.getUniqueId());
            if (profile != null && profile.canAscend()) {
                // Chỉ tính players đã đạt CHANTIEN 10
                onlineProfiles.add(profile);
            }
        }
        
        if (onlineProfiles.isEmpty()) {
            return 0;  // Không có player nào đạt ascension → boss level 0
        }
        
        int totalAscension = 0;
        for (PlayerProfile profile : onlineProfiles) {
            totalAscension += profile.getAscensionProfile().getAscensionLevel();
        }
        
        return totalAscension / onlineProfiles.size();
    }
    
    /**
     * Get spawn location
     * TODO: Có thể config hoặc random từ danh sách locations
     */
    private Location getSpawnLocation() {
        // Tạm thời dùng default location
        // Sau này có thể config hoặc random từ danh sách
        return DEFAULT_SPAWN_LOCATION.clone();
    }
    
    /**
     * Format location để hiển thị
     */
    private String formatLocation(Location loc) {
        return String.format("X:%.0f Y:%.0f Z:%.0f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    /**
     * Get current world boss
     */
    public BossEntity getCurrentWorldBoss() {
        return currentWorldBoss;
    }
    
    /**
     * Check có world boss đang active không
     */
    public boolean hasActiveBoss() {
        return currentWorldBoss != null && !currentWorldBoss.isDead();
    }
    
    /**
     * Force spawn boss (admin command)
     */
    public void forceSpawn(Location location) {
        if (currentWorldBoss != null && !currentWorldBoss.isDead()) {
            currentWorldBoss.getEntity().remove();  // Remove boss cũ
            bossManager.removeBoss(currentWorldBoss.getEntity().getUniqueId());
        }
        
        DEFAULT_SPAWN_LOCATION.setWorld(location.getWorld());
        DEFAULT_SPAWN_LOCATION.setX(location.getX());
        DEFAULT_SPAWN_LOCATION.setY(location.getY());
        DEFAULT_SPAWN_LOCATION.setZ(location.getZ());
        
        spawnWorldBoss();
    }
}
