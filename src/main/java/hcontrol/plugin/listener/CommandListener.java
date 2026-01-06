package hcontrol.plugin.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase().split(" ")[0];
        
        // Chặn lệnh /reload cho tất cả người chơi (kể cả OP)
        if (command.equals("/reload") || command.equals("/rl") || 
            command.equals("/minecraft:reload") || command.equals("/bukkit:reload")) {
            
            event.setCancelled(true);
            event.getPlayer().sendMessage("");
            event.getPlayer().sendMessage(ChatColor.RED + "✖ ━━━━━━━━━━━━━━━━━━━ ✖");
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "  Lệnh này chỉ Console mới được dùng!");
            event.getPlayer().sendMessage(ChatColor.RED + "✖ ━━━━━━━━━━━━━━━━━━━ ✖");
            event.getPlayer().sendMessage("");
        }
    }
}
