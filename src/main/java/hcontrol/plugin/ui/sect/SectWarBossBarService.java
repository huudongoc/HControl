package hcontrol.plugin.ui.sect;

import hcontrol.plugin.event.SectWarStartEvent;
import hcontrol.plugin.sect.Sect;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * SECT WAR BOSSBAR SERVICE
 * Hiển thị BossBar khi Sect War bắt đầu
 * Trigger từ SectWarStartEvent
 * Không chạy thường xuyên
 */
public class SectWarBossBarService {
    
    private final Plugin plugin;
    private BossBar currentBossBar;
    private BukkitRunnable updateTask;
    
    public SectWarBossBarService(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Xử lý SectWarStartEvent - hiển thị BossBar
     */
    public void handleSectWarStart(SectWarStartEvent event) {
        // Xóa boss bar cũ nếu có
        clearBossBar();
        
        Sect sect1 = event.getSect1();
        Sect sect2 = event.getSect2();
        List<UUID> sect1Members = event.getSect1Members();
        List<UUID> sect2Members = event.getSect2Members();
        int durationSeconds = event.getDurationSeconds();
        
        // Tạo boss bar
        String title = String.format("§c⚔ §lCHIẾN TRANH MÔN PHÁI: §e%s §7vs §e%s §c⚔", 
            sect1.getName(), sect2.getName());
        
        currentBossBar = Bukkit.createBossBar(
            title,
            BarColor.RED,
            BarStyle.SEGMENTED_10
        );
        
        // Thêm tất cả members vào boss bar
        for (UUID memberUuid : sect1Members) {
            Player player = Bukkit.getPlayer(memberUuid);
            if (player != null && player.isOnline()) {
                currentBossBar.addPlayer(player);
            }
        }
        
        for (UUID memberUuid : sect2Members) {
            Player player = Bukkit.getPlayer(memberUuid);
            if (player != null && player.isOnline()) {
                currentBossBar.addPlayer(player);
            }
        }
        
        // Set progress ban đầu = 1.0 (100%)
        currentBossBar.setProgress(1.0);
        
        // Task update countdown
        final int[] remainingSeconds = {durationSeconds};
        
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentBossBar == null) {
                    cancel();
                    return;
                }
                
                remainingSeconds[0]--;
                
                // Update progress (countdown)
                double progress = (double) remainingSeconds[0] / durationSeconds;
                currentBossBar.setProgress(Math.max(0.0, progress));
                
                // Update title với countdown
                int minutes = remainingSeconds[0] / 60;
                int seconds = remainingSeconds[0] % 60;
                String timeText = String.format("%d:%02d", minutes, seconds);
                
                String updatedTitle = String.format("§c⚔ §lCHIẾN TRANH: §e%s §7vs §e%s §7[§c%s§7] §c⚔", 
                    sect1.getName(), sect2.getName(), timeText);
                currentBossBar.setTitle(updatedTitle);
                
                // Kết thúc khi hết thời gian
                if (remainingSeconds[0] <= 0) {
                    clearBossBar();
                    cancel();
                }
            }
        };
        
        // Chạy mỗi giây (20 ticks)
        updateTask.runTaskTimer(plugin, 0L, 20L);
    }
    
    /**
     * Xóa boss bar
     */
    public void clearBossBar() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        if (currentBossBar != null) {
            currentBossBar.removeAll();
            currentBossBar = null;
        }
    }
    
    /**
     * Cleanup khi disable plugin
     */
    public void shutdown() {
        clearBossBar();
    }
}
