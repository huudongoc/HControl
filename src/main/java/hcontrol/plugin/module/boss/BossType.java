package hcontrol.plugin.module.boss;

/**
 * BOSS TYPE
 * Loai boss (color code, tier)
 */
public enum BossType {
    FIELD_BOSS("§e", "Dia Boss", 1),         // boss ngoai dong
    DUNGEON_BOSS("§6", "Pho Ban Boss", 2),   // boss trong dungeon
    WORLD_BOSS("§c", "The Gioi Boss", 3),    // world boss event
    RAID_BOSS("§4§l", "Raid Boss", 4);       // end game raid
    
    private final String color;
    private final String displayName;
    private final int tier;
    
    BossType(String color, String displayName, int tier) {
        this.color = color;
        this.displayName = displayName;
        this.tier = tier;
    }
    
    public String getColor() { return color; }
    public String getDisplayName() { return displayName; }
    public int getTier() { return tier; }
}
