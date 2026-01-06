package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.PlayerUIService;

public class JoinServerListener implements Listener {
    private final PlayerUIService playerUIService;
    private final PlayerManager playerManager;
    
    public JoinServerListener(PlayerUIService playerUIService, PlayerManager playerManager) {
        this.playerUIService = playerUIService;
        this.playerManager = playerManager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Phase 1: Create/load profile FIRST
        PlayerProfile profile = playerManager.get(event.getPlayer().getUniqueId());
        if (profile == null) {
            profile = new PlayerProfile(event.getPlayer().getUniqueId());
            playerManager.add(profile);
        }
        
        // Then UI
        event.setJoinMessage(null);
        playerUIService.handlePlayerJoin(event.getPlayer());
    }
}