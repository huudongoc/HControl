package hcontrol.plugin.stats;

public enum StatType {
    // Primary stats (base)
    STRENGTH("STR", "Suc manh"),
    AGILITY("AGI", "Nhanh nhen"),
    INTELLIGENCE("INT", "Tri tue"),
    VITALITY("VIT", "Sinh luc"),
    LUCK("LCK", "May man"),
    
    // Derived stats (tinh tu primary)
    MAX_HP("HP", "Mau toi da"),
    MAX_MANA("MANA", "Mana toi da"),
    ATTACK("ATK", "Sat thuong vat ly"),
    MAGIC_ATTACK("MATK", "Sat thuong phep"),
    DEFENSE("DEF", "Phong thu"),
    CRIT_RATE("CRIT", "Ti le chi mang"),
    DODGE("DODGE", "Ne tranh");
    
    private final String shortName;
    private final String displayName;
    
    StatType(String shortName, String displayName) {
        this.shortName = shortName;
        this.displayName = displayName;
    }
    
    public String getShortName() {
        return shortName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isPrimary() {
        return this.ordinal() < 5; // 5 stat dau la primary
    }
    
    public boolean isDerived() {
        return !isPrimary();
    }
}