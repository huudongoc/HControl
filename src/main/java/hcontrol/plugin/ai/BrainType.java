package hcontrol.plugin.ai;

/**
 * PHASE 7 — BRAIN TYPE
 * Cac loai brain cho mob
 */
public enum BrainType {
    /**
     * Passive - Khong tan cong, chi chay tron
     * VD: Pig, Cow, Chicken
     */
    PASSIVE,
    
    /**
     * Aggressive - Tan cong ngay khi thay player trong range
     * VD: Zombie, Skeleton, Spider
     */
    AGGRESSIVE,
    
    /**
     * Guard - Chi tan cong khi bi danh hoac player qua gan
     * VD: Iron Golem, Villager Guard
     */
    GUARD,
    
    /**
     * Elite - Thong minh hon, dung skill, retreat khi low HP
     * VD: Elite mob, Mini-boss
     */
    ELITE,
    
    /**
     * Boss - Rat thong minh, nhieu phase, skill phuc tap
     * VD: World boss, Dungeon boss
     */
    BOSS,
    
    /**
     * Neutral - Khong tan cong tru khi bi khieu khich
     * VD: Enderman, Wolf
     */
    NEUTRAL
}
