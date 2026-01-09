package hcontrol.plugin.command;

import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.RoleService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SET ROLE COMMAND - Admin only
 * /setrole <player> <role> - set role cho player khac
 * /setrole <player> remove - xoa role cua player
 * /setrole clearall - xoa role cua tat ca players
 * /setrole list - xem danh sach role
 */
public class SetRoleCommand implements CommandExecutor, TabCompleter {
    
    private final PlayerManager playerManager;
    private final RoleService roleService;
    
    public SetRoleCommand(PlayerManager playerManager, RoleService roleService) {
        this.playerManager = playerManager;
        this.roleService = roleService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // SetRoleCommand chi cho admin
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        // Check permission: chi admin moi dung duoc
        if (!player.hasPermission("hcontrol.admin")) {
            player.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        // /setrole clearall - xoa role cua tat ca
        if (args.length == 1 && args[0].equalsIgnoreCase("clearall")) {
            roleService.clearAllRoles(playerManager);
            player.sendMessage("§aĐã xóa role của tất cả players!");
            return true;
        }
        
        // /setrole list - hien thi danh sach role
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            showRoleList(player);
            return true;
        }
        
        // /setrole <player> <role> hoac /setrole <player> remove
        if (args.length < 2) {
            player.sendMessage("§cCú pháp:");
            player.sendMessage("§7/setrole <player> <role> §f- set role cho player");
            player.sendMessage("§7/setrole <player> remove §f- xóa role của player");
            player.sendMessage("§7/setrole clearall §f- xóa role của tất cả");
            player.sendMessage("§7/setrole list §f- xem danh sách role");
            return true;
        }
        
        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        
        if (targetPlayer == null) {
            player.sendMessage("§cKhông tìm thấy player: " + targetName);
            return true;
        }
        
        PlayerProfile targetProfile = playerManager.get(targetPlayer.getUniqueId());
        if (targetProfile == null) {
            player.sendMessage("§cPlayer chưa load profile!");
            return true;
        }
        
        // /setrole <player> remove
        if (args[1].equalsIgnoreCase("remove")) {
            roleService.removeRole(targetProfile);
            player.sendMessage("§aĐã xóa role của " + targetPlayer.getName());
            targetPlayer.sendMessage("§7Role của bạn đã bị xóa bởi " + player.getName());
            return true;
        }
        
        // /setrole <player> <role>
        String roleName = args[1].toUpperCase();
        Title title;
        
        try {
            title = Title.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cKhông tìm thấy role: " + roleName);
            player.sendMessage("§7Dùng /setrole list để xem danh sách role");
            return true;
        }
        
        if (title == Title.NONE) {
            player.sendMessage("§cKhông thể set role NONE!");
            return true;
        }
        
        boolean success = roleService.setRole(targetProfile, title);
        if (success) {
            player.sendMessage("§aĐã set role " + title.getFullDisplay() + " §acho " + targetPlayer.getName());
            targetPlayer.sendMessage("§aBạn đã được set role: " + title.getFullDisplay());
        } else {
            player.sendMessage("§cKhông thể set role!");
        }
        
        return true;
    }
    
    /**
     * Hien thi danh sach role (title)
     */
    private void showRoleList(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("§6§l━━━━━━━━ §e⚡ DANH SACH ROLE §6§l━━━━━━━━");
        sender.sendMessage("");
        
        for (Title title : Title.values()) {
            if (title == Title.NONE) continue;
            
            sender.sendMessage("  §7► " + title.getFullDisplay() + 
                             " §8[" + title.getRarity().getDisplayName() + "]" + 
                             " §7(" + title.name() + ")");
        }
        
        sender.sendMessage("");
        sender.sendMessage("§7Sử dụng: §f/setrole <player> <role>");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
    }
    
    /**
     * Tab completion - filter suggestions theo permission
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // SetRoleCommand chi cho admin
        if (!(sender instanceof Player player) || !player.hasPermission("hcontrol.admin")) {
            return new ArrayList<>();
        }
        
        // SetRoleCommand chi cho admin, nen chi suggest admin commands
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("list");
            completions.add("clearall");
            
            // Suggest online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
            
            // Filter theo input
            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        
        // Args 1: Role name (khi args[0] la player name)
        if (args.length == 2) {
            // Neu args[0] la clearall hoac list, khong suggest gi
            if (args[0].equalsIgnoreCase("clearall") || args[0].equalsIgnoreCase("list")) {
                return new ArrayList<>();
            }
            
            // Suggest role names + remove
            List<String> completions = new ArrayList<>();
            completions.add("remove");
            
            String input = args[1].toLowerCase();
            return Arrays.stream(Title.values())
                    .filter(title -> title != Title.NONE)
                    .map(title -> title.name().toLowerCase())
                    .filter(name -> name.startsWith(input) || input.isEmpty())
                    .collect(Collectors.toList());
        }
        
        // Khong suggest gi neu qua 2 args
        return new ArrayList<>();
    }
}
