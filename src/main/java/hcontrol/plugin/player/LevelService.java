package hcontrol.plugin.player;

import hcontrol.plugin.event.PlayerLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LevelService {

    // EXP curve: level^2 * 100
    public long getRequiredExp(int level) {
        return (long) level * level * 100;
    }

    public void sendLevelInfo(Player player, PlayerProfile profile) {
        long currentExp = profile.getExp();
        long requiredExp = getRequiredExp(profile.getLevel() + 1);
        long progress = currentExp * 100 / requiredExp;
        
        player.sendMessage(String.format(
            "§aLevel: §e%d §7[§e%d%%§7] §7| §aEXP: §e%d§7/§e%d", 
            profile.getLevel(), progress, currentExp, requiredExp
        ));
    }

    public void addExp(PlayerProfile profile, long amount) {
        long newExp = profile.getExp() + amount;
        profile.setExp(newExp);
        
        // Check level up
        while (canLevelUp(profile)) {
            levelUp(profile);
        }
    }

    private boolean canLevelUp(PlayerProfile profile) {
        long required = getRequiredExp(profile.getLevel() + 1);
        return profile.getExp() >= required;
    }

    private void levelUp(PlayerProfile profile) {
        int newLevel = profile.getLevel() + 1;
        long required = getRequiredExp(newLevel);
        
        profile.setLevel(newLevel);
        profile.setExp(profile.getExp() - required); // Carry over excess EXP
        
        // TODO: Level up event, stat point reward
    }
}
