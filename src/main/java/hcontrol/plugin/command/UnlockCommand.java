package hcontrol.plugin.command;

import hcontrol.plugin.player.LevelService;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command unlock tier & breakthrough
 * /unlock - dot pha len tier tiep theo (Ha->Trung->Thuong->Dinh)
 * /unlock breakthrough - mo khoa do kiep
 */
public class UnlockCommand implements CommandExecutor {

    private final PlayerManager playerManager;
    private final LevelService levelService;

    public UnlockCommand(PlayerManager playerManager, LevelService levelService) {
        this.playerManager = playerManager;
        this.levelService = levelService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được!");
            return true;
        }

        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cLỗi: Không tìm thấy profile!");
            return true;
        }

        // /unlock - dot pha tier (Ha->Trung->Thuong->Dinh)
        // /unlock - dot pha tier (Ha->Trung->Thuong->Dinh)
        if (args.length == 0) {
            levelService.unlockNextTier(profile);
            return true;
        }
        
        // /unlock breakthrough - mo khoa do kiep
        if (args[0].equalsIgnoreCase("breakthrough")) {
            profile.setBreakthroughUnlocked(true);
            player.sendMessage("§a✔ Đã mở khóa độ kiếp!");
            return true;
        }
        
        player.sendMessage("§cCú pháp: /unlock hoặc /unlock breakthrough");
        return true;
    }
}
