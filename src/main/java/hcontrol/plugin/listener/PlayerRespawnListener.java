package hcontrol.plugin.listener;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.PlayerHealthService;
import hcontrol.plugin.ui.PlayerUIService;
import hcontrol.plugin.ui.ScoreboardService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * XU LY HOI SINH PLAYER
 * Reset HP ve max, sync vanilla health, update UI
 */
public class PlayerRespawnListener implements Listener {
    
    private final PlayerManager playerManager;
    private final PlayerHealthService healthService;
    private final PlayerUIService uiService;
    private final ScoreboardService scoreboardService;
    
    public PlayerRespawnListener(PlayerManager playerManager, 
                                PlayerHealthService healthService,
                                PlayerUIService uiService,
                                ScoreboardService scoreboardService) {
        this.playerManager = playerManager;
        this.healthService = healthService;
        this.uiService = uiService;
        this.scoreboardService = scoreboardService;
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        if (profile == null) return;
        
        // Reset HP ve max (hoi sinh full mau)
        double maxHP = profile.getStats().getMaxHP();
        profile.setCurrentHP(maxHP);
        
        // Reset Linh Khi ve max
        double maxLingQi = profile.getStats().getMaxLingQi();
        profile.setCurrentLingQi(maxLingQi);
        
        // Sync vanilla health
        healthService.syncHealth(player, profile);
        
        // Set hunger full (tu si khong can an)
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        // Update UI (ActionBar + Scoreboard)
        // Delay 1 tick de dam bao player da respawn xong
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            CoreContext.getInstance().getPlugin(),
            () -> {
                // Update scoreboard (hien thi HP/Linh Khi moi)
                scoreboardService.updateScoreboard(player);
                
                // Optional: Gui message thong bao
                player.sendMessage("§a✦ Hồi sinh thành công!");
            },
            1L  // 1 tick delay
        );
    }
}
