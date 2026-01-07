package hcontrol.plugin.model;

import hcontrol.plugin.stats.StatContainer;
import hcontrol.plugin.stats.StatType;

public class PlayerStats {
    
    private final StatContainer statContainer;
    private int level; // can de tinh derived stat
    
    public PlayerStats() {
        this.statContainer = new StatContainer();
        this.level = 1;
        initDefaultStats();
    }
    
    // === INIT ===
    
    private void initDefaultStats() {
        // stat mac dinh (chua chon class)
        statContainer.setBase(StatType.STRENGTH, 2);
        statContainer.setBase(StatType.AGILITY, 2);
        statContainer.setBase(StatType.INTELLIGENCE, 2);
        statContainer.setBase(StatType.VITALITY, 2);
        statContainer.setBase(StatType.LUCK, 2);
    }
    
    // === LEVEL ===
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    // === PRIMARY STAT ===
    
    public int getStrength() {
        return statContainer.getTotal(StatType.STRENGTH);
    }
    
    public int getAgility() {
        return statContainer.getTotal(StatType.AGILITY);
    }
    
    public int getIntelligence() {
        return statContainer.getTotal(StatType.INTELLIGENCE);
    }
    
    public int getVitality() {
        return statContainer.getTotal(StatType.VITALITY);
    }
    
    public int getLuck() {
        return statContainer.getTotal(StatType.LUCK);
    }
    
    // them stat point (tieu diem khi level up)
    public void addPrimaryStat(StatType type, int amount) {
        if (!type.isPrimary()) {
            throw new IllegalArgumentException("Chi them duoc primary stat");
        }
        statContainer.addBase(type, amount);
    }
    
    // === DERIVED STAT (tinh tu primary) ===
    
    public int getMaxHP() {
        int vit = getVitality();
        return vit * 10 + level * 5;
    }
    
    public int getMaxMana() {
        int intel = getIntelligence();
        return intel * 8 + level * 3;
    }
    
    public double getAttack() {
        int str = getStrength();
        int agi = getAgility();
        return str * 2.0 + agi * 0.5;
    }
    
    public double getMagicAttack() {
        int intel = getIntelligence();
        return intel * 2.5;
    }
    
    public double getDefense() {
        int vit = getVitality();
        return vit * 1.5;
    }
    
    public double getCritRate() {
        int agi = getAgility();
        int lck = getLuck();
        return agi * 0.3 + lck * 0.5; // %
    }
    
    public double getDodge() {
        int agi = getAgility();
        return agi * 0.2; // %
    }
    
    // === STAT CONTAINER ===
    
    public StatContainer getStatContainer() {
        return statContainer;
    }
    
    // === DEBUG ===
    
    @Override
    public String toString() {
        return String.format(
            "§7[§eLv%d§7] §fSTR:§a%d §fAGI:§a%d §fINT:§a%d §fVIT:§a%d §fLCK:§a%d",
            level, getStrength(), getAgility(), getIntelligence(), getVitality(), getLuck()
        );
    }
    
    public String toDetailString() {
        return String.format(
            """
            §7§m--------------------
            §e§lSTAT DETAILS §7(Lv%d)
            §7Primary:
              §fSTR: §a%d §7| §fAGI: §a%d §7| §fINT: §a%d
              §fVIT: §a%d §7| §fLCK: §a%d
            §7Derived:
              §fHP: §c%d §7| §fMana: §9%d
              §fATK: §c%.1f §7| §fMATK: §9%.1f
              §fDEF: §e%.1f §7| §fCRIT: §6%.1f%% §7| §fDODGE: §b%.1f%%
            §7§m--------------------
            """,
            level,
            getStrength(), getAgility(), getIntelligence(), getVitality(), getLuck(),
            getMaxHP(), getMaxMana(),
            getAttack(), getMagicAttack(),
            getDefense(), getCritRate(), getDodge()
        );
    }
}
