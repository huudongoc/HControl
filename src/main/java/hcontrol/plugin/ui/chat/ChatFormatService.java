package hcontrol.plugin.ui.chat;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.Title;
import hcontrol.plugin.model.TitleRarity;
import hcontrol.plugin.player.PlayerProfile;

/**
 * CHAT FORMAT SERVICE
 * Format chat message voi khung mau dep dua tren title/role cua player
 * Neu khong co title, format theo canh gioi (don gian hon)
 */
public class ChatFormatService {
    
    /**
     * Format chat message voi khung mau dep
     * @param profile Player profile (co the null)
     * @param playerName Ten player
     * @param message Tin nhan chat
     * @return Formatted message voi khung mau
     */
    public String formatChatMessage(PlayerProfile profile, String playerName, String message) {
        TitleRarity rarity = TitleRarity.COMMON;
        String titleIcon = "";
        String titleName = "";
        CultivationRealm realm = null;
        
        // Lay realm neu co profile
        if (profile != null) {
            realm = profile.getRealm();
            
            // Lay title neu co
            if (profile.getActiveTitle() != null) {
                Title title = profile.getActiveTitle();
                if (title != Title.NONE) {
                    rarity = title.getRarity();
                    titleIcon = title.getIcon();
                    titleName = title.getDisplayName();
                }
            }
        }
        
        // Neu co title (co titleName) -> dung format title (dep)
        // Neu khong co title -> dung format realm (don gian)
        if (!titleName.isEmpty()) {
            return buildChatFrame(rarity, titleIcon, titleName, playerName, message, realm);
        } else {
            // Khong co title -> format theo realm (don gian hon)
            return formatRealmChat(realm, playerName, message);
        }
    }
    
    /**
     * Xay dung khung chat dep theo rarity (format 1 dong cho chat box)
     */
    private String buildChatFrame(TitleRarity rarity, String titleIcon, String titleName, 
                                  String playerName, String message, CultivationRealm realm) {
        switch (rarity) {
            case ADMIN:
                return formatAdminChat(titleIcon, titleName, playerName, message, realm);
            case MYTHIC:
                return formatMythicChat(titleIcon, titleName, playerName, message, realm);
            case LEGENDARY:
                return formatLegendaryChat(titleIcon, titleName, playerName, message, realm);
            case EPIC:
                return formatEpicChat(titleIcon, titleName, playerName, message, realm);
            case RARE:
                return formatRareChat(titleIcon, titleName, playerName, message, realm);
            case SPECIAL:
                return formatSpecialChat(titleIcon, titleName, playerName, message, realm);
            case EVENT:
                return formatEventChat(titleIcon, titleName, playerName, message, realm);
            default: // COMMON
                return formatRealmChat(realm, playerName, message);
        }
    }
    
    /**
     * Format chat cho ADMIN - Khung do dam voi hieu ung
     */
    private String formatAdminChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§4§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§4§l║ §c§l" + titleIcon + "§c§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §4§l║";
    }
    
    /**
     * Format chat cho MYTHIC - Khung do voi vien dep
     */
    private String formatMythicChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§c§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§c§l╔═ §6§l" + titleIcon + "§6§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §c§l═╗";
    }
    
    /**
     * Format chat cho LEGENDARY - Khung vang/cam dep
     */
    private String formatLegendaryChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§6§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§6§l┏━ §e§l" + titleIcon + "§e§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §6§l━┓";
    }
    
    /**
     * Format chat cho EPIC - Khung tim
     */
    private String formatEpicChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§5§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§5§l╭─ §d§l" + titleIcon + "§d§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §5§l─╮";
    }
    
    /**
     * Format chat cho RARE - Khung xanh la
     */
    private String formatRareChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§a§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§a§l┌─ §2§l" + titleIcon + "§2§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §a§l─┐";
    }
    
    /**
     * Format chat cho SPECIAL - Khung xanh duong
     */
    private String formatSpecialChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§b§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§b§l┌─ §3§l" + titleIcon + "§3§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §b§l─┐";
    }
    
    /**
     * Format chat cho EVENT - Khung vang su kien
     */
    private String formatEventChat(String titleIcon, String titleName, String playerName, String message, CultivationRealm realm) {
        String titlePart = titleName.isEmpty() ? "" : " §7[§e§l" + titleName + "§7]";
        String realmPart = (realm != null && titleName.isEmpty()) ? " §8[" + realm.getColor() + realm.getDisplayName() + "§8]" : "";
        return "§e§l┌─ §6§l" + titleIcon + "§6§l" + playerName + titlePart + realmPart + " §7» §f" + message + " §e§l─┐";
    }
    
    /**
     * Format chat theo CANH GIOI (don gian, khong dep bang roles)
     * Dung khi khong co title hoac title la COMMON
     */
    private String formatRealmChat(CultivationRealm realm, String playerName, String message) {
        if (realm == null) {
            // Khong co realm -> format don gian nhat
            return "§8┌─ §7" + playerName + " §8─┐ §7» §f" + message;
        }
        
        String realmColor = realm.getColor();
        String realmName = realm.getDisplayName();
        
        // Format don gian voi canh gioi (khong co khung dep nhu roles)
        return "§8[§r" + realmColor + realmName + "§8] §7" + playerName + " §7» §f" + message;
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
            
            // Neu khong co title, hien thi realm (don gian)
            CultivationRealm realm = profile.getRealm();
            if (realm != null) {
                return realm.getColor() + "[" + realm.getDisplayName() + "] §f" + message;
            }
        }
        return "§f" + message;
    }
}