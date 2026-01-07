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
        
        player.sendMessage("§7§m--------------------");
        player.sendMessage(String.format(
            "§e§lLEVEL INFO"
        ));
        player.sendMessage(String.format(
            "§7Level: §f%d §7| §7EXP: §e%d§7/§e%d §7(§e%d%%§7)", 
            profile.getLevel(), currentExp, requiredExp, progress
        ));
        player.sendMessage(String.format(
            "§7Stat Points: §a%d", 
            profile.getStatPoints()
        ));
        player.sendMessage("§7§m--------------------");
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
        int oldLevel = profile.getLevel();
        int newLevel = oldLevel + 1;
        long required = getRequiredExp(newLevel);
        
        profile.setLevel(newLevel);
        profile.setExp(profile.getExp() - required); // Carry over excess EXP
        
        // cong 5 stat point
        profile.addStatPoints(5);
        
        // thong bao level up
        Player player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("§e§lLEVEL UP!");
            player.sendMessage("§7Ban da len cap §f" + newLevel);
            player.sendMessage("§7+§a5 §7Stat Points");
            player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
        }
        
        // fire event
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(profile, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
    }
}
