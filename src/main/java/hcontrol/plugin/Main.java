package hcontrol.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.core.LifecycleManager;

/**
 * PHASE 0 — FOUNDATION
 * Main chỉ làm wiring, không chứa logic
 */
public class Main extends JavaPlugin {
    private LifecycleManager lifecycleManager;
    
    @Override
    public void onEnable() {
        // Init lifecycle manager
        this.lifecycleManager = new LifecycleManager(this);
        
        // Init core context (singleton)
        CoreContext.initialize(this, lifecycleManager);
        
        // Register tất cả module callbacks
        CoreContext.getInstance().registerAllModules();
        
        // Execute enable callbacks
        lifecycleManager.enableAll();
        
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("HControl RPG đã được kích hoạt!");
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @Override
    public void onDisable() {
        if (lifecycleManager != null) {
            lifecycleManager.disableAll();
        }
        
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("HControl RPG đã được tắt!");
        getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}