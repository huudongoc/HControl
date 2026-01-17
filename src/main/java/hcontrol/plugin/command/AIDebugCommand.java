package hcontrol.plugin.command;

import hcontrol.plugin.ai.AIService;
import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.skill.SkillCooldownManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * DEBUG COMMAND - Test AI system
 * Usage: /aidebug [info|clear]
 */
public class AIDebugCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được command này!");
            return true;
        }
        
        if (!player.isOp()) {
            player.sendMessage("§cBạn không có quyền!");
            return true;
        }
        
        CoreContext ctx = CoreContext.getInstance();
        AIService aiService = ctx.getAIService();
        
        if (args.length == 0) {
            player.sendMessage("§e=== AI Debug ===");
            player.sendMessage("§7/aidebug info - Xem thông tin AI");
            player.sendMessage("§7/aidebug clear - Clear cooldowns");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "info":
                int brainCount = aiService != null ? aiService.getActiveBrainCount() : 0;
                player.sendMessage("§e=== AI System Info ===");
                player.sendMessage("§7Active brains: §a" + brainCount);
                player.sendMessage("§7AI Service: " + (aiService != null ? "§aRunning" : "§cStopped"));
                break;
                
            case "clear":
                SkillCooldownManager cooldowns = ctx.getCooldownManager();
                if (cooldowns != null) {
                    cooldowns.clearAll();
                    player.sendMessage("§aĐã clear tất cả cooldowns!");
                }
                break;
                
            default:
                player.sendMessage("§cCommand không hợp lệ!");
        }
        
        return true;
    }
}
