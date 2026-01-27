package hcontrol.plugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import hcontrol.plugin.classsystem.ClassProfile;
import hcontrol.plugin.classsystem.ClassService;
import hcontrol.plugin.classsystem.ClassType;
import hcontrol.plugin.core.ClassContext;
import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;

/**
 * PHASE 5 — CLASS COMMAND
 * Command để set và xem class cho player
 * Usage: /class <set|info|list> [classType]
 */
public class ClassCommand implements CommandExecutor, TabCompleter {
    
    private final PlayerManager playerManager;
    
    public ClassCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    /**
     * Lazy load ClassService từ CoreContext
     */
    private ClassService getClassService() {
        CoreContext ctx = CoreContext.getInstance();
        ClassContext classContext = ctx.getClassContext();
        if (classContext == null) {
            return null;
        }
        return classContext.getClassService();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được command này!");
            return true;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cProfile chưa load!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "set":
                handleSet(player, profile, args);
                break;
            case "info":
                handleInfo(player, profile);
                break;
            case "list":
                handleList(player);
                break;
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Set class cho player
     */
    private void handleSet(Player player, PlayerProfile profile, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /class set <classType>");
            player.sendMessage("§7Ví dụ: /class set SWORD_CULTIVATOR");
            return;
        }
        
        String classTypeStr = args[1].toUpperCase();
        ClassType classType;
        
        try {
            classType = ClassType.valueOf(classTypeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cClass type không hợp lệ: " + classTypeStr);
            player.sendMessage("§7Dùng /class list để xem danh sách class types");
            return;
        }
        
        // Set class profile
        ClassProfile classProfile = new ClassProfile(classType);
        profile.setClassProfile(classProfile);

        // Persist immediately to storage (if available)
        try {
            CoreContext ctx = CoreContext.getInstance();
            if (ctx != null && ctx.getPlayerContext() != null && ctx.getPlayerContext().getPlayerStorage() != null) {
                ctx.getPlayerContext().getPlayerStorage().save(profile);
            }
        } catch (Exception e) {
            // ignore persistence errors but log to sender
            player.sendMessage("§cLỗi khi lưu class: " + e.getMessage());
        }

        player.sendMessage("§a✓ Đã set class: §e" + classType);
        player.sendMessage("§7Mastery Level: §e" + classProfile.getMasteryLevel());
    }
    
    /**
     * Xem class hiện tại
     */
    private void handleInfo(Player player, PlayerProfile profile) {
        ClassProfile classProfile = profile.getClassProfile();
        
        if (classProfile == null) {
            player.sendMessage("§7Bạn chưa chọn class!");
            player.sendMessage("§7Dùng /class set <classType> để chọn class");
            return;
        }
        
        player.sendMessage("§6=== CLASS INFO ===");
        player.sendMessage("§7Class: §e" + classProfile.getType());
        player.sendMessage("§7Mastery Level: §e" + classProfile.getMasteryLevel());
        
        // Show modifiers nếu có ClassService
        ClassService classService = getClassService();
        if (classService != null) {
            var modifiers = classService.getModifiers(profile);
            if (!modifiers.isEmpty()) {
                player.sendMessage("§7Modifiers: §e" + modifiers.size());
                for (var modifier : modifiers) {
                    player.sendMessage("  §7- " + modifier.getType() + " modifier");
                }
            }
        }
    }
    
    /**
     * List tất cả class types
     */
    private void handleList(Player player) {
        // Open GUI if possible
        try {
            hcontrol.plugin.ui.classsystem.ClassMenuGUI menu = new hcontrol.plugin.ui.classsystem.ClassMenuGUI(playerManager);
            menu.open(player);
            return;
        } catch (Throwable t) {
            // Fallback to text list
        }

        player.sendMessage("§6=== CLASS TYPES ===");
        for (ClassType type : ClassType.values()) {
            player.sendMessage("§7- §e" + type);
        }
    }
    
    /**
     * Send help message
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6=== CLASS COMMAND ===");
        player.sendMessage("§7/class set <classType> §7- Set class cho bạn");
        player.sendMessage("§7/class info §7- Xem class hiện tại");
        player.sendMessage("§7/class list §7- List tất cả class types");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Tab complete cho action
            return Arrays.asList("set", "info", "list").stream()
                .filter(action -> action.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // Tab complete cho class type
            return Arrays.stream(ClassType.values())
                .map(Enum::name)
                .filter(type -> type.startsWith(args[1].toUpperCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
