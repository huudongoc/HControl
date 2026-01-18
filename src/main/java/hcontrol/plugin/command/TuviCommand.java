package hcontrol.plugin.command;

import hcontrol.plugin.service.LevelService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * TU VI COMMAND
 * Xem thong tin tu vi va tang tu vi (test)
 */
public class TuviCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final LevelService levelService;

    public TuviCommand(PlayerManager playerManager, LevelService levelService) {
        this.playerManager = playerManager;
        this.levelService = levelService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cProfile not found");
            return true;
        }

        // Check level
        if (args.length == 0) {
            levelService.sendLevelInfo(player, profile);
            return true;
        }

        // Add cultivation (thay cho exp) - CHI ADMIN MOI DUNG DUOC
        if (args[0].equalsIgnoreCase("add")) {
            // Check permission: chi admin/op moi duoc add tu vi
            if (!player.hasPermission("hcontrol.admin")) {
                player.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
                return true;
            }
            
            // Case 1: /tuvi add <số> - add cho chính mình
            if (args.length == 2) {
                long cultivation;
                try {
                    cultivation = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cSố không hợp lệ: " + args[1]);
                    return true;
                }

                if (cultivation <= 0) {
                    player.sendMessage("§cTu vi phải > 0");
                    return true;
                }

                levelService.addCultivation(profile, cultivation);
                player.sendMessage("§a+§e" + cultivation + " §dTu vi");
                levelService.sendLevelInfo(player, profile);
                return true;
            }
            
            // Case 2: /tuvi add <player> <số> - add cho người khác
            if (args.length == 3) {
                String targetName = args[1];
                Player targetPlayer = Bukkit.getPlayer(targetName);
                
                if (targetPlayer == null) {
                    player.sendMessage("§cKhông tìm thấy player: " + targetName);
                    return true;
                }
                
                PlayerProfile targetProfile = playerManager.get(targetPlayer.getUniqueId());
                if (targetProfile == null) {
                    player.sendMessage("§cProfile của " + targetName + " chưa load!");
                    return true;
                }
                
                long cultivation;
                try {
                    cultivation = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cSố không hợp lệ: " + args[2]);
                    return true;
                }

                if (cultivation <= 0) {
                    player.sendMessage("§cTu vi phải > 0");
                    return true;
                }

                levelService.addCultivation(targetProfile, cultivation);
                player.sendMessage("§a✓ Đã thêm §e" + cultivation + " §dTu vi §7cho §f" + targetName);
                
                // Thông báo cho target player
                targetPlayer.sendMessage("§a+§e" + cultivation + " §dTu vi §7(từ admin)");
                levelService.sendLevelInfo(targetPlayer, targetProfile);
                return true;
            }
            
            // Wrong usage
            player.sendMessage("§cCách dùng:");
            player.sendMessage("§7/tuvi add <số> §7- Thêm tu vi cho chính mình");
            player.sendMessage("§7/tuvi add <player> <số> §7- Thêm tu vi cho người khác");
            return true;
        }

        // Wrong usage
        player.sendMessage("§cCách dùng: /tuvi [add <số>] hoặc /tuvi [add <player> <số>]");
        return true;
    }
}