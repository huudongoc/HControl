package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.service.DisplayFormatService;
import hcontrol.plugin.ui.chat.ChatBubbleService;
import hcontrol.plugin.ui.chat.ChatFormatService;
import hcontrol.plugin.ui.entity.EntityDialogService;
import hcontrol.plugin.ui.entity.EntityNameplateService;
import hcontrol.plugin.ui.player.NameplateService;
import hcontrol.plugin.ui.player.PlayerUIService;
import hcontrol.plugin.ui.player.ScoreboardService;
import hcontrol.plugin.ui.player.ScoreboardUpdateTask;
import hcontrol.plugin.ui.sect.SectWarBossBarService;
import hcontrol.plugin.ui.tribulation.TribulationUI;
import hcontrol.plugin.ui.tribulation.UiStateService;

/**
 * UI CONTEXT — Player UI + Entity UI + Tribulation UI
 * Quan ly tat ca service lien quan den UI/Visual
 */
public class UIContext {
    
    private final Main plugin;
    private final DisplayFormatService displayFormatService;
    
    // Player UI
    private PlayerUIService playerUIService;
    private ScoreboardService scoreboardService;
    private NameplateService nameplateService;
    private ChatBubbleService chatBubbleService;
    private ChatFormatService chatFormatService;
    
    // Entity UI
    private EntityNameplateService entityNameplateService;
    private EntityDialogService entityDialogService;
    
    // Tribulation UI
    private UiStateService uiStateService;
    private TribulationUI tribulationUI;
    
    // Sect UI
    private SectWarBossBarService sectWarBossBarService;
    
    // Runtime tasks
    private ScoreboardUpdateTask scoreboardUpdateTask;
    
    public UIContext(Main plugin) {
        this.plugin = plugin;
        this.displayFormatService = DisplayFormatService.getInstance();
    }
    
    /**
     * Init Player UI Services (goi tu registerPlayerSystem)
     * Can truyen vao CultivationProgressService tu PlayerContext
     */
    public void initPlayerUI(PlayerManager playerManager, hcontrol.plugin.service.CultivationProgressService cultivationProgressService) {
        this.playerUIService = new PlayerUIService(playerManager, displayFormatService, cultivationProgressService);
        this.scoreboardService = new ScoreboardService(playerManager, displayFormatService, cultivationProgressService);
        this.nameplateService = new NameplateService(playerManager);
        this.chatBubbleService = new ChatBubbleService(plugin);
        this.chatFormatService = new ChatFormatService();
        
        // Tribulation UI
        this.uiStateService = new UiStateService();
        this.tribulationUI = new TribulationUI(plugin, uiStateService);
        
        // Sect UI
        this.sectWarBossBarService = new SectWarBossBarService(plugin);
    }
    
    /**
     * Init Entity UI Services (goi tu registerCombatSystem)
     */
    public void initEntityUI(EntityManager entityManager) {
        this.entityNameplateService = new EntityNameplateService(entityManager, plugin, displayFormatService);
        this.entityDialogService = new EntityDialogService(plugin);

        entityNameplateService.startGlobalUpdater();
    }

    
    // ========== GETTERS ==========
    
    public DisplayFormatService getDisplayFormatService() { return displayFormatService; }
    
    public PlayerUIService getPlayerUIService() { return playerUIService; }
    public ScoreboardService getScoreboardService() { return scoreboardService; }
    public NameplateService getNameplateService() { return nameplateService; }
    public ChatBubbleService getChatBubbleService() { return chatBubbleService; }
    public ChatFormatService getChatFormatService() { return chatFormatService; }
    
    public EntityNameplateService getEntityNameplateService() { return entityNameplateService; }
    public EntityDialogService getEntityDialogService() { return entityDialogService; }
    
    public UiStateService getUiStateService() { return uiStateService; }
    public TribulationUI getTribulationUI() { return tribulationUI; }
    
    public SectWarBossBarService getSectWarBossBarService() { return sectWarBossBarService; }
    
    public ScoreboardUpdateTask getScoreboardUpdateTask() { return scoreboardUpdateTask; }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Update tat ca UI cho player (scoreboard + nameplate)
     * Helper method de tranh duplicate code
     * Note: Dùng Object thay vì Player để tránh import Bukkit trong core
     * Caller sẽ cast về Player trước khi gọi (trong command/listener layer)
     */
    public void updateAllUI(Object player) {
        if (scoreboardService != null && player instanceof org.bukkit.entity.Player) {
            scoreboardService.updateScoreboard((org.bukkit.entity.Player) player);
        }
        if (nameplateService != null && player instanceof org.bukkit.entity.Player) {
            nameplateService.updateNameplate((org.bukkit.entity.Player) player);
        }
    }
    
    // ========== SETTERS (cho lifecycle) ==========
    
    public void setScoreboardUpdateTask(ScoreboardUpdateTask scoreboardUpdateTask) {
        this.scoreboardUpdateTask = scoreboardUpdateTask;
    }
}
