package hcontrol.plugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.model.DeathContext;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.DeathMessageService;
import hcontrol.plugin.service.DeathService;

/**
 * Xu ly player death - reset HP ve 0 trong profile
 * REFACTORED: Su dung DeathService va DeathMessageService
 */
public class PlayerDeathListener implements Listener {

    private final PlayerManager playerManager;
    private final DeathService deathService;
    private final DeathMessageService deathMessageService;
    private final java.util.Set<java.util.UUID> processedDeaths = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    
    public PlayerDeathListener(PlayerManager playerManager, DeathService deathService, DeathMessageService deathMessageService) {
        this.playerManager = playerManager;
        this.deathService = deathService;
        this.deathMessageService = deathMessageService;
    }
    
    /**
     * Khi player chet - set HP = 0 trong profile
     * De scoreboard hien thi dung
     * REFACTORED: Su dung DeathService de xac dinh nguyen nhan chet
     * Mục tiêu: chỉ còn 1 message từ plugin (không còn dòng "player died")
     * - Chặn death message mặc định của server
     * - Plugin tự broadcast + tự log đúng message YAML
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(""); // Chặn vanilla death message
        Player player = event.getEntity();
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        if (profile == null) return;

        // Kiem tra xem da xu ly death cho player nay chua (tranh duplicate log)
        java.util.UUID playerUUID = player.getUniqueId();
        if (processedDeaths.contains(playerUUID)) {
            return; // Da xu ly roi, skip
        }
        processedDeaths.add(playerUUID);
        
        // Xoa UUID sau 1 tick de tranh memory leak
        var plugin = CoreContext.getInstance().getPlugin();
        if (plugin != null) {
            Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> processedDeaths.remove(playerUUID),
                1L
            );
        }

        // Build DeathContext tu player va event (truyen event de lay thong tin killer + weapon)
        var lastDamageCause = event.getEntity().getLastDamageCause();
        var damageCause = lastDamageCause != null 
            ? lastDamageCause.getCause() 
            : org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM;
        DeathContext ctx = deathService.buildContext(player, profile, damageCause, event);
        
        // Build death message tu DeathContext
        String deathMsg = deathMessageService.buildMessage(ctx);

        // Set custom death message - server sẽ tự động log và broadcast message này
        // Flag processedDeaths đảm bảo chỉ set 1 lần (tránh duplicate)
        // Không cần plugin tự log vì server đã log rồi
        event.setDeathMessage(deathMsg);

        // Delegate death logic to PlayerHealthService
        var healthService = CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
        healthService.handleDeath(player, profile);
    }
    
    /**
     * Ngăn player di chuyen khi da chet (HP = 0 hoac isDead = true)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check player da chet trong game
        if (player.isDead()) {
            // Cancel movement - giu player o vi tri chet
            event.setCancelled(true);
            return;
        }
        
        // Check HP = 0 trong profile (double check)
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile != null && profile.getCurrentHP() <= 0) {
            // Cancel movement - giu player o vi tri
            event.setCancelled(true);
        }
    }
}