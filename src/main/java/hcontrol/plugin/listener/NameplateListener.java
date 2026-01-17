package hcontrol.plugin.listener;

import hcontrol.plugin.event.PlayerStateChangeEvent;
import hcontrol.plugin.ui.player.NameplateService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

/**
 * NAMEPLATE LISTENER
 * Lắng nghe PlayerStateChangeEvent để tự động cập nhật nameplate
 * 
 * 📌 NGUYÊN TẮC: Chỉ có 1 nơi duy nhất gọi updateNameplate() - đây!
 */
public class NameplateListener implements Listener {
    
    private final JavaPlugin plugin;
    private final NameplateService nameplateService;
    
    public NameplateListener(JavaPlugin plugin, NameplateService nameplateService) {
        this.plugin = plugin;
        this.nameplateService = nameplateService;
    }
    
    /**
     * Lắng nghe mọi thay đổi trạng thái của player
     * 🔥 Invalidate cache và rebuild static prefix
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerStateChange(PlayerStateChangeEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) return;
        
        // Invalidate cache để force rebuild static prefix
        nameplateService.invalidateCache(player.getUniqueId());
        
        // Update nameplate (force rebuild)
        nameplateService.updateNameplate(player, true);
        
        // 🔥 Update tab list khi realm thay đổi (để tab và chat cập nhật cảnh giới)
        if (event.getType() == hcontrol.plugin.event.PlayerStateChangeType.REALM_CHANGE) {
            var playerManager = hcontrol.plugin.core.CoreContext.getInstance().getPlayerManager();
            if (playerManager != null) {
                var profile = playerManager.get(player.getUniqueId());
                if (profile != null) {
                    var healthService = hcontrol.plugin.core.CoreContext.getInstance()
                        .getPlayerContext().getPlayerHealthService();
                    if (healthService != null) {
                        // Update tab list để hiển thị cảnh giới mới
                        healthService.syncHealth(player, profile);
                    }
                }
            }
        }
        
        // Cập nhật nameplate của tất cả player khác để họ thấy thay đổi
        // (ví dụ: player join sect → tất cả player khác cần thấy tag môn phái mới)
        nameplateService.updateAllNameplates();
    }
    
    /**
     * Khi player join server
     * 🔥 Batch update để tránh loop spam
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Delay để đảm bảo profile đã được load
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    // Batch update tất cả player (bao gồm player mới)
                    // Tránh loop spam từng player một
                    var playerManager = hcontrol.plugin.core.CoreContext.getInstance()
                        .getPlayerManager();
                    if (playerManager != null) {
                        Collection<hcontrol.plugin.player.PlayerProfile> online = 
                            playerManager.getAllOnline();
                        nameplateService.batchUpdate(online);
                    }
                }
            }
        }.runTaskLater(plugin, 20L); // 1 giây delay
    }
    
    /**
     * Khi player quit server
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        nameplateService.removeNameplate(event.getPlayer());
    }
}
