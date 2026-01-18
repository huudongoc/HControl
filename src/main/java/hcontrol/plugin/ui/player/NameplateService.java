package hcontrol.plugin.ui.player;

import hcontrol.plugin.master.DiscipleInfo;
import hcontrol.plugin.master.MasterManager;
import hcontrol.plugin.master.MasterRelation;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.sect.Sect;
import hcontrol.plugin.sect.SectManager;
import hcontrol.plugin.sect.SectMember;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NAMEPLATE SERVICE - OPTIMIZED
 * 
 * 🔥 Cache static prefix (realm, sect, title, master) - chỉ rebuild khi state change
 * 🔥 HP render riêng (thay đổi liên tục)
 * 🔥 Batch update để tránh loop spam
 * 
 * Format: [MônPhái] [Sư/Đồ] [CảnhGiới] Player ❤HP% [DanhHiệu]
 */
public class NameplateService {
    
    private final PlayerManager playerManager;
    private final Map<UUID, NameplateData> cache = new HashMap<>();
    private final Map<UUID, Long> lastHPUpdateTime = new HashMap<>();
    private static final long HP_UPDATE_COOLDOWN_MS = 100; // 100ms cho HP update
    private static final String CHAT_SEPARATOR = " §8» §f";
    
    // Optional dependencies (có thể null)
    private SectManager sectManager;
    private MasterManager masterManager;
    
    // Config flags
    private boolean showSect = true;
    private boolean showMasterStatus = true;
    private boolean showRealm = true;
    private boolean showTitle = true;
    
    public NameplateService(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    // Setters for optional dependencies
    public void setSectManager(SectManager sectManager) {
        this.sectManager = sectManager;
    }
    
    public void setMasterManager(MasterManager masterManager) {
        this.masterManager = masterManager;
    }
    
    // Config setters
    public void setShowSect(boolean show) { 
        this.showSect = show; 
        invalidateAllCache(); // Force rebuild khi config thay đổi
    }
    
    public void setShowMasterStatus(boolean show) { 
        this.showMasterStatus = show; 
        invalidateAllCache();
    }
    
    public void setShowRealm(boolean show) { 
        this.showRealm = show; 
        invalidateAllCache();
    }
    
    public void setShowTitle(boolean show) { 
        this.showTitle = show; 
        invalidateAllCache();
    }
    
    public boolean isShowSect() { return showSect; }
    public boolean isShowMasterStatus() { return showMasterStatus; }
    public boolean isShowRealm() { return showRealm; }
    public boolean isShowTitle() { return showTitle; }
    
    // ===== PUBLIC API =====
    
    /**
     * Update nameplate cho player (với HP update nhanh)
     */
    public void updateNameplate(Player player) {
        updateNameplate(player, false);
    }
    
    /**
     * Update nameplate cho player
     * @param force true = force rebuild static prefix, false = dùng cache nếu có
     */
    public void updateNameplate(Player player, boolean force) {
        if (player == null || !player.isOnline()) return;
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return;
        
        UUID uuid = player.getUniqueId();
        
        // Lấy hoặc tạo cache
        NameplateData data = cache.get(uuid);
        if (data == null || force) {
            data = rebuildStaticData(player, profile);
            cache.put(uuid, data);
        }
        
        // Tính HP percent
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getMaxHP();
        double hpPercent = maxHP > 0 ? (currentHP / maxHP) * 100.0 : 0.0;
        
        // Build full prefix với HP (HP render riêng)
        String prefix = data.buildFullPrefix(hpPercent);
        String suffix = data.getStaticSuffix();
        
        // Apply to scoreboard
        applyToScoreboard(player, prefix, suffix);
    }
    
    /**
     * 🔥 BATCH UPDATE - Tránh loop spam
     * Dùng khi: join server, reload plugin, sect war start
     */
    public void batchUpdate(Collection<PlayerProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) return;
        
        List<Player> players = profiles.stream()
            .map(PlayerProfile::getPlayer)
            .filter(p -> p != null && p.isOnline())
            .collect(Collectors.toList());
        
        if (players.isEmpty()) return;
        
        // Rebuild cache cho tất cả (force)
        for (Player player : players) {
            PlayerProfile profile = playerManager.get(player.getUniqueId());
            if (profile != null) {
                NameplateData data = rebuildStaticData(player, profile);
                cache.put(player.getUniqueId(), data);
            }
        }
        
        // Apply tất cả cùng lúc
        for (Player player : players) {
            PlayerProfile profile = playerManager.get(player.getUniqueId());
            if (profile != null) {
                NameplateData data = cache.get(player.getUniqueId());
                if (data != null) {
                    double currentHP = profile.getCurrentHP();
                    double maxHP = profile.getMaxHP();
                    double hpPercent = maxHP > 0 ? (currentHP / maxHP) * 100.0 : 0.0;
                    
                    String prefix = data.buildFullPrefix(hpPercent);
                    String suffix = data.getStaticSuffix();
                    applyToScoreboard(player, prefix, suffix);
                }
            }
        }
    }
    
    /**
     * Update chỉ HP (nhanh, dùng cache static prefix)
     */
    public void updateHP(Player player) {
        if (player == null || !player.isOnline()) return;
        
        UUID uuid = player.getUniqueId();
        
        // Throttle HP update
        long now = System.currentTimeMillis();
        Long lastUpdate = lastHPUpdateTime.get(uuid);
        if (lastUpdate != null && (now - lastUpdate) < HP_UPDATE_COOLDOWN_MS) {
            return; // Skip nếu quá nhanh
        }
        lastHPUpdateTime.put(uuid, now);
        
        PlayerProfile profile = playerManager.get(uuid);
        if (profile == null) return;
        
        NameplateData data = cache.get(uuid);
        if (data == null) {
            // Chưa có cache -> rebuild toàn bộ
            updateNameplate(player, true);
            return;
        }
        
        // Chỉ update HP (dùng cache static prefix)
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getMaxHP();
        double hpPercent = maxHP > 0 ? (currentHP / maxHP) * 100.0 : 0.0;
        
        String prefix = data.buildFullPrefix(hpPercent);
        String suffix = data.getStaticSuffix();
        
        applyToScoreboard(player, prefix, suffix);
    }
    
    /**
     * 🔥 Build tab list name - REAL-TIME (không dùng cache cho realm)
     * Format: [CảnhGiới Level][MônPhái] PlayerName ❤85%
     * 
     * 📌 LƯU Ý: Tab list cần data REAL-TIME để cập nhật khi realm thay đổi
     * Luôn lấy realm trực tiếp từ profile (không dùng cache)
     * 
     * @param player Player
     * @return Tab list display name hoặc null nếu không có data
     */
    public String buildTabListName(Player player) {
        if (player == null || !player.isOnline()) return null;
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return null;
        
        UUID uuid = player.getUniqueId();
        
        // Tính HP percent
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getMaxHP();
        double hpPercent = maxHP > 0 ? (currentHP / maxHP) * 100.0 : 100.0;
        
        // Lấy màu HP
        String hpColor = getHPColor(hpPercent);
        
        // Build tab list name: [CảnhGiới Level][MônPhái] PlayerName ❤85%
        
        // 🔥 Lấy realm tag TRỰC TIẾP từ profile (REAL-TIME, không dùng cache)
        String realmTag = "";
        if (showRealm) {
            CultivationRealm realm = profile.getRealm();
            int level = profile.getRealmLevel();
            realmTag = getRealmTag(realm, level);
        }
        
        // Lấy sect tag
        String sectTag = "";
        if (showSect && sectManager != null) {
            Sect sect = sectManager.getPlayerSect(uuid);
            if (sect != null) {
                SectMember member = sect.getMember(uuid);
                sectTag = buildSectTag(sect, member);
            }
        }
        
        // Build: [CảnhGiới Level][MônPhái] PlayerName ❤85%
        StringBuilder tabListName = new StringBuilder();
        if (!realmTag.isEmpty()) {
            tabListName.append(realmTag);
        }
        if (!sectTag.isEmpty()) {
            tabListName.append(sectTag);
        }
        tabListName.append(" §f").append(player.getName());
        tabListName.append(" ").append(hpColor).append("❤").append(String.format("%.0f%%", hpPercent));
        
        return tabListName.toString();
    }
    
    /**
     * Lấy màu HP theo phần trăm (dùng chung với NameplateData)
     */
    private String getHPColor(double hpPercent) {
        if (hpPercent >= 75) {
            return "§a";  // xanh lá
        } else if (hpPercent >= 50) {
            return "§e";  // vàng
        } else if (hpPercent >= 25) {
            return "§6";  // cam
        } else {
            return "§c";  // đỏ
        }
    }
    
    /**
     * 🔥 Build chat prefix từ NameplateData (reuse data)
     * Format: [CảnhGiới Level][MônPhái][DanhHiệu] PlayerName ❤HP%
     * Dùng cho ChatFormatService - PHASE 5
     * 
     * 📌 LƯU Ý: Chat format cần data REAL-TIME, không dùng cache
     * Vì chat format được gọi mỗi lần chat, nên luôn lấy data mới nhất
     * 
     * @param player Player
     * @return Chat prefix với đầy đủ thông tin: realm, sect, title, HP
     */
    public String buildChatPrefix(Player player) {
        if (player == null || !player.isOnline()) return "";
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return "";
        
        UUID uuid = player.getUniqueId();
        
        // 🔥 Chat format cần data REAL-TIME, không dùng cache
        // Luôn lấy data mới nhất
        StringBuilder chatPrefix = new StringBuilder();
        
        // 1. Cảnh giới + Level
        if (showRealm) {
            CultivationRealm realm = profile.getRealm();
            int level = profile.getRealmLevel();
            String realmTag = getRealmTag(realm, level);
            chatPrefix.append(realmTag);
        }
        
        // 2. Môn phái
        if (showSect && sectManager != null) {
            Sect sect = sectManager.getPlayerSect(uuid);
            if (sect != null) {
                chatPrefix.append("§7[").append(sect.getName()).append("]");
            }
        }
        
        // 3. Danh hiệu (Title)
        if (showTitle) {
            Title title = profile.getActiveTitle();
            if (title != null && title != Title.NONE) {
                String color = title.getRarity().getColor();
                chatPrefix.append(" ").append(color).append("〈").append(title.getDisplayName()).append("〉");
            }
        }
        
        // 4. Master status (Sư phụ hoặc Đệ tử)
        if (showMasterStatus && masterManager != null) {
            // Kiểm tra là sư phụ
            MasterRelation masterRel = masterManager.getMaster(uuid);
            if (masterRel != null && masterRel.getDiscipleCount() > 0) {
                chatPrefix.append(" §7[Sư phụ]");
            } else {
                // Kiểm tra là đệ tử
                DiscipleInfo discipleInfo = masterManager.getDisciple(uuid);
                if (discipleInfo != null && discipleInfo.hasMaster()) {
                    chatPrefix.append(" §7[Đệ tử]");
                }
            }
        }
        
        return chatPrefix.toString();
    }
    
    /**
     * Build chat format với HP
     * Format: [CảnhGiới Level][MônPhái][DanhHiệu] PlayerName ❤HP%: message
     * 
     * @param player Player
     * @return Chat format string với placeholder %1$s (player name) và %2$s (message)
     */
    // public String buildChatFormat(Player player) {
    //     if (player == null || !player.isOnline()) return "§7%1$s: §f%2$s";
        
    //     PlayerProfile profile = playerManager.get(player.getUniqueId());
    //     if (profile == null) return "§7%1$s: §f%2$s";
        
    //     // Lấy prefix (realm, sect, title, master)
    //     String prefix = buildChatPrefix(player);
        
    //     // Tính HP percent
    //     double currentHP = profile.getCurrentHP();
    //     double maxHP = profile.getMaxHP();
    //     double hpPercent = maxHP > 0 ? (currentHP / maxHP) * 100.0 : 100.0;
        
    //     // Lấy màu HP
    //     String hpColor = getHPColor(hpPercent);
        
    //     // Build format: prefix + PlayerName + ❤HP%: message
    //     StringBuilder format = new StringBuilder();
        
    //     if (!prefix.isEmpty()) {
    //         format.append(prefix).append(" ");
    //     }
        
    //     format.append("§f%1$s ");  // Player name
    //     // Format HP percent và escape % thành %% để tránh lỗi format specifier
    //     String hpPercentStr = String.format("%.0f", hpPercent) + "%%";
    //     format.append(hpColor).append("❤").append(hpPercentStr);  // HP
    //     format.append(": §f%2$s");  // Message
        
    //     return format.toString();
    // }

    public String buildChatMessage(Player player, String message) {
        if (player == null || !player.isOnline()) {
            return "§7" + player.getName() + ": §f" + message;
        }
    
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            return "§7" + player.getName() + ": §f" + message;
        }
    
        String prefix = buildChatPrefix(player);
    
        double hpPercent = profile.getMaxHP() > 0
                ? (profile.getCurrentHP() / profile.getMaxHP()) * 100.0
                : 100.0;
    
        String hpColor = getHPColor(hpPercent);
    
        StringBuilder sb = new StringBuilder();
    
        if (!prefix.isEmpty()) {
            sb.append(prefix).append(" ");
        }
    
        sb.append("§f").append(player.getName()).append(" ");
        sb.append(hpColor).append("❤").append((int) hpPercent).append("%");
        sb.append(CHAT_SEPARATOR).append(message);

    
        return sb.toString();
    }
    
    
    /**
     * Invalidate cache cho player (force rebuild khi state change)
     */
    public void invalidateCache(UUID playerUuid) {
        NameplateData data = cache.get(playerUuid);
        if (data != null) {
            data.invalidate();
        }
    }
    
    /**
     * Update tất cả nameplate (dùng cho join/reload)
     */
    public void updateAllNameplates() {
        Collection<PlayerProfile> online = playerManager.getAllOnline();
        batchUpdate(online);
    }
    
    /**
     * Remove nameplate khi quit
     */
    public void removeNameplate(Player player) {
        UUID uuid = player.getUniqueId();
        cache.remove(uuid);
        lastHPUpdateTime.remove(uuid);
        
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        if (team != null) {
            team.unregister();
        }
        
        // Xóa khỏi scoreboard của người khác
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            
            Scoreboard otherBoard = other.getScoreboard();
            Team otherTeam = otherBoard.getTeam(player.getName());
            if (otherTeam != null) {
                otherTeam.unregister();
            }
        }
    }
    
    // ===== PRIVATE HELPERS =====
    
    /**
     * 🔥 Rebuild static prefix/suffix (chỉ khi state change)
     * Cache này không đổi cho đến khi có event
     */
    private NameplateData rebuildStaticData(Player player, PlayerProfile profile) {
        UUID uuid = player.getUniqueId();
        NameplateData data = new NameplateData(uuid);
        
        StringBuilder prefix = new StringBuilder();
        
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
        
        // 3. Cảnh giới (không có HP ở đây)
        if (showRealm) {
            CultivationRealm realm = profile.getRealm();
            int level = profile.getRealmLevel();
            prefix.append(getRealmTag(realm, level)).append(" ");
        }
        
        // Giới hạn độ dài
        String staticPrefix = prefix.toString();
        if (staticPrefix.length() > 50) {
            staticPrefix = staticPrefix.substring(0, 50);
        }
        data.setStaticPrefix(staticPrefix);
        
        // 4. Suffix (danh hiệu)
        String suffix = buildSuffix(profile);
        data.setStaticSuffix(suffix);
        
        return data;
    }
    
    private String buildSuffix(PlayerProfile profile) {
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
    
    private String getRealmTag(CultivationRealm realm, int level) {
        // Hiển thị đầy đủ tên cảnh giới (không viết tắt)
        String displayName = realm.getDisplayName();
        String color = realm.getColor();
        
        // CHỈ hiển thị tier (Hạ/Trung/Thượng/Đỉnh), KHÔNG hiển thị số level
        // Người khác không thể nhìn ra level cụ thể
        String tierName = getTierName(level);
        
        return color + "[" + displayName + " " + tierName + "]";
    }
    
    /**
     * Lấy tên tier từ level (Hạ/Trung/Thượng/Đỉnh)
     * Người khác chỉ thấy tier, không thấy số level cụ thể
     */
    private String getTierName(int level) {
        if (level <= 3) return "§7Hạ";
        if (level <= 6) return "§eTrung";
        if (level <= 9) return "§6Thượng";
        return "§cĐỉnh";
    }
    
    private void applyToScoreboard(Player player, String prefix, String suffix) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(player.getName());
        
        if (team == null) {
            team = scoreboard.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }
        
        team.setPrefix(prefix);
        team.setSuffix(suffix);
        
        // Đảm bảo player không bị set custom name
        if (player.getCustomName() != null && !player.getCustomName().equals(player.getName())) {
            player.setCustomName(null);
            player.setCustomNameVisible(false);
        }
        
        // Cho player khác nhìn thấy
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
    
    private void invalidateAllCache() {
        for (NameplateData data : cache.values()) {
            data.invalidate();
        }
    }
}
