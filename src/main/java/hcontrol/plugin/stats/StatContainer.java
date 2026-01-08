package hcontrol.plugin.stats;

import java.util.EnumMap;
import java.util.Map;

public class StatContainer {
    
    private final Map<StatType, Integer> baseStats = new EnumMap<>(StatType.class);
    private final Map<StatType, Integer> bonusStats = new EnumMap<>(StatType.class);
    
    public StatContainer() {
        // init tat ca stat = 0
        for (StatType type : StatType.values()) {
            baseStats.put(type, 0);
            bonusStats.put(type, 0);
        }
    }
    
    // === BASE STAT ===
    
    public int getBase(StatType type) {
        return baseStats.getOrDefault(type, 0);
    }
    
    public void setBase(StatType type, int value) {
        baseStats.put(type, Math.max(0, value));
    }
    
    public void addBase(StatType type, int amount) {
        setBase(type, getBase(type) + amount);
    }
    
    // === BONUS STAT (tu item, buff) ===
    
    public int getBonus(StatType type) {
        return bonusStats.getOrDefault(type, 0);
    }
    
    public void setBonus(StatType type, int value) {
        bonusStats.put(type, Math.max(0, value));
    }
    
    public void addBonus(StatType type, int amount) {
        setBonus(type, getBonus(type) + amount);
    }
    
    public void clearBonus() {
        bonusStats.replaceAll((k, v) -> 0);
    }
    
    // === TOTAL STAT (base + bonus) ===
    
    public int getTotal(StatType type) {
        return getBase(type) + getBonus(type);
    }
    
    // === DEBUG ===
    
    // public String toDebugString() {
    //     StringBuilder sb = new StringBuilder();
    //     for (StatType type : StatType.values()) {
    //         if (type.isPrimary()) {
    //             int base = getBase(type);
    //             int bonus = getBonus(type);
    //             int total = getTotal(type);
    //             sb.append(String.format("%s: %d (+%d) = %d\n", 
    //                 type.getShortName(), base, bonus, total));
    //         }
    //     }
    //     return sb.toString();
    // }
}