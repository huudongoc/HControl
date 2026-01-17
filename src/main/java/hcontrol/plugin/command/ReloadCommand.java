package hcontrol.plugin.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import hcontrol.plugin.core.LifecycleManager;

public class ReloadCommand implements CommandExecutor {

    private final LifecycleManager lifecycle;

    public ReloadCommand(LifecycleManager lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("hcontrol.reload")) {
            sender.sendMessage(ChatColor.RED + "Bạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        // Check neu co arg "config" thi chi reload config
        if (args.length > 0 && args[0].equalsIgnoreCase("config")) {
            sender.sendMessage(ChatColor.YELLOW + "Đang reload config...");
            try {
                var deathMsgConfig = hcontrol.plugin.core.CoreContext.getInstance()
                    .getCombatContext().getDeathService();
                sender.sendMessage(ChatColor.GOLD + "Xóa và tạo lại file death-messages.yml...");
                
                // Force delete va recreate config file
                java.io.File configFile = new java.io.File(
                    hcontrol.plugin.core.CoreContext.getInstance().getPlugin().getDataFolder(), 
                    "death-messages.yml"
                );
                if (configFile.exists()) {
                    configFile.delete();
                }
                
                // Reload se tao file moi
                lifecycle.reloadAll();
                sender.sendMessage(ChatColor.GREEN + "✔ Reload config thành công!");
                return true;
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✖ Lỗi khi reload config: " + e.getMessage());
                e.printStackTrace();
                return true;
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Đang reload plugin...");

        try {
            lifecycle.reloadAll();
            sender.sendMessage(ChatColor.GREEN + "✔ Reload thành công!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "✖ Lỗi khi reload: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}
