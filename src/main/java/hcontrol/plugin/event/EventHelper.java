package hcontrol.plugin.event;

import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * EVENT HELPER
 * Utility class để bắn PlayerStateChangeEvent dễ dàng
 */
public class EventHelper {
    
    /**
     * Bắn event thay đổi trạng thái player
     */
    public static void fireStateChange(Player player, PlayerProfile profile, PlayerStateChangeType type) {
        fireStateChange(player, profile, type, null);
    }
    
    /**
     * Bắn event với data kèm theo
     */
    public static void fireStateChange(Player player, PlayerProfile profile, PlayerStateChangeType type, Object data) {
        if (player == null || !player.isOnline() || profile == null) {
            return;
        }
        
        PlayerStateChangeEvent event = new PlayerStateChangeEvent(player, profile, type, data);
        Bukkit.getPluginManager().callEvent(event);
    }
    
    /**
     * Bắn event từ PlayerProfile (tự động lấy Player)
     */
    public static void fireStateChange(PlayerProfile profile, PlayerStateChangeType type) {
        if (profile == null) return;
        
        Player player = profile.getPlayer();
        if (player == null || !player.isOnline()) return;
        
        fireStateChange(player, profile, type);
    }
    
    /**
     * Bắn event từ PlayerProfile với data
     */
    public static void fireStateChange(PlayerProfile profile, PlayerStateChangeType type, Object data) {
        if (profile == null) return;
        
        Player player = profile.getPlayer();
        if (player == null || !player.isOnline()) return;
        
        fireStateChange(player, profile, type, data);
    }
}
