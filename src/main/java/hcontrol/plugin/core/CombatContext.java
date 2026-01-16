package hcontrol.plugin.core;

import hcontrol.plugin.Main;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.module.boss.BossManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.service.CombatService;
import hcontrol.plugin.service.DamageEffectService;
import hcontrol.plugin.service.DeathMessageConfig;
import hcontrol.plugin.service.DeathMessageService;
import hcontrol.plugin.service.DeathService;
import hcontrol.plugin.service.DisableDameService;
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
    private final DisableDameService disableDameService;
    private final CombatService combatService;
    private final DeathService deathService;
    private final DeathMessageConfig deathMessageConfig;
    private final DeathMessageService deathMessageService;
    
    public CombatContext(Main plugin, PlayerManager playerManager, EntityManager entityManager, BossManager bossManager) {
        this.soundService = new SoundService();
        this.levelUpEffectService = new LevelUpEffectService(soundService);
        this.damageEffectService = new DamageEffectService(soundService);
        this.eventEffectService = new EventEffectService(soundService);
        this.disableDameService = new DisableDameService();
        this.combatService = new CombatService(playerManager, plugin, damageEffectService, entityManager);
        
        // Death system services - truyen combatService vao DeathService
        this.deathService = new DeathService(entityManager, bossManager, playerManager, this.combatService);
        this.deathMessageConfig = new DeathMessageConfig(plugin);
        this.deathMessageService = new DeathMessageService(deathMessageConfig);
    }
    
    // ========== GETTERS ==========
    
    public SoundService getSoundService() { return soundService; }
    public LevelUpEffectService getLevelUpEffectService() { return levelUpEffectService; }
    public DamageEffectService getDamageEffectService() { return damageEffectService; }
    public EventEffectService getEventEffectService() { return eventEffectService; }
    public DisableDameService getDisableDameService() { return disableDameService; }
    public CombatService getCombatService() { return combatService; }
    public DeathService getDeathService() { return deathService; }
    public DeathMessageService getDeathMessageService() { return deathMessageService; }
}
