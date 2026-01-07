package hcontrol.plugin.ui;

import hcontrol.plugin.model.CultivatorProfile;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerUIService {
    
    private final PlayerManager playerManager;
    
    public PlayerUIService(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    public void handlePlayerJoin(Player player) {
        // Broadcast join message
        Bukkit.broadcastMessage(
            ChatColor.GREEN + "✦ " + 
            ChatColor.AQUA + player.getName() + 
            ChatColor.GRAY + " da tham gia server " +
            ChatColor.GREEN + "✦"
        );
        
        // Lay profile de hien thi thong tin
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            // Fallback neu chua load xong
            sendBasicWelcome(player);
            return;
        }
        
        // Hien thi thong tin cultivator chi tiet
        displayCultivatorInfo(player, profile);
        
        // Title
        player.sendTitle(
            ChatColor.GOLD + "✦ " + profile.getRealm().toString() + ChatColor.GOLD + " ✦",
            ChatColor.YELLOW + "➤ " + player.getName() + " ➤",
            10, 70, 20
        );
    }
    
    /**
     * Hien thi day du thong tin cultivator
     */
    private void displayCultivatorInfo(Player player, PlayerProfile profile) {
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━ §e⚡ TU SI THONG TIN §6§l━━━━━━━━━━━━━");
        player.sendMessage("");
        
        // Canh gioi + Level
        player.sendMessage("§7  ► Canh gioi: " + profile.getRealm().toString());
        player.sendMessage("§7  ► Level: §fLv." + profile.getLevel() + " §7/ " + profile.getRealm().getRequiredLevel());
        player.sendMessage("§7  ► Kinh nghiem: §e" + profile.getExp() + " §7/ " + getRequiredExp(profile));
        
        player.sendMessage("");
        
        // Stats co ban (Can Cot - Root)
        var stats = profile.getStats();
        player.sendMessage("§7  ► §bCan Cot: §f" + stats.getRoot());
        player.sendMessage("§7  ► §bLinh Luc: §f" + stats.getSpirit());
        player.sendMessage("§7  ► §bThe Phach: §f" + stats.getPhysique());
        player.sendMessage("§7  ► §bNgo Tinh: §f" + stats.getComprehension());
        player.sendMessage("§7  ► §bKhi Van: §f" + stats.getFortune());
        
        player.sendMessage("");
        
        // LOAI BO getAttack - damage tu REALM, khong tu stat
        player.sendMessage("§7  ► §9Phong Thu: §f" + String.format("%.0f", stats.getDefense()));
        player.sendMessage("§7  ► §aMax HP: §f" + stats.getMaxHP());
        player.sendMessage("§7  ► §3Max Linh Khi: §f" + stats.getMaxLingQi());
        
        player.sendMessage("");
        
        // Diem stat con lai
        int statPoints = profile.getStatPoints();
        if (statPoints > 0) {
            player.sendMessage("§e  ⚠ Ban con §6" + statPoints + " §ediem stat chua phan phoi!");
            player.sendMessage("§7  Su dung: §f/stat <ten_stat> <so_luong>");
        }
        
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
    }
    
    /**
     * Welcome message don gian (fallback)
     */
    private void sendBasicWelcome(Player player) {
        player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════ ✦");
        player.sendMessage(ChatColor.AQUA + "    Chao mung den server!");
        player.sendMessage(ChatColor.GOLD + "✦ ═══════════════════ ✦");
    }
    
    /**
     * Tinh exp can thiet len level ke tiep
     */
    private long getRequiredExp(PlayerProfile profile) {
        int level = profile.getLevel();
        return (long) (Math.pow(level, 2) * 100);
    }
    
    public void handlePlayerQuit(Player player) {
      
        
        // Broadcast quit message
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ ━━━━━━━━━━━━━━━━━━━ ⚠");
        Bukkit.broadcastMessage(ChatColor.GRAY + "    ➜ " + player.getName() + ChatColor.DARK_GRAY + " đã rời khỏi server");
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ ━━━━━━━━━━━━━━━━━━━ ⚠");
    }
}
