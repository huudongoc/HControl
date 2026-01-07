package hcontrol.plugin.player;

import hcontrol.plugin.model.CultivationRealm;

import hcontrol.plugin.model.CultivatorProfile;
import hcontrol.plugin.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * CULTIVATION TASK
 * Tick-based cultivation system (tu luyen lien tuc)
 */
public class CultivationTask extends BukkitRunnable {
    
    private final PlayerManager playerManager;
    private final CultivationService cultivationService;
    
    public CultivationTask(PlayerManager playerManager, CultivationService cultivationService) {
        this.playerManager = playerManager;
        this.cultivationService = cultivationService;
    }
    
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            var profile = playerManager.get(player.getUniqueId());
            if (profile == null) continue;
            
            // Convert PlayerProfile -> CultivatorProfile (tam thoi)
            // TODO: migrate toan bo sang CultivatorProfile
            
            // Tu luyen passive (1 tick = 1 tu vi neu dung yen)
            if (isPlayerMeditating(player)) {
                // Player dang ngoi thien
                // cultivationService.cultivate(profile, 1);
                
                // Hien actionbar
                player.sendActionBar("§aĐang tu luyện... §7+" + 1 + " Tu Vi");
            }
        }
    }
    
    /**
     * Kiem tra player co dang meditation khong
     * (tam thoi check sneaking)
     */
    private boolean isPlayerMeditating(Player player) {
        return player.isSneaking() && !player.isFlying();
    }
    
    /**
     * Start task
     */
    public void start(Plugin plugin) {
        // Chay moi giay (20 ticks)
        this.runTaskTimer(plugin, 20L, 20L);
    }
}
