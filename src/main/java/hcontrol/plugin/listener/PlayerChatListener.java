package hcontrol.plugin.listener;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.chat.ChatBubbleService;
import hcontrol.plugin.ui.chat.ChatFormatService;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

/**
 * CHAT LISTENER
 * Bat event chat de format chat voi khung mau dep va hien chat bubble
 */
public class PlayerChatListener implements Listener {
    
    private final ChatBubbleService chatBubbleService;
    private final ChatFormatService chatFormatService;
    private final PlayerManager playerManager;
    private final Plugin plugin;
    
    public PlayerChatListener(ChatBubbleService chatBubbleService, 
                             ChatFormatService chatFormatService,
                             PlayerManager playerManager,
                             Plugin plugin) {
        this.chatBubbleService = chatBubbleService;
        this.chatFormatService = chatFormatService;
        this.playerManager = playerManager;
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Lay profile de format chat
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        
        // Format chat message voi khung mau dep
        String formattedMessage = chatFormatService.formatChatMessage(profile, player.getName(), message);
        event.setFormat(formattedMessage);
        
        // Hien thi chat bubble (cat ngan neu qua dai)
        String bubbleText = message.length() > 30 
            ? message.substring(0, 27) + "..." 
            : message;
        
        String formattedBubble = chatFormatService.formatBubbleText(profile, bubbleText);
        
        // Run sync vi armor stand phai spawn tren main thread
        org.bukkit.Bukkit.getScheduler().runTask(
            plugin,
            () -> chatBubbleService.showChatBubble(player, formattedBubble)
        );
    }
}
