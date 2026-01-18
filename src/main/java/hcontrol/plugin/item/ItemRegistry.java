package hcontrol.plugin.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PHASE 8A — ITEM REGISTRY
 * Quản lý item templates
 * Load từ YAML, cache trong RAM
 */
public class ItemRegistry {
    
    private final Map<String, ItemTemplate> templates = new HashMap<>();
    
    /**
     * Register item template
     */
    public void register(ItemTemplate template) {
        templates.put(template.getId(), template);
    }
    
    /**
     * Get item template by ID
     */
    public ItemTemplate get(String itemId) {
        return templates.get(itemId);
    }
    
    /**
     * Check if item exists
     */
    public boolean has(String itemId) {
        return templates.containsKey(itemId);
    }
    
    /**
     * Get all templates
     */
    public List<ItemTemplate> getAll() {
        return new ArrayList<>(templates.values());
    }
    
    /**
     * Get total number of registered items
     */
    public int size() {
        return templates.size();
    }
    
    /**
     * Load items từ YAML config
     */
    public void loadFromConfig(FileConfiguration config) {
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            return;
        }
        
        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
            if (itemSection == null) {
                continue;
            }
            
            try {
                ItemTemplate template = loadItemTemplate(itemId, itemSection);
                if (template != null) {
                    register(template);
                }
            } catch (Exception e) {
                System.err.println("[ItemRegistry] Lỗi load item " + itemId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Load một item template từ config section
     */
    private ItemTemplate loadItemTemplate(String itemId, ConfigurationSection section) {
        ItemTemplate.Builder builder = new ItemTemplate.Builder(itemId);
        
        // Display name
        if (section.contains("display_name")) {
            builder.displayName(section.getString("display_name"));
        }
        
        // Type
        if (section.contains("type")) {
            try {
                builder.type(ItemType.valueOf(section.getString("type").toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.type(ItemType.ARTIFACT); // Default
            }
        }
        
        // Slot
        if (section.contains("slot")) {
            try {
                builder.slot(EquipmentSlot.valueOf(section.getString("slot").toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.slot(EquipmentSlot.HAND); // Default
            }
        }
        
        // Min realm
        if (section.contains("min_realm")) {
            try {
                builder.minRealm(hcontrol.plugin.model.CultivationRealm.valueOf(
                    section.getString("min_realm").toUpperCase()
                ));
            } catch (IllegalArgumentException e) {
                builder.minRealm(hcontrol.plugin.model.CultivationRealm.PHAMNHAN); // Default
            }
        }
        
        // Min level
        if (section.contains("min_level")) {
            builder.minLevel(section.getInt("min_level"));
        }
        
        // Stats
        if (section.contains("stats")) {
            ConfigurationSection statsSection = section.getConfigurationSection("stats");
            if (statsSection != null) {
                for (String statKey : statsSection.getKeys(false)) {
                    try {
                        StatType statType = StatType.valueOf(statKey.toUpperCase());
                        double value = statsSection.getDouble(statKey);
                        builder.stat(statType, value);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid stat type
                    }
                }
            }
        }
        
        // Material
        if (section.contains("material")) {
            try {
                builder.material(Material.valueOf(section.getString("material").toUpperCase()));
            } catch (IllegalArgumentException e) {
                builder.material(Material.DIAMOND_SWORD); // Default
            }
        }
        
        // Lore
        if (section.contains("lore")) {
            List<String> lore = section.getStringList("lore");
            builder.lore(lore);
        }
        
        return builder.build();
    }
}
