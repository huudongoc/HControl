package hcontrol.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.core.LifecycleManager;
import hcontrol.plugin.listener.BukkitEventRegistry;

/**
 * PHASE 0 — TRUCCO
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
        
        // Inject EventRegistry để CoreContext không phải import Bukkit
        CoreContext.getInstance().setEventRegistry(new BukkitEventRegistry(this));
        
        // Register tất cả module callbacks
        CoreContext.getInstance().registerAllModules();
        
        // Execute enable callbacks
        lifecycleManager.enableAll();
        
        //getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("HControl on!");
       // getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @Override
    public void onDisable() {
        if (lifecycleManager != null) {
            lifecycleManager.disableAll();
        }
        
   //     getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        getLogger().info("HControl off!");
    //    getLogger().info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}