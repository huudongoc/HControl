package hcontrol.plugin.ui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ENTITY DIALOG SERVICE
 * Foundation cho NPC dialog, quest, boss noi chuyen...
 * Hien thi chat bubble tren dau entity (giong player chat bubble)
 */
public class EntityDialogService {
    
    private final Plugin plugin;
    private final Map<UUID, ArmorStand> activeBubbles = new HashMap<>();
    
    public EntityDialogService(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Hien thi dialog tren dau entity
     * @param entity Entity noi chuyen (NPC, boss, mob...)
     * @param message Noi dung dialog
     * @param duration Thoi gian hien thi (ticks) - mac dinh 100 ticks (5 giay)
     */
    public void showDialog(LivingEntity entity, String message, int duration) {
        // Xoa dialog cu neu co
        removeDialog(entity);
        
        // Tao armor stand invisible tren dau entity
        Location loc = entity.getLocation().add(0, entity.getHeight() + 0.7, 0);
        ArmorStand bubble = (ArmorStand) entity.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        
        // Config armor stand
        bubble.setVisible(false);
        bubble.setGravity(false);
        bubble.setMarker(true);
        bubble.setSmall(true);
        bubble.setCustomName(message);
        bubble.setCustomNameVisible(true);
        bubble.setInvulnerable(true);
        
        activeBubbles.put(entity.getUniqueId(), bubble);
        
        // Task di theo entity va tu dong xoa sau duration
        final int[] taskIdHolder = {-1};
        
        taskIdHolder[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            // Neu entity da chet hoac dialog da bi xoa
            if (!entity.isValid() || entity.isDead() || !activeBubbles.containsKey(entity.getUniqueId())) {
                Bukkit.getScheduler().cancelTask(taskIdHolder[0]);
                removeDialog(entity);
                return;
            }
            
            // Update vi tri theo entity
            Location newLoc = entity.getLocation().add(0, entity.getHeight() + 0.7, 0);
            bubble.teleport(newLoc);
            
        }, 0L, 1L);
        
        // Huy task sau khi het thoi gian
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskIdHolder[0]);
            removeDialog(entity);
        }, duration);
    }
    
    /**
     * Hien thi dialog mac dinh 5 giay (lau hon player chat)
     */
    public void showDialog(LivingEntity entity, String message) {
        showDialog(entity, message, 100); // 5 giay
    }
    
    /**
     * Xoa dialog cua entity
     */
    public void removeDialog(LivingEntity entity) {
        ArmorStand bubble = activeBubbles.remove(entity.getUniqueId());
        if (bubble != null && !bubble.isDead()) {
            bubble.remove();
        }
    }
    
    /**
     * Xoa tat ca dialog (khi disable plugin)
     */
    public void removeAllDialogs() {
        for (ArmorStand bubble : activeBubbles.values()) {
            if (bubble != null && !bubble.isDead()) {
                bubble.remove();
            }
        }
        activeBubbles.clear();
    }
    
    /**
     * FUTURE: Multi-line dialog cho quest NPC
     */
    public void showMultilineDialog(LivingEntity entity, String[] lines, int durationPerLine) {
        // TODO: Spawn nhieu armor stands theo vertical
        // Foundation cho quest dialog system
    }
    
    /**
     * FUTURE: Dialog voi nut lua chon (click de lua chon)
     */
    public void showChoiceDialog(LivingEntity entity, String question, String[] choices) {
        // TODO: Foundation cho quest choice system
        // Hien thi choices, player click vao entity de chon
    }
    
    /**
     * FUTURE: Emit custom event khi dialog ket thuc
     */
    public void showDialogWithCallback(LivingEntity entity, String message, Runnable onComplete) {
        // TODO: Foundation cho quest completion callback
    }
}
