package hcontrol.plugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import hcontrol.plugin.core.LifecycleManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.ui.PlayerUIService;

public class JoinServerListener implements Listener {
    private final PlayerUIService playerUIService;
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private final LifecycleManager lifecycleManager;
    
    public JoinServerListener(PlayerUIService playerUIService, PlayerManager playerManager, 
                             PlayerStorage playerStorage, LifecycleManager lifecycleManager) {
        this.playerUIService = playerUIService;
        this.playerManager = playerManager;
        this.playerStorage = playerStorage;
        this.lifecycleManager = lifecycleManager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();
        
        // load tu file, neu chua co thi tao moi
        PlayerProfile profile = playerStorage.load(uuid);
        if (profile == null) {
            profile = new PlayerProfile(uuid);
            player.sendMessage("§e§lChao mung! §7Lan dau choi server a?");
        }
        
        playerManager.add(profile);
        
        // callback lifecycle
        lifecycleManager.onPlayerLoad(profile);
        
        // UI
        event.setJoinMessage(null);
        playerUIService.handlePlayerJoin(player);
    }
}