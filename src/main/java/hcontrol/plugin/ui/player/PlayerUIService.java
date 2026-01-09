package hcontrol.plugin.ui.player;


import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CultivationProgressService;
import hcontrol.plugin.service.DisplayFormatService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * PLAYER UI SERVICE
 * Hien thi thong tin player khi join/quit
 * KHONG chua logic tinh toan - chi su dung DisplayFormatService va CultivationProgressService
 */
public class PlayerUIService {
    
    private final PlayerManager playerManager;
    private final DisplayFormatService displayFormatService;
    private final CultivationProgressService cultivationProgressService;
    
    public PlayerUIService(PlayerManager playerManager, DisplayFormatService displayFormatService, CultivationProgressService cultivationProgressService) {
        this.playerManager = playerManager;
        this.displayFormatService = displayFormatService;
        this.cultivationProgressService = cultivationProgressService;
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
        
        // Canh gioi + tier (KHONG hien thi level so) - su dung DisplayFormatService
        String realmTierText = displayFormatService.formatRealmTier(profile.getRealm(), profile.getLevel());
        player.sendMessage("§7  ► Canh gioi: " + realmTierText);
        
        // Tu vi progress - su dung CultivationProgressService
        long currentCult = profile.getCultivation();
        long requiredCult = cultivationProgressService.getRequiredCultivation(profile);
        double cultPercent = cultivationProgressService.getCultivationPercent(profile);
        player.sendMessage("§7  ► Tu vi: §e" + String.format("%.1f%%", cultPercent) + " §8(" + currentCult + "/" + requiredCult + ")");
        
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
    
    public void handlePlayerQuit(Player player) {
      
        
        // Broadcast quit message
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ ━━━━━━━━━━━━━━━━━━━ ⚠");
        Bukkit.broadcastMessage(ChatColor.GRAY + "    ➜ " + player.getName() + ChatColor.DARK_GRAY + " đã rời khỏi server");
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ ━━━━━━━━━━━━━━━━━━━ ⚠");
    }
}
