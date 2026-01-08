package hcontrol.plugin.ui;


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
        
        // Canh gioi + tier (KHONG hien thi level so)
        String tierName = getTierName(profile.getLevel());
        player.sendMessage("§7  ► Canh gioi: " + profile.getRealm().getColor() + profile.getRealm().getDisplayName() + " " + tierName);
        
        // Tu vi progress
        long currentCult = profile.getCultivation();
        long requiredCult = getRequiredCultivation(profile);
        double cultPercent = requiredCult > 0 ? (double)currentCult / requiredCult * 100 : 100.0;
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
    
    /**
     * Tinh cultivation can thiet len level ke tiep
     */
    private long getRequiredCultivation(PlayerProfile profile) {
        int level = profile.getLevel();
        int maxLevel = getMaxLevelForRealm(profile.getRealm());
        if (level >= maxLevel) return 0;
        
        return (long) (100 * Math.pow(level + 1, 2));
    }
    
    private int getMaxLevelForRealm(hcontrol.plugin.model.CultivationRealm realm) {
        switch(realm) {
            case MORTAL: return 10;
            case QI_REFINING: return 9;
            case FOUNDATION: return 9;
            case GOLDEN_CORE: return 9;
            default: return 10;
        }
    }
    
    /**
     * Lay tier name tu level
     */
    private String getTierName(int level) {
        if (level <= 3) return "§7Hạ";
        if (level <= 6) return "§eTrung";
        if (level <= 9) return "§6Thượng";
        return "§cĐỉnh";
    }
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
