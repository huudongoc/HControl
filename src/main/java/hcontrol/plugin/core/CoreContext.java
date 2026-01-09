package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.command.CommandRegistry;
import hcontrol.plugin.listener.EventRegistry;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityRegistry;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.listener.EntityLifecycleListener;
import hcontrol.plugin.listener.JoinServerListener;
import hcontrol.plugin.listener.OutServerListener;
import hcontrol.plugin.listener.PlayerChatListener;
import hcontrol.plugin.listener.PlayerCombatListener;
import hcontrol.plugin.listener.PlayerDeathListener;
import hcontrol.plugin.listener.PlayerRespawnListener;
import hcontrol.plugin.module.boss.BossManager;
import hcontrol.plugin.player.AutoSaveTask;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.service.BreakthroughService;
import hcontrol.plugin.service.CombatService;
import hcontrol.plugin.service.DamageEffectService;
import hcontrol.plugin.service.EventEffectService;
import hcontrol.plugin.service.LevelService;
import hcontrol.plugin.service.LevelUpEffectService;
import hcontrol.plugin.service.PlayerHealthService;
import hcontrol.plugin.service.SoundService;
import hcontrol.plugin.service.StatService;
import hcontrol.plugin.service.TitleService;
import hcontrol.plugin.service.TribulationService;
import hcontrol.plugin.ui.chat.ChatBubbleService;
import hcontrol.plugin.ui.entity.EntityDialogService;
import hcontrol.plugin.ui.entity.EntityNameplateService;
import hcontrol.plugin.ui.player.NameplateService;
import hcontrol.plugin.ui.player.PlayerUIService;
import hcontrol.plugin.ui.player.ScoreboardService;
import hcontrol.plugin.ui.player.ScoreboardUpdateTask;
import hcontrol.plugin.ui.tribulation.TribulationUI;
import hcontrol.plugin.ui.tribulation.UiStateService;
import hcontrol.plugin.ui.tribulation.listener.TribulationInputListener;

/**
 * PHASE 0 — FOUNDATION (REFACTORED)
 * Singleton context CHI chua SubContext
 * Moi SubContext quan ly services theo domain rieng
 */
public class CoreContext {
    private static CoreContext instance;
    
    // System-level dependencies
    private final Main plugin;
    private final LifecycleManager lifecycleManager;
    private EventRegistry eventRegistry;
    
    // Domain Contexts (5 SubContext)
    private final PlayerContext playerContext;
    private final CombatContext combatContext;
    private final EntityContext entityContext;
    private final UIContext uiContext;
    private final CultivationContext cultivationContext;
    
    // Listeners (lifecycle-managed, KHONG nen de trong SubContext)
    private JoinServerListener joinListener;
    private OutServerListener outListener;
    private PlayerCombatListener combatListener;
    private PlayerDeathListener deathListener;
    private PlayerChatListener chatListener;
    private PlayerRespawnListener respawnListener;
    private EntityLifecycleListener entityLifecycleListener;
    private TribulationInputListener tribulationInputListener;
    
    private CoreContext(Main plugin, LifecycleManager lifecycleManager) {
        this.plugin = plugin;
        this.lifecycleManager = lifecycleManager;
        
        // Khoi tao SubContexts theo thu tu phu thuoc
        // 1. Entity Context (khong phu thuoc ai)
        this.entityContext = new EntityContext();
        
        // 2. Player Context (khong phu thuoc ai, nhung LevelService se inject sau)
        this.playerContext = new PlayerContext(plugin);
        
        // 3. Combat Context (phu thuoc PlayerManager, EntityManager)
        this.combatContext = new CombatContext(
            plugin,
            playerContext.getPlayerManager(),
            entityContext.getEntityManager()
        );
        
        // 4. Inject LevelService vao PlayerContext (sau khi CombatContext da co LevelUpEffectService)
        LevelService levelService = new LevelService(combatContext.getLevelUpEffectService());
        playerContext.setLevelService(levelService);
        
        // 5. Cultivation Context (phu thuoc Plugin)
        this.cultivationContext = new CultivationContext(plugin);
        
        // 6. UI Context (init sau trong lifecycle callbacks)
        this.uiContext = new UIContext(plugin);
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
     * Set EventRegistry (được gọi từ Main sau khi khởi tạo)
     * Để tránh import Bukkit trong core package
     */
    public void setEventRegistry(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
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
     * 
     * QUY TRÌNH THÊM MODULE MỚI:
     * 1. Tạo Context mới (nếu cần) → xem CONTEXT_EXPANSION_GUIDE.md
     * 2. Thêm field trong CoreContext constructor
     * 3. Tạo method register[Module]System() như bên dưới
     * 4. Gọi register[Module]System() trong registerAllModules()
     * 
     * Ví dụ thêm Context mới (PHASE 8+):
     * - registerItemSystem()
     * - registerWorldSystem()
     * - registerEconomySystem()
     */
    public void registerAllModules() {
             
        // Commands
        registerCommands();
        
        // PHASE 1: Player System
        registerPlayerSystem();
        
        // PHASE 3: Combat System
        registerCombatSystem();
        
        // PHASE 5+: Thêm các module mới ở đây
        // registerClassSystem();     // PHASE 5
        // registerSkillSystem();     // PHASE 6 (thêm vào CombatContext)
        // registerItemSystem();      // PHASE 8
        // registerWorldSystem();     // PHASE 9
        // registerEconomySystem();   // PHASE 10
    }
    /**
     * Register Commands - Sử dụng CommandRegistry để đơn giản hóa
     * Note: CommandRegistry nằm trong package command (không phải core) vì import Bukkit
     */
    private void registerCommands() {
        lifecycleManager.registerOnEnable(() -> {
            CommandRegistry registry = new CommandRegistry(plugin, this);
            registry.registerAll();
        });
    }
    /**
     * PHASE 1 — PLAYER SYSTEM
     */
    private void registerPlayerSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 1] Đang khởi tạo Player System...");
            
            // Init Player UI trong UIContext (inject CultivationProgressService)
            uiContext.initPlayerUI(
                playerContext.getPlayerManager(), 
                playerContext.getCultivationProgressService()
            );
            
            // Inject NameplateService vào RoleService (de update nameplate khi set role)
            cultivationContext.getRoleService().setNameplateService(uiContext.getNameplateService());
            
            // Register Listeners
            joinListener = new JoinServerListener(
                uiContext.getPlayerUIService(),
                uiContext.getScoreboardService(),
                uiContext.getNameplateService(),
                playerContext.getPlayerManager(),
                playerContext.getPlayerStorage(),
                lifecycleManager
            );
            outListener = new OutServerListener(
                uiContext.getPlayerUIService(),
                playerContext.getPlayerManager(),
                playerContext.getPlayerStorage(),
                lifecycleManager
            );
            respawnListener = new PlayerRespawnListener(
                playerContext.getPlayerManager(),
                playerContext.getPlayerHealthService(),
                uiContext.getPlayerUIService(),
                uiContext.getScoreboardService(),
                uiContext.getNameplateService()
            );
            
            eventRegistry.registerEvents(joinListener);
            eventRegistry.registerEvents(outListener);
            eventRegistry.registerEvents(respawnListener);
            
            // Chat bubble listener
            chatListener = new PlayerChatListener(
                uiContext.getChatBubbleService(),
                uiContext.getChatFormatService(),
                playerContext.getPlayerManager(),
                plugin
            );
            eventRegistry.registerEvents(chatListener);
            
            // Breakthrough input listener (F/Q keys)
            tribulationInputListener = new TribulationInputListener(
                uiContext.getUiStateService(),
                uiContext.getTribulationUI(),
                cultivationContext.getBreakthroughService(),
                playerContext.getPlayerManager()
            );
            eventRegistry.registerEvents(tribulationInputListener);
            
            // Start auto-save task (5 phut)
            AutoSaveTask autoSaveTask = new AutoSaveTask(
                plugin, 
                playerContext.getPlayerManager(), 
                playerContext.getPlayerStorage(), 
                5 * 60 * 20L
            );
            autoSaveTask.start();
            playerContext.setAutoSaveTask(autoSaveTask);
            
            // Start scoreboard update task (1 giay)
            ScoreboardUpdateTask scoreboardUpdateTask = new ScoreboardUpdateTask(
                uiContext.getScoreboardService(), 
                playerContext.getPlayerManager()
            );
            scoreboardUpdateTask.start(plugin);
            uiContext.setScoreboardUpdateTask(scoreboardUpdateTask);
            
            lifecycleManager.enableModule("PlayerSystem");
            plugin.getLogger().info("[PHASE 1] ✓ Player System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 1] Đang tắt Player System...");
            
            // Stop auto-save task
            AutoSaveTask autoSaveTask = playerContext.getAutoSaveTask();
            if (autoSaveTask != null) {
                autoSaveTask.stop();
                playerContext.setAutoSaveTask(null);
            }
            
            // Stop scoreboard update task
            ScoreboardUpdateTask scoreboardUpdateTask = uiContext.getScoreboardUpdateTask();
            if (scoreboardUpdateTask != null) {
                scoreboardUpdateTask.cancel();
                uiContext.setScoreboardUpdateTask(null);
            }
            
            // Remove all chat bubbles
            if (uiContext.getChatBubbleService() != null) {
                uiContext.getChatBubbleService().removeAllBubbles();
            }
            
            // Clear all UI states
            if (uiContext.getUiStateService() != null) {
                playerContext.getPlayerManager().getAllOnline()
                    .forEach(p -> uiContext.getUiStateService().clear(p.getUuid()));
            }
            
            // Save tat ca player dang online
            plugin.getLogger().info("[PHASE 1] Đang lưu dữ liệu " + 
                playerContext.getPlayerManager().getAllOnline().size() + " player...");
            for (PlayerProfile profile : playerContext.getPlayerManager().getAllOnline()) {
                try {
                    playerContext.getPlayerStorage().save(profile);
                } catch (Exception e) {
                    plugin.getLogger().severe("Lỗi khi lưu player " + profile.getUuid() + ": " + e.getMessage());
                }
            }
            playerContext.getPlayerManager().clear();
            
            // Clear listeners
            joinListener = null;
            outListener = null;
            chatListener = null;
            tribulationInputListener = null;
            
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
            combatContext.getCombatService().setNameplateService(uiContext.getNameplateService());
            
            // Inject NameplateService vao TitleService
            cultivationContext.getTitleService().setNameplateService(uiContext.getNameplateService());
            
            // Register CombatListener
            combatListener = new PlayerCombatListener(
                playerContext.getPlayerManager(), 
                combatContext.getCombatService(),
                combatContext.getDisableDameService()
            );
            eventRegistry.registerEvents(combatListener);
            
            // Register DeathListener
            deathListener = new PlayerDeathListener(playerContext.getPlayerManager());
            eventRegistry.registerEvents(deathListener);
            
            // Init Entity UI Services
            uiContext.initEntityUI(entityContext.getEntityManager());
            
            // Register EntityLifecycleListener (mob spawn/death tracking + nameplate)
            entityLifecycleListener = new EntityLifecycleListener(
                entityContext.getEntityManager(),
                entityContext.getEntityService(),
                uiContext.getEntityNameplateService()
            );
            eventRegistry.registerEvents(entityLifecycleListener);
            
            lifecycleManager.enableModule("CombatSystem");
            plugin.getLogger().info("[PHASE 3] ✓ Combat System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 3] Đang tắt Combat System...");
            
            // Clear listeners
            combatListener = null;
            deathListener = null;
            entityLifecycleListener = null;
            
            // Cleanup entity UI
            if (uiContext.getEntityNameplateService() != null) {
                uiContext.getEntityNameplateService().stopAllTasks();
            }
            if (uiContext.getEntityDialogService() != null) {
                uiContext.getEntityDialogService().removeAllDialogs();
            }
            
            // Cleanup entity profiles
            entityContext.getEntityManager().clear();
            
            lifecycleManager.disableModule("CombatSystem");
            plugin.getLogger().info("[PHASE 3] ✓ Combat System đã tắt!");
        });
    }
    

    // ===== GETTERS — SubContext (PRIMARY) =====
    
    public PlayerContext getPlayerContext() { return playerContext; }
    public CombatContext getCombatContext() { return combatContext; }
    public EntityContext getEntityContext() { return entityContext; }
    public UIContext getUIContext() { return uiContext; }
    public CultivationContext getCultivationContext() { return cultivationContext; }
    
    // ===== GETTERS — System Level =====
    
    public Main getPlugin() { return plugin; }
    public LifecycleManager getLifecycleManager() { return lifecycleManager; }
    
    // ===== GETTERS — Backward Compatibility (DEPRECATED) =====
    // De tranh break code cu, giu lai getters
    // TODO: Thay the dần bang SubContext getters
    
    @Deprecated
    public PlayerManager getPlayerManager() { return playerContext.getPlayerManager(); }
    
    @Deprecated
    public LevelService getLevelService() { return playerContext.getLevelService(); }
    
    @Deprecated
    public PlayerStorage getPlayerStorage() { return playerContext.getPlayerStorage(); }
    
    @Deprecated
    public StatService getStatService() { return playerContext.getStatService(); }
    
    @Deprecated
    public PlayerHealthService getPlayerHealthService() { return playerContext.getPlayerHealthService(); }
    
    @Deprecated
    public CombatService getCombatService() { return combatContext.getCombatService(); }
    
    @Deprecated
    public SoundService getSoundService() { return combatContext.getSoundService(); }
    
    @Deprecated
    public LevelUpEffectService getLevelUpEffectService() { return combatContext.getLevelUpEffectService(); }
    
    @Deprecated
    public DamageEffectService getDamageEffectService() { return combatContext.getDamageEffectService(); }
    
    @Deprecated
    public EventEffectService getEventEffectService() { return combatContext.getEventEffectService(); }
    
    @Deprecated
    public EntityManager getEntityManager() { return entityContext.getEntityManager(); }
    
    @Deprecated
    public EntityRegistry getEntityRegistry() { return entityContext.getEntityRegistry(); }
    
    @Deprecated
    public EntityService getEntityService() { return entityContext.getEntityService(); }
    
    @Deprecated
    public BossManager getBossManager() { return entityContext.getBossManager(); }
    
    @Deprecated
    public BreakthroughService getBreakthroughService() { return cultivationContext.getBreakthroughService(); }
    
    @Deprecated
    public TitleService getTitleService() { return cultivationContext.getTitleService(); }
    
    @Deprecated
    public TribulationService getTribulationService() { return cultivationContext.getTribulationService(); }
    
    @Deprecated
    public PlayerUIService getPlayerUIService() { return uiContext.getPlayerUIService(); }
    
    @Deprecated
    public ScoreboardService getScoreboardService() { return uiContext.getScoreboardService(); }
    
    @Deprecated
    public NameplateService getNameplateService() { return uiContext.getNameplateService(); }
    
    @Deprecated
    public ChatBubbleService getChatBubbleService() { return uiContext.getChatBubbleService(); }
    
    @Deprecated
    public EntityNameplateService getEntityNameplateService() { return uiContext.getEntityNameplateService(); }
    
    @Deprecated
    public EntityDialogService getEntityDialogService() { return uiContext.getEntityDialogService(); }
    
    @Deprecated
    public UiStateService getUiStateService() { return uiContext.getUiStateService(); }
    
    @Deprecated
    public TribulationUI getTribulationUI() { return uiContext.getTribulationUI(); }
}