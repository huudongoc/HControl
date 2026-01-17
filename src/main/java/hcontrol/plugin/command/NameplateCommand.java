package hcontrol.plugin.command;

import hcontrol.plugin.ui.player.NameplateService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NAMEPLATE COMMAND
 * /nameplate - Hiển thị trạng thái
 * /nameplate toggle <option> - Bật/tắt hiển thị
 * /nameplate refresh - Làm mới nameplate
 */
public class NameplateCommand implements CommandExecutor, TabCompleter {
    
    private final NameplateService nameplateService;
    
    public NameplateCommand(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showStatus(sender);
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "toggle" -> handleToggle(sender, args);
            case "refresh" -> handleRefresh(sender);
            case "help" -> showHelp(sender);
            default -> showHelp(sender);
        }
        
        return true;
    }
    
    private void showStatus(CommandSender sender) {
        sender.sendMessage("§6═══════ NAMEPLATE STATUS ═══════");
        sender.sendMessage("§eMôn Phái: " + (nameplateService.isShowSect() ? "§aON" : "§cOFF"));
        sender.sendMessage("§eCảnh Giới: " + (nameplateService.isShowRealm() ? "§aON" : "§cOFF"));
        sender.sendMessage("§eDanh Hiệu: " + (nameplateService.isShowTitle() ? "§aON" : "§cOFF"));
        sender.sendMessage("§eSư/Đồ: " + (nameplateService.isShowMasterStatus() ? "§aON" : "§cOFF"));
        sender.sendMessage("§7─────────────────────────────────");
        sender.sendMessage("§7/nameplate toggle <option> §f- Bật/tắt");
        sender.sendMessage("§7/nameplate refresh §f- Làm mới");
        sender.sendMessage("§6═════════════════════════════════");
    }
    
    private void handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hcontrol.admin")) {
            sender.sendMessage("§cKhông có quyền!");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /nameplate toggle <sect|realm|title|master>");
            return;
        }
        
        String option = args[1].toLowerCase();
        
        switch (option) {
            case "sect", "monphai" -> {
                nameplateService.setShowSect(!nameplateService.isShowSect());
                sender.sendMessage("§a[Nameplate] Môn Phái: " + 
                    (nameplateService.isShowSect() ? "§aBẬT" : "§cTẮT"));
            }
            case "realm", "canhgioi" -> {
                nameplateService.setShowRealm(!nameplateService.isShowRealm());
                sender.sendMessage("§a[Nameplate] Cảnh Giới: " + 
                    (nameplateService.isShowRealm() ? "§aBẬT" : "§cTẮT"));
            }
            case "title", "danhhieu" -> {
                nameplateService.setShowTitle(!nameplateService.isShowTitle());
                sender.sendMessage("§a[Nameplate] Danh Hiệu: " + 
                    (nameplateService.isShowTitle() ? "§aBẬT" : "§cTẮT"));
            }
            case "master", "sudo" -> {
                nameplateService.setShowMasterStatus(!nameplateService.isShowMasterStatus());
                sender.sendMessage("§a[Nameplate] Sư/Đồ: " + 
                    (nameplateService.isShowMasterStatus() ? "§aBẬT" : "§cTẮT"));
            }
            default -> sender.sendMessage("§cOption không hợp lệ: sect, realm, title, master");
        }
        
        // Cập nhật nameplate tất cả player
        nameplateService.updateAllNameplates();
    }
    
    private void handleRefresh(CommandSender sender) {
        if (sender instanceof Player player) {
            nameplateService.updateNameplate(player, true);
            sender.sendMessage("§a[Nameplate] Đã làm mới nameplate của bạn!");
        } else if (sender.hasPermission("hcontrol.admin")) {
            nameplateService.updateAllNameplates();
            sender.sendMessage("§a[Nameplate] Đã làm mới nameplate tất cả player!");
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6═══════ NAMEPLATE HELP ═══════");
        sender.sendMessage("§e/nameplate §f- Xem trạng thái");
        sender.sendMessage("§e/nameplate toggle <option> §f- Bật/tắt hiển thị");
        sender.sendMessage("§e/nameplate refresh §f- Làm mới nameplate");
        sender.sendMessage("§7Options: sect, realm, title, master");
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("toggle", "refresh", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            suggestions.addAll(Arrays.asList("sect", "realm", "title", "master"));
        }
        
        String lastArg = args[args.length - 1].toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lastArg))
            .collect(Collectors.toList());
    }
}
