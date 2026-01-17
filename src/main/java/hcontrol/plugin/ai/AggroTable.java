package hcontrol.plugin.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PHASE 7 — AGGRO TABLE
 * Theo doi threat/aggro cua mob doi voi tung player
 * 
 * Threat tang khi:
 * - Player danh mob
 * - Player hoi mau cho ally
 * - Player dung skill gay threat
 * 
 * Mob uu tien tan cong player co threat cao nhat
 */
public class AggroTable {
    
    private final Map<UUID, Double> threats = new HashMap<>();
    private long lastDecayTime = System.currentTimeMillis();
    
    /**
     * Them threat cho player
     * 
     * @param playerUUID UUID cua player
     * @param amount So luong threat them vao
     */
    public void addThreat(UUID playerUUID, double amount) {
        double current = threats.getOrDefault(playerUUID, 0.0);
        threats.put(playerUUID, current + amount);
    }
    
    /**
     * Set threat cho player (ghi de)
     * 
     * @param playerUUID UUID cua player
     * @param amount So luong threat moi
     */
    public void setThreat(UUID playerUUID, double amount) {
        if (amount <= 0) {
            threats.remove(playerUUID);
        } else {
            threats.put(playerUUID, amount);
        }
    }
    
    /**
     * Lay threat cua player
     * 
     * @param playerUUID UUID cua player
     * @return So luong threat
     */
    public double getThreat(UUID playerUUID) {
        return threats.getOrDefault(playerUUID, 0.0);
    }
    
    /**
     * Lay player co threat cao nhat
     * 
     * @return UUID cua player (null neu khong co)
     */
    public UUID getHighestThreat() {
        UUID highest = null;
        double maxThreat = 0;
        
        for (Map.Entry<UUID, Double> entry : threats.entrySet()) {
            if (entry.getValue() > maxThreat) {
                maxThreat = entry.getValue();
                highest = entry.getKey();
            }
        }
        
        return highest;
    }
    
    /**
     * Xoa player khoi aggro table
     * 
     * @param playerUUID UUID cua player
     */
    public void remove(UUID playerUUID) {
        threats.remove(playerUUID);
    }
    
    /**
     * Clear tat ca threat
     */
    public void clear() {
        threats.clear();
    }
    
    /**
     * Kiem tra co player nao trong aggro table khong
     * 
     * @return true neu co player
     */
    public boolean isEmpty() {
        return threats.isEmpty();
    }
    
    /**
     * Decay threat theo thoi gian
     * Goi method nay moi tick hoac moi giay
     * 
     * @param decayRate % threat giam moi giay (VD: 0.05 = giam 5%/giay)
     */
    public void decay(double decayRate) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastDecayTime;
        
        if (elapsed < 1000) {
            return; // chua du 1 giay
        }
        
        lastDecayTime = now;
        
        // Giam threat cua tat ca players
        threats.entrySet().removeIf(entry -> {
            double newThreat = entry.getValue() * (1.0 - decayRate);
            if (newThreat < 1.0) {
                return true; // xoa neu threat qua thap
            }
            entry.setValue(newThreat);
            return false;
        });
    }
    
    /**
     * Nhan threat (dung khi mob buff hoac rage mode)
     * 
     * @param multiplier He so nhan
     */
    public void multiplyAll(double multiplier) {
        for (UUID uuid : threats.keySet()) {
            threats.put(uuid, threats.get(uuid) * multiplier);
        }
    }
    
    /**
     * Get so luong players trong aggro table
     * 
     * @return So luong
     */
    public int size() {
        return threats.size();
    }
}
