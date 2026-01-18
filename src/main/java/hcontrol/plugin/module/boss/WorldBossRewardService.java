package hcontrol.plugin.module.boss;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hcontrol.plugin.model.AscensionProfile;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.AscensionService;
import hcontrol.plugin.service.LevelService;

import java.util.List;
import java.util.UUID;

/**
 * WORLD BOSS REWARD SERVICE
 * Phân phối reward sau khi kill world boss
 * 
 * Rewards:
 * - Cultivation (scale theo ascension level)
 * - Items (Phase 8+)
 * - Title (optional)
 */
public class WorldBossRewardService {
    
    private final PlayerManager playerManager;
    private final AscensionService ascensionService;
    private final LevelService levelService;
    
    public WorldBossRewardService(PlayerManager playerManager, AscensionService ascensionService, LevelService levelService) {
        this.playerManager = playerManager;
        this.ascensionService = ascensionService;
        this.levelService = levelService;
    }
    
    /**
     * Phân phối rewards cho tất cả participants
     */
    public void distributeRewards(WorldBossParticipation participation, int bossAscensionLevel) {
        List<WorldBossParticipation.ParticipationData> topDamage = 
            participation.getTopDamageDealers(10);
        
        if (topDamage.isEmpty()) {
            return;
        }
        
        // Tính total damage để phân phối reward
        double totalDamage = topDamage.stream()
            .mapToDouble(WorldBossParticipation.ParticipationData::getTotalDamage)
            .sum();
        
        // Phân phối rewards
        for (WorldBossParticipation.ParticipationData data : topDamage) {
            UUID playerUUID = data.getPlayerUUID();
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue;  // Player offline, skip
            }
            
            PlayerProfile profile = playerManager.get(playerUUID);
            if (profile == null) {
                continue;
            }
            
            // Tính reward dựa trên damage contribution
            double damageContribution = data.getTotalDamage() / totalDamage;
            
            // Cultivation reward (scale theo ascension level)
            long cultivationReward = calculateCultivationReward(
                bossAscensionLevel, 
                damageContribution,
                profile.getAscensionProfile().getAscensionLevel()
            );
            
            // Add cultivation (dùng LevelService để tự động level up)
            levelService.addCultivation(profile, cultivationReward);
            
            // Item rewards (Phase 8+)
            // TODO: Implement item rewards
            
            // Title rewards (optional)
            // TODO: Implement title rewards
            
            // Send reward message
            player.sendMessage("");
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e§l    ⚡ WORLD BOSS REWARDS");
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage("§7Damage: §e" + String.format("%.0f", data.getTotalDamage()) + 
                " §7(" + String.format("%.1f", damageContribution * 100) + "%)");
            player.sendMessage("§7Cultivation: §e+" + cultivationReward);
            player.sendMessage("");
            
            // Top 3 get special message
            int rank = topDamage.indexOf(data) + 1;
            if (rank <= 3) {
                String rankText = switch (rank) {
                    case 1 -> "§6§l🥇 #1";
                    case 2 -> "§7§l🥈 #2";
                    case 3 -> "§c§l🥉 #3";
                    default -> "";
                };
                player.sendMessage(rankText + " §7Top Damage!");
                for (org.bukkit.entity.Player onlinePlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage("§6[World Boss] " + rankText + " §e" + 
                        player.getName() + " §7đã đạt top " + rank + " damage!");
                }
            }
        }
    }
    
    /**
     * Tính cultivation reward
     * Formula: base * (1 + bossAscensionLevel * 0.1) * damageContribution * playerAscensionMultiplier
     */
    private long calculateCultivationReward(int bossAscensionLevel, double damageContribution, int playerAscensionLevel) {
        long base = 100_000L;  // 100K base
        
        // Boss ascension multiplier
        double bossMultiplier = 1.0 + (bossAscensionLevel * 0.1);
        
        // Player ascension multiplier (players có ascension cao hơn nhận thêm bonus)
        double playerMultiplier = 1.0 + (playerAscensionLevel * 0.05);
        
        // Final reward
        double reward = base * bossMultiplier * damageContribution * playerMultiplier;
        
        return (long) reward;
    }
    
    /**
     * Calculate item rewards (Phase 8+)
     * TODO: Implement khi có Item System
     */
    private void giveItemRewards(Player player, PlayerProfile profile, 
                                 double damageContribution, int bossAscensionLevel) {
        // TODO: Implement item rewards
    }
    
    /**
     * Calculate title rewards (optional)
     * TODO: Implement title unlocks
     */
    private void giveTitleRewards(Player player, PlayerProfile profile, int rank) {
        // TODO: Implement title rewards
    }
}
