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
     * @param profile Player profile (co the null)
     * @param playerName Ten player
     * @param message Tin nhan chat
     * @return Formatted message
     */
    public String formatChatMessage(PlayerProfile profile, String playerName, String message) {
        if (profile == null) {
            return "§7" + playerName + ": §f" + message;
        }
        
        Player player = profile.getPlayer();
        if (player == null || !player.isOnline()) {
            return "§7" + playerName + ": §f" + message;
        }
        
        // 🔥 Reuse NameplateData từ NameplateService
        // Extract sect name và master status từ static prefix
        String chatPrefix = "";
        
        if (nameplateService != null) {
            // Lấy static prefix từ NameplateService (đã có cache)
            // Format static prefix: [MônPhái] [Sư/Đồ] [CảnhGiới]
            // Cần extract: [MônPhái] và [Sư/Đồ] để format chat
            chatPrefix = nameplateService.buildChatPrefix(player);
        }
        
        // Format: [Thanh Vân][Sư phụ] Player: nội dung
        StringBuilder formatted = new StringBuilder();
        
        if (!chatPrefix.isEmpty()) {
            formatted.append(chatPrefix).append(" ");
        }
        
        formatted.append("§f").append(playerName).append(": §f").append(message);
        
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