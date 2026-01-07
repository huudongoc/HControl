package hcontrol.plugin.ui;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import hcontrol.plugin.Main;
import hcontrol.plugin.player.PlayerManager;

/**
 * TASK UPDATE SCOREBOARD
 * Update scoreboard moi 1 giay cho tat ca player online
 */
public class ScoreboardUpdateTask extends BukkitRunnable {
    
    private final ScoreboardService scoreboardService;
    private final PlayerManager playerManager;
    
    public ScoreboardUpdateTask(ScoreboardService scoreboardService, PlayerManager playerManager) {
        this.scoreboardService = scoreboardService;
        this.playerManager = playerManager;
    }
    
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (playerManager.isOnline(player.getUniqueId())) {
                scoreboardService.updateScoreboard(player);
            }
        });
    }
    
    /**
     * Start task (update moi 1 giay = 20 ticks)
     */
    public void start(Main plugin) {
        this.runTaskTimer(plugin, 20L, 20L); // delay 1s, repeat 1s
    }
}
