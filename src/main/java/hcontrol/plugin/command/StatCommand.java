package hcontrol.plugin.command;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.StatService;
import hcontrol.plugin.stats.StatType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final StatService statService;

    public StatCommand(PlayerManager playerManager, StatService statService) {
        this.playerManager = playerManager;
        this.statService = statService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChi player moi dung duoc lenh nay");
            return true;
        }

        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhong tim thay profile");
            return true;
        }

        // /stat → xem stat
        if (args.length == 0) {
            player.sendMessage(statService.getStatInfo(profile));
            return true;
        }

        // /stat add <type> <amount> → them stat
        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            // parse stat type
            StatType type = statService.parseStatType(args[1]);
            if (type == null) {
                player.sendMessage("§cStat khong hop le: " + args[1]);
                player.sendMessage("§7Co the dung: CC (Can Cot), LL (Linh Luc), TP (The Phach), NT (Ngo Tinh), KV (Khi Van)");
                return true;
            }
            
            if (!type.isPrimary()) {
                player.sendMessage("§cChi them duoc primary stat (CC/LL/TP/NT/KV)");
                return true;
            }
            
            // parse amount
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cSo khong hop le: " + args[2]);
                return true;
            }
            
            // allocate stat (service xu ly logic)
            boolean success = statService.allocateStatPoints(profile, type, amount);
            if (!success) {
                // chi co the that bai neu khong du stat point (da validate type o tren)
                player.sendMessage("§cKhong du stat point! (Co: " + profile.getStatPoints() + ")");
                return true;
            }
            
            // thanh cong
            player.sendMessage("§a+§e" + amount + " §f" + type.getShortName());
            player.sendMessage("§7Stat point con lai: §f" + profile.getStatPoints());
            
            // Sync vanilla health neu them The Phach (maxHP tang)
            if (type == hcontrol.plugin.stats.StatType.THE_PHACH) {
                var healthService = hcontrol.plugin.core.CoreContext.getInstance().getPlayerHealthService();
                healthService.syncHealth(player, profile);
            }
            
            return true;
        }

        // sai cu phap
        player.sendMessage("§cCu phap: /stat [add <CC|LL|TP|NT|KV> <so luong>]");
        player.sendMessage("§7CC=Can Cot, LL=Linh Luc, TP=The Phach, NT=Ngo Tinh, KV=Khi Van");
        return true;
    }
}