package hcontrol.plugin.listener;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.PlayerHealthService;
import hcontrol.plugin.ui.player.NameplateService;
import hcontrol.plugin.ui.player.PlayerUIService;
import hcontrol.plugin.ui.player.ScoreboardService;

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
    private final NameplateService nameplateService;
    
    public PlayerRespawnListener(PlayerManager playerManager, 
                                PlayerHealthService healthService,
                                PlayerUIService uiService,
                                ScoreboardService scoreboardService,
                                NameplateService nameplateService) {
        this.playerManager = playerManager;
        this.healthService = healthService;
        this.uiService = uiService;
        this.scoreboardService = scoreboardService;
        this.nameplateService = nameplateService;
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        if (profile == null) return;
        
        // Delegate respawn logic to PlayerHealthService
        healthService.handleRespawn(player, profile);
        
        // Restore movement speed (enable lai sau khi hoi sinh)
        player.setWalkSpeed(0.2f);  // Vanilla default walk speed
        player.setFlySpeed(0.1f);   // Vanilla default fly speed
        
        // Set hunger full (tu si khong can an)
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        // Update UI NGAY LẬP TỨC (attempt 1)
        healthService.syncHealth(player, profile);
        
        // Update UI LẦN 2 sau delay (để đảm bảo client nhận được)
        // Delay 10 ticks (0.5s) để đảm bảo player đã respawn xong hoàn toàn và client ready
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            CoreContext.getInstance().getPlugin(),
            () -> {
                // Force update health service LẦN 2 (sẽ update tablist)
                healthService.syncHealth(player, profile);
                
                // Update scoreboard
                if (scoreboardService != null) {
                    scoreboardService.updateScoreboard(player);
                }
                
                // Force update nameplate (bypass cooldown va cache de cap nhat HP)
                // Nameplate hien thi HP% nen can update khi HP thay doi tu 0 ve max
                if (nameplateService != null) {
                    nameplateService.updateNameplate(player, true);
                }
                
                // Optional: Gui message thong bao
                player.sendMessage("§a✦ Hồi sinh thành công!");
            },
            10L // Delay 10 ticks (0.5s) để client sync xong
        );
    }   
}
