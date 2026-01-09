package hcontrol.plugin.command;

import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.RoleService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ROLE COMMAND - User xem va set role cua chinh minh
 * /role - xem role da so huu
 * /role <role> - set role (neu da so huu)
 * /role remove - gỡ bỏ role
 */
public class RoleCommand implements CommandExecutor, TabCompleter {
    
    private final PlayerManager playerManager;
    private final RoleService roleService;
    
    public RoleCommand(PlayerManager playerManager, RoleService roleService) {
        this.playerManager = playerManager;
        this.roleService = roleService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return true;
        }
        
        // /role (khong co args) - xem role da so huu
        if (args.length == 0) {
            showOwnRoles(player, profile);
            return true;
        }
        
        // /role remove - gỡ bỏ role
        if (args[0].equalsIgnoreCase("remove")) {
            roleService.removeRole(profile);
            player.sendMessage("§aĐã gỡ bỏ role!");
            return true;
        }
        
        // /role <role> - set role cua chinh minh (neu da so huu)
        String roleName = args[0].toUpperCase();
        Title title;
        
        try {
            title = Title.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cKhông tìm thấy role: " + roleName);
            showOwnRoles(player, profile);
            return true;
        }
        
        if (title == Title.NONE) {
            roleService.removeRole(profile);
            player.sendMessage("§aĐã gỡ bỏ role!");
            return true;
        }
        
        // Chi set neu da so huu
        boolean success = roleService.setOwnRole(profile, title);
        if (success) {
            player.sendMessage("§aĐã set role: " + title.getFullDisplay());
        } else {
            player.sendMessage("§cBạn chưa sở hữu role này!");
            showOwnRoles(player, profile);
        }
        
        return true;
    }
    
    /**
     * Hien thi role da so huu cua chinh minh
     */
    private void showOwnRoles(Player player, PlayerProfile profile) {
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━ §e⚡ ROLE CỦA BẠN §6§l━━━━━━━━");
        player.sendMessage("");
        
        Title activeRole = profile.getActiveTitle();
        player.sendMessage("§7Role hiện tại: " + (activeRole == Title.NONE ? "§7Không có" : activeRole.getFullDisplay()));
        player.sendMessage("");
        
        player.sendMessage("§e§lRole đã sở hữu:");
        boolean hasAny = false;
        
        for (Title title : profile.getUnlockedTitles()) {
            if (title == Title.NONE) continue;
            hasAny = true;
            
            String active = title == activeRole ? " §a✓" : "";
            player.sendMessage("  §7► " + title.getFullDisplay() + 
                             " §8[" + title.getRarity().getDisplayName() + "]" + active);
        }
        
        if (!hasAny) {
            player.sendMessage("  §7Chưa có role nào!");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Sử dụng: §f/role <role> §7để set role");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
    }
    
    /**
     * Tab completion - chi suggest role da so huu
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            return new ArrayList<>();
        }
        
        // Args 0: role name hoac remove
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("remove");
            
            for (Title title : profile.getUnlockedTitles()) {
                if (title != Title.NONE) {
                    completions.add(title.name().toLowerCase());
                }
            }
            
            String input = args[0].toLowerCase();
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
