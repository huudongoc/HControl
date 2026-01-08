package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.service.CombatService;
import hcontrol.plugin.service.DamageEffectService;
import hcontrol.plugin.service.EventEffectService;
import hcontrol.plugin.service.LevelUpEffectService;
import hcontrol.plugin.service.SoundService;

/**
 * COMBAT CONTEXT — PHASE 3
 * Quan ly tat ca service lien quan den Combat
 */
public class CombatContext {
    
    private final SoundService soundService;
    private final LevelUpEffectService levelUpEffectService;
    private final DamageEffectService damageEffectService;
    private final EventEffectService eventEffectService;
    private final CombatService combatService;
    
    public CombatContext(Main plugin, PlayerManager playerManager, EntityManager entityManager) {
        this.soundService = new SoundService();
        this.levelUpEffectService = new LevelUpEffectService(soundService);
        this.damageEffectService = new DamageEffectService(soundService);
        this.eventEffectService = new EventEffectService(soundService);
        this.combatService = new CombatService(playerManager, plugin, damageEffectService, entityManager);
    }
    
    // ========== GETTERS ==========
    
    public SoundService getSoundService() { return soundService; }
    public LevelUpEffectService getLevelUpEffectService() { return levelUpEffectService; }
    public DamageEffectService getDamageEffectService() { return damageEffectService; }
    public EventEffectService getEventEffectService() { return eventEffectService; }
    public CombatService getCombatService() { return combatService; }
}
