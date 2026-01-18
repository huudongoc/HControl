package hcontrol.plugin.classsystem;

/**
 * PHASE 5 — CLASS PROFILE
 * Class information cho player
 */
public class ClassProfile {
    
    private final ClassType type;
    private int masteryLevel;
    
    public ClassProfile(ClassType type) {
        this.type = type;
        this.masteryLevel = 1;
    }
    
    public ClassType getType() {
        return type;
    }
    
    public int getMasteryLevel() {
        return masteryLevel;
    }
    
    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = Math.max(1, masteryLevel);
    }
    
    public void increaseMastery() {
        masteryLevel++;
    }
}
