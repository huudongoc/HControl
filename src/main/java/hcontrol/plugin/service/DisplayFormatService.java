package hcontrol.plugin.service;

import org.bukkit.entity.EntityType;

import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;    

/**
 * DISPLAY FORMAT SERVICE
 * Xu ly logic format hien thi (HP color, tier names, nameplate format...)
 * KHONG chua logic tinh toan (chi chua logic format/display)
 */
public class DisplayFormatService {
    
    private static DisplayFormatService instance;
    
    private DisplayFormatService() {
        // Private constructor de khong the new tu ben ngoai
    }
    
    public static DisplayFormatService getInstance() {
        if (instance == null) {
            instance = new DisplayFormatService();
        }
        return instance;
    }
    /**
     * Lay mau sac HP theo phan tram
     * @param hpPercent phan tram HP (0-100)
     * @return color code (§a, §e, §6, §c)
     */
    public String getHPColor(double hpPercent) {
        if (hpPercent >= 75) {
            return "§a";  // xanh la - khoe manh
        } else if (hpPercent >= 50) {
            return "§e";  // vang - binh thuong
        } else if (hpPercent >= 25) {
            return "§6";  // cam - nguy hiem
        } else {
            return "§c";  // do - sap chet
        }
    }
    
    /**
     * Lay ten tier tu level (Ha/Trung/Thuong/Dinh)
     * @param level level hien tai
     * @return tier name co mau sac
     */
    public String getTierName(int level) {
        if (level <= 3) return "§7Hạ";
        if (level <= 6) return "§eTrung";
        if (level <= 9) return "§6Thượng";
        return "§cĐỉnh";
    }
    
    /**
     * Format nameplate cho player
     * Format: [Title Icon] [Realm Tier] ❤ HP%
     * KHÔNG có text "Player❤" để phân biệt rõ với entity nameplate
     */
    public String formatPlayerNameplate(PlayerProfile profile, String titleIcon) {
        CultivationRealm realm = profile.getRealm();
        int level = profile.getLevel();
        
        // Tinh % HP
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getStats().getMaxHP();
        double hpPercent = (currentHP / maxHP) * 100.0;
        
        // Mau sac HP
        String hpColor = getHPColor(hpPercent);
        
        // Tier name
        String tierName = getTierName(level);
        
        // Danh hieu (neu co)
        String titleDisplay = (titleIcon != null && !titleIcon.isEmpty()) ? titleIcon + " " : "";
        
        // Format: [Title Icon] [Realm Tier] ❤ HP% (CHỈ HIỆN %)
        return titleDisplay + 
               realm.getColor() + "[" + realm.getDisplayName() + " " + tierName + "] " +
               hpColor + "❤ " + String.format("%.0f%%", hpPercent) + " §f";
    }
    
    /**
     * Format nameplate cho entity (boss/elite/mob)
     * Format: [BOSS/Elite] [Realm] Name ❤ currentHP/maxHP
     */
    public String formatEntityNameplate(EntityProfile profile, String displayName) {
        // Safety check: KHONG format nameplate cho Player
        if (profile.getEntityType() == EntityType.PLAYER) {
            return "";
        }
        
        // Boss/Elite prefix
        //neu profile la player thi khong hien thi prefix
  
        String prefix = "";
        if (profile.isBoss()) {
            prefix = "§4§l[BOSS] ";
        } else if (profile.isElite()) {
            prefix = "§6§l[Tinh Anh] ";
        }
        
        // Tinh % HP
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getMaxHP();
        double hpPercent = (currentHP / maxHP) * 100.0;
        
        // Mau sac HP
        String hpColor = getHPColor(hpPercent);
        
        // Realm color
        CultivationRealm realm = profile.getRealm();
        String realmColor = realm.getColor();
        String realmName = realm.getDisplayName();
        
        // Format: [Prefix] [Realm] Name ❤ currentHP/maxHP
        return prefix + 
               realmColor + "[" + realmName + "] §f" + 
               displayName + " " +
               hpColor + "❤ MODS " + String.format("%.0f", currentHP) + "/" + String.format("%.0f", maxHP) + "%";
    }
    
    /**
     * Format text hien thi realm + tier (khong hien thi level so)
     * Vi du: "§6Luyen Khi §eTrung"
     */
    public String formatRealmTier(CultivationRealm realm, int level) {
        String tierName = getTierName(level);
        return realm.getColor() + realm.getDisplayName() + " " + tierName;
    }
    
    /**
     * Format HP text (vi du: "§c❤ §f120§7/§c200")
     */
    public String formatHP(double currentHP, double maxHP) {
        return "§c❤ §f" + String.format("%.0f", currentHP) + "§7/§c" + String.format("%.0f", maxHP);
    }
    
    /**
     * Format Linh Khi text (vi du: "§9✦ §f80§7/§9100")
     */
    public String formatLingQi(double currentLQ, double maxLQ) {
        return "§9✦ §f" + String.format("%.0f", currentLQ) + "§7/§9" + String.format("%.0f", maxLQ);
    }
    
    /**
     * Format cultivation progress text (vi du: "§fTu vi: §a45.2%")
     */
    public String formatCultivationProgress(double percent) {
        return "§fTu vi: §a" + String.format("%.1f%%", percent);
    }
}
