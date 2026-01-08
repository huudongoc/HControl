package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.player.AutoSaveTask;
import hcontrol.plugin.player.LevelService;
import hcontrol.plugin.player.PlayerHealthService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerStorage;
import hcontrol.plugin.service.StatService;

/**
 * PLAYER CONTEXT — PHASE 1
 * Quan ly tat ca service lien quan den Player
 */
public class PlayerContext {
    
    private final Main plugin;
    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;
    private LevelService levelService;  // Khong final de co the inject sau
    private final PlayerHealthService playerHealthService;
    private final StatService statService;
    
    // Runtime tasks
    private AutoSaveTask autoSaveTask;
    
    public PlayerContext(Main plugin) {
        this.plugin = plugin;
        this.playerManager = new PlayerManager();
        this.playerStorage = new PlayerStorage(plugin.getDataFolder());
        this.playerHealthService = new PlayerHealthService();
        this.statService = new StatService();
        this.levelService = null;  // Inject sau khi CombatContext da tao
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
        this.levelService = levelService;
    }
    
    // ========== GETTERS ==========
    
    public Main getPlugin() { return plugin; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public PlayerStorage getPlayerStorage() { return playerStorage; }
    public LevelService getLevelService() { return levelService; }
    public PlayerHealthService getPlayerHealthService() { return playerHealthService; }
    public StatService getStatService() { return statService; }
    public AutoSaveTask getAutoSaveTask() { return autoSaveTask; }
    
    // ========== SETTERS (cho lifecycle + dependency injection) ==========
    
    public void setLevelService(LevelService levelService) {
        this.levelService = levelService;
    }
    
    public void setAutoSaveTask(AutoSaveTask autoSaveTask) {
        this.autoSaveTask = autoSaveTask;
    }
}
