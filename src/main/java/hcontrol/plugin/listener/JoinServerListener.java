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
import hcontrol.plugin.ui.player.NameplateService;
import hcontrol.plugin.ui.player.PlayerUIService;
import hcontrol.plugin.ui.player.ScoreboardService;

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
        
        // sync vanilla health with tu tien stats
        var healthService = hcontrol.plugin.core.CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
        healthService.syncHealth(player, profile);
        
        // set attack speed based on AGI (danh nhanh hon vanilla)
        updateAttackSpeed(player, profile);
        
        // TAT HUNGER - tu si khong can an (set full de khong bi hien hieu ung doi)
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        
        // create scoreboard ben phai
        scoreboardService.createScoreboard(player);
        
        // update nameplate tren dau (force bypass throttle)
        nameplateService.updateNameplate(player, true);
        
        // Force update UI sau 1 tick de dam bao HP da sync xong
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            hcontrol.plugin.core.CoreContext.getInstance().getPlugin(),
            () -> {
                scoreboardService.updateScoreboard(player);
                nameplateService.updateNameplate(player, true); // force bypass throttle
            },
            1L
        );
        
        // UI
        event.setJoinMessage(null);
        playerUIService.handlePlayerJoin(player);
    }
    
    /**
     * Update attack speed based on AGI stat
     */
    private void updateAttackSpeed(org.bukkit.entity.Player player, PlayerProfile profile) {
        int agility = profile.getStats().getAgility();
        double attackSpeed = 1.0 + (agility / 10.0) * 0.5;
        
        var attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(attackSpeed);
        }
    }
}