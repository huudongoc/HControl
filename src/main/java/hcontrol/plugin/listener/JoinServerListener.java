package hcontrol.plugin.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import hcontrol.plugin.core.LifecycleManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.ui.PlayerUIService;
import hcontrol.plugin.ui.ScoreboardService;
import hcontrol.plugin.ui.NameplateService;

public class JoinServerListener implements Listener {
    private final PlayerUIService playerUIService;
    private final ScoreboardService scoreboardService;
    private final NameplateService nameplateService;
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private final LifecycleManager lifecycleManager;
    
    public JoinServerListener(PlayerUIService playerUIService, ScoreboardService scoreboardService,
                             NameplateService nameplateService, PlayerManager playerManager, 
                             PlayerStorage playerStorage, LifecycleManager lifecycleManager) {
        this.playerUIService = playerUIService;
        this.scoreboardService = scoreboardService;
        this.nameplateService = nameplateService;
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
        
        // set attack speed based on AGI (danh nhanh hon vanilla)
        updateAttackSpeed(player, profile);
        
        // create scoreboard ben phai
        scoreboardService.createScoreboard(player);
        
        // update nameplate tren dau
        nameplateService.updateNameplate(player);
        
        // UI
        event.setJoinMessage(null);
        playerUIService.handlePlayerJoin(player);
    }
    
    /**
     * Update attack speed based on AGI stat
     */
    private void updateAttackSpeed(org.bukkit.entity.Player player, PlayerProfile profile) {
        int agility = profile.getStats().getAgility();
        double attackSpeed = 4.0 + (agility / 10.0) * 0.5;
        
        var attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(attackSpeed);
        }
    }
}