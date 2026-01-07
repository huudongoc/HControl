package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import hcontrol.plugin.core.LifecycleManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.ui.PlayerUIService;

public class OutServerListener implements Listener {
    private final PlayerUIService playerUIService;
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private final LifecycleManager lifecycleManager;
    
    public OutServerListener(PlayerUIService playerUIService, PlayerManager playerManager,
                            PlayerStorage playerStorage, LifecycleManager lifecycleManager) {
        this.playerUIService = playerUIService;
        this.playerManager = playerManager;
        this.playerStorage = playerStorage;
        this.lifecycleManager = lifecycleManager;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();
        
        PlayerProfile profile = playerManager.get(uuid);
        if (profile != null) {
            // callback lifecycle
            lifecycleManager.onPlayerSave(profile);
            
            // save xuong file
            playerStorage.save(profile);
            
            // xoa khoi RAM
            playerManager.remove(uuid);
        }
        
        // UI
        event.setQuitMessage(null);
        playerUIService.handlePlayerQuit(player);
    }
}