package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.classsystem.ClassRegistry;
import hcontrol.plugin.classsystem.ClassService;

/**
 * PHASE 5 — CLASS CONTEXT
 * SubContext quản lý class system
 */
public class ClassContext {
    
    private final Main plugin;
    private final ClassRegistry registry;
    private ClassService classService;
    
    public ClassContext(Main plugin) {
        this.plugin = plugin;
        this.registry = new ClassRegistry();
    }
    
    /**
     * Initialize class system
     */
    public void initialize() {
        // Tạo ClassService
        this.classService = new ClassService(registry);
        
        plugin.getLogger().info("[PHASE 5] Class System đã khởi động! (" + 
            registry.getModifiers(hcontrol.plugin.classsystem.ClassType.SWORD_CULTIVATOR).size() + 
            " modifiers cho SWORD_CULTIVATOR)");
    }
    
    // ========== GETTERS ==========
    
    public ClassRegistry getRegistry() {
        return registry;
    }
    
    public ClassService getClassService() {
        return classService;
    }
}
