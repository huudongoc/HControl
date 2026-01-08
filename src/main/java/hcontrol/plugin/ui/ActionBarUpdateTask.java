package hcontrol.plugin.ui;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import hcontrol.plugin.Main;
import hcontrol.plugin.player.PlayerManager;

/**
 * ACTION BAR UPDATE TASK
 * Update action bar (thanh thong tin duoi man hinh) moi 0.5 giay cho tat ca player online
 */
public class ActionBarUpdateTask extends BukkitRunnable {
    
    private final ActionBarService actionBarService;
    private final PlayerManager playerManager;
    
    public ActionBarUpdateTask(ActionBarService actionBarService, PlayerManager playerManager) {
        this.actionBarService = actionBarService;
        this.playerManager = playerManager;
    }
    
    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (playerManager.isOnline(player.getUniqueId())) {
                try {
                    actionBarService.render(player);
                } catch (Exception e) {
                    // log error nhung khong crash task
                    // tranh 1 player loi lam toan bo task dung
                }
            }
        });
    }
    
    /**
     * Start task (update moi 0.5 giay = 10 ticks)
     */
    public void start(Main plugin) {
        this.runTaskTimer(plugin, 10L, 10L); // delay 0.5s, repeat 0.5s
    }
}
