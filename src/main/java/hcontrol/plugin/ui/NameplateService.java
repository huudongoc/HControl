package hcontrol.plugin.ui;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.DisplayFormatService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NAMEPLATE SERVICE
 * Hien thi ten, level, realm, danh hieu tren dau player
 * KHONG chua logic tinh toan - chi su dung DisplayFormatService
 */
public class NameplateService {
    
    private final PlayerManager playerManager;
    private final DisplayFormatService displayFormatService;
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>();
    private static final long UPDATE_COOLDOWN_MS = 1000; // 1 giay - tranh flash
    
    // Cache prefix de tranh update neu khong thay doi
    private final Map<UUID, String> lastPrefix = new HashMap<>();
    
    public NameplateService(PlayerManager playerManager, DisplayFormatService displayFormatService) {
        this.playerManager = playerManager;
        this.displayFormatService = displayFormatService;
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
        
        // Lay title icon (neu co)
        String titleIcon = "";
        if (profile.getActiveTitle() != null && profile.getActiveTitle().getIcon() != null) {
            titleIcon = profile.getActiveTitle().getIcon();
        }
        
        // Su dung DisplayFormatService de format nameplate (KHONG tinh toan logic o day)
        String prefix = displayFormatService.formatPlayerNameplate(profile, titleIcon);
        
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
}
