package hcontrol.plugin.player;

import hcontrol.plugin.model.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerProfile {

    private final UUID uuid;
    private int level;
    private long exp;
    private int statPoints;
    
    // LINK STATS
    private final PlayerStats stats;
    
    // COMBAT STATE
    private double currentHP;
    private double currentMana;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.exp = 0L;
        this.statPoints = 0;
        
        // khoi tao stats
        this.stats = new PlayerStats();
        this.stats.setLevel(this.level);
        
        // khoi tao HP/Mana = max
        this.currentHP = stats.getMaxHP();
        this.currentMana = stats.getMaxMana();
    }

    // === BASIC ===

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    // === LEVEL & EXP ===

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.stats.setLevel(level); // sync level vao stats
    }

    public void addLevel(int amount) {
        setLevel(this.level + amount);
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public void addExp(long amount) {
        this.exp += amount;
    }

    public void removeExp(long amount) {
        this.exp = Math.max(0, this.exp - amount);
    }

    // === STAT POINTS ===

    public int getStatPoints() {
        return statPoints;
    }

    public void setStatPoints(int statPoints) {
        this.statPoints = statPoints;
    }

    public void addStatPoints(int amount) {
        this.statPoints += amount;
    }

    public void removeStatPoints(int amount) {
        this.statPoints = Math.max(0, this.statPoints - amount);
    }
    
    // === STATS ===
    
    public PlayerStats getStats() {
        return stats;
    }
    
    // === HP / MANA ===
    
    public double getCurrentHP() {
        return currentHP;
    }
    
    public void setCurrentHP(double hp) {
        this.currentHP = Math.max(0, Math.min(hp, stats.getMaxHP()));
    }
    
    public void addHP(double amount) {
        setCurrentHP(currentHP + amount);
    }
    
    public void removeHP(double amount) {
        setCurrentHP(currentHP - amount);
    }
    
    public boolean isDead() {
        return currentHP <= 0;
    }
    
    public double getCurrentMana() {
        return currentMana;
    }
    
    public void setCurrentMana(double mana) {
        this.currentMana = Math.max(0, Math.min(mana, stats.getMaxMana()));
    }
    
    public void addMana(double amount) {
        setCurrentMana(currentMana + amount);
    }
    
    public void removeMana(double amount) {
        setCurrentMana(currentMana - amount);
    }
    
    public boolean hasEnoughMana(double cost) {
        return currentMana >= cost;
    }
}
