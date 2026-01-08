package hcontrol.plugin.ui;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NAMEPLATE SERVICE
 * Hien thi ten, level, realm, danh hieu tren dau player/boss
 */
public class NameplateService {
    
    private final PlayerManager playerManager;
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>();
    private static final long UPDATE_COOLDOWN_MS = 1000; // 1 giay - tranh flash
    
    // Cache prefix de tranh update neu khong thay doi
    private final Map<UUID, String> lastPrefix = new HashMap<>(); // 0.5s cooldown
    
    public NameplateService(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    /**
     * Update nameplate cho player (voi cooldown de tranh spam)
     */
    public void updateNameplate(Player player) {
        updateNameplate(player, false);
    }
    
    /**
     * Update nameplate cho player
     * @param force true = bo qua cooldown, false = check cooldown
     */
    public void updateNameplate(Player player, boolean force) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        UUID uuid = player.getUniqueId();
        
        // Check cooldown (neu khong force)
        if (!force) {
            long now = System.currentTimeMillis();
            Long lastUpdate = lastUpdateTime.get(uuid);
            if (lastUpdate != null && (now - lastUpdate) < UPDATE_COOLDOWN_MS) {
                return; // skip update (qua nhanh)
            }
            lastUpdateTime.put(uuid, now);
        }
        
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
        
        // Tier name (Ha/Trung/Thuong/Dinh)
        String tierName = getTierName(level);
        
        // Format: [Title Icon] [Realm Tier] ❤ HP% PlayerName (KHONG hien thi level so)
        String prefix = titleDisplay + 
                       realm.getColor() + "[" + realm.getDisplayName() + " " + tierName + "] " +
                       hpColor + "❤ " + String.format("%.0f", hpPercent) + "% §f";
        
        // CHECK NEU PREFIX KHONG THAY DOI - SKIP UPDATE (tranh flash)
        String cachedPrefix = lastPrefix.get(uuid);
        if (cachedPrefix != null && cachedPrefix.equals(prefix)) {
            return; // khong thay doi - skip
        }
        lastPrefix.put(uuid, prefix);
        
        // Set vao scoreboard team (khong lam mat custom name)
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }
        
        team.setPrefix(prefix);
        
        // Cho player khac nhin thay
        // NOTE: Loop nay cap nhat scoreboard cua TAT CA PLAYER ONLINE
        // → Throttle chi check UUID cua player duoc update, KHONG check player nao dang nhin thay
        // → Co the gay "flash" neu nhieu player update lien tiep (vi moi player cap nhat scoreboard cua tat ca)
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
        UUID uuid = player.getUniqueId();
        
        // Cleanup cache
        lastUpdateTime.remove(uuid);
        lastPrefix.remove(uuid);
        
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
    
    /**
     * Lay tier name tu level
     */
    private String getTierName(int level) {
        if (level <= 3) return "§7Hạ";
        if (level <= 6) return "§eTrung";
        if (level <= 9) return "§6Thượng";
        return "§cĐỉnh";
    }
}
