package hcontrol.plugin.item;

import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PHASE 8A — ITEM TEMPLATE
 * Data-only model cho item definition
 * Không chứa logic, chỉ chứa data
 */
public class ItemTemplate {
    
    private final String id;
    private final String displayName;
    private final ItemType type;
    private final EquipmentSlot slot;
    private final CultivationRealm minRealm;
    private final int minLevel;
    private final Map<StatType, Double> stats;
    private final Material material;
    private final List<String> lore;
    
    private ItemTemplate(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.type = builder.type;
        this.slot = builder.slot;
        this.minRealm = builder.minRealm;
        this.minLevel = builder.minLevel;
        this.stats = new HashMap<>(builder.stats);
        this.material = builder.material;
        this.lore = List.copyOf(builder.lore);
    }
    
    // ========== GETTERS ==========
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public EquipmentSlot getSlot() {
        return slot;
    }
    
    public CultivationRealm getMinRealm() {
        return minRealm;
    }
    
    public int getMinLevel() {
        return minLevel;
    }
    
    public Map<StatType, Double> getStats() {
        return new HashMap<>(stats);
    }
    
    public double getStat(StatType statType) {
        return stats.getOrDefault(statType, 0.0);
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public List<String> getLore() {
        return List.copyOf(lore);
    }
    
    // ========== BUILDER ==========
    
    public static class Builder {
        private final String id;
        private String displayName;
        private ItemType type = ItemType.ARTIFACT;
        private EquipmentSlot slot = EquipmentSlot.HAND;
        private CultivationRealm minRealm = CultivationRealm.PHAMNHAN;
        private int minLevel = 1;
        private final Map<StatType, Double> stats = new HashMap<>();
        private Material material = Material.DIAMOND_SWORD;
        private List<String> lore = List.of();
        
        public Builder(String id) {
            this.id = id;
            this.displayName = id;
        }
        
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder type(ItemType type) {
            this.type = type;
            return this;
        }
        
        public Builder slot(EquipmentSlot slot) {
            this.slot = slot;
            return this;
        }
        
        public Builder minRealm(CultivationRealm minRealm) {
            this.minRealm = minRealm;
            return this;
        }
        
        public Builder minLevel(int minLevel) {
            this.minLevel = minLevel;
            return this;
        }
        
        public Builder stat(StatType statType, double value) {
            this.stats.put(statType, value);
            return this;
        }
        
        public Builder material(Material material) {
            this.material = material;
            return this;
        }
        
        public Builder lore(List<String> lore) {
            this.lore = lore != null ? List.copyOf(lore) : List.of();
            return this;
        }
        
        public ItemTemplate build() {
            return new ItemTemplate(this);
        }
    }
}
