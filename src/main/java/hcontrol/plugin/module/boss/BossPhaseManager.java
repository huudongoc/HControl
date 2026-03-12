package hcontrol.plugin.module.boss;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * BOSS PHASE MANAGER
 * Quản lý phase transitions và special abilities theo ENGINE_RULES
 * 
 * Phase System:
 * - Phase 1: 100% - 75% HP
 * - Phase 2: 75% - 50% HP
 * - Phase 3: 50% - 25% HP
 * - Phase 4 (Berserk): 25% - 0% HP
 * 
 * Special Abilities:
 * - Modifier-based design (không hard-code abilities)
 * - Phase-specific modifiers
 * - Scaling theo boss ascension level
 */
public class BossPhaseManager {
    
    private final BossEntity boss;
    private int currentPhase;
    private final int maxPhases;
    private final Set<Integer> triggeredPhases;
    private final int bossAscensionLevel;
    
    // Phase thresholds (HP percentage)
    private static final double[] PHASE_THRESHOLDS = {
        1.0,   // Phase 1: 100%
        0.75,  // Phase 2: 75%
        0.50,  // Phase 3: 50%
        0.25   // Phase 4: 25% (Berserk)
    };
    
    public BossPhaseManager(BossEntity boss, int bossAscensionLevel) {
        this.boss = boss;
        this.bossAscensionLevel = bossAscensionLevel;
        this.maxPhases = 4;
        this.currentPhase = 1;
        this.triggeredPhases = new HashSet<>();
        this.triggeredPhases.add(1);  // Phase 1 always triggered
    }
    
    /**
     * Update phase dựa trên HP
     * @return true nếu chuyển phase
     */
    public boolean updatePhase() {
        LivingEntity entity = boss.getEntity();
        if (entity.isDead()) {
            return false;
        }
        
        double currentHP = entity.getHealth();
        double maxHP = entity.getMaxHealth();
        double hpPercent = currentHP / maxHP;
        
        // Determine phase dựa trên HP
        int newPhase = determinePhase(hpPercent);
        
        if (newPhase > currentPhase && !triggeredPhases.contains(newPhase)) {
            // Chuyển sang phase mới
            transitionToPhase(newPhase);
            return true;
        }
        
        return false;
    }
    
    /**
     * Determine phase dựa trên HP percentage
     */
    private int determinePhase(double hpPercent) {
        for (int i = PHASE_THRESHOLDS.length - 1; i >= 0; i--) {
            if (hpPercent <= PHASE_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }
    
    /**
     * Transition sang phase mới
     */
    private void transitionToPhase(int newPhase) {
        currentPhase = newPhase;
        triggeredPhases.add(newPhase);
        boss.nextPhase();
        
        // Apply phase-specific modifiers
        applyPhaseModifiers(newPhase);
        
        // Announce phase transition
        announcePhaseTransition(newPhase);
        
        // Visual effects
        applyPhaseEffects(newPhase);
    }
    
    /**
     * Apply modifiers cho phase (modifier-based design)
     */
    private void applyPhaseModifiers(int phase) {
        LivingEntity entity = boss.getEntity();
        
        switch (phase) {
            case 2 -> {
                // Phase 2: Tăng tốc độ
                // Modifier: SPEED +20%
                entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, 
                    Integer.MAX_VALUE, 
                    0, 
                    false, 
                    false
                ));
            }
            case 3 -> {
                // Phase 3: Tăng damage
                // Modifier: STRENGTH +30%
                entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE, 
                    Integer.MAX_VALUE, 
                    0, 
                    false, 
                    false
                ));
            }
            case 4 -> {
                // Phase 4 (Berserk): Tăng tất cả
                // Modifier: SPEED +40%, STRENGTH +50%, RESISTANCE +20%
                entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, 
                    Integer.MAX_VALUE, 
                    1, 
                    false, 
                    false
                ));
                entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.INCREASE_DAMAGE, 
                    Integer.MAX_VALUE, 
                    1, 
                    false, 
                    false
                ));
                entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, 
                    Integer.MAX_VALUE, 
                    0, 
                    false, 
                    false
                ));
            }
        }
    }
    
    /**
     * Announce phase transition
     */
    private void announcePhaseTransition(int phase) {
        String phaseMessage = switch (phase) {
            case 2 -> "§e§l⚡ PHASE 2 §7- Boss tăng tốc độ!";
            case 3 -> "§c§l⚔ PHASE 3 §7- Boss tăng sát thương!";
            case 4 -> "§4§l☠ PHASE 4: BERSERK §7- Boss vào trạng thái bạo tẩu!";
            default -> "§7Phase " + phase;
        };
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage("");
            onlinePlayer.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            onlinePlayer.sendMessage("§c§l[WORLD BOSS] §6" + boss.getBossName());
            onlinePlayer.sendMessage(phaseMessage);
            onlinePlayer.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            onlinePlayer.sendMessage("");
            
            // Title notification
            onlinePlayer.sendTitle(
                "§c§l" + boss.getBossName(),
                phaseMessage,
                10, 40, 10
            );
        }
    }
    
    /**
     * Apply visual effects cho phase transition
     */
    private void applyPhaseEffects(int phase) {
        LivingEntity entity = boss.getEntity();
        
        // Spawn particles at boss location
        // TODO: Có thể thêm EventEffectService để spawn particles
        
        // Play sound
        entity.getWorld().playSound(
            entity.getLocation(),
            org.bukkit.Sound.ENTITY_WITHER_SPAWN,
            2.0f,
            1.0f + (phase * 0.2f)
        );
    }
    
    /**
     * Get current phase
     */
    public int getCurrentPhase() {
        return currentPhase;
    }
    
    /**
     * Get max phases
     */
    public int getMaxPhases() {
        return maxPhases;
    }
    
    /**
     * Check đã trigger phase chưa
     */
    public boolean hasTriggeredPhase(int phase) {
        return triggeredPhases.contains(phase);
    }
    
    /**
     * Get boss ascension level
     */
    public int getBossAscensionLevel() {
        return bossAscensionLevel;
    }
}
