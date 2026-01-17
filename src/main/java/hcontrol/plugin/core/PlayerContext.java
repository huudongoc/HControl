package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.player.AutoSaveTask;
import hcontrol.plugin.playerskill.PlayerSkillExecutor;
import hcontrol.plugin.playerskill.PlayerSkillRegistry;
import hcontrol.plugin.playerskill.PlayerSkillService;
import hcontrol.plugin.service.LevelService;
import hcontrol.plugin.service.PlayerHealthService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.service.CultivationProgressService;
import hcontrol.plugin.service.SpiritualRootService;
import hcontrol.plugin.service.StatService;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

/**
 * PLAYER CONTEXT — PHASE 1 + PHASE 6
 * Quan ly tat ca service lien quan den Player
 * PHASE 6: Player Skill System
 */
public class PlayerContext {
    
    private final Main plugin;
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private LevelService levelService;  // Khong final de co the inject sau
    private CultivationProgressService cultivationProgressService;  // Khong final de co the inject sau
    private final PlayerHealthService playerHealthService;
    private final StatService statService;
    private final SpiritualRootService spiritualRootService;
    
    // PHASE 6: Player Skill System
    private final PlayerSkillRegistry skillRegistry;
    private PlayerSkillService skillService;  // init sau (can IdentityRuleService)
    private PlayerSkillExecutor skillExecutor;  // init sau (can CombatService)
    
    // Runtime tasks
    private AutoSaveTask autoSaveTask;
    
    public PlayerContext(Main plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager();
        this.playerStorage = new PlayerStorage(plugin.getDataFolder());
        this.playerHealthService = new PlayerHealthService();
        this.statService = new StatService();
        this.spiritualRootService = new SpiritualRootService();
        this.levelService = null;  // Inject sau khi CombatContext da tao
        this.cultivationProgressService = null;  // Inject sau khi co LevelService
        
        // PHASE 6: Skill Registry (load từ YAML, fallback to defaults)
        this.skillRegistry = new PlayerSkillRegistry();
        loadSkillsFromConfig();
    }
    
    /**
     * Constructor voi LevelService dependency injection
     */
    public PlayerContext(Main plugin, LevelService levelService) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager();
        this.playerStorage = new PlayerStorage(plugin.getDataFolder());
        this.playerHealthService = new PlayerHealthService();
        this.statService = new StatService();
        this.spiritualRootService = new SpiritualRootService();
        this.levelService = levelService;
        
        // PHASE 6: Skill Registry (load từ YAML, fallback to defaults)
        this.skillRegistry = new PlayerSkillRegistry();
        loadSkillsFromConfig();
    }
    
    /**
     * Load skills từ player-skills.yml
     */
    private void loadSkillsFromConfig() {
        File skillsFile = new File(plugin.getDataFolder(), "player-skills.yml");
        
        // Save default config nếu chưa tồn tại
        if (!skillsFile.exists()) {
            plugin.saveResource("player-skills.yml", false);
        }
        
        // Load từ YAML
        if (skillsFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(skillsFile);
            skillRegistry.loadFromConfig(config);
            plugin.getLogger().info("[PHASE 6] Đã load " + skillRegistry.size() + " skills từ YAML");
        }
        
        // Fallback nếu YAML rỗng hoặc lỗi
        if (skillRegistry.size() == 0) {
            skillRegistry.registerDefaultSkills();
            plugin.getLogger().info("[PHASE 6] Dùng default skills (fallback)");
        }
    }
    
    /**
     * PHASE 6: Init Skill System
     * Goi sau khi CombatContext da tao (can CombatService + IdentityRuleService)
     */
    public void initSkillSystem(hcontrol.plugin.service.CombatService combatService, 
                                 hcontrol.plugin.identity.IdentityRuleService identityRules) {
        this.skillExecutor = new PlayerSkillExecutor(combatService, plugin);
        this.skillService = new PlayerSkillService(playerManager, skillRegistry, identityRules, skillExecutor);
    }
    
    // ========== GETTERS ==========
    
    public Main getPlugin() { return plugin; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public PlayerStorage getPlayerStorage() { return playerStorage; }
    public LevelService getLevelService() { return levelService; }
    public CultivationProgressService getCultivationProgressService() { return cultivationProgressService; }
    public PlayerHealthService getPlayerHealthService() { return playerHealthService; }
    public StatService getStatService() { return statService; }
    public SpiritualRootService getSpiritualRootService() { return spiritualRootService; }
    public AutoSaveTask getAutoSaveTask() { return autoSaveTask; }
    
    // PHASE 6: Skill System
    public PlayerSkillRegistry getSkillRegistry() { return skillRegistry; }
    public PlayerSkillService getSkillService() { return skillService; }
    public PlayerSkillExecutor getSkillExecutor() { return skillExecutor; }
    
    // ========== SETTERS (cho lifecycle + dependency injection) ==========
    
    public void setLevelService(LevelService levelService) {
        this.levelService = levelService;
        // Khi set LevelService, tao luon CultivationProgressService
        if (this.cultivationProgressService == null) {
            this.cultivationProgressService = new CultivationProgressService(levelService);
        }
    }
    
    public void setAutoSaveTask(AutoSaveTask autoSaveTask) {
        this.autoSaveTask = autoSaveTask;
    }
}
