package hcontrol.plugin.module.boss;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CombatService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * WORLD BOSS LISTENER
 * Track participation và distribute rewards khi boss chết
 */
public class WorldBossListener implements Listener {
    
    private final BossManager bossManager;
    private final EntityManager entityManager;
    private final PlayerManager playerManager;
    private final WorldBossRewardService rewardService;
    private final CombatService combatService;
    private final WorldBossManager worldBossManager;
    
    public WorldBossListener(BossManager bossManager, EntityManager entityManager,
                            PlayerManager playerManager, WorldBossRewardService rewardService, 
                            CombatService combatService, WorldBossManager worldBossManager) {
        this.bossManager = bossManager;
        this.entityManager = entityManager;
        this.playerManager = playerManager;
        this.rewardService = rewardService;
        this.combatService = combatService;
        this.worldBossManager = worldBossManager;
    }
    
    /**
     * Track damage khi player đánh boss
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamageBoss(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        
        BossEntity boss = bossManager.getBoss(entity.getUniqueId());
        if (boss == null || boss.getType() != BossType.WORLD_BOSS) {
            return;  // Không phải world boss
        }
        
        // Track damage
        double damage = event.getFinalDamage();
        worldBossManager.trackDamage(player.getUniqueId(), entity.getUniqueId(), damage);
        
        // Update phase (nếu có phase manager)
        boss.updatePhase();
    }
    
    /**
     * Track damage từ WorldBossManager
     * Deprecated - dùng event listener thay vào
     */
    @Deprecated
    public void trackDamage(UUID playerUUID, UUID bossUUID, double damage) {
        // Method này được gọi từ WorldBossManager.trackDamage()
        // Không cần implement lại ở đây
    }
    
    /**
     * Handle boss death
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        BossEntity boss = bossManager.getBoss(entity.getUniqueId());
        
        if (boss == null || boss.getType() != BossType.WORLD_BOSS) {
            return;  // Không phải world boss
        }
        
        // Get boss ascension level từ phase manager
        int bossAscensionLevel = 0;
        if (boss.getPhaseManager() != null) {
            bossAscensionLevel = boss.getPhaseManager().getBossAscensionLevel();
        }
        
        // Get participation từ WorldBossManager
        WorldBossParticipation participation = worldBossManager.getCurrentParticipation();
        if (participation != null && participation.getBossUUID().equals(entity.getUniqueId())) {
            // Distribute rewards
            rewardService.distributeRewards(participation, bossAscensionLevel);
        }
        
        // Announce
        for (org.bukkit.entity.Player onlinePlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage("§c§l[WORLD BOSS] §e" + boss.getBossName() + 
                " §7đã bị tiêu diệt!");
            onlinePlayer.sendMessage("§7Top damage dealers đã nhận rewards!");
        }
        
        // Clear boss
        bossManager.removeBoss(entity.getUniqueId());
        
        // Clear participation
        // Note: participation sẽ được reset khi spawn boss mới
    }
}
