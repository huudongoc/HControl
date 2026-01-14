package hcontrol.plugin.model;

/**
 * DANH HIEU - TITLE SYSTEM
 * Hien thi tren dau player (tren realm)
 */
public enum Title {
    // DANH HIEU MAC DINH
    NONE("", "", TitleRarity.COMMON),
    
    // DANH HIEU SU KIEN (Event Titles)
    TET_2026("Xuan Binh Ngo", "§e✦ ", TitleRarity.EVENT),
    HALLOWEEN("Ac Quy", "§6☠ ", TitleRarity.EVENT),
    CHRISTMAS("Thanh Nhan", "§f❄ ", TitleRarity.EVENT),
    
    // DANH HIEU THANH TICH (Achievement Titles)
    FIRST_BLOOD("Suyt Chet", "§c⚔ ", TitleRarity.RARE),
    MASS_KILLER("Sat Nhan", "§4☠ ", TitleRarity.EPIC),
    CHANTIEN("Bat Tu", "§6✦ ", TitleRarity.LEGENDARY),
    
    // DANH HIEU BANG HOI (Guild Titles)
    GUILD_LEADER("Bang Chu", "§6★ ", TitleRarity.SPECIAL),
    GUILD_OFFICER("Truong Lao", "§e◆ ", TitleRarity.SPECIAL),
    GUILD_ELITE("Tinh Anh", "§a● ", TitleRarity.SPECIAL),
    
    // DANH HIEU TU TIEN (Cultivation Titles)
    HEAVENLY_TALENT("Thien Tai", "§b✦ ", TitleRarity.LEGENDARY),
    DEMON_LORD("Ma Ton", "§5☠ ", TitleRarity.LEGENDARY),
    SWORD_SAINT("Kiem Thanh", "§f⚔ ", TitleRarity.LEGENDARY),
    
    // DANH HIEU DANG CAP (Rank Titles)
    TOP_1("Thien Ha De Nhat", "§6§l❶ ", TitleRarity.MYTHIC),
    TOP_10("Cao Thu", "§e◈ ", TitleRarity.EPIC),
    TOP_100("Danh Gia", "§a● ", TitleRarity.RARE),
    
    // DANH HIEU ĐỘC (Unique Titles)
    DEVELOPER("Khai Thien Chu", "§b§l✦ ", TitleRarity.ADMIN),
    ADMIN("Quan Tri", "§c§l✦ ", TitleRarity.ADMIN),
    VIP("Quy Toc", "§6♔ ", TitleRarity.SPECIAL);
    
    private final String displayName;
    private final String prefix;  // icon/symbol
    private final TitleRarity rarity;
    
    Title(String displayName, String prefix, TitleRarity rarity) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.rarity = rarity;
    }
    
    public String getDisplayName() { return displayName; }
    public String getPrefix() { return prefix; }
    public TitleRarity getRarity() { return rarity; }
    
    /**
     * Format day du: [icon] name
     */
    public String getFullDisplay() {
        if (this == NONE) return "";
        return prefix + rarity.getColor() + displayName + "§r";
    }
    
    /**
     * Chi icon (cho nameplate)
     */
    public String getIcon() {
        return prefix;
    }
    
    @Override
    public String toString() {
        return getFullDisplay();
    }
}
