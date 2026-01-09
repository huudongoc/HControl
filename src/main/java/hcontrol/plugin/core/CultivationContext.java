package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.service.BreakthroughService;
import hcontrol.plugin.service.TitleService;
import hcontrol.plugin.service.TribulationService;

/**
 * CULTIVATION CONTEXT — Tu tien mechanics
 * Quan ly Breakthrough, Tribulation, Title
 */
public class CultivationContext {
    
    private final BreakthroughService breakthroughService;
    private final TitleService titleService;
    private final TribulationService tribulationService;
    
    public CultivationContext(Main plugin) {
        this.breakthroughService = new BreakthroughService();
        this.titleService = new TitleService();
        this.tribulationService = new TribulationService(plugin, breakthroughService);
    }
    
    // ========== GETTERS ==========
    
    public BreakthroughService getBreakthroughService() { return breakthroughService; }
    public TitleService getTitleService() { return titleService; }
    public TribulationService getTribulationService() { return tribulationService; }
}
