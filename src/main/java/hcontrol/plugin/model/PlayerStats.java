package hcontrol.plugin.model;

public class PlayerStats {
    private int strength;      // Sức mạnh
    private int agility;       // Nhanh nhẹn
    private int intelligence;  // Trí tuệ
    private int vitality;      // Sinh lực
    private int luck;          // May mắn
    
    public PlayerStats(int strength, int agility, int intelligence, int vitality, int luck) {
        this.strength = strength;
        this.agility = agility;
        this.intelligence = intelligence;
        this.vitality = vitality;
        this.luck = luck;
    }
    
    // Stats mặc định cho người chưa chọn class (1-3)
    public static PlayerStats getDefaultStats() {
        return new PlayerStats(2, 2, 2, 2, 2);
    }
    
    // Getters
    public int getStrength() { return strength; }
    public int getAgility() { return agility; }
    public int getIntelligence() { return intelligence; }
    public int getVitality() { return vitality; }
    public int getLuck() { return luck; }
    
    // Setters
    public void setStrength(int strength) { this.strength = strength; }
    public void setAgility(int agility) { this.agility = agility; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }
    public void setVitality(int vitality) { this.vitality = vitality; }
    public void setLuck(int luck) { this.luck = luck; }
    
    @Override
    public String toString() {
        return String.format("STR: %d | AGI: %d | INT: %d | VIT: %d | LCK: %d", 
            strength, agility, intelligence, vitality, luck);
    }
}
