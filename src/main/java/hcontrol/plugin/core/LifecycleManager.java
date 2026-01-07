package hcontrol.plugin.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import hcontrol.plugin.Main;
import hcontrol.plugin.player.PlayerProfile;

/**
 * PHASE 0 — Quản lý lifecycle của tất cả module
 */
public class LifecycleManager {
    
    private final Main plugin;
    private final List<Runnable> onEnableCallbacks = new ArrayList<>();
    private final List<Runnable> onDisableCallbacks = new ArrayList<>();
    private final List<Consumer<PlayerProfile>> onPlayerLoadCallbacks = new ArrayList<>();
    private final List<Consumer<PlayerProfile>> onPlayerSaveCallbacks = new ArrayList<>();
    private final Map<String, Boolean> moduleStates = new HashMap<>();
    
    public LifecycleManager(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Đăng ký callback khi plugin enable
     */
    public void registerOnEnable(Runnable callback) {
        onEnableCallbacks.add(callback);
    }
    
    /**
     * Đăng ký callback khi plugin disable
     */
    public void registerOnDisable(Runnable callback) {
        onDisableCallbacks.add(callback);
    }
    
    /**
     * Đăng ký callback khi player load data
     */
    public void registerOnPlayerLoad(Consumer<PlayerProfile> callback) {
        onPlayerLoadCallbacks.add(callback);
    }
    
    /**
     * Đăng ký callback khi player save data
     */
    public void registerOnPlayerSave(Consumer<PlayerProfile> callback) {
        onPlayerSaveCallbacks.add(callback);
    }
    
    /**
     * Gọi callback khi player load
     */
    public void onPlayerLoad(PlayerProfile profile) {
        for (Consumer<PlayerProfile> callback : onPlayerLoadCallbacks) {
            try {
                callback.accept(profile);
            } catch (Exception e) {
                plugin.getLogger().severe("LỖI khi load player: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gọi callback khi player save
     */
    public void onPlayerSave(PlayerProfile profile) {
        for (Consumer<PlayerProfile> callback : onPlayerSaveCallbacks) {
            try {
                callback.accept(profile);
            } catch (Exception e) {
                plugin.getLogger().severe("LỖI khi save player: " + e.getMessage());
            }
        }
    }
    
    /**
     * Enable tất cả module theo thứ tự
     */
    public void enableAll() {
        plugin.getLogger().info("=== ĐANG KHỞI ĐỘNG HCONTROL RPG ===");
        
        for (int i = 0; i < onEnableCallbacks.size(); i++) {
            try {
                onEnableCallbacks.get(i).run();
            } catch (Exception e) {
                plugin.getLogger().severe("LỖI khi enable module #" + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("=== HCONTROL RPG ĐÃ KHỞI ĐỘNG XONG ===");
    }
    
    /**
     * Disable tất cả module theo thứ tự ngược lại
     */
    public void disableAll() {
        plugin.getLogger().info("=== ĐANG TẮT HCONTROL RPG ===");
        
        for (int i = onDisableCallbacks.size() - 1; i >= 0; i--) {
            try {
                onDisableCallbacks.get(i).run();
            } catch (Exception e) {
                plugin.getLogger().severe("LỖI khi disable module #" + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("=== HCONTROL RPG ĐÃ TẮT ===");
    }
    
    /**
     * Reload tất cả module
     */
    public void reloadAll() {
        plugin.getLogger().info("=== BẮT ĐẦU RELOAD ===");
        
        disableAll();
        
        onEnableCallbacks.clear();
        onDisableCallbacks.clear();
        moduleStates.clear();
        
        CoreContext.reset();
        CoreContext.initialize(plugin, this);
        CoreContext.getInstance().registerAllModules();
        
        enableAll();
        
        plugin.getLogger().info("=== RELOAD HOÀN TẤT ===");
    }
    
    /**
     * Enable một module cụ thể
     */
    public void enableModule(String moduleName) {
        moduleStates.put(moduleName, true);
        plugin.getLogger().info("✓ Module " + moduleName + " enabled");
    }
    
    /**
     * Disable một module cụ thể
     */
    public void disableModule(String moduleName) {
        moduleStates.put(moduleName, false);
        plugin.getLogger().info("✗ Module " + moduleName + " disabled");
    }
    
    /**
     * Kiểm tra module có enabled không
     */
    public boolean isModuleEnabled(String moduleName) {
        return moduleStates.getOrDefault(moduleName, false);
    }
}
