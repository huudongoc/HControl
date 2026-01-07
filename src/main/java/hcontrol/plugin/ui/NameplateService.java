package hcontrol.plugin.ui;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * NAMEPLATE SERVICE
 * Hien thi ten, level, realm, danh hieu tren dau player/boss
 */
public class NameplateService {
    
    private final PlayerManager playerManager;
    
    public NameplateService(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    /**
     * Update nameplate cho player
     */
    public void updateNameplate(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        CultivationRealm realm = profile.getRealm();
        int level = profile.getLevel();
        
        // Tinh % HP
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getStats().getMaxHP();
        double hpPercent = (currentHP / maxHP) * 100.0;
        
        // Mau sac HP theo %
        String hpColor;
        if (hpPercent >= 75) {
            hpColor = "§a";  // xanh la - khoe manh
        } else if (hpPercent >= 50) {
            hpColor = "§e";  // vang - binh thuong
        } else if (hpPercent >= 25) {
            hpColor = "§6";  // cam - nguy hiem
        } else {
            hpColor = "§c";  // do - sap chet
        }
        
        // Danh hieu (neu co)
        String titleDisplay = "";
        if (profile.getActiveTitle() != null && profile.getActiveTitle().getIcon() != null) {
            titleDisplay = profile.getActiveTitle().getIcon();
        }
        
        // Format: [Title Icon] [Realm] Lv.XX ❤ HP% PlayerName
        String prefix = titleDisplay + 
                       realm.getColor() + "[" + realm.getDisplayName() + "] " +
                       "§7Lv." + level + " " +
                       hpColor + "❤ " + String.format("%.0f", hpPercent) + "% §f";
        
        // Set vao scoreboard team (khong lam mat custom name)
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }
        
        team.setPrefix(prefix);
        
        // Cho player khac nhin thay
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            
            Scoreboard otherBoard = other.getScoreboard();
            Team otherTeam = otherBoard.getTeam(player.getName());
            
            if (otherTeam == null) {
                otherTeam = otherBoard.registerNewTeam(player.getName());
                otherTeam.addEntry(player.getName());
            }
            
            otherTeam.setPrefix(prefix);
        }
    }
    
    /**
     * Update nameplate cho tat ca player online
     */
    public void updateAllNameplates() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateNameplate(player);
        }
    }
    
    /**
     * Remove nameplate khi quit
     */
    public void removeNameplate(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        if (team != null) {
            team.unregister();
        }
        
        // Xoa khoi scoreboard cua nguoi khac
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            
            Scoreboard otherBoard = other.getScoreboard();
            Team otherTeam = otherBoard.getTeam(player.getName());
            if (otherTeam != null) {
                otherTeam.unregister();
            }
        }
    }
}
