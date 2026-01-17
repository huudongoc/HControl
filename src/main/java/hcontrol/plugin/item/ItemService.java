package hcontrol.plugin.item;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * PHASE 8A — ITEM SERVICE
 * Business logic cho item system
 * - Equip/unequip items
 * - Get total stats từ equipped items
 * - Validate requirements
 */
public class ItemService {
    
    private final ItemRegistry registry;
    
    public ItemService(ItemRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Equip item vào slot
     * @return true nếu thành công
     */
    public boolean equipItem(PlayerProfile profile, String itemId, EquipmentSlot slot) {
        ItemTemplate template = registry.get(itemId);
        if (template == null) {
            return false;
        }
        
        // Check requirements
        if (!canEquip(profile, template)) {
            return false;
        }
        
        // Check slot match
        if (template.getSlot() != slot) {
            return false;
        }
        
        // Equip item (lưu vào PlayerProfile)
        profile.equipItem(slot, itemId);
        
        return true;
    }
    
    /**
     * Unequip item từ slot
     */
    public void unequipItem(PlayerProfile profile, EquipmentSlot slot) {
        profile.unequipItem(slot);
    }
    
    /**
     * Get total stats từ tất cả equipped items
     */
    public Map<StatType, Double> getTotalStats(PlayerProfile profile) {
        Map<StatType, Double> totalStats = new HashMap<>();
        
        // Lấy tất cả equipped items
        Map<EquipmentSlot, String> equipped = profile.getEquippedItems();
        
        for (Map.Entry<EquipmentSlot, String> entry : equipped.entrySet()) {
            String itemId = entry.getValue();
            ItemTemplate template = registry.get(itemId);
            
            if (template != null) {
                // Cộng dồn stats
                for (Map.Entry<StatType, Double> statEntry : template.getStats().entrySet()) {
                    StatType statType = statEntry.getKey();
                    double value = statEntry.getValue();
                    totalStats.put(statType, totalStats.getOrDefault(statType, 0.0) + value);
                }
            }
        }
        
        return totalStats;
    }
    
    /**
     * Get stat value từ equipped items
     */
    public double getStat(PlayerProfile profile, StatType statType) {
        Map<StatType, Double> totalStats = getTotalStats(profile);
        return totalStats.getOrDefault(statType, 0.0);
    }
    
    /**
     * Check if player can equip item
     */
    public boolean canEquip(PlayerProfile profile, ItemTemplate template) {
        // Check realm requirement
        if (profile.getRealm().ordinal() < template.getMinRealm().ordinal()) {
            return false;
        }
        
        // Check level requirement
        if (profile.getLevel() < template.getMinLevel()) {
            return false;
        }
        
        return true;
    }
}
