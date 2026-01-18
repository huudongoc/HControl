package hcontrol.plugin.ui.player;

import java.util.UUID;

/**
 * NAMEPLATE DATA - Cache cho nameplate
 * 
 * Chứa phần static (không đổi): realm, sect, title, master
 * HP render riêng vì thay đổi liên tục
 */
public class NameplateData {
    
    private final UUID playerUuid;
    private String staticPrefix;  // [MônPhái] [Sư/Đồ] [CảnhGiới]
    private String staticSuffix;  // [DanhHiệu]
    
    // Version để check xem có cần rebuild không
    private int version;
    
    public NameplateData(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.staticPrefix = "";
        this.staticSuffix = "";
        this.version = 0;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public String getStaticPrefix() {
        return staticPrefix;
    }
    
    public void setStaticPrefix(String staticPrefix) {
        this.staticPrefix = staticPrefix;
        this.version++;
    }
    
    public String getStaticSuffix() {
        return staticSuffix;
    }
    
    public void setStaticSuffix(String staticSuffix) {
        this.staticSuffix = staticSuffix;
        this.version++;
    }
    
    /**
     * Invalidate cache - force rebuild
     */
    public void invalidate() {
        this.version++;
    }
    
    public int getVersion() {
        return version;
    }
    
    /**
     * Build full prefix với HP: staticPrefix + " ❤ " + hpPercent
     */
    public String buildFullPrefix(double hpPercent) {
        String hpColor = getHPColor(hpPercent);
        return staticPrefix + " " + hpColor + "❤ " + String.format("%.0f%%", hpPercent) + " §f";
    }
    
    /**
     * Lấy màu HP theo phần trăm
     */
    private String getHPColor(double hpPercent) {
        if (hpPercent >= 75) {
            return "§a";  // xanh lá - khỏe mạnh
        } else if (hpPercent >= 50) {
            return "§e";  // vàng - bình thường
        } else if (hpPercent >= 25) {
            return "§6";  // cam - nguy hiểm
        } else {
            return "§c";  // đỏ - sắp chết
        }
    }
}
