package hcontrol.plugin.module.boss;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WORLD BOSS COMMAND
 * Admin command để quản lý world boss
 * 
 * Commands:
 * - /worldboss spawn - Force spawn boss
 * - /worldboss status - Check boss status
 * - /worldboss kill - Kill current boss
 */
public class WorldBossCommand implements CommandExecutor, TabCompleter {
    
    private final WorldBossManager worldBossManager;
    
    public WorldBossCommand(WorldBossManager worldBossManager) {
        this.worldBossManager = worldBossManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hcontrol.admin.worldboss")) {
            sender.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "spawn" -> handleSpawn(sender);
            case "status" -> handleStatus(sender);
            case "kill" -> handleKill(sender);
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    /**
     * Handle spawn command
     */
    private void handleSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cLệnh này chỉ dùng được trong game!");
            return;
        }
        
        // Check nếu có boss đang active
        BossEntity currentBoss = worldBossManager.getSpawnService().getCurrentWorldBoss();
        if (currentBoss != null && !currentBoss.isDead()) {
            sender.sendMessage("§c[World Boss] Đã có boss đang active!");
            sender.sendMessage("§7Dùng §e/worldboss kill §7để kill boss hiện tại trước.");
            return;
        }
        
        // Force spawn tại vị trí player
        Location spawnLoc = player.getLocation();
        worldBossManager.getSpawnService().forceSpawn(spawnLoc);
        
        sender.sendMessage("§a[World Boss] Đã spawn boss tại vị trí của bạn!");
    }
    
    /**
     * Handle status command
     */
    private void handleStatus(CommandSender sender) {
        BossEntity currentBoss = worldBossManager.getSpawnService().getCurrentWorldBoss();
        
        if (currentBoss == null || currentBoss.isDead()) {
            sender.sendMessage("§e[World Boss] Không có boss nào đang active.");
            return;
        }
        
        // Get boss info
        String bossName = currentBoss.getBossName();
        int phase = currentBoss.getCurrentPhase();
        double hp = currentBoss.getEntity().getHealth();
        double maxHP = currentBoss.getEntity().getMaxHealth();
        double hpPercent = (hp / maxHP) * 100;
        
        // Get participation info
        WorldBossParticipation participation = worldBossManager.getCurrentParticipation();
        int participantCount = 0;
        if (participation != null) {
            participantCount = participation.getAllParticipants().size();
        }
        
        // Send status
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§c§l[WORLD BOSS STATUS]");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§7Boss: §e" + bossName);
        sender.sendMessage("§7Phase: §e" + phase + "/4");
        sender.sendMessage("§7HP: §c" + String.format("%.0f", hp) + "/" + String.format("%.0f", maxHP) + 
            " §7(" + String.format("%.1f", hpPercent) + "%)");
        sender.sendMessage("§7Participants: §e" + participantCount);
        
        if (currentBoss.getPhaseManager() != null) {
            int bossAscensionLevel = currentBoss.getPhaseManager().getBossAscensionLevel();
            sender.sendMessage("§7Ascension Level: §e" + bossAscensionLevel);
        }
        
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * Handle kill command
     */
    private void handleKill(CommandSender sender) {
        BossEntity currentBoss = worldBossManager.getSpawnService().getCurrentWorldBoss();
        
        if (currentBoss == null || currentBoss.isDead()) {
            sender.sendMessage("§c[World Boss] Không có boss nào đang active!");
            return;
        }
        
        // Kill boss
        currentBoss.getEntity().setHealth(0);
        sender.sendMessage("§a[World Boss] Đã kill boss!");
    }
    
    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§c§l[WORLD BOSS COMMANDS]");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§e/worldboss spawn §7- Force spawn boss");
        sender.sendMessage("§e/worldboss status §7- Check boss status");
        sender.sendMessage("§e/worldboss kill §7- Kill current boss");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hcontrol.admin.worldboss")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("spawn", "status", "kill");
        }
        
        return new ArrayList<>();
    }
}
