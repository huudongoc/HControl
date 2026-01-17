package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.service.AscensionService;
import hcontrol.plugin.service.BreakthroughService;
import hcontrol.plugin.service.RoleService;
import hcontrol.plugin.service.TitleService;
import hcontrol.plugin.service.TribulationLogicService;
import hcontrol.plugin.service.TribulationService;

/**
 * CULTIVATION CONTEXT — Tu tien mechanics
 * Quan ly Breakthrough, Tribulation, Title, Role, Ascension (ENDGAME)
 */
public class CultivationContext {
    
    private final BreakthroughService breakthroughService;
    private final TitleService titleService;
    private final TribulationService tribulationService;
    private final TribulationLogicService tribulationLogicService;
    private final RoleService roleService;
    private final AscensionService ascensionService;  // ENDGAME
    
    public CultivationContext(Main plugin) {
        this.breakthroughService = new BreakthroughService();
        this.titleService = new TitleService();
        this.tribulationLogicService = new TribulationLogicService();
        this.tribulationService = new TribulationService(plugin, breakthroughService);
        this.roleService = new RoleService(titleService);
        this.ascensionService = new AscensionService();  // ENDGAME
    }
    
    // ========== GETTERS ==========
    
    public BreakthroughService getBreakthroughService() { return breakthroughService; }
    public TitleService getTitleService() { return titleService; }
    public TribulationService getTribulationService() { return tribulationService; }
    public TribulationLogicService getTribulationLogicService() { return tribulationLogicService; }
    public RoleService getRoleService() { return roleService; }
    public AscensionService getAscensionService() { return ascensionService; }  // ENDGAME
}
