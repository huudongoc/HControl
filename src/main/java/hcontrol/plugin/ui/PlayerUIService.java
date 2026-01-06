package hcontrol.plugin.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerUIService {
    
    public void handlePlayerJoin(Player player) {
        //UI welcome//message//title
        
        // Broadcast join message
        Bukkit.broadcastMessage(
            ChatColor.GREEN + "✦ " + 
            ChatColor.AQUA + player.getName() + 
            ChatColor.GRAY + " đã tham gia server " +
            ChatColor.GREEN + "✦"
        );
        
        // Welcome message
        player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════ ✦");
        player.sendMessage(ChatColor.AQUA + "    Welcome to the server!");
        
        player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════ ✦");
        
        // Title
        player.sendTitle(
            ChatColor.GOLD + "✦ " + ChatColor.GREEN + "Welcome" + ChatColor.GOLD + " ✦",
            ChatColor.YELLOW + "➤ " + player.getName() + " ➤",
            10, 70, 20
        );
    }
    
    public void handlePlayerQuit(Player player) {
      
        
        // Broadcast quit message
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ ━━━━━━━━━━━━━━━━━━━ ⚠");
        Bukkit.broadcastMessage(ChatColor.GRAY + "    ➜ " + player.getName() + ChatColor.DARK_GRAY + " đã rời khỏi server");
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ ━━━━━━━━━━━━━━━━━━━ ⚠");
    }
}
