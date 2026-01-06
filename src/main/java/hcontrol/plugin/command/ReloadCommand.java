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
