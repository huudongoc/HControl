package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.command.CommandRegistry;
import hcontrol.plugin.command.MasterCommand;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityRegistry;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.listener.EntityLifecycleListener;
import hcontrol.plugin.listener.EventRegistry;
import hcontrol.plugin.listener.JoinServerListener;
import hcontrol.plugin.listener.OutServerListener;
import hcontrol.plugin.listener.PlayerChatListener;
import hcontrol.plugin.listener.PlayerCombatListener;
import hcontrol.plugin.listener.PlayerDeathListener;
import hcontrol.plugin.listener.PlayerRespawnListener;
import hcontrol.plugin.listener.PlayerSkillHotbarListener;
import hcontrol.plugin.module.boss.BossManager;
import hcontrol.plugin.master.MasterManager;
import hcontrol.plugin.master.MasterService;
import hcontrol.plugin.listener.SkillCreatorListener;
import hcontrol.plugin.skill.custom.SkillInstanceManager;
import hcontrol.plugin.skill.custom.SkillTemplateRegistry;
import hcontrol.plugin.sect.SectManager;
import hcontrol.plugin.sect.SectService;
import hcontrol.plugin.ui.skill.SkillMenuGUI;
import hcontrol.plugin.ui.skill.SkillMenuListener;
import hcontrol.plugin.playerskill.SkillBookService;
import hcontrol.plugin.listener.SkillBookListener;
import hcontrol.plugin.listener.PlayerSkillProjectileListener;
import hcontrol.plugin.command.SkillBookCommand;
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
import hcontrol.plugin.service.SpiritualRootService;
import hcontrol.plugin.service.StatService;
import hcontrol.plugin.service.TitleService;
import hcontrol.plugin.service.TribulationLogicService;
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
 * PHASE 0 — TRUCCO (REFACTORED)
 * Singleton context CHI chua SubContext
 * Moi SubContext quan ly services theo domain rieng
 */
public class CoreContext {
    private static CoreContext instance;
    
    // System-level dependencies
    private final Main plugin;
    private final LifecycleManager lifecycleManager;
    private EventRegistry eventRegistry;
    
    // Domain Contexts (7 SubContext)
    private final PlayerContext playerContext;
    private final CombatContext combatContext;
    private final EntityContext entityContext;
    private final UIContext uiContext;
    private final CultivationContext cultivationContext;
    private final ItemContext itemContext;  // PHASE 8A
    private final ClassContext classContext;  // PHASE 5
    
    // Listeners (lifecycle-managed, KHONG nen de trong SubContext)
    private JoinServerListener joinListener;
    private OutServerListener outListener;
    private PlayerCombatListener combatListener;
    private PlayerDeathListener deathListener;
    private PlayerChatListener chatListener;
    private PlayerRespawnListener respawnListener;
    private EntityLifecycleListener entityLifecycleListener;
    private TribulationInputListener tribulationInputListener;
    private PlayerSkillHotbarListener skillHotbarListener;
    private SkillMenuListener skillMenuListener;
    private SkillBookListener skillBookListener;
    private SkillBookService skillBookService;
    private PlayerSkillProjectileListener skillProjectileListener;
    
    // Sect System
    private SectManager sectManager;
    private SectService sectService;
    
    // Master-Disciple System
    private MasterManager masterManager;
    private MasterService masterService;
    
    // Custom Skill System (NEW ARCHITECTURE)
    private SkillTemplateRegistry skillTemplateRegistry;
    private SkillInstanceManager skillInstanceManager;
    private SkillCreatorListener skillCreatorListener;
    
    private CoreContext(Main plugin, LifecycleManager lifecycleManager) {
        this.plugin = plugin;
        this.lifecycleManager = lifecycleManager;
        
        // Khoi tao SubContexts theo thu tu phu thuoc
        // 1. Entity Context (khong phu thuoc ai)
        this.entityContext = new EntityContext();
        
        // 2. Player Context (khong phu thuoc ai, nhung LevelService se inject sau)
        this.playerContext = new PlayerContext(plugin);
        
        // 3. Combat Context (phu thuoc PlayerManager, EntityManager, BossManager)
        this.combatContext = new CombatContext(
            plugin,
            playerContext.getPlayerManager(),
            entityContext.getEntityManager(),
            entityContext.getBossManager()
        );
        
        // 4. Inject LevelService vao PlayerContext (sau khi CombatContext da co LevelUpEffectService)
        LevelService levelService = new LevelService(combatContext.getLevelUpEffectService());
        playerContext.setLevelService(levelService);
        
        // 5. Cultivation Context (phu thuoc Plugin)
        this.cultivationContext = new CultivationContext(plugin);
        
        // 6. UI Context (init sau trong lifecycle callbacks)
        this.uiContext = new UIContext(plugin);
        
        // 7. Item Context (PHASE 8A - phu thuoc Plugin)
        this.itemContext = new ItemContext(plugin);
        
        // 8. Class Context (PHASE 5 - phu thuoc Plugin)
        this.classContext = new ClassContext(plugin);
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
        registerClassSystem();     // PHASE 5
        // registerSkillSystem();     // PHASE 6 (thêm vào CombatContext)
        registerItemSystem();      // PHASE 8A
        // registerWorldSystem();     // PHASE 9
        
        // SECT SYSTEM
        registerSectSystem();
        
        // MASTER-DISCIPLE SYSTEM
        registerMasterSystem();
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
            
            // Inject dependencies vào NameplateService (sau khi sect/master system init)
            // Sẽ được set trong registerSectSystem và registerMasterSystem
            
            // Register NameplateListener (lắng nghe PlayerStateChangeEvent)
            hcontrol.plugin.listener.NameplateListener nameplateListener = new hcontrol.plugin.listener.NameplateListener(
                plugin,
                uiContext.getNameplateService()
            );
            eventRegistry.registerEvents(nameplateListener);
            plugin.getLogger().info("[PHASE 1] ✓ NameplateListener registered");
            
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
            // 🔥 Sử dụng TribulationService để xử lý logic thiên kiếp
            tribulationInputListener = new TribulationInputListener(
                uiContext.getUiStateService(),
                uiContext.getTribulationUI(),
                cultivationContext.getTribulationService(),
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
            
            // Shutdown SectWarBossBarService
            if (uiContext.getSectWarBossBarService() != null) {
                uiContext.getSectWarBossBarService().shutdown();
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
            
            // PHASE 8A: ItemService sẽ được inject trong registerItemSystem() (sau khi ItemContext initialize)
            
            // Inject NameplateService vao TitleService
            // TitleService không cần inject NameplateService nữa - dùng event
            
            // Register CombatListener
            combatListener = new PlayerCombatListener(
                playerContext.getPlayerManager(), 
                combatContext.getCombatService(),
                combatContext.getDisableDameService()
            );
            eventRegistry.registerEvents(combatListener);
            
            // Register DeathListener (inject DeathService va DeathMessageService)
            deathListener = new PlayerDeathListener(
                playerContext.getPlayerManager(),
                combatContext.getDeathService(),
                combatContext.getDeathMessageService()
            );
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
            
            // PHASE 7: Init AI System
            entityContext.initAI(plugin);
            if (entityContext.getAIService() != null) {
                entityContext.getAIService().start();
                plugin.getLogger().info("[PHASE 7] ✓ AI System đã khởi động!");
            }
            
            // PHASE 7.2: Init Mob Skill System
            entityContext.initSkills(combatContext.getCombatService());
            plugin.getLogger().info("[PHASE 7.2] ✓ Mob Skill System đã khởi động!");
            
            // WORLD BOSS SYSTEM - ENDGAME (sau khi có Ascension)
            entityContext.initWorldBoss(
                plugin,
                playerContext.getPlayerManager(),
                cultivationContext.getAscensionService(),
                combatContext.getCombatService()
            );
            plugin.getLogger().info("[World Boss] ✓ World Boss System đã khởi động!");
            
            // PHASE 6: Init Player Skill System
            playerContext.initSkillSystem(
                combatContext.getCombatService(),
                combatContext.getIdentityRuleService()
            );
            plugin.getLogger().info("[PHASE 6] ✓ Player Skill System đã khởi động!");
            
            // PHASE 6: Register Skill Command + GUI (sau khi service đã init)
            CommandRegistry skillCmdRegistry = new CommandRegistry(plugin, this);
            SkillMenuGUI skillMenuGUI = skillCmdRegistry.registerSkillCommand();
            
            // PHASE 6: Register PlayerSkillHotbarListener (để dùng skill bằng phím F)
            skillHotbarListener = new PlayerSkillHotbarListener(
                playerContext.getPlayerManager(),
                playerContext.getSkillService()
            );
            eventRegistry.registerEvents(skillHotbarListener);
            plugin.getLogger().info("[PHASE 6] ✓ Skill Hotbar Listener đã đăng ký!");
            
            // PHASE 6: Register SkillMenuListener (để xử lý click trong GUI)
            if (skillMenuGUI != null) {
                skillMenuListener = new SkillMenuListener(
                    playerContext.getPlayerManager(),
                    playerContext.getSkillService(),
                    skillMenuGUI,
                    plugin
                );
                eventRegistry.registerEvents(skillMenuListener);
                plugin.getLogger().info("[PHASE 6] ✓ Skill Menu Listener đã đăng ký!");
            }
            
            // PHASE 6: Skill Book System
            skillBookService = new SkillBookService(plugin, playerContext.getSkillRegistry());
            skillBookListener = new SkillBookListener(
                playerContext.getPlayerManager(),
                playerContext.getSkillService(),
                skillBookService
            );
            eventRegistry.registerEvents(skillBookListener);
            
            // Register skillbook command
            org.bukkit.command.PluginCommand skillbookCmd = plugin.getCommand("skillbook");
            if (skillbookCmd != null) {
                SkillBookCommand sbCmd = new SkillBookCommand(skillBookService, playerContext.getSkillRegistry());
                skillbookCmd.setExecutor(sbCmd);
                skillbookCmd.setTabCompleter(sbCmd);
            }
            plugin.getLogger().info("[PHASE 6] ✓ Skill Book System đã khởi động!");
            
            // PHASE 6: Register PlayerSkillProjectileListener (xử lý ranged skill hit)
            // PHASE 5A: Truyền CombatService để class modifiers hoạt động
            skillProjectileListener = new PlayerSkillProjectileListener(
                plugin,
                playerContext.getPlayerManager(),
                playerContext.getSkillRegistry(),
                combatContext.getCombatService()
            );
            eventRegistry.registerEvents(skillProjectileListener);
            plugin.getLogger().info("[PHASE 6] ✓ Skill Projectile Listener đã đăng ký!");
            
            lifecycleManager.enableModule("CombatSystem");
            plugin.getLogger().info("[PHASE 3] ✓ Combat System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 3] Đang tắt Combat System...");
            
            // PHASE 7: Stop AI System
            if (entityContext.getAIService() != null) {
                entityContext.getAIService().stop();
                plugin.getLogger().info("[PHASE 7] ✓ AI System đã tắt!");
            }
            
            // WORLD BOSS SYSTEM: Shutdown
            if (entityContext.getWorldBossManager() != null) {
                entityContext.getWorldBossManager().shutdown();
                plugin.getLogger().info("[World Boss] ✓ World Boss System đã tắt!");
            }
            
            // Clear listeners
            combatListener = null;
            deathListener = null;
            entityLifecycleListener = null;
            skillHotbarListener = null;
            skillMenuListener = null;
            skillBookListener = null;
            skillBookService = null;
            skillProjectileListener = null;
            
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
    
    /**
     * PHASE 8A — ITEM SYSTEM
     */
    private void registerItemSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 8A] Đang khởi tạo Item System...");
            
            // Initialize ItemContext (load items.yml, registry, cache)
            itemContext.initialize();
            
            // Inject ItemService vào CombatService (nếu chưa inject trong registerCombatSystem)
            if (itemContext.getItemService() != null && combatContext.getCombatService() != null) {
                combatContext.getCombatService().setItemService(itemContext.getItemService());
                plugin.getLogger().info("[PHASE 8A] ✓ ItemService đã inject vào CombatService");
            }
            
            lifecycleManager.enableModule("ItemSystem");
            plugin.getLogger().info("[PHASE 8A] ✓ Item System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 8A] Đang tắt Item System...");
            
            // Clear cache if needed (ItemContext không có state cần clear)
            
            lifecycleManager.disableModule("ItemSystem");
            plugin.getLogger().info("[PHASE 8A] ✓ Item System đã tắt!");
        });
    }
    
    /**
     * PHASE 5 — CLASS SYSTEM
     */
    private void registerClassSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[PHASE 5] Đang khởi tạo Class System...");
            
            // Initialize ClassContext
            classContext.initialize();
            
            // Inject ClassService vào CombatService
            if (classContext.getClassService() != null && combatContext.getCombatService() != null) {
                combatContext.getCombatService().setClassService(classContext.getClassService());
                plugin.getLogger().info("[PHASE 5] ✓ ClassService đã inject vào CombatService");
            }
            
            lifecycleManager.enableModule("ClassSystem");
            plugin.getLogger().info("[PHASE 5] ✓ Class System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[PHASE 5] Đang tắt Class System...");
            
            lifecycleManager.disableModule("ClassSystem");
            plugin.getLogger().info("[PHASE 5] ✓ Class System đã tắt!");
        });
    }
    
    /**
     * SECT SYSTEM — Môn Phái
     */
    private void registerSectSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[SECT] Đang khởi tạo Sect System...");
            
            // Init SectManager (CRUD + Storage)
            sectManager = new SectManager(plugin);
            
            // Init SectService (Business Logic)
            sectService = new SectService(sectManager, playerContext.getPlayerManager());
            
            // Register SectCommand
            CommandRegistry sectCmdRegistry = new CommandRegistry(plugin, this);
            sectCmdRegistry.registerSectCommand(sectService);
            
            // Inject SectManager vào NameplateService
            if (uiContext != null && uiContext.getNameplateService() != null) {
                uiContext.getNameplateService().setSectManager(sectManager);
                plugin.getLogger().info("[SECT] ✓ Đã inject SectManager vào NameplateService");
            }
            
            // Inject NameplateService và SectManager vào ChatFormatService (PHASE 5: reuse data)
            if (uiContext != null && uiContext.getChatFormatService() != null) {
                if (uiContext.getNameplateService() != null) {
                    uiContext.getChatFormatService().setNameplateService(uiContext.getNameplateService());
                }
                uiContext.getChatFormatService().setSectManager(sectManager);
                plugin.getLogger().info("[SECT] ✓ Đã inject SectManager vào ChatFormatService (bubble format)");
            }
            
            // Register SectWarListener (lắng nghe SectWarStartEvent)
            if (uiContext != null && uiContext.getSectWarBossBarService() != null) {
                hcontrol.plugin.listener.SectWarListener sectWarListener = 
                    new hcontrol.plugin.listener.SectWarListener(uiContext.getSectWarBossBarService());
                eventRegistry.registerEvents(sectWarListener);
                plugin.getLogger().info("[SECT] ✓ SectWarListener registered");
            }
            
            lifecycleManager.enableModule("SectSystem");
            plugin.getLogger().info("[SECT] ✓ Sect System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[SECT] Đang tắt Sect System...");
            
            // Save data
            if (sectManager != null) {
                sectManager.saveData();
            }
            
            sectManager = null;
            sectService = null;
            
            lifecycleManager.disableModule("SectSystem");
            plugin.getLogger().info("[SECT] ✓ Sect System đã tắt!");
        });
    }
    
    /**
     * MASTER-DISCIPLE SYSTEM — Bái Sư + Custom Skill Creation
     */
    private void registerMasterSystem() {
        lifecycleManager.registerOnEnable(() -> {
            plugin.getLogger().info("[MASTER] Đang khởi tạo Master-Disciple System...");
            
            // Init MasterManager (CRUD + Storage)
            masterManager = new MasterManager(plugin);
            
            // Init MasterService (Business Logic)
            masterService = new MasterService(
                masterManager, 
                playerContext.getPlayerManager(),
                playerContext.getSkillService(),
                playerContext.getSkillRegistry()
            );
            
            // ===== NEW CUSTOM SKILL ARCHITECTURE =====
            // 1. SkillTemplateRegistry - quản lý bản thiết kế skill
            skillTemplateRegistry = new SkillTemplateRegistry(plugin);
            plugin.getLogger().info("[MASTER] ✓ SkillTemplateRegistry loaded " + 
                skillTemplateRegistry.getTemplateCount() + " custom templates");
            
            // 2. SkillInstanceManager - quản lý skill đã học của mỗi người
            skillInstanceManager = new SkillInstanceManager(plugin, skillTemplateRegistry);
            plugin.getLogger().info("[MASTER] ✓ SkillInstanceManager initialized");
            
            // 3. SkillCreatorListener - xử lý GUI tạo skill mới
            skillCreatorListener = new SkillCreatorListener(
                plugin,
                skillTemplateRegistry,
                skillInstanceManager
            );
            eventRegistry.registerEvents(skillCreatorListener);
            plugin.getLogger().info("[MASTER] ✓ SkillCreatorListener registered");
            
            // Inject MasterManager vào NameplateService
            if (uiContext != null && uiContext.getNameplateService() != null) {
                uiContext.getNameplateService().setMasterManager(masterManager);
                plugin.getLogger().info("[MASTER] ✓ Đã inject MasterManager vào NameplateService");
            }
            
            // Inject MasterManager vào ChatFormatService (bubble format)
            if (uiContext != null && uiContext.getChatFormatService() != null) {
                uiContext.getChatFormatService().setMasterManager(masterManager);
                plugin.getLogger().info("[MASTER] ✓ Đã inject MasterManager vào ChatFormatService (bubble format)");
            }
            
            // Register MasterCommand với SkillCreatorListener
            MasterCommand masterCmd = new MasterCommand(masterService, playerContext.getPlayerManager());
            masterCmd.setSkillCreatorListener(skillCreatorListener);
            
            org.bukkit.command.PluginCommand cmd = plugin.getCommand("master");
            if (cmd != null) {
                cmd.setExecutor(masterCmd);
                cmd.setTabCompleter(masterCmd);
                plugin.getLogger().info("[MASTER] ✓ Master command đã được đăng ký!");
            }
            
            // Register AdminSkillCommand (backdoor)
            hcontrol.plugin.command.AdminSkillCommand adminSkillCmd = new hcontrol.plugin.command.AdminSkillCommand();
            org.bukkit.command.PluginCommand askillCmd = plugin.getCommand("askill");
            if (askillCmd != null) {
                askillCmd.setExecutor(adminSkillCmd);
                askillCmd.setTabCompleter(adminSkillCmd);
                plugin.getLogger().info("[MASTER] ✓ Admin Skill command đã được đăng ký!");
            }
            
            // Register HControlAdminCommand (backdoor tổng hợp)
            hcontrol.plugin.command.HControlAdminCommand hadminCmd = new hcontrol.plugin.command.HControlAdminCommand();
            org.bukkit.command.PluginCommand hadminPluginCmd = plugin.getCommand("hadmin");
            if (hadminPluginCmd != null) {
                hadminPluginCmd.setExecutor(hadminCmd);
                hadminPluginCmd.setTabCompleter(hadminCmd);
                plugin.getLogger().info("[MASTER] ✓ HControl Admin command đã được đăng ký!");
            }
            
            // Register NameplateCommand
            if (uiContext != null && uiContext.getNameplateService() != null) {
                hcontrol.plugin.command.NameplateCommand npCmd = new hcontrol.plugin.command.NameplateCommand(uiContext.getNameplateService());
                org.bukkit.command.PluginCommand npPluginCmd = plugin.getCommand("nameplate");
                if (npPluginCmd != null) {
                    npPluginCmd.setExecutor(npCmd);
                    npPluginCmd.setTabCompleter(npCmd);
                    plugin.getLogger().info("[MASTER] ✓ Nameplate command đã được đăng ký!");
                }
            }
            
            lifecycleManager.enableModule("MasterSystem");
            plugin.getLogger().info("[MASTER] ✓ Master-Disciple System đã sẵn sàng!");
        });
        
        lifecycleManager.registerOnDisable(() -> {
            plugin.getLogger().info("[MASTER] Đang tắt Master-Disciple System...");
            
            // Save data
            if (masterManager != null) {
                masterManager.saveData();
            }
            if (skillTemplateRegistry != null) {
                skillTemplateRegistry.saveData();
            }
            if (skillInstanceManager != null) {
                skillInstanceManager.saveData();
            }
            
            masterManager = null;
            masterService = null;
            skillTemplateRegistry = null;
            skillInstanceManager = null;
            skillCreatorListener = null;
            
            lifecycleManager.disableModule("MasterSystem");
            plugin.getLogger().info("[MASTER] ✓ Master-Disciple System đã tắt!");
        });
    }

    // ===== GETTERS — SubContext (PRIMARY) =====
    
    public PlayerContext getPlayerContext() { return playerContext; }
    public CombatContext getCombatContext() { return combatContext; }
    public EntityContext getEntityContext() { return entityContext; }
    public UIContext getUIContext() { return uiContext; }
    public CultivationContext getCultivationContext() { return cultivationContext; }
    public ItemContext getItemContext() { return itemContext; }  // PHASE 8A
    public ClassContext getClassContext() { return classContext; }  // PHASE 5
    
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
    
    public hcontrol.plugin.module.boss.WorldBossManager getWorldBossManager() { 
        return entityContext.getWorldBossManager(); 
    }
    
    @Deprecated
    public BreakthroughService getBreakthroughService() { return cultivationContext.getBreakthroughService(); }
    
    @Deprecated
    public TitleService getTitleService() { return cultivationContext.getTitleService(); }
    
    @Deprecated
    public TribulationService getTribulationService() { return cultivationContext.getTribulationService(); }
    
    public TribulationLogicService getTribulationLogicService() { return cultivationContext.getTribulationLogicService(); }
    
    public SpiritualRootService getSpiritualRootService() { return playerContext.getSpiritualRootService(); }
    
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
    
    // ===== PHASE 7: AI System getters =====
    
    public hcontrol.plugin.ai.AIService getAIService() { 
        return entityContext.getAIService(); 
    }
    
    public hcontrol.plugin.ai.BrainRegistry getBrainRegistry() { 
        return entityContext.getBrainRegistry(); 
    }
    
    // ===== PHASE 7.2: Skill System getters =====
    
    public hcontrol.plugin.skill.SkillRegistry getSkillRegistry() {
        return entityContext.getSkillRegistry();
    }
    
    public hcontrol.plugin.skill.SkillExecutor getSkillExecutor() {
        return entityContext.getSkillExecutor();
    }
    
    public hcontrol.plugin.skill.SkillCooldownManager getCooldownManager() {
        return entityContext.getCooldownManager();
    }
    
    // ===== MASTER-DISCIPLE SYSTEM GETTERS =====
    
    public MasterManager getMasterManager() { return masterManager; }
    public MasterService getMasterService() { return masterService; }
    
    // ===== CUSTOM SKILL SYSTEM GETTERS (NEW ARCHITECTURE) =====
    
    public SkillTemplateRegistry getSkillTemplateRegistry() { return skillTemplateRegistry; }
    public SkillInstanceManager getSkillInstanceManager() { return skillInstanceManager; }
    public SkillCreatorListener getSkillCreatorListener() { return skillCreatorListener; }
    
    // ===== SECT SYSTEM GETTERS =====
    
    public SectManager getSectManager() { return sectManager; }
    public SectService getSectService() { return sectService; }
}