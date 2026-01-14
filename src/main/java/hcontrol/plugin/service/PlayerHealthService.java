package hcontrol.plugin.service;

import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

/**
 * PLAYER HEALTH SERVICE
 * Dong bo vanilla health voi tu tien HP system
 * SCALE VANILLA HEALTH: Tu tien HP co the rat lon (100-1000+)
 * Vanilla health luon la 20 hearts max de tranh tran man hinh
 */
public class PlayerHealthService {
    
    // Vanilla health max = 20 hearts (tranh tran man hinh)
    private static final double VANILLA_MAX_HEALTH = 20.0;
    
    /**
     * Sync vanilla health voi tu tien profile
     * Goi khi: join, level up, add stat, breakthrough, etc.
     * 
     * Scale: Tu tien HP (0-maxHP) -> Vanilla (0-20)
     */
    public void syncHealth(Player player, PlayerProfile profile) {
        if (player == null || !player.isOnline()) return;
        
        // Set vanilla max health = 20 hearts (co dinh)
        var maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(VANILLA_MAX_HEALTH);
        }
        
        // Scale current HP tu tien -> vanilla
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getStats().getMaxHP();
        
        // // Clamp current HP trong range [0, maxHP]
        // currentHP = Math.max(0, Math.min(currentHP, maxHP));
        
        // // Scale: (currentHP / maxHP) * 20
        // double vanillaHealth = maxHP > 0 ? (currentHP / maxHP) * VANILLA_MAX_HEALTH : VANILLA_MAX_HEALTH;
        // vanillaHealth = Math.max(0.5, Math.min(VANILLA_MAX_HEALTH, vanillaHealth)); // min 0.5 de khong chet
        
// player.setHealth(vanillaHealth);
        // Scale: (currentHP / maxHP) * 20
        double vanillaHealth = maxHP > 0 ? (currentHP / maxHP) * VANILLA_MAX_HEALTH : VANILLA_MAX_HEALTH;
        // Neu HP <= 0 thi chet (vanillaHealth = 0), neu HP > 0 thi min 0.5 de khong chet
        if (currentHP <= 0) {
            vanillaHealth = 0;
        } else {
            vanillaHealth = Math.max(0.5, Math.min(VANILLA_MAX_HEALTH, vanillaHealth));
        }
        
        player.setHealth(vanillaHealth);
        
        // Update profile neu HP bi clamp
        if (currentHP != profile.getCurrentHP()) {
            profile.setCurrentHP(currentHP);
        }
        
        // Sync tablist display name (HP hien thi tren tab)
        updateTabListName(player, profile);
    }
    
    /**
     * Update vanilla health khi currentHP thay doi (sau combat)
     * Chi update current, khong update max
     * 
     * Scale: Tu tien HP (0-maxHP) -> Vanilla (0-20)
     */
    public void updateCurrentHealth(Player player, PlayerProfile profile) {
        if (player == null || !player.isOnline()) return;
        if (player.isDead()) return;
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getStats().getMaxHP();
        // Clamp
        currentHP = Math.max(0, Math.min(currentHP, maxHP));
        
        // // Scale: (currentHP / maxHP) * 20
        // double vanillaHealth = maxHP > 0 ? (currentHP / maxHP) * VANILLA_MAX_HEALTH : VANILLA_MAX_HEALTH;
        // vanillaHealth = Math.max(0.5, Math.min(VANILLA_MAX_HEALTH, vanillaHealth));
        
        // player.setHealth(vanillaHealth);
        
        // Scale: (currentHP / maxHP) * 20
        double vanillaHealth = maxHP > 0 ? (currentHP / maxHP) * VANILLA_MAX_HEALTH : VANILLA_MAX_HEALTH;
        // Neu HP <= 0 thi chet (vanillaHealth = 0), neu HP > 0 thi min 0.5 de khong chet
        if (currentHP <= 0) {
            vanillaHealth = 0;
        } else {
            vanillaHealth = Math.max(0.5, Math.min(VANILLA_MAX_HEALTH, vanillaHealth));
        }
        
        player.setHealth(vanillaHealth);
        
        if (currentHP != profile.getCurrentHP()) {
            profile.setCurrentHP(currentHP);
        }
        
        // Sync tablist display name (HP hien thi tren tab)
        updateTabListName(player, profile);
    }
    
    /**
     * Handle player respawn - reset HP và Linh Khi về max
     * Gọi từ PlayerRespawnListener
     */
    public void handleRespawn(Player player, PlayerProfile profile) {
        if (player == null || !player.isOnline() || profile == null) return;
        
        // Reset HP ve max (hoi sinh full mau)
        double maxHP = profile.getStats().getMaxHP();
        profile.setCurrentHP(maxHP);
        
        // Reset Linh Khi ve max
        double maxLingQi = profile.getStats().getMaxLingQi();
        profile.setCurrentLingQi(maxLingQi);
        
        // Sync vanilla health
        syncHealth(player, profile);
    }
    
    /**
     * Handle player death - set HP = 0 trong profile
     * Gọi từ PlayerDeathListener
     */
    public void handleDeath(Player player, PlayerProfile profile) {
        if (player == null || profile == null) return;
        
        // Set HP = 0 trong profile
        profile.setCurrentHP(0);
        
        // Sync vanilla health (sẽ set về 0) đã = 0 rồi nên không cần UpdateCurrentHealth
        //updateCurrentHealth(player, profile);
    }

    /**
     * Update tablist display name voi HP hien tai
     * Format: [Realm] PlayerName HP% ❤ 85%
     */
    private void updateTabListName(Player player, PlayerProfile profile) {
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getStats().getMaxHP();
        double hpPercent = maxHP > 0 ? (currentHP / maxHP) * 100.0 : 100.0;
        
        // Mau sac HP theo %
        String hpColor;
        if (hpPercent >= 75) {
            hpColor = "§a";  // xanh la
        } else if (hpPercent >= 50) {
            hpColor = "§e";  // vang
        } else if (hpPercent >= 25) {
            hpColor = "§6";  // cam
        } else {
            hpColor = "§c";  // do
        }
        
        // Format: [LK] PlayerName ❤ 85%
        String realmShort = profile.getRealm().getShortName();
        String displayName = String.format("%s§7[%s] §f%s %s❤ %.0f%%",
            profile.getRealm().getColor(),
            realmShort,
            player.getName(),
            hpColor,
            hpPercent
        );
        
        player.setPlayerListName(displayName);
    }
}
