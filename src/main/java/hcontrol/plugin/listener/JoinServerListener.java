package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import hcontrol.plugin.ui.PlayerUIService;

public class JoinServerListener implements Listener {
    private final PlayerUIService playerUIService;
    
    public JoinServerListener(PlayerUIService playerUIService) {
        this.playerUIService = playerUIService;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        playerUIService.handlePlayerJoin(event.getPlayer());
    }
}