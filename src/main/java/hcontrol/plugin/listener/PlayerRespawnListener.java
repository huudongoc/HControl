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
        
        // Update UI (ActionBar + Scoreboard + Nameplate)
        // Delay 2 ticks de dam bao player da respawn xong hoan toan
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            CoreContext.getInstance().getPlugin(),
            () -> {
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
            2L  // 2 ticks delay de dam bao respawn hoan toan
        );
        
    }
}
