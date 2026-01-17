package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.chat.ChatBubbleService;
import hcontrol.plugin.ui.chat.ChatFormatService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CHAT BUBBLE TEST COMMAND
 * Test chat bubble với message tùy chỉnh
 * Usage: /bubbletest <message> [duration]
 */
public class ChatBubbleTestCommand implements CommandExecutor {
    
    private final PlayerManager playerManager;
    
    public ChatBubbleTestCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được command này!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§e=== Chat Bubble Test ===");
            player.sendMessage("§7/bubbletest <message> §7- Hiển thị bubble với message");
            player.sendMessage("§7/bubbletest <message> <duration> §7- Hiển thị bubble với thời gian (ticks)");
            player.sendMessage("§7Ví dụ: /bubbletest Xin chào mọi người");
            player.sendMessage("§7Ví dụ: /bubbletest Test bubble 100 §7(5 giây)");
            return true;
        }
        
        // Lấy message
        StringBuilder messageBuilder = new StringBuilder();
        int durationIndex = -1;
        
        // Tìm duration nếu có (số ở cuối)
        for (int i = args.length - 1; i >= 0; i--) {
            try {
                Integer.parseInt(args[i]);
                durationIndex = i;
                break;
            } catch (NumberFormatException e) {
                // Không phải số, tiếp tục
            }
        }
        
        // Build message
        int messageEnd = durationIndex >= 0 ? durationIndex : args.length;
        for (int i = 0; i < messageEnd; i++) {
            if (i > 0) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();
        
        // Lấy duration
        int duration = 60; // Mặc định 3 giây
        if (durationIndex >= 0) {
            try {
                duration = Integer.parseInt(args[durationIndex]);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        // Lấy services
        CoreContext ctx = CoreContext.getInstance();
        ChatBubbleService bubbleService = ctx.getUIContext().getChatBubbleService();
        ChatFormatService formatService = ctx.getUIContext().getChatFormatService();
        
        if (bubbleService == null || formatService == null) {
            player.sendMessage("§cChat bubble service chưa được khởi tạo!");
            return true;
        }
        
        // Lấy profile để format
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cProfile chưa load!");
            return true;
        }
        
        // Format bubble text
        String formattedBubble = formatService.formatBubbleText(profile, message);
        
        // Hiển thị bubble
        bubbleService.showChatBubble(player, formattedBubble, duration);
        
        player.sendMessage("§a✓ Đã hiển thị chat bubble!");
        player.sendMessage("§7Message: §f" + formattedBubble);
        player.sendMessage("§7Duration: §f" + duration + " ticks (" + (duration / 20.0) + " giây)");
        
        return true;
    }
}
