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
        // stat mac dinh tu tien (Pham Nhan)
        statContainer.setBase(StatType.ROOT, 2);
        statContainer.setBase(StatType.SPIRIT, 2);
        statContainer.setBase(StatType.PHYSIQUE, 2);
        statContainer.setBase(StatType.COMPREHENSION, 2);
        statContainer.setBase(StatType.FORTUNE, 2);
    }
    
    // === LEVEL ===
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    // === PRIMARY STAT (TU TIEN STATS) ===
    
    public int getRoot() {
        return statContainer.getTotal(StatType.ROOT);
    }
    
    public int getSpirit() {
        return statContainer.getTotal(StatType.SPIRIT);
    }
    
    public int getPhysique() {
        return statContainer.getTotal(StatType.PHYSIQUE);
    }
    
    public int getComprehension() {
        return statContainer.getTotal(StatType.COMPREHENSION);
    }
    
    public int getFortune() {
        return statContainer.getTotal(StatType.FORTUNE);
    }
    
    // backward compatible (old names)
    public int getStrength() { return getRoot(); }
    public int getIntelligence() { return getSpirit(); }
    public int getVitality() { return getPhysique(); }
    public int getAgility() { return getComprehension(); }
    public int getLuck() { return getFortune(); }
    
    // them stat point (tieu diem khi level up)
    public void addPrimaryStat(StatType type, int amount) {
        if (!type.isPrimary()) {
            throw new IllegalArgumentException("Chi them duoc primary stat");
        }
        statContainer.addBase(type, amount);
    }
    
    // === DERIVED STAT (TU TIEN FORMULAS) ===
    
    public int getMaxHP() {
        int phy = getPhysique();  // The Phach
        return phy * 15 + level * 10;  // tang them HP cho tu tien
    }
    
    public int getMaxLingQi() {  // Linh Khi thay Mana
        int spr = getSpirit();  // Linh Luc
        return spr * 12 + level * 5;
    }
    
    public int getMaxMana() {  // backward compatible
        return getMaxLingQi();
    }
    
    // LOAI BO getAttack / getMagicAttack
    // Tu tien damage DEN TU REALM, KHONG TU STAT
    // Stat chi anh huong HP / Linh Khi / Defense
    
    public double getDefense() {  // Phong Thu (chi dung trong DamageFormula)
        int phy = getPhysique();  // The Phach
        return phy * 2.0;  // chi dung cho formula tu tien, khong dung RPG
    }
    
    // LOAI BO getCritRate / getDodge
    // Tu tien KHONG CO crit/dodge kieu RPG
    // Chi co Dao Factor (random 0.9-1.1) trong DamageFormula
    
    // === STAT CONTAINER ===
    
    public StatContainer getStatContainer() {
        return statContainer;
    }
    
    // === DEBUG ===
    
    @Override
    public String toString() {
        return String.format(
            "§7[§eLv%d§7] §fCan Cot:§a%d §fLinh Luc:§a%d §fThe Phach:§a%d §fNgo Tinh:§a%d §fKhi Van:§a%d",
            level, getRoot(), getSpirit(), getPhysique(), getComprehension(), getFortune()
        );
    }
    
    public String toDetailString() {
        return String.format(
            """
            §7§m--------------------
            §e§lTHUỘC TÍNH TU TIÊN §7(Lv%d)
            §7Cơ bản:
              §fCăn Cốt: §a%d §7| §fLinh Lực: §a%d
              §fThể Phách: §a%d §7| §fNgộ Tính: §a%d
              §fKhí Vận: §a%d
            §7Chiến đấu:
              §fSinh Mạng: §c%d §7| §fLinh Khí: §9%d
              §fPhòng Ngự: §e%.1f
            §7§m--------------------
            """,
            level,
            getRoot(), getSpirit(), getPhysique(), getComprehension(), getFortune(),
            getMaxHP(), getMaxLingQi(),
            getDefense()
        );
    }
}
