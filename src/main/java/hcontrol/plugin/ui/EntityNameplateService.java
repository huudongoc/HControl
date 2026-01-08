package hcontrol.plugin.ui;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ENTITY NAMEPLATE SERVICE
 * Hien thi ten + HP tren dau mob (giong player nameplate)
 * Foundation cho quest NPC, boss, elite...
 */
public class EntityNameplateService {
    
    private final EntityManager entityManager;
    private final Plugin plugin;
    private final Map<UUID, BukkitTask> updateTasks = new HashMap<>();
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>(); // throttle map
    private static final long UPDATE_COOLDOWN_MS = 1000; // 1 giay throttle (giong Player)
    
    public EntityNameplateService(EntityManager entityManager, Plugin plugin) {
        this.entityManager = entityManager;
        this.plugin = plugin;
    }
    
    /**
     * Enable nameplate cho entity (boss, elite, NPC...)
     * Tu dong update HP theo thoi gian thuc
     * KHONG dung cho Player (player co NameplateService rieng)
     */
    public void enableNameplate(LivingEntity entity) {
        // Safety check: KHONG apply cho Player
        if (entity instanceof org.bukkit.entity.Player) return;
        
        EntityProfile profile = entityManager.get(entity.getUniqueId());
        if (profile == null) return;
        
        enableNameplate(entity, profile);
    }
    
    /**
     * Enable nameplate voi profile da update (de tranh doc cached profile cu)
     */
    public void enableNameplate(LivingEntity entity, EntityProfile profile) {
        // Safety check: KHONG apply cho Player
        if (entity instanceof org.bukkit.entity.Player) return;
        if (profile == null) return;
        
        UUID uuid = entity.getUniqueId();
        
        // Cancel task cu neu dang chay (tranh duplicate)
        BukkitTask oldTask = updateTasks.remove(uuid);
        if (oldTask != null) {
            oldTask.cancel();
        }
        
        // Update nameplate ngay voi profile DA UPDATE (force bypass throttle)
        updateNameplate(entity, profile, true);
        
        // Task tu dong update moi 100 ticks (5 giay) - tranh spam lag
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) {
                disableNameplate(entity);
                return;
            }
            
            EntityProfile currentProfile = entityManager.get(uuid);
            if (currentProfile == null) {
                disableNameplate(entity);
                return;
            }
            
            // Auto update task - force bypass throttle
            updateNameplate(entity, currentProfile, true);
        }, 100L, 100L);
        
        // ADD TASK VAO MAP de co the cancel sau nay
        updateTasks.put(uuid, task);
    }
    
    /**
     * Disable nameplate va stop update task
     */
    public void disableNameplate(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        
        BukkitTask task = updateTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        
        // Cleanup throttle map
        lastUpdateTime.remove(uuid);
        
        // Reset entity name
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }
    
    /**
     * Update nameplate display (public de CombatService goi khi bi danh)
     * Co throttle de tranh flash spam (1 giay cooldown)
     */
    public void updateNameplate(LivingEntity entity, EntityProfile profile) {
        updateNameplate(entity, profile, false);
    }
    
    /**
     * Update nameplate voi force option
     * @param force true = bo qua throttle, false = check cooldown
     */
    public void updateNameplate(LivingEntity entity, EntityProfile profile, boolean force) {
        UUID uuid = entity.getUniqueId();
        
        // Check cooldown (neu khong force)
        if (!force) {
            long now = System.currentTimeMillis();
            Long lastUpdate = lastUpdateTime.get(uuid);
            if (lastUpdate != null && (now - lastUpdate) < UPDATE_COOLDOWN_MS) {
                return; // skip update - qua nhanh (tranh flash)
            }
            lastUpdateTime.put(uuid, now);
        }
        // Tinh % HP
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getMaxHP();
        double hpPercent = (currentHP / maxHP) * 100.0;
        
        // Mau sac HP theo %
        String hpColor;
        if (hpPercent >= 75) {
            hpColor = "§a";  // xanh la
        } else if (hpPercent >= 50) {
            hpColor = "§e";  // vang
        } else if (hpPercent >= 25) {
            hpColor = "§6";  // cam
        } else {
            hpColor = "§c";  // do
        }
        
        // Boss/Elite prefix
        String prefix = "";
        if (profile.isBoss()) {
            prefix = "§4§l[BOSS] ";
        } else if (profile.isElite()) {
            prefix = "§6§l[Tinh Anh] ";
        }
        
        // Realm color
        CultivationRealm realm = profile.getRealm();
        String realmColor = realm.getColor();
        String realmName = realm.getDisplayName();
        
        // Ten hien thi
        String displayName = profile.getDisplayName();
        
        // Format GON: [Realm] Name ❤ HP%
        String nameplate = prefix + 
                          realmColor + "[" + realmName + "] §f" + 
                          displayName + " " +
                          hpColor + "❤ " + String.format("%.0f", hpPercent) + "%";
        
        entity.setCustomName(nameplate);
        entity.setCustomNameVisible(true);
    }
    
    /**
     * Stop tat ca update tasks (khi disable plugin)
     */
    public void stopAllTasks() {
        for (BukkitTask task : updateTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        updateTasks.clear();
        lastUpdateTime.clear(); // cleanup throttle map
    }
}
