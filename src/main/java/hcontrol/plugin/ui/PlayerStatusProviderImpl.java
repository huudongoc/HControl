package hcontrol.plugin.ui;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * PLAYER STATUS PROVIDER IMPLEMENTATION
 * Lay thong tin trang thai player de hien thi len UI (ActionBar)
 */
public class PlayerStatusProviderImpl implements PlayerStatusProvider {
    
    private final PlayerManager playerManager;
    
    public PlayerStatusProviderImpl(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    @Override
    public PlayerStatusSnapshot getStatus(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        // neu chua load profile thi tra ve default
        if (profile == null) {
            return new PlayerStatusSnapshot(
                1,
                20.0,
                20.0,
                100.0,
                100.0
            );
        }
        
        // lay thong tin tu profile
        return new PlayerStatusSnapshot(
            profile.getLevel(),
            profile.getCurrentHP(),
            profile.getStats().getMaxHP(),
            profile.getCurrentLingQi(),
            profile.getStats().getMaxLingQi()
        );
    }
}
