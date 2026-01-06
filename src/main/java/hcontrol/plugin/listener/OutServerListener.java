package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import hcontrol.plugin.ui.PlayerUIService;

public class OutServerListener implements Listener {
    private final PlayerUIService playerUIService;
    
    public OutServerListener(PlayerUIService playerUIService) {
        this.playerUIService = playerUIService;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        playerUIService.handlePlayerQuit(event.getPlayer());
    }
}