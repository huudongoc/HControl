package hcontrol.plugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        
        // Set HP = 0 trong profile
        profile.setCurrentHP(0);
        
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
}
