package hcontrol.plugin.ui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CHAT BUBBLE SERVICE
 * Hien thi chat message tren dau player (cho NPC/boss noi chuyen sau)
 */
public class ChatBubbleService {
    
    private final Plugin plugin;
    private final Map<UUID, ArmorStand> activeBubbles = new HashMap<>();
    
    public ChatBubbleService(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Hien thi chat bubble tren dau player
     * @param duration thoi gian hien thi (ticks) - mac dinh 60 ticks (3 giay)
     */
    public void showChatBubble(Player player, String message, int duration) {
        // Xoa bubble cu neu co
        removeChatBubble(player);
        
        // Tao armor stand invisible tren dau player
        Location loc = player.getLocation().add(0, player.getHeight() + 0.5, 0);
        ArmorStand bubble = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        
        // Config armor stand
        bubble.setVisible(false);
        bubble.setGravity(false);
        bubble.setMarker(true);
        bubble.setSmall(true);
        bubble.setCustomName(message);
        bubble.setCustomNameVisible(true);
        bubble.setInvulnerable(true);
        
        activeBubbles.put(player.getUniqueId(), bubble);
        
        // Task di theo player va tu dong xoa sau duration
        int[] tick = {0};
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            tick[0]++;
            
            // Neu player offline hoac qua thoi gian
            if (!player.isOnline() || tick[0] >= duration) {
                removeChatBubble(player);
                return;
            }
            
            // Update vi tri theo player
            Location newLoc = player.getLocation().add(0, player.getHeight() + 0.5, 0);
            bubble.teleport(newLoc);
            
        }, 0L, 1L);
        
        // Huy task sau khi het thoi gian
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskId);
            removeChatBubble(player);
        }, duration);
    }
    
    /**
     * Hien thi chat bubble mac dinh 3 giay
     */
    public void showChatBubble(Player player, String message) {
        showChatBubble(player, message, 60); // 3 giay
    }
    
    /**
     * Xoa chat bubble
     */
    public void removeChatBubble(Player player) {
        ArmorStand bubble = activeBubbles.remove(player.getUniqueId());
        if (bubble != null && !bubble.isDead()) {
            bubble.remove();
        }
    }
    
    /**
     * Xoa tat ca bubble (khi disable plugin)
     */
    public void removeAllBubbles() {
        for (ArmorStand bubble : activeBubbles.values()) {
            if (bubble != null && !bubble.isDead()) {
                bubble.remove();
            }
        }
        activeBubbles.clear();
    }
}
