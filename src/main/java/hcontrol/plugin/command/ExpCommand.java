package hcontrol.plugin.command;

import hcontrol.plugin.player.LevelService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExpCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final LevelService levelService;

    public ExpCommand(PlayerManager playerManager, LevelService levelService) {
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

        // Add cultivation (thay cho exp)
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
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

        // Wrong usage
        player.sendMessage("§cCách dùng: /exp [add <số>]");
        return true;
    }
}