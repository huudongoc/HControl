package hcontrol.plugin.ui.chat;

import hcontrol.plugin.master.MasterManager;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.sect.Sect;
import hcontrol.plugin.sect.SectManager;
import hcontrol.plugin.sect.SectMember;
import hcontrol.plugin.sect.SectRank;
import hcontrol.plugin.ui.player.NameplateService;
import org.bukkit.entity.Player;

/**
 * CHAT FORMAT SERVICE
 * Format chat message: [Thanh Vân][Sư phụ] Player: nội dung
 * 🔥 PHASE 5: Reuse NameplateData từ NameplateService
 * 📌 Không tự build prefix - chỉ format từ data có sẵn
 */
public class ChatFormatService {
    
    // Inject NameplateService để reuse data
    private NameplateService nameplateService;
    
    // Optional dependencies cho bubble format (sect rank, admin status)
    private SectManager sectManager;
    private MasterManager masterManager;
    
    public ChatFormatService() {
        // Dependencies sẽ được set từ CoreContext
    }
    
    public void setNameplateService(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    public void setSectManager(SectManager sectManager) {
        this.sectManager = sectManager;
    }
    
    public void setMasterManager(MasterManager masterManager) {
        this.masterManager = masterManager;
    }
    
    /**
     * Format chat message: [CảnhGiới Level][MônPhái][DanhHiệu] PlayerName ❤HP%: message
     * 🔥 Reuse NameplateData từ NameplateService
     * 
     * 📌 LƯU Ý: Bukkit AsyncPlayerChatEvent.setFormat() cần placeholder:
     * - %1$s = player name
     * - %2$s = message
     * 
     * @param profile Player profile (co the null)
     * @param playerName Ten player
     * @param message Tin nhan chat
     * @return Formatted message với placeholder
     */
    // public String formatChatMessage(PlayerProfile profile, String playerName, String message) {
    //     // 🔥 Reuse NameplateData từ NameplateService
    //     if (profile != null) {
    //         Player player = profile.getPlayer();
    //         if (player != null && player.isOnline()) {
    //             if (nameplateService != null) {
    //                 // Lấy format đầy đủ từ NameplateService (realm, sect, title, HP)
    //                 return nameplateService.buildChatMessage(player, message);
    //             }
    //         }
    //     }
        
    //     // Fallback: nếu không có profile hoặc nameplateService null
    //     return "§7%1$s: §f%2$s";
    // }

    public String formatChatMessage(PlayerProfile profile, String playerName, String message) {

    // 🔥 Reuse NameplateData từ NameplateService
    if (profile != null) {
        Player player = profile.getPlayer();
        if (player != null && player.isOnline() && nameplateService != null) {
            return nameplateService.buildChatMessage(player, message);
        }
    }

    // ✅ Fallback an toàn – KHÔNG format string
    return "§7" + playerName + "§7: §f" + message;
}

    
    /**
     * Format bubble text đẹp cho chat bubble
     * Hiển thị: [Title Icon] [Sect Rank] [Admin/VIP] message
     * 🔥 Format đẹp với rank màu sắc và admin status
     */
    public String formatBubbleText(PlayerProfile profile, String message) {
        if (profile == null) {
            return "§f" + message;
        }
        
        Player player = profile.getPlayer();
        if (player == null || !player.isOnline()) {
            return "§f" + message;
        }
        
        StringBuilder prefix = new StringBuilder();
        
        // 1. Title icon (ưu tiên cao nhất)
        Title title = profile.getActiveTitle();
        if (title != null && title != Title.NONE) {
            prefix.append(title.getIcon()).append(" ");
        }
        
        // 2. Sect rank với màu đẹp (nếu có)
        if (sectManager != null) {
            Sect sect = sectManager.getPlayerSect(player.getUniqueId());
            if (sect != null) {
                SectMember member = sect.getMember(player.getUniqueId());
                if (member != null) {
                    SectRank rank = member.getRank();
                    String rankColor = getSectRankColor(rank);
                    
                    // Rút gọn tên môn phái nếu dài
                    String sectName = sect.getName();
                    if (sectName.length() > 6) {
                        sectName = sectName.substring(0, 5) + "..";
                    }
                    
                    prefix.append(rankColor).append("[").append(sectName).append("] ");
                }
            }
        }
        
        // 3. Admin/VIP status (nếu có)
        if (player.isOp() || player.hasPermission("hcontrol.admin")) {
            // Check title có phải ADMIN/DEVELOPER không
            if (title == Title.ADMIN || title == Title.DEVELOPER) {
                prefix.append(title.getIcon()).append(" ");
            } else {
                // Nếu không có title admin nhưng là op → hiển thị admin icon
                prefix.append("§c§l✦ ");
            }
        } else if (title == Title.VIP) {
            prefix.append(title.getIcon()).append(" ");
        }
        
        // 4. Realm color (fallback nếu không có gì)
        if (prefix.length() == 0) {
            CultivationRealm realm = profile.getRealm();
            if (realm != null) {
                prefix.append(realm.getColor()).append(" ");
            }
        }
        
        return prefix.toString() + "§f" + message;
    }
    
    /**
     * Lấy màu theo sect rank (giống NameplateService.buildSectTag)
     */
    private String getSectRankColor(SectRank rank) {
        if (rank == null) return "§7";
        
        return switch (rank) {
            case LEADER -> "§6§l";        // Vàng đậm - Chưởng Môn
            case VICE_LEADER -> "§6";     // Vàng - Phó Môn
            case ELDER -> "§e";           // Vàng nhạt - Trưởng Lão
            case CORE_DISCIPLE -> "§a";   // Xanh lá - Chân Truyền
            case INNER_DISCIPLE -> "§2";  // Xanh đậm - Nội Môn
            case OUTER_DISCIPLE -> "§7";  // Xám - Ngoại Môn
        };
    }
}