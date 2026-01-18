package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.item.ItemRegistry;
import hcontrol.plugin.item.ItemService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * PHASE 8A — ITEM CONTEXT
 * SubContext quản lý item system
 */
public class ItemContext {
    
    private final Main plugin;
    private final ItemRegistry registry;
    private ItemService itemService;
    
    public ItemContext(Main plugin) {
        this.plugin = plugin;
        this.registry = new ItemRegistry();
    }
    
    /**
     * Initialize item system
     * Load config và tạo services
     */
    public void initialize() {
        // Load items từ YAML
        loadItemsFromConfig();
        
        // Tạo ItemService
        this.itemService = new ItemService(registry);
        
        plugin.getLogger().info("[PHASE 8A] Item System đã khởi động! (" + registry.size() + " items)");
    }
    
    /**
     * Load items từ items.yml
     */
    private void loadItemsFromConfig() {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        
        // Save default config nếu chưa tồn tại
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        
        // Load từ YAML
        if (itemsFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
            registry.loadFromConfig(config);
        }
    }
    
    // ========== GETTERS ==========
    
    public ItemRegistry getRegistry() {
        return registry;
    }
    
    public ItemService getItemService() {
        return itemService;
    }
}
