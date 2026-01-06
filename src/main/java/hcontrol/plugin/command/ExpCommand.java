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

        // Add exp
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            long exp;
            try {
                exp = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number: " + args[1]);
                return true;
            }

            if (exp <= 0) {
                player.sendMessage("§cExp must be positive");
                return true;
            }

            levelService.addExp(profile, exp);
            player.sendMessage("§a+§e" + exp + " §aEXP");
            player.sendMessage(
                "§7DEBUG | Level=§f" + profile.getLevel() +
                " §7| Exp=§f" + profile.getExp()
            );
            return true;
        }

        // Wrong usage
        player.sendMessage("§cUsage: /exp [add <amount>]");
        return true;
    }
}