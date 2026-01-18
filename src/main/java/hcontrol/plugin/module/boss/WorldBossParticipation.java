package hcontrol.plugin.module.boss;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * WORLD BOSS PARTICIPATION TRACKING
 * Track ai tham gia đánh boss và damage dealt
 */
public class WorldBossParticipation {
    
    private final UUID bossUUID;
    private final Map<UUID, ParticipationData> participants = new HashMap<>();
    private final long startTime;
    
    public WorldBossParticipation(UUID bossUUID) {
        this.bossUUID = bossUUID;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Record damage từ player
     */
    public void recordDamage(UUID playerUUID, double damage) {
        participants.computeIfAbsent(playerUUID, k -> new ParticipationData(playerUUID))
            .addDamage(damage);
    }
    
    /**
     * Record heal từ player (nếu có)
     */
    public void recordHeal(UUID playerUUID, double heal) {
        participants.computeIfAbsent(playerUUID, k -> new ParticipationData(playerUUID))
            .addHeal(heal);
    }
    
    /**
     * Get participation data của player
     */
    public ParticipationData getParticipation(UUID playerUUID) {
        return participants.get(playerUUID);
    }
    
    /**
     * Get tất cả participants
     */
    public Collection<ParticipationData> getAllParticipants() {
        return participants.values();
    }
    
    /**
     * Get top damage dealers
     */
    public List<ParticipationData> getTopDamageDealers(int count) {
        List<ParticipationData> sorted = new ArrayList<>(participants.values());
        sorted.sort((a, b) -> Double.compare(b.getTotalDamage(), a.getTotalDamage()));
        return sorted.subList(0, Math.min(count, sorted.size()));
    }
    
    /**
     * Get total participation time (seconds)
     */
    public long getParticipationTime() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    
    /**
     * Get boss UUID
     */
    public UUID getBossUUID() {
        return bossUUID;
    }
    
    /**
     * Participation data cho mỗi player
     */
    public static class ParticipationData {
        private final UUID playerUUID;
        private double totalDamage;
        private double totalHeal;
        private long firstHitTime;
        private long lastHitTime;
        private int hitCount;
        
        public ParticipationData(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.firstHitTime = System.currentTimeMillis();
            this.lastHitTime = System.currentTimeMillis();
        }
        
        public void addDamage(double damage) {
            this.totalDamage += damage;
            this.hitCount++;
            this.lastHitTime = System.currentTimeMillis();
        }
        
        public void addHeal(double heal) {
            this.totalHeal += heal;
        }
        
        // Getters
        public UUID getPlayerUUID() { return playerUUID; }
        public double getTotalDamage() { return totalDamage; }
        public double getTotalHeal() { return totalHeal; }
        public long getFirstHitTime() { return firstHitTime; }
        public long getLastHitTime() { return lastHitTime; }
        public int getHitCount() { return hitCount; }
        
        /**
         * Get participation duration (seconds)
         */
        public long getParticipationDuration() {
            return (lastHitTime - firstHitTime) / 1000;
        }
    }
}
