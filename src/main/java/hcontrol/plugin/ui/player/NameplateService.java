package hcontrol.plugin.ui.player;

import hcontrol.plugin.master.DiscipleInfo;
import hcontrol.plugin.master.MasterManager;
import hcontrol.plugin.master.MasterRelation;
import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.sect.Sect;
import hcontrol.plugin.sect.SectManager;
import hcontrol.plugin.sect.SectMember;
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
 * Hien thi ten, level, realm, danh hieu, mon phai, su do tren dau player
 * 
 * Format: [MônPhái] [Sư/Đồ] [CảnhGiới] Player ❤HP% [DanhHiệu]
 */
public class NameplateService {
    
    private final PlayerManager playerManager;
    private final DisplayFormatService displayFormatService;
    private final Map<UUID, Long> lastUpdateTime = new HashMap<>();
    private static final long UPDATE_COOLDOWN_MS = 100; // 100ms - update NHANH cho combat
    
    // Optional dependencies (có thể null)
    private SectManager sectManager;
    private MasterManager masterManager;
    
    // Config flags
    private boolean showSect = true;
    private boolean showMasterStatus = true;
    private boolean showRealm = true;
    private boolean showTitle = true;
    
    public NameplateService(PlayerManager playerManager, DisplayFormatService displayFormatService) {
        this.playerManager = playerManager;
        this.displayFormatService = displayFormatService;
    }
    
    // Setters for optional dependencies
    public void setSectManager(SectManager sectManager) {
        this.sectManager = sectManager;
    }
    
    public void setMasterManager(MasterManager masterManager) {
        this.masterManager = masterManager;
    }
    
    // Config setters
    public void setShowSect(boolean show) { this.showSect = show; }
    public void setShowMasterStatus(boolean show) { this.showMasterStatus = show; }
    public void setShowRealm(boolean show) { this.showRealm = show; }
    public void setShowTitle(boolean show) { this.showTitle = show; }
    
    public boolean isShowSect() { return showSect; }
    public boolean isShowMasterStatus() { return showMasterStatus; }
    public boolean isShowRealm() { return showRealm; }
    public boolean isShowTitle() { return showTitle; }
    
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
        
        // Build prefix với thông tin mới
        String prefix = buildFullPrefix(player, profile);
        String suffix = buildSuffix(player, profile);
        
        // Set vao scoreboard team (khong lam mat custom name)
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }
        
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        
        // ĐẢM BẢO player KHÔNG bị set custom name như entity
        if (player.getCustomName() != null && !player.getCustomName().equals(player.getName())) {
            player.setCustomName(null);
            player.setCustomNameVisible(false);
        }
        
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
            otherTeam.setSuffix(suffix);
        }
    }
    
    /**
     * Build prefix với tất cả thông tin: [MônPhái] [Sư/Đồ] [CảnhGiới] ❤HP%
     */
    private String buildFullPrefix(Player player, PlayerProfile profile) {
        StringBuilder prefix = new StringBuilder();
        UUID uuid = player.getUniqueId();
        
        // 1. Môn phái
        if (showSect && sectManager != null) {
            Sect sect = sectManager.getPlayerSect(uuid);
            if (sect != null) {
                SectMember member = sect.getMember(uuid);
                String sectTag = buildSectTag(sect, member);
                prefix.append(sectTag).append(" ");
            }
        }
        
        // 2. Trạng thái Sư Phụ/Đệ Tử
        if (showMasterStatus && masterManager != null) {
            String masterTag = buildMasterTag(uuid);
            if (!masterTag.isEmpty()) {
                prefix.append(masterTag).append(" ");
            }
        }
        
        // 3. Cảnh giới + HP (dùng DisplayFormatService)
        if (showRealm) {
            String titleIcon = "";
            if (showTitle && profile.getActiveTitle() != null && profile.getActiveTitle().getIcon() != null) {
                titleIcon = profile.getActiveTitle().getIcon();
            }
            prefix.append(displayFormatService.formatPlayerNameplate(profile, titleIcon));
        }
        
        // Giới hạn độ dài (Minecraft limit: 64 chars)
        String result = prefix.toString();
        if (result.length() > 60) {
            result = result.substring(0, 60);
        }
        
        return result;
    }
    
    /**
     * Build suffix với danh hiệu
     */
    @SuppressWarnings("unused")
    private String buildSuffix(Player player, PlayerProfile profile) {
        if (!showTitle) return "";
        
        Title title = profile.getActiveTitle();
        if (title == null) return "";
        
        // Màu theo rarity (dùng color từ enum)
        String color = title.getRarity().getColor();
        
        String suffix = " " + color + "〈" + title.getDisplayName() + "〉";
        
        // Giới hạn độ dài
        if (suffix.length() > 60) {
            suffix = suffix.substring(0, 60);
        }
        
        return suffix;
    }
    
    private String buildSectTag(Sect sect, SectMember member) {
        if (sect == null) return "";
        
        // Màu theo rank
        String color = "§7";
        if (member != null) {
            color = switch (member.getRank()) {
                case LEADER -> "§6§l";        // Vàng đậm - Chưởng Môn
                case VICE_LEADER -> "§6";     // Vàng - Phó Môn
                case ELDER -> "§e";           // Vàng nhạt - Trưởng Lão
                case CORE_DISCIPLE -> "§a";   // Xanh lá - Chân Truyền
                case INNER_DISCIPLE -> "§2";  // Xanh đậm - Nội Môn
                case OUTER_DISCIPLE -> "§7";  // Xám - Ngoại Môn
            };
        }
        
        // Rút gọn tên môn phái nếu dài
        String sectName = sect.getName();
        if (sectName.length() > 6) {
            sectName = sectName.substring(0, 5) + "..";
        }
        
        return color + "[" + sectName + "]";
    }
    
    private String buildMasterTag(UUID playerUuid) {
        if (masterManager == null) return "";
        
        // Kiểm tra là sư phụ
        MasterRelation masterRel = masterManager.getMaster(playerUuid);
        if (masterRel != null && masterRel.getDiscipleCount() > 0) {
            return "§d[Sư]"; // Tím - Sư Phụ
        }
        
        // Kiểm tra là đệ tử
        DiscipleInfo discipleInfo = masterManager.getDisciple(playerUuid);
        if (discipleInfo != null && discipleInfo.hasMaster()) {
            return "§5[Đồ]"; // Tím đậm - Đệ Tử
        }
        
        return "";
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
