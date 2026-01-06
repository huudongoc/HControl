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
        if (!(sender instanceof Player player)) return true;

        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return true;

        // Check level
        if (args.length == 0) {
            long currentExp = profile.getExp();
            long requiredExp = levelService.getExpToNext(profile.getLevel());
            player.sendMessage(String.format("§aLevel: §e%d §7| §aEXP: §e%d§7/§e%d", 
                profile.getLevel(), currentExp, requiredExp));
            return true;
        }

        // Add exp
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            long exp;
            try {
                exp = Long.parseLong(args[1]);
            } catch (NumberFormatException e) {
                return true;
            }

            levelService.addExp(profile, exp);
            player.sendMessage(
    "DEBUG | Level=" + profile.getLevel() +
    " | Exp=" + profile.getExp()
);

            return true;
        }

        return true;
    }
}