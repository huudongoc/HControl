package hcontrol.plugin.command;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.CultivationService;
import hcontrol.plugin.model.CultivatorProfile;
import hcontrol.plugin.player.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CULTIVATOR INFO COMMAND
 * Hien thi thong tin tu si
 */
public class CultivatorCommand implements CommandExecutor {
    
    private final PlayerManager playerManager;
    private final CultivationService cultivationService;
    
    public CultivatorCommand(PlayerManager playerManager, CultivationService cultivationService) {
        this.playerManager = playerManager;
        this.cultivationService = cultivationService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChi player moi dung duoc lenh nay!");
            return true;
        }
        
        var profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhong tim thay profile!");
            return true;
        }
        
        // TODO: Convert PlayerProfile -> CultivatorProfile
        // Tam thoi dung PlayerProfile
        
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l    TU SI THONG TIN");
        player.sendMessage("");
        player.sendMessage("§7Canh gioi: " + profile.getRealm().toString() + " [§fLv" + profile.getLevel() + "§7]");
        
        // Hien thi tu vi (cultivation)
        CultivationRealm nextRealm = profile.getRealm().getNext();
        if (nextRealm != null) {
            player.sendMessage("§7Tu vi: §a" + profile.getCultivation() + " §7/ §e" + nextRealm.getRequiredCultivation());
        } else {
            player.sendMessage("§7Tu vi: §a" + profile.getCultivation() + " §7(Max realm)");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Linh can: §a" + profile.getSpiritualRoot().name() + " §7(" + profile.getRootQuality().name() + ")");
        player.sendMessage("§7Dao tam: §b" + String.format("%.1f%%", profile.getDaoHeart()));
        player.sendMessage("§7Noi thuong: §c" + String.format("%.1f%%", profile.getInnerInjury()));
        player.sendMessage("");
        player.sendMessage("§6§lTRANG THAI MO KHOA:");
        player.sendMessage("§7Level unlock: " + (profile.isNextLevelUnlocked() ? "§a✔ Da mo khoa" : "§c✘ Chua mo khoa"));
        player.sendMessage("§7Breakthrough unlock: " + (profile.isBreakthroughUnlocked() ? "§a✔ Da mo khoa" : "§c✘ Chua mo khoa"));
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        return true;
    }
}
