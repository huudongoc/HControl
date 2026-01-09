package hcontrol.plugin.ui.entity;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.service.DisplayFormatService;
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
 * KHONG chua logic tinh toan - chi su dung DisplayFormatService
 */
public class EntityNameplateService {
    
    private final EntityManager entityManager;
    private final Plugin plugin;
    private final DisplayFormatService displayFormatService;
    private final Map<UUID, BukkitTask> updateTasks = new HashMap<>();
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>(); // throttle map
    private static final long UPDATE_COOLDOWN_MS = 1000; // 1 giay throttle (giong Player)
    
    public EntityNameplateService(EntityManager entityManager, Plugin plugin, DisplayFormatService displayFormatService) {
        this.entityManager = entityManager;
        this.plugin = plugin;
        this.displayFormatService = displayFormatService;
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
        // Safety check: KHONG bao gio update nameplate cho Player (player co NameplateService rieng)
        if (entity instanceof Player) {
            return; // KHONG set custom name cho player
        }
        
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
        
        // Su dung DisplayFormatService de format nameplate (KHONG tinh toan logic o day)
        String displayName = profile.getDisplayName();
        String nameplate = displayFormatService.formatEntityNameplate(profile, displayName);
        
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
