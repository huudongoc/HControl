package hcontrol.plugin.listener;

import hcontrol.plugin.event.SectWarStartEvent;
import hcontrol.plugin.ui.sect.SectWarBossBarService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * SECT WAR LISTENER
 * Lắng nghe SectWarStartEvent để hiển thị BossBar
 */
public class SectWarListener implements Listener {
    
    private final SectWarBossBarService bossBarService;
    
    public SectWarListener(SectWarBossBarService bossBarService) {
        this.bossBarService = bossBarService;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSectWarStart(SectWarStartEvent event) {
        // Hiển thị BossBar khi war bắt đầu
        bossBarService.handleSectWarStart(event);
    }
}
