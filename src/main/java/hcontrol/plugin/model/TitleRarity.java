package hcontrol.plugin.model;

/**
 * DO HIEM DANH HIEU
 */
public enum TitleRarity {
    COMMON("Thuong", "§7"),
    RARE("Hiem", "§a"),
    EPIC("Sieu Hiem", "§5"),
    LEGENDARY("Truyen Thuyet", "§6"),
    MYTHIC("Than Thoai", "§c§l"),
    EVENT("Su Kien", "§e"),
    SPECIAL("Dac Biet", "§b"),
    ADMIN("Quan Tri", "§4§l");
    
    private final String displayName;
    private final String color;
    
    TitleRarity(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}
