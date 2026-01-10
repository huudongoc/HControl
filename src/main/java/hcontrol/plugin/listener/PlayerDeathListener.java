package hcontrol.plugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.DisplayFormatService;

/**
 * Xu ly player death - reset HP ve 0 trong profile
 */
public class PlayerDeathListener implements Listener {

    private final PlayerManager playerManager;
    
    public PlayerDeathListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    /**
     * Khi player chet - set HP = 0 trong profile
     * De scoreboard hien thi dung
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        if (profile == null) return;
        
        // Delegate death logic to PlayerHealthService
        var healthService = hcontrol.plugin.core.CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
        healthService.handleDeath(player, profile);
        
        // Update scoreboard (se hien thi HP: 0/max)
        var scoreboardService = hcontrol.plugin.core.CoreContext.getInstance().getUIContext().getScoreboardService();
        if (scoreboardService != null) {
            scoreboardService.updateScoreboard(player);
        }
            
        // Custom death message (optional) từ formatPlayerNameplate
        String deathMsg = DisplayFormatService.getInstance().formatPlayerNameplate(profile, "") + " §cđã tử vong";
        
        // Kiem tra nguyen nhan chet và custom death message
        if (event.getDeathMessage() != null && !event.getDeathMessage().isEmpty()) {
            event.setDeathMessage(deathMsg);
        }
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
            return;
        }
    }
}
