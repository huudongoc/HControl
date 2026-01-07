package hcontrol.plugin.player;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * PHASE 1 — Auto-save player data dinh ky
 */
public class AutoSaveTask {
    
    private final Plugin plugin;
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private final long intervalTicks; // 20 tick = 1 giay
    
    private BukkitTask task;
    
    public AutoSaveTask(Plugin plugin, PlayerManager playerManager, PlayerStorage playerStorage) {
        this(plugin, playerManager, playerStorage, 5 * 60 * 20L); // 5 phut mac dinh
    }
    
    public AutoSaveTask(Plugin plugin, PlayerManager playerManager, PlayerStorage playerStorage, long intervalTicks) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.playerStorage = playerStorage;
        this.intervalTicks = intervalTicks;
    }
    
    /**
     * Bat dau auto-save task
     */
    public void start() {
        if (task != null) {
            plugin.getLogger().warning("Auto-save task da chay roi!");
            return;
        }
        
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            int count = 0;
            for (PlayerProfile profile : playerManager.getAllOnline()) {
                try {
                    playerStorage.save(profile);
                    count++;
                } catch (Exception e) {
                    plugin.getLogger().severe("Loi khi auto-save player " + profile.getUuid() + ": " + e.getMessage());
                }
            }
            
            if (count > 0) {
                plugin.getLogger().info("[Auto-Save] Da luu " + count + " player");
            }
        }, intervalTicks, intervalTicks);
        
        plugin.getLogger().info("[Auto-Save] Task da khoi dong (moi " + (intervalTicks / 20) + " giay)");
    }
    
    /**
     * Dung auto-save task
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            plugin.getLogger().info("[Auto-Save] Task da dung");
        }
    }
    
    /**
     * Kiem tra task co dang chay khong
     */
    public boolean isRunning() {
        return task != null;
    }
}
