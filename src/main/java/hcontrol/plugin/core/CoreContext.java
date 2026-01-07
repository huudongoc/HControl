package hcontrol.plugin.core;

import org.bukkit.Bukkit;

import hcontrol.plugin.Main;
import hcontrol.plugin.command.ExpCommand;
import hcontrol.plugin.command.ReloadCommand;
import hcontrol.plugin.command.StatCommand;
import hcontrol.plugin.listener.PlayerCombatListener;
import hcontrol.plugin.player.LevelService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.listener.JoinServerListener;
import hcontrol.plugin.listener.OutServerListener;
import hcontrol.plugin.service.DisableDameService;
import hcontrol.plugin.service.StatService;
import hcontrol.plugin.ui.PlayerUIService;

/**
 * PHASE 0 — FOUNDATION
 * Singleton context chứa tất cả dependencies
 */
public class CoreContext {
    private static CoreContext instance;
    
    private final Main plugin;
    private final LifecycleManager lifecycleManager;
    private final PlayerManager playerManager;
    private final LevelService levelService;
    private final PlayerStorage playerStorage;
    private final StatService statService;
    
    //ui
    private PlayerUIService playerUIService;


// Services
    private DisableDameService disableDameService;
    
    // Listeners
    private JoinServerListener joinListener;
    private OutServerListener outListener;
    private PlayerCombatListener combatListener;
    
    private CoreContext(Main plugin, LifecycleManager lifecycleManager) {
        this.plugin = plugin;
        this.lifecycleManager = lifecycleManager;
        this.playerManager = new PlayerManager();
        this.playerStorage = new PlayerStorage(plugin.getDataFolder());
        this.levelService = new LevelService();
        this.statService = new StatService();
    }
    
    /**
     * Initialize singleton instance
     */
    public static void initialize(Main plugin, LifecycleManager lifecycleManager) {
        if (instance != null) {
            throw new IllegalStateException("CoreContext đã được khởi tạo rồi!");
        }
        instance = new CoreContext(plugin, lifecycleManager);
    }
    
    /**
     * Get singleton instance
     */
    public static CoreContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CoreContext chưa được khởi tạo!");
        }
        return instance;
    }
    
    /**
     * Reset instance (for reload)
     */
    public static void reset() {
        instance = null;
    }
    
    /**
     * PHASE 0: Register tất cả module callbacks
     */
    public void registerAllModules() {
        // PHASE 1: Player System
        registerPlayerSystem();
        
        // PHASE 3: Combat System
        registerCombatSystem();
        
        // Commands
        registerCommands();
    }
    
    /**
     * PHASE 1 — PLAYER SYSTEM
     */
    private void registerPlayerSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 1] Đang khởi tạo Player System...");
            
            // Init PlayerService
            playerUIService = new PlayerUIService();
            
            // Register Listeners
            joinListener = new JoinServerListener(playerUIService, playerManager, playerStorage, lifecycleManager);
            outListener = new OutServerListener(playerUIService, playerManager, playerStorage, lifecycleManager);
            
            Bukkit.getPluginManager().registerEvents(joinListener, plugin);
            Bukkit.getPluginManager().registerEvents(outListener, plugin);
            
            lifecycleManager.enableModule("PlayerSystem");
            plugin.getLogger().info("[PHASE 1] ✓ Player System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 1] Đang tắt Player System...");
            
            // save tat ca player dang online
            plugin.getLogger().info("[PHASE 1] Đang lưu dữ liệu " + playerManager.getAllOnline().size() + " player...");
            for (PlayerProfile profile : playerManager.getAllOnline()) {
                try {
                    playerStorage.save(profile);
                } catch (Exception e) {
                    plugin.getLogger().severe("Lỗi khi lưu player " + profile.getUuid() + ": " + e.getMessage());
                }
            }
            playerManager.clear();
            
            playerUIService = null;
            joinListener = null;
            outListener = null;
            
            lifecycleManager.disableModule("PlayerSystem");
            plugin.getLogger().info("[PHASE 1] ✓ Player System đã tắt!");
        });
    }
    
    /**
     * PHASE 3 — COMBAT SYSTEM
     */
    private void registerCombatSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 3] Đang khởi tạo Combat System...");
            
            // Init DisableDameService
            disableDameService = new DisableDameService();
            
            // Register CombatListener
            combatListener = new PlayerCombatListener(disableDameService);
            Bukkit.getPluginManager().registerEvents(combatListener, plugin);
            
            lifecycleManager.enableModule("CombatSystem");
            plugin.getLogger().info("[PHASE 3] ✓ Combat System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 3] Đang tắt Combat System...");
            
            disableDameService = null;
            combatListener = null;
            
            lifecycleManager.disableModule("CombatSystem");
        });
    }
    
    /**
     * Register Commands
     */
    private void registerCommands() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 0] Đang đăng ký commands...");
            
            plugin.getCommand("hc").setExecutor(new ReloadCommand(lifecycleManager));
            plugin.getCommand("exp").setExecutor(new ExpCommand(playerManager, levelService));
            plugin.getCommand("stat").setExecutor(new StatCommand(playerManager, statService));
            
            plugin.getLogger().info("[PHASE 0] ✓ Commands đã được đăng ký!");
        });
    }
    
    // ===== GETTERS =====
    public Main getPlugin() { return plugin; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public LevelService getLevelService() { return levelService; }
    public PlayerStorage getPlayerStorage() { return playerStorage; }
    public StatService getStatService() { return statService; }
    public LifecycleManager getLifecycleManager() { return lifecycleManager; }
    public PlayerUIService getPlayerUIService() { return playerUIService; }
    public DisableDameService getDisableDameService() { return disableDameService; }
}