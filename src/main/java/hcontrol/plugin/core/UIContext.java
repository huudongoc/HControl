package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.ui.ChatBubbleService;
import hcontrol.plugin.ui.EntityDialogService;
import hcontrol.plugin.ui.EntityNameplateService;
import hcontrol.plugin.ui.NameplateService;
import hcontrol.plugin.ui.PlayerUIService;
import hcontrol.plugin.ui.ScoreboardService;
import hcontrol.plugin.ui.ScoreboardUpdateTask;
import hcontrol.plugin.ui.TribulationUI;
import hcontrol.plugin.ui.UiStateService;

/**
 * UI CONTEXT — Player UI + Entity UI + Tribulation UI
 * Quan ly tat ca service lien quan den UI/Visual
 */
public class UIContext {
    
    private final Main plugin;
    
    // Player UI
    private PlayerUIService playerUIService;
    private ScoreboardService scoreboardService;
    private NameplateService nameplateService;
    private ChatBubbleService chatBubbleService;
    
    // Entity UI
    private EntityNameplateService entityNameplateService;
    private EntityDialogService entityDialogService;
    
    // Tribulation UI
    private UiStateService uiStateService;
    private TribulationUI tribulationUI;
    
    // Runtime tasks
    private ScoreboardUpdateTask scoreboardUpdateTask;
    
    public UIContext(Main plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Init Player UI Services (goi tu registerPlayerSystem)
     */
    public void initPlayerUI(PlayerManager playerManager) {
        this.playerUIService = new PlayerUIService(playerManager);
        this.scoreboardService = new ScoreboardService(playerManager);
        this.nameplateService = new NameplateService(playerManager);
        this.chatBubbleService = new ChatBubbleService(plugin);
        
        // Tribulation UI
        this.uiStateService = new UiStateService();
        this.tribulationUI = new TribulationUI(plugin, uiStateService);
    }
    
    /**
     * Init Entity UI Services (goi tu registerCombatSystem)
     */
    public void initEntityUI(EntityManager entityManager) {
        this.entityNameplateService = new EntityNameplateService(entityManager, plugin);
        this.entityDialogService = new EntityDialogService(plugin);
    }
    
    // ========== GETTERS ==========
    
    public PlayerUIService getPlayerUIService() { return playerUIService; }
    public ScoreboardService getScoreboardService() { return scoreboardService; }
    public NameplateService getNameplateService() { return nameplateService; }
    public ChatBubbleService getChatBubbleService() { return chatBubbleService; }
    
    public EntityNameplateService getEntityNameplateService() { return entityNameplateService; }
    public EntityDialogService getEntityDialogService() { return entityDialogService; }
    
    public UiStateService getUiStateService() { return uiStateService; }
    public TribulationUI getTribulationUI() { return tribulationUI; }
    
    public ScoreboardUpdateTask getScoreboardUpdateTask() { return scoreboardUpdateTask; }
    
    // ========== SETTERS (cho lifecycle) ==========
    
    public void setScoreboardUpdateTask(ScoreboardUpdateTask scoreboardUpdateTask) {
        this.scoreboardUpdateTask = scoreboardUpdateTask;
    }
}
