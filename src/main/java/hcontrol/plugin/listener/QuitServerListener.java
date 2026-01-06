package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;

public class QuitServerListener implements Listener {
    private final PlayerManager playerManager;
    
    public QuitServerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerProfile profile = playerManager.get(event.getPlayer().getUniqueId());
        if (profile != null) {
            // TODO Phase 12: Save to storage
            playerManager.remove(event.getPlayer().getUniqueId());
        }
        
        event.setQuitMessage(null);
    }
}