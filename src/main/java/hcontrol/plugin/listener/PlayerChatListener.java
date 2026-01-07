package hcontrol.plugin.listener;

import hcontrol.plugin.ui.ChatBubbleService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * CHAT LISTENER
 * Bat event chat de hien chat bubble
 */
public class PlayerChatListener implements Listener {
    
    private final ChatBubbleService chatBubbleService;
    
    public PlayerChatListener(ChatBubbleService chatBubbleService) {
        this.chatBubbleService = chatBubbleService;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Hien thi chat bubble (cat ngan neu qua dai)
        String bubbleText = message.length() > 30 
            ? message.substring(0, 27) + "..." 
            : message;
        
        // Run sync vi armor stand phai spawn tren main thread
        org.bukkit.Bukkit.getScheduler().runTask(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HControl"),
            () -> chatBubbleService.showChatBubble(player, "§f" + bubbleText)
        );
    }
}
