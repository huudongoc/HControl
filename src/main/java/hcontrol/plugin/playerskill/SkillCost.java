package hcontrol.plugin.playerskill;

import hcontrol.plugin.player.PlayerProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * PHASE 6 — SKILL COST
 * Chi phi de cast skill (LingQi, item, etc.)
 */
public class SkillCost {
    
    private final double lingQi;
    private final Map<String, Integer> items; // Item cost (Phase 8+)
    
    /**
     * Constructor with LingQi cost only
     */
    public SkillCost(double lingQi) {
        this.lingQi = lingQi;
        this.items = new HashMap<>();
    }
    
    /**
     * Constructor with LingQi and item costs
     */
    public SkillCost(double lingQi, Map<String, Integer> items) {
        this.lingQi = lingQi;
        this.items = items != null ? new HashMap<>(items) : new HashMap<>();
    }
    
    /**
     * Check if player can afford this cost
     */
    public boolean canAfford(PlayerProfile profile) {
        // Check LingQi
        if (profile.getCurrentLingQi() < lingQi) {
            return false;
        }
        
        // TODO PHASE 8: Check item costs
        // For now, only check LingQi
        
        return true;
    }
    
    /**
     * Deduct cost from player
     */
    public void deduct(PlayerProfile profile) {
        double newLingQi = profile.getCurrentLingQi() - lingQi;
        profile.setCurrentLingQi(Math.max(0, newLingQi));
        
        // TODO PHASE 8: Deduct items from inventory
    }
    
    /**
     * Get missing cost info (for error messages)
     */
    public String getMissingCostMessage(PlayerProfile profile) {
        if (profile.getCurrentLingQi() < lingQi) {
            double missing = lingQi - profile.getCurrentLingQi();
            return String.format("§cThiếu %.0f Linh Khí", missing);
        }
        
        // TODO PHASE 8: Check missing items
        
        return null;
    }
    
    // ========== GETTERS ==========
    
    public double getLingQi() {
        return lingQi;
    }
    
    public Map<String, Integer> getItems() {
        return new HashMap<>(items);
    }
    
    public boolean hasItemCost() {
        return !items.isEmpty();
    }
}
