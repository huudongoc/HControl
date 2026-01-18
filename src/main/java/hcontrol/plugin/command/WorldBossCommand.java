package hcontrol.plugin.command;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.module.boss.WorldBossManager;

/**
 * WORLD BOSS COMMAND - ENDGAME
 * Admin command để quản lý world boss
 * 
 * Usage: /worldboss [spawn|info|force]
 */
public class WorldBossCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        CoreContext ctx = CoreContext.getInstance();
        WorldBossManager worldBossManager = ctx.getWorldBossManager();
        
        if (worldBossManager == null) {
            player.sendMessage("§cWorld Boss System chưa khởi động!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "spawn":
            case "force":
                handleSpawn(player, worldBossManager);
                break;
            case "info":
                handleInfo(player, worldBossManager);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void handleSpawn(Player player, WorldBossManager worldBossManager) {
        Location loc = player.getLocation();
        worldBossManager.forceSpawn(loc);
        player.sendMessage("§a✓ Đã force spawn world boss tại vị trí của bạn!");
    }
    
    private void handleInfo(Player player, WorldBossManager worldBossManager) {
        var spawnService = worldBossManager.getSpawnService();
        if (spawnService == null) {
            player.sendMessage("§cWorld Boss Spawn Service chưa khởi động!");
            return;
        }
        
        var currentBoss = spawnService.getCurrentWorldBoss();
        if (currentBoss == null || currentBoss.isDead()) {
            player.sendMessage("§7Không có world boss đang active.");
            player.sendMessage("§7Boss sẽ spawn tự động sau 2 giờ.");
            return;
        }
        
        player.sendMessage("§6§l=== WORLD BOSS INFO ===");
        player.sendMessage("§7Boss: §e" + currentBoss.getBossName());
        player.sendMessage("§7Phase: §e" + currentBoss.getCurrentPhase());
        player.sendMessage("§7Location: §e" + formatLocation(currentBoss.getLocation()));
        
        var participation = worldBossManager.getCurrentParticipation();
        if (participation != null) {
            player.sendMessage("§7Participants: §e" + participation.getAllParticipants().size());
            var topDamage = participation.getTopDamageDealers(3);
            if (!topDamage.isEmpty()) {
                player.sendMessage("§7Top Damage:");
                for (int i = 0; i < topDamage.size(); i++) {
                    var data = topDamage.get(i);
                    org.bukkit.entity.Player topPlayer = org.bukkit.Bukkit.getPlayer(data.getPlayerUUID());
                    String playerName = topPlayer != null ? topPlayer.getName() : "Unknown";
                    player.sendMessage("§7  " + (i + 1) + ". §e" + playerName + 
                        " §7- §c" + String.format("%.0f", data.getTotalDamage()));
                }
            }
        }
        
        player.sendMessage("§6§l======================");
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== WORLD BOSS COMMAND ===");
        player.sendMessage("§7/worldboss spawn §7- Force spawn boss tại vị trí của bạn");
        player.sendMessage("§7/worldboss info §7- Xem thông tin boss hiện tại");
    }
    
    private String formatLocation(Location loc) {
        return String.format("X:%.0f Y:%.0f Z:%.0f", loc.getX(), loc.getY(), loc.getZ());
    }
}
