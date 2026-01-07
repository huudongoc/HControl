package hcontrol.plugin.core;

import org.bukkit.Bukkit;

import hcontrol.plugin.Main;
import hcontrol.plugin.command.BreakthroughCommand;
import hcontrol.plugin.command.CultivatorCommand;
import hcontrol.plugin.command.ExpCommand;
import hcontrol.plugin.command.ReloadCommand;
import hcontrol.plugin.command.StatCommand;
import hcontrol.plugin.command.SpawnBossCommand;
import hcontrol.plugin.command.TitleCommand;
import hcontrol.plugin.command.UnlockCommand;
import hcontrol.plugin.player.BreakthroughService;
import hcontrol.plugin.listener.PlayerCombatListener;
import hcontrol.plugin.listener.PlayerChatListener;
import hcontrol.plugin.player.AutoSaveTask;
import hcontrol.plugin.player.LevelService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.listener.JoinServerListener;
import hcontrol.plugin.listener.OutServerListener;
import hcontrol.plugin.service.CombatService;
import hcontrol.plugin.service.StatService;
import hcontrol.plugin.service.TitleService;
import hcontrol.plugin.ui.PlayerUIService;
import hcontrol.plugin.ui.ScoreboardService;
import hcontrol.plugin.ui.ScoreboardUpdateTask;
import hcontrol.plugin.ui.NameplateService;
import hcontrol.plugin.ui.ChatBubbleService;
import hcontrol.plugin.module.boss.BossManager;

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
    private final CombatService combatService;
    private final BossManager bossManager;
    private final BreakthroughService breakthroughService;
    private final TitleService titleService;
    
    //ui
    private PlayerUIService playerUIService;
    private ScoreboardService scoreboardService;
    private ScoreboardUpdateTask scoreboardUpdateTask;
    private NameplateService nameplateService;
    private ChatBubbleService chatBubbleService;
    
    // Auto-save
    private AutoSaveTask autoSaveTask;
    
    // Listeners
    private JoinServerListener joinListener;
    private OutServerListener outListener;
    private PlayerCombatListener combatListener;
    private PlayerChatListener chatListener;
    
    private CoreContext(Main plugin, LifecycleManager lifecycleManager) {
        this.plugin = plugin;
        this.lifecycleManager = lifecycleManager;
        this.playerManager = new PlayerManager();
        this.playerStorage = new PlayerStorage(plugin.getDataFolder());
        this.levelService = new LevelService();
        this.statService = new StatService();
        this.combatService = new CombatService(playerManager, plugin); // inject PlayerManager + Plugin
        this.breakthroughService = new BreakthroughService();
        this.titleService = new TitleService();
        this.bossManager = new BossManager();
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
             
        // Commands
        registerCommands();
        // PHASE 1: Player System
        registerPlayerSystem();
        
        // PHASE 3: Combat System
        registerCombatSystem();
   
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
            plugin.getCommand("breakthrough").setExecutor(new BreakthroughCommand(playerManager, breakthroughService));
            plugin.getCommand("spawnboss").setExecutor(new SpawnBossCommand(bossManager));
            plugin.getCommand("cultivator").setExecutor(new CultivatorCommand(playerManager, null)); // CultivationService chua co
            plugin.getCommand("title").setExecutor(new TitleCommand(playerManager, titleService));
            plugin.getCommand("unlock").setExecutor(new UnlockCommand(playerManager));
            
            plugin.getLogger().info("[PHASE 0] ✓ Commands đã được đăng ký!");
        });
    }
    
    
    /**
     * PHASE 1 — PLAYER SYSTEM
     */
    private void registerPlayerSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 1] Đang khởi tạo Player System...");
            
            // Init PlayerService
            playerUIService = new PlayerUIService(playerManager);
            scoreboardService = new ScoreboardService(playerManager);
            nameplateService = new NameplateService(playerManager);
            chatBubbleService = new ChatBubbleService(plugin);
            
            // Register Listeners
            joinListener = new JoinServerListener(playerUIService, scoreboardService, nameplateService, playerManager, playerStorage, lifecycleManager);
            outListener = new OutServerListener(playerUIService, playerManager, playerStorage, lifecycleManager);
            
            Bukkit.getPluginManager().registerEvents(joinListener, plugin);
            Bukkit.getPluginManager().registerEvents(outListener, plugin);
            
            // Chat bubble listener
            chatListener = new PlayerChatListener(chatBubbleService);
            Bukkit.getPluginManager().registerEvents(chatListener, plugin);
            
            // Start auto-save task (5 phut)
            autoSaveTask = new AutoSaveTask(plugin, playerManager, playerStorage, 5 * 60 * 20L);
            autoSaveTask.start();
            
            // Start scoreboard update task (1 giay)
            scoreboardUpdateTask = new ScoreboardUpdateTask(scoreboardService, playerManager);
            scoreboardUpdateTask.start(plugin);
            
            lifecycleManager.enableModule("PlayerSystem");
            plugin.getLogger().info("[PHASE 1] ✓ Player System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 1] Đang tắt Player System...");
            
            // stop auto-save task
            if (autoSaveTask != null) {
                autoSaveTask.stop();
                autoSaveTask = null;
            }
            
            // stop scoreboard update task
            if (scoreboardUpdateTask != null) {
                scoreboardUpdateTask.cancel();
                scoreboardUpdateTask = null;
            }
            
            // remove all chat bubbles
            if (chatBubbleService != null) {
                chatBubbleService.removeAllBubbles();
            }
            
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
            
            // Inject NameplateService vao CombatService
            combatService.setNameplateService(nameplateService);
            
            // Inject NameplateService vao TitleService
            titleService.setNameplateService(nameplateService);
            
            // Register CombatListener
            combatListener = new PlayerCombatListener(playerManager, combatService);
            Bukkit.getPluginManager().registerEvents(combatListener, plugin);
            
            lifecycleManager.enableModule("CombatSystem");
            plugin.getLogger().info("[PHASE 3] ✓ Combat System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 3] Đang tắt Combat System...");
            
            combatListener = null;
            
            lifecycleManager.disableModule("CombatSystem");
            plugin.getLogger().info("[PHASE 3] ✓ Combat System đã tắt!");
        });
    }
    
 
    // ===== GETTERS =====
    public Main getPlugin() { return plugin; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public LevelService getLevelService() { return levelService; }
    public PlayerStorage getPlayerStorage() { return playerStorage; }
    public StatService getStatService() { return statService; }
    public CombatService getCombatService() { return combatService; }
    public BreakthroughService getBreakthroughService() { return breakthroughService; }
    public TitleService getTitleService() { return titleService; }
    public LifecycleManager getLifecycleManager() { return lifecycleManager; }
    public PlayerUIService getPlayerUIService() { return playerUIService; }
    public ScoreboardService getScoreboardService() { return scoreboardService; }
    public NameplateService getNameplateService() { return nameplateService; }
    public ChatBubbleService getChatBubbleService() { return chatBubbleService; }
    public BossManager getBossManager() { return bossManager; }
}