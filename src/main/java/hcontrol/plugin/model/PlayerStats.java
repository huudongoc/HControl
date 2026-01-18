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
        statContainer.setBase(StatType.CAN_COT, 2);
        statContainer.setBase(StatType.LINH_LUC, 2);
        statContainer.setBase(StatType.THE_PHACH, 2);
        statContainer.setBase(StatType.NGO_TINH, 2);
        statContainer.setBase(StatType.KHI_VAN, 2);
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
        return statContainer.getTotal(StatType.CAN_COT);
    }
    
    public int getSpirit() {
        return statContainer.getTotal(StatType.LINH_LUC);
    }
    
    public int getPhysique() {
        return statContainer.getTotal(StatType.THE_PHACH);
    }
    
    public int getComprehension() {
        return statContainer.getTotal(StatType.NGO_TINH);
    }
    
    public int getFortune() {
        return statContainer.getTotal(StatType.KHI_VAN);
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
    
    /**
     * Get baseHP tu stat, level, body... (KHONG co realm multiplier)
     */
    private int getBaseHP() {
        int phy = getPhysique();  // The Phach
        return phy * 15 + level * 10;  // tu tien: the phach -> sinh mang
    }
    
    /**
     * Get maxHP with realm multiplier applied
     * Realm multiplier: realm cao hon -> baseHP cao hon tu nhien
     * 
     * 📌 Luật khóa: PlayerStats KHÔNG lưu realm, realm luôn được truyền từ ngoài vào
     * 
     * ⚠️ TUYỆT ĐỐI không được để realm == null mà apply multiplier
     * Nếu realm == null → chỉ trả về baseHP (không có multiplier)
     */
    public int getMaxHP(CultivationRealm realm) {
        double baseHP = getBaseHP();  // HP tu stat, level, body...
        if (realm == null) {
            return (int)baseHP;  // Không apply multiplier nếu realm == null
        }
        return (int)(baseHP * realm.getStatMultiplier());
    }
    
    /**
     * Backward compatibility - dùng PHAMNHAN làm default
     */
    public int getMaxHP() {
        return getMaxHP(CultivationRealm.PHAMNHAN);
    }
    
    public int getMaxLingQi() {  // Linh Khi (thay Mana)
        int spr = getSpirit();  // Linh Luc
        return spr * 12 + level * 5;  // tu tien: linh luc -> linh khi
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
            "§7[§eLv%d§7] CC:§a%d LL:§a%d TP:§a%d NT:§a%d KV:§a%d",
            level, getRoot(), getSpirit(), getPhysique(), getComprehension(), getFortune()
        );
    }
    
    public String toDetailString() {
        return String.format(
            """
            §7§m--------------------
            §e§lTHUỘC TÍNH TU TIÊN §7(Lv%d)
            §7Cơ bản:
              §fCC (Căn Cốt): §a%d §7| §fLL (Linh Lực): §a%d
              §fTP (Thể Phách): §a%d §7| §fNT (Ngộ Tính): §a%d
              §fKV (Khí Vận): §a%d
            §7Chiến đấu:
              §fSM (Sinh Mạng): §c%d §7| §fLK (Linh Khí): §9%d
              §fPN (Phòng Ngự): §e%.1f
            §7§m--------------------
            """,
            level,
            getRoot(), getSpirit(), getPhysique(), getComprehension(), getFortune(),
            getMaxHP(), getMaxLingQi(),
            getDefense()
        );
    }
}
