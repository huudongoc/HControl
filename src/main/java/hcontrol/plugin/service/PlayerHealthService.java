package hcontrol.plugin.service;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;

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
        double maxHP = profile.getMaxHP();  // Dung profile.getMaxHP() de apply realm multiplier
        
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

        // Neu HP = 0 thi cho chet, neu > 0 thi min 0.5 de khong chet ngoai y muon
        vanillaHealth = currentHP <= 0 ? 0 : Math.max(0.5, Math.min(VANILLA_MAX_HEALTH, vanillaHealth));

        
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
        double maxHP = profile.getMaxHP();  // Dung profile.getMaxHP() de apply realm multiplier
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

        // Neu HP = 0 thi cho chet, neu > 0 thi min 0.5 de khong chet ngoai y muon
        vanillaHealth = currentHP <= 0 ? 0 : Math.max(0.5, Math.min(VANILLA_MAX_HEALTH, vanillaHealth));

        
        player.setHealth(vanillaHealth);
        
        if (currentHP != profile.getCurrentHP()) {
            profile.setCurrentHP(currentHP);
        }
        
        // Sync tablist display name (HP hien thi tren tab)
        updateTabListName(player, profile);
        
        // Sync nameplate (HP hien thi tren dau player)
        // CHANGED: Update SYNC de tranh delay trong combat (cooldown 100ms da du de tranh lag)
        var nameplateService = hcontrol.plugin.core.CoreContext.getInstance().getUIContext().getNameplateService();
        if (nameplateService != null) {
            // Chỉ update HP (dùng cache static prefix)
            nameplateService.updateHP(player);
        }
    }
    
    /**
     * Handle player respawn - reset HP và Linh Khi về max
     * Gọi từ PlayerRespawnListener 
     */
    public void handleRespawn(Player player, PlayerProfile profile) {
        if (player == null || !player.isOnline() || profile == null) return;
        
        // Reset HP ve max (hoi sinh full mau)
        double maxHP = profile.getMaxHP();  // Dung profile.getMaxHP() de apply realm multiplier
        profile.setCurrentHP(maxHP);
        
        // Reset Linh Khi ve max
        double maxLingQi = profile.getStats().getMaxLingQi();
        profile.setCurrentLingQi(maxLingQi);

        var scoreboardService = hcontrol.plugin.core.CoreContext.getInstance().getUIContext().getScoreboardService();
        if (scoreboardService != null) {
            scoreboardService.updateScoreboard(player);
        }
        
        // Sync vanilla health
        syncHealth(player, profile);
    }
    
    /**
     * Handle player death - set HP = 0 trong profile
     * Gọi từ PlayerDeathListener
     * 
     * LƯU Ý: KHÔNG gọi setHealth(0) ở đây vì:
     * - PlayerDeathEvent chỉ được trigger khi player đã chết (health = 0)
     * - Việc gọi setHealth(0) trong event handler sẽ trigger lại PlayerDeathEvent
     * - Gây ra vòng lặp vô hạn (stack overflow)
     */
    public void handleDeath(Player player, PlayerProfile profile) {
        if (player == null || profile == null) return;
        
        // Set HP = 0 trong profile để scoreboard/UI hiển thị đúng
        profile.setCurrentHP(0);
        
        // KHÔNG set vanilla health ở đây vì:
        // - Player đã chết rồi (PlayerDeathEvent đã được trigger)
        // - setHealth(0) sẽ trigger lại PlayerDeathEvent -> vòng lặp vô hạn
        
        // Disable movement - player khong the di chuyen khi chet
        // Minecraft se tu dong handle viec nay khi player.isDead() = true, nhung can ensure
        if (player.isDead()) {
            player.setWalkSpeed(0.0f);  // Disable walking
            player.setFlySpeed(0.0f);   // Disable flying
        }
        
        // Update scoreboard 1 LẦN để hiện HP = 0
        // Fix: không update trong if(isDead) để tránh duplicate
        var scoreboardService = hcontrol.plugin.core.CoreContext.getInstance().getUIContext().getScoreboardService();
        if (scoreboardService != null) {
            scoreboardService.updateScoreboard(player);
        }
    }

    /**
     * Update tablist display name voi HP hien tai
     * Format: [LK][Thanh Vân] PlayerName ❤85%
     * 🔥 Reuse NameplateData từ NameplateService
     */
    private void updateTabListName(Player player, PlayerProfile profile) {
        // Reuse NameplateData từ NameplateService
        var nameplateService = hcontrol.plugin.core.CoreContext.getInstance().getUIContext().getNameplateService();
        if (nameplateService != null) {
            String displayName = nameplateService.buildTabListName(player);
            if (displayName != null) {
                player.setPlayerListName(displayName);
                forceTabListRefresh(player);
                return;
            }
        }
        
        // Fallback nếu NameplateService không available
        double currentHP = profile.getCurrentHP();
        double maxHP = profile.getMaxHP();  // Dung profile.getMaxHP() de apply realm multiplier
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
        
        // Format fallback: [CảnhGiới Tier] PlayerName ❤ 85% (CHỈ HIỆN TIER, không hiện số level)
        CultivationRealm realm = profile.getRealm();
        String realmName = realm.getDisplayName();
        int level = profile.getRealmLevel();
        
        // Chỉ hiển thị tier (Hạ/Trung/Thượng/Đỉnh), không hiển thị số level
        // Dùng DisplayFormatService để lấy tier name
        var displayFormatService = hcontrol.plugin.service.DisplayFormatService.getInstance();
        String tierName = displayFormatService.getTierName(level);
        
        String displayName = String.format("%s[%s %s] §f%s %s❤ %.0f%%",
            realm.getColor(),
            realmName,
            tierName,
            player.getName(),
            hpColor,
            hpPercent
        );
        
        player.setPlayerListName(displayName);
        forceTabListRefresh(player);
    }
    
    /**
     * Force refresh tablist để clients update display name
     */
    private void forceTabListRefresh(Player player) {
        // Minecraft tự động sync player list name khi:
        // 1. Player join/quit
        // 2. Player change world
        // 3. Server gọi updateDisplayName()
        
        // Không cần làm gì thêm - Bukkit API tự động sync
        // NHƯNG nếu vẫn có issue, có thể dùng:
        // - Hide/show player (heavy operation)
        // - Send custom packet (cần ProtocolLib)
        
        // For now: just rely on Bukkit sync
        // Nếu vẫn delay, có thể do client-side cache
    }
}
