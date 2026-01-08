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
        
        // Update nameplate ngay voi profile DA UPDATE
        updateNameplate(entity, profile);
        
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
            
            updateNameplate(entity, currentProfile);
        }, 100L, 100L);
        
        // ADD TASK VAO MAP de co the cancel sau nay
        updateTasks.put(uuid, task);
    }
    
    /**
     * Disable nameplate va stop update task
     */
    public void disableNameplate(LivingEntity entity) {
        BukkitTask task = updateTasks.remove(entity.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Reset entity name
        entity.setCustomName(null);
        entity.setCustomNameVisible(false);
    }
    
    /**
     * Update nameplate display (public de CombatService goi khi bi danh)
     */
    public void updateNameplate(LivingEntity entity, EntityProfile profile) {
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
    }
}
