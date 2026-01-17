package hcontrol.plugin.ui.chat;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerProfile;
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
    
    public ChatFormatService() {
        // Dependencies sẽ được set từ CoreContext
    }
    
    public void setNameplateService(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    /**
     * Format chat message: [Thanh Vân][Sư phụ] Player: nội dung
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
    public String formatChatMessage(PlayerProfile profile, String playerName, String message) {
        // 🔥 Reuse NameplateData từ NameplateService
        String chatPrefix = "";

        if (profile != null) {
            Player player = profile.getPlayer();
            if (player != null && player.isOnline()) {
                if (nameplateService != null) {
                    // Lấy static prefix từ NameplateService (real-time data)
                    chatPrefix = nameplateService.buildChatPrefix(player);
                } else {
                    // Fallback: nếu nameplateService null, format đơn giản
                    return "§7%1$s: §f%2$s";
                }
            }
        }

        // Format: [Thanh Vân][Sư phụ] %1$s: %2$s
        // %1$s = player name, %2$s = message (Bukkit sẽ tự thay thế)
        StringBuilder formatted = new StringBuilder();

        if (!chatPrefix.isEmpty()) {
            formatted.append(chatPrefix).append(" ");
        }

        formatted.append("§f%1$s: §f%2$s");
        
        return formatted.toString();
    }
    
    /**
     * Format bubble text don gian cho chat bubble (khong co khung phuc tap)
     * Hien thi title icon neu co, neu khong hien thi realm
     */
    public String formatBubbleText(PlayerProfile profile, String message) {
        if (profile != null) {
            // Uu tien title icon
            if (profile.getActiveTitle() != null) {
                Title title = profile.getActiveTitle();
                if (title != Title.NONE) {
                    return title.getIcon() + " §f" + message;
                }
            }
            
            // // Neu khong co title, hien thi realm (don gian)
            // CultivationRealm realm = profile.getRealm();
            // if (realm != null) {
            //     return realm.getColor() + "[" + realm.getDisplayName() + "] §f" + message;
            // }

            // Neu khong co title, hien thi realm (don gian)
            CultivationRealm realm = profile.getRealm();
            if (realm != null) {
                return realm.getColor()  + " §f" + message;
            }
        }
        return "§f" + message;
    }
}