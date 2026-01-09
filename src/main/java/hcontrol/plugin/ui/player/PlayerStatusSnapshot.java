package hcontrol.plugin.ui.player;

public class PlayerStatusSnapshot {

    private final int level;

    private final double currentHp;
    private final double maxHp;

    private final double currentMana;
    private final double maxMana;

    public PlayerStatusSnapshot(
            int level,
            double currentHp,
            double maxHp,
            double currentMana,
            double maxMana
    ) {
        this.level = level;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.currentMana = currentMana;
        this.maxMana = maxMana;
    }

    public int getLevel() {
        return level;
    }

    public double getCurrentHp() {
        return currentHp;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public double getCurrentMana() {
        return currentMana;
    }

    public double getMaxMana() {
        return maxMana;
    }
}
