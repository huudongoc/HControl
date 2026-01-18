package hcontrol.plugin.command;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.sect.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SECT COMMAND
 * /sect create <name> - Tạo môn phái
 * /sect info [name]   - Xem thông tin môn phái
 * /sect list          - Danh sách môn phái
 * /sect join <name>   - Xin gia nhập
 * /sect leave         - Rời môn phái
 * /sect invite <player> - Mời người
 * /sect accept/deny   - Chấp nhận/từ chối lời mời
 * /sect kick <player> - Kick thành viên
 * /sect promote <player> - Thăng cấp
 * /sect demote <player>  - Giáng cấp
 * /sect transfer <player> - Chuyển quyền chưởng môn
 * /sect disband       - Giải tán môn phái
 * /sect members       - Danh sách thành viên
 */
public class SectCommand implements CommandExecutor, TabCompleter {
    
    private final SectService sectService;
    private final SectManager sectManager;
    private final PlayerManager playerManager;
    
    public SectCommand(SectService sectService, PlayerManager playerManager) {
        this.sectService = sectService;
        this.sectManager = sectService.getSectManager();
        this.playerManager = playerManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        if (args.length == 0) {
            // Mặc định: xem info môn phái của mình
            handleInfo(player, null);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "info" -> handleInfo(player, args.length > 1 ? args[1] : null);
            case "list" -> handleList(player);
            case "join" -> handleJoin(player, args);
            case "leave" -> handleLeave(player);
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            case "kick" -> handleKick(player, args);
            case "promote" -> handlePromote(player, args);
            case "demote" -> handleDemote(player, args);
            case "transfer" -> handleTransfer(player, args);
            case "disband" -> handleDisband(player);
            case "members" -> handleMembers(player);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }
        
        return true;
    }
    
    // ===== SUB COMMANDS =====
    
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect create <tên môn phái>");
            return;
        }
        
        String sectName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String error = sectService.createSect(player, sectName);
        
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§a§l✓ §7Đã sáng lập môn phái §6" + sectName + "§7!");
            player.sendMessage("§7Dùng §e/sect info §7để xem thông tin.");
        }
    }
    
    private void handleInfo(Player player, String sectName) {
        Sect sect;
        
        if (sectName == null) {
            sect = sectManager.getPlayerSect(player.getUniqueId());
            if (sect == null) {
                player.sendMessage("§cBạn không có môn phái! Dùng §e/sect list §cđể xem danh sách.");
                return;
            }
        } else {
            sect = sectManager.getSect(sectName);
            if (sect == null) {
                player.sendMessage("§cKhông tìm thấy môn phái: " + sectName);
                return;
            }
        }
        
        // Hiển thị thông tin
        SectMember leader = sect.getMember(sect.getLeaderUuid());
        String leaderName = leader != null ? leader.getPlayerName() : "Unknown";
        
        player.sendMessage("§6═══════════════════════════════════════");
        player.sendMessage("§6§l" + sect.getName());
        player.sendMessage("§7" + sect.getDescription());
        if (!sect.getMotto().isEmpty()) {
            player.sendMessage("§e\"" + sect.getMotto() + "\"");
        }
        player.sendMessage("");
        player.sendMessage("§eChưởng Môn: §f" + leaderName);
        player.sendMessage("§eThành viên: §f" + sect.getMemberCount() + "/" + sect.getMaxMembers());
        player.sendMessage("§eCấp môn phái: §f" + sect.getLevel());
        player.sendMessage("§eKinh nghiệm: §f" + sect.getExperience() + "/" + sect.getRequiredExpForLevel(sect.getLevel() + 1));
        player.sendMessage("§eKho báu: §f" + sect.getTreasury() + " Linh Thạch");
        player.sendMessage("§eTrạng thái: " + (sect.isRecruiting() ? "§aTuyển người" : "§cĐóng cửa"));
        player.sendMessage("§6═══════════════════════════════════════");
    }
    
    private void handleList(Player player) {
        Collection<Sect> allSects = sectManager.getAllSects();
        
        if (allSects.isEmpty()) {
            player.sendMessage("§7Chưa có môn phái nào!");
            return;
        }
        
        player.sendMessage("§6═══════ DANH SÁCH MÔN PHÁI ═══════");
        for (Sect sect : allSects) {
            String status = sect.isRecruiting() ? "§a[Tuyển]" : "§c[Đóng]";
            player.sendMessage(status + " §6" + sect.getName() + " §7- " + 
                sect.getMemberCount() + "/" + sect.getMaxMembers() + " thành viên");
        }
        player.sendMessage("§6═══════════════════════════════════");
        player.sendMessage("§7Dùng §e/sect join <tên> §7để gia nhập.");
    }
    
    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect join <tên môn phái>");
            return;
        }
        
        String sectName = args[1];
        String error = sectService.requestJoin(player, sectName);
        
        if (error != null) {
            player.sendMessage(error);
        } else {
            Sect sect = sectManager.getSect(sectName);
            if (sect != null && sect.isRequireApproval()) {
                player.sendMessage("§a§l✓ §7Đã gửi đơn xin gia nhập §6" + sect.getName() + "§7!");
                player.sendMessage("§7Vui lòng chờ Trưởng lão duyệt.");
            } else {
                player.sendMessage("§a§l✓ §7Đã gia nhập §6" + sectManager.getSect(sectName).getName() + "§7!");
            }
        }
    }
    
    private void handleLeave(Player player) {
        Sect sect = sectManager.getPlayerSect(player.getUniqueId());
        if (sect == null) {
            player.sendMessage("§cBạn không có môn phái!");
            return;
        }
        
        String sectName = sect.getName();
        String error = sectService.leaveSect(player);
        
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§c§l✗ §7Đã rời khỏi §6" + sectName + "§7!");
        }
    }
    
    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect invite <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cKhông tìm thấy player: " + args[1]);
            return;
        }
        
        String error = sectService.invitePlayer(player, target);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§a§l✓ §7Đã mời §e" + target.getName() + " §7gia nhập môn phái!");
        }
    }
    
    private void handleAccept(Player player) {
        String error = sectService.acceptInvite(player);
        if (error != null) {
            player.sendMessage(error);
        } else {
            Sect sect = sectManager.getPlayerSect(player.getUniqueId());
            if (sect != null) {
                player.sendMessage("§a§l✓ §7Đã gia nhập §6" + sect.getName() + "§7!");
            }
        }
    }
    
    private void handleDeny(Player player) {
        String error = sectService.denyInvite(player);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§7Đã từ chối lời mời.");
        }
    }
    
    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect kick <player>");
            return;
        }
        
        String error = sectService.kickMember(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§a§l✓ §7Đã kick §e" + args[1] + " §7khỏi môn phái!");
        }
    }
    
    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect promote <player>");
            return;
        }
        
        String error = sectService.promoteMember(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§a§l✓ §7Đã thăng cấp §e" + args[1] + "§7!");
        }
    }
    
    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect demote <player>");
            return;
        }
        
        String error = sectService.demoteMember(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§c§l✗ §7Đã giáng cấp §e" + args[1] + "§7!");
        }
    }
    
    private void handleTransfer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /sect transfer <player>");
            return;
        }
        
        String error = sectService.transferLeadership(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§a§l✓ §7Đã chuyển giao quyền Chưởng Môn cho §e" + args[1] + "§7!");
        }
    }
    
    private void handleDisband(Player player) {
        Sect sect = sectManager.getPlayerSect(player.getUniqueId());
        if (sect == null) {
            player.sendMessage("§cBạn không có môn phái!");
            return;
        }
        
        String sectName = sect.getName();
        String error = sectService.disbandSect(player);
        
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§c§l✗ §7Đã giải tán §6" + sectName + "§7!");
        }
    }
    
    private void handleMembers(Player player) {
        Sect sect = sectManager.getPlayerSect(player.getUniqueId());
        if (sect == null) {
            player.sendMessage("§cBạn không có môn phái!");
            return;
        }
        
        player.sendMessage("§6═══════ THÀNH VIÊN " + sect.getName().toUpperCase() + " ═══════");
        
        // Sắp xếp theo rank (cao -> thấp)
        List<SectMember> members = new ArrayList<>(sect.getAllMembers());
        members.sort((a, b) -> b.getRank().getLevel() - a.getRank().getLevel());
        
        for (SectMember member : members) {
            Player p = Bukkit.getPlayer(member.getPlayerUuid());
            String online = (p != null && p.isOnline()) ? "§a●" : "§c○";
            
            player.sendMessage(online + " " + member.getFullTitle() + 
                " §7- Cống hiến: §e" + member.getContribution());
        }
        
        player.sendMessage("§6═══════════════════════════════════════");
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6═══════ LỆNH MÔN PHÁI ═══════");
        player.sendMessage("§e/sect create <tên> §7- Sáng lập môn phái");
        player.sendMessage("§e/sect info [tên] §7- Xem thông tin");
        player.sendMessage("§e/sect list §7- Danh sách môn phái");
        player.sendMessage("§e/sect join <tên> §7- Xin gia nhập");
        player.sendMessage("§e/sect leave §7- Rời môn phái");
        player.sendMessage("§e/sect members §7- Danh sách thành viên");
        player.sendMessage("§e/sect invite <player> §7- Mời người");
        player.sendMessage("§e/sect accept/deny §7- Đồng ý/Từ chối");
        player.sendMessage("§e/sect kick <player> §7- Kick thành viên");
        player.sendMessage("§e/sect promote/demote <player> §7- Thăng/Giáng cấp");
        player.sendMessage("§e/sect transfer <player> §7- Chuyển quyền chưởng môn");
        player.sendMessage("§e/sect disband §7- Giải tán môn phái");
        player.sendMessage("§6═══════════════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList(
                "create", "info", "list", "join", "leave", "members",
                "invite", "accept", "deny", "kick", "promote", "demote",
                "transfer", "disband", "help"
            ));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            
            if (sub.equals("join") || sub.equals("info")) {
                // Suggest sect names
                suggestions.addAll(sectManager.getAllSects().stream()
                    .map(Sect::getSectId)
                    .collect(Collectors.toList()));
            } else if (sub.equals("invite")) {
                // Suggest online players not in a sect
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!sectManager.hasPlayerSect(p.getUniqueId())) {
                        suggestions.add(p.getName());
                    }
                }
            } else if (sub.equals("kick") || sub.equals("promote") || sub.equals("demote") || sub.equals("transfer")) {
                // Suggest sect members
                if (sender instanceof Player player) {
                    Sect sect = sectManager.getPlayerSect(player.getUniqueId());
                    if (sect != null) {
                        for (SectMember member : sect.getAllMembers()) {
                            if (!member.getPlayerUuid().equals(player.getUniqueId())) {
                                suggestions.add(member.getPlayerName());
                            }
                        }
                    }
                }
            }
        }
        
        String lastArg = args[args.length - 1].toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lastArg))
            .collect(Collectors.toList());
    }
}
