package hcontrol.plugin.command;

import hcontrol.plugin.master.*;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.listener.SkillCreatorListener;
import hcontrol.plugin.skill.custom.SkillTemplate;
import hcontrol.plugin.skill.custom.SkillTemplateRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MASTER COMMAND
 * /master                    - Xem thông tin sư đồ
 * /master request <player>   - Xin bái sư
 * /master accept <player>    - Chấp nhận đệ tử
 * /master deny <player>      - Từ chối đệ tử
 * /master invite <player>    - Mời làm đệ tử
 * /master join               - Chấp nhận lời mời
 * /master decline            - Từ chối lời mời
 * /master leave              - Rời khỏi sư phụ
 * /master kick <disciple>    - Đuổi đệ tử
 * /master teach <disciple> <skill> - Truyền skill
 * /master list               - Danh sách sư phụ đang nhận đệ tử
 * /master toggle             - Bật/tắt nhận đệ tử
 * /master title <title>      - Đặt danh xưng
 * /master disciples          - Xem danh sách đệ tử
 * /master createskill        - Mở GUI tạo công pháp mới
 * /master myskills           - Xem công pháp đã tạo
 */
public class MasterCommand implements CommandExecutor, TabCompleter {
    
    private final MasterService masterService;
    private final MasterManager masterManager;
    private final PlayerManager playerManager;
    private SkillCreatorListener skillCreatorListener;
    
    public MasterCommand(MasterService masterService, PlayerManager playerManager) {
        this.masterService = masterService;
        this.masterManager = masterService.getMasterManager();
        this.playerManager = playerManager;
    }
    
    public void setSkillCreatorListener(SkillCreatorListener listener) {
        this.skillCreatorListener = listener;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        if (args.length == 0) {
            handleInfo(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "request", "bai" -> handleRequest(player, args);
            case "accept" -> handleAccept(player, args);
            case "deny" -> handleDeny(player, args);
            case "invite", "moi" -> handleInvite(player, args);
            case "join" -> handleJoin(player);
            case "decline" -> handleDecline(player);
            case "leave", "roi" -> handleLeave(player);
            case "kick", "duoi" -> handleKick(player, args);
            case "teach", "truyen" -> handleTeach(player, args);
            case "list" -> handleList(player);
            case "toggle" -> handleToggle(player);
            case "title", "xung" -> handleTitle(player, args);
            case "disciples", "detu" -> handleDisciples(player);
            case "createskill", "taoskill", "sangche" -> handleCreateSkill(player);
            case "myskills", "congphap" -> handleMySkills(player);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }
        
        return true;
    }
    
    // ===== SUB COMMANDS =====
    
    private void handleInfo(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Kiểm tra có phải đệ tử không
        DiscipleInfo discipleInfo = masterManager.getDisciple(uuid);
        if (discipleInfo != null && discipleInfo.hasMaster()) {
            showDiscipleInfo(player, discipleInfo);
            return;
        }
        
        // Kiểm tra có phải sư phụ không
        MasterRelation masterRel = masterManager.getMaster(uuid);
        if (masterRel != null && masterRel.getDiscipleCount() > 0) {
            showMasterInfo(player, masterRel);
            return;
        }
        
        // Chưa có quan hệ
        player.sendMessage("§7Bạn chưa có sư phụ và chưa nhận đệ tử.");
        player.sendMessage("§7Dùng §e/master request <player> §7để bái sư.");
        player.sendMessage("§7Dùng §e/master invite <player> §7để mời đệ tử.");
        player.sendMessage("§7Dùng §e/master list §7để xem sư phụ đang nhận đệ tử.");
    }
    
    private void showDiscipleInfo(Player player, DiscipleInfo info) {
        player.sendMessage("§6═══════════════════════════════════════");
        player.sendMessage("§e§lTHÔNG TIN ĐỆ TỬ");
        player.sendMessage("");
        player.sendMessage("§eSư phụ: §6" + info.getMasterName());
        
        if (info.getJoinedAt() != null) {
            Duration duration = Duration.between(info.getJoinedAt(), Instant.now());
            long days = duration.toDays();
            player.sendMessage("§eThời gian bái sư: §f" + days + " ngày");
        }
        
        player.sendMessage("§eĐiểm hiếu kính: §f" + info.getContribution());
        
        if (!info.getLearnedSkills().isEmpty()) {
            player.sendMessage("§eSkills được truyền: §f" + String.join(", ", info.getLearnedSkills()));
        }
        
        player.sendMessage("");
        player.sendMessage("§7/master leave §8- Rời khỏi sư phụ");
        player.sendMessage("§6═══════════════════════════════════════");
    }
    
    private void showMasterInfo(Player player, MasterRelation masterRel) {
        player.sendMessage("§6═══════════════════════════════════════");
        player.sendMessage("§6§l" + masterRel.getDisplayTitle());
        player.sendMessage("");
        player.sendMessage("§eSố đệ tử: §f" + masterRel.getDiscipleCount() + "/" + masterRel.getMaxDisciples());
        player.sendMessage("§eTrạng thái: " + (masterRel.isAcceptingDisciples() ? "§aĐang nhận đệ tử" : "§cĐóng"));
        
        if (!masterRel.getTaughtSkills().isEmpty()) {
            player.sendMessage("§eSkills đã truyền: §f" + masterRel.getTaughtSkills().size() + " skills");
        }
        
        // Danh sách đệ tử
        player.sendMessage("");
        player.sendMessage("§e§lĐệ tử:");
        for (UUID dUuid : masterRel.getDisciples()) {
            DiscipleInfo dInfo = masterManager.getDisciple(dUuid);
            if (dInfo != null) {
                Player dp = Bukkit.getPlayer(dUuid);
                String online = (dp != null && dp.isOnline()) ? "§a●" : "§c○";
                player.sendMessage("  " + online + " §f" + dInfo.getDiscipleName() + 
                    " §7- Hiếu kính: §e" + dInfo.getContribution());
            }
        }
        
        // Pending requests
        List<String> pending = masterService.getPendingRequestsFor(player.getUniqueId());
        if (!pending.isEmpty()) {
            player.sendMessage("");
            player.sendMessage("§e§lYêu cầu bái sư:");
            for (String name : pending) {
                player.sendMessage("  §e" + name + " §7- /master accept " + name);
            }
        }
        
        player.sendMessage("§6═══════════════════════════════════════");
    }
    
    private void handleRequest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /master request <tên sư phụ>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cKhông tìm thấy player: " + args[1]);
            return;
        }
        
        String error = masterService.requestMaster(player, target);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§a§l✓ §7Đã gửi yêu cầu bái sư tới §6" + target.getName() + "§7!");
            player.sendMessage("§7Vui lòng chờ phản hồi...");
        }
    }
    
    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /master accept <tên đệ tử>");
            return;
        }
        
        String error = masterService.acceptDisciple(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        }
    }
    
    private void handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /master deny <tên đệ tử>");
            return;
        }
        
        String error = masterService.denyDisciple(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§7Đã từ chối " + args[1] + ".");
        }
    }
    
    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /master invite <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cKhông tìm thấy player: " + args[1]);
            return;
        }
        
        String error = masterService.inviteDisciple(player, target);
        if (error != null) {
            player.sendMessage(error);
        }
    }
    
    private void handleJoin(Player player) {
        String error = masterService.acceptInvite(player);
        if (error != null) {
            player.sendMessage(error);
        }
    }
    
    private void handleDecline(Player player) {
        String error = masterService.declineInvite(player);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("§7Đã từ chối lời mời.");
        }
    }
    
    private void handleLeave(Player player) {
        String error = masterService.leavemaster(player);
        if (error != null) {
            player.sendMessage(error);
        }
    }
    
    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /master kick <tên đệ tử>");
            return;
        }
        
        String error = masterService.kickDisciple(player, args[1]);
        if (error != null) {
            player.sendMessage(error);
        }
    }
    
    private void handleTeach(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cCách dùng: /master teach <tên đệ tử> <skill_id>");
            return;
        }
        
        String error = masterService.teachSkill(player, args[1], args[2]);
        if (error != null) {
            player.sendMessage(error);
        }
    }
    
    private void handleList(Player player) {
        Collection<MasterRelation> allMasters = masterManager.getAllMasters();
        List<MasterRelation> available = new ArrayList<>();
        
        for (MasterRelation master : allMasters) {
            if (master.isAcceptingDisciples() && !master.isFull()) {
                available.add(master);
            }
        }
        
        if (available.isEmpty()) {
            player.sendMessage("§7Hiện không có ai đang nhận đệ tử.");
            return;
        }
        
        player.sendMessage("§6═══════ SƯ PHỤ ĐANG NHẬN ĐỆ TỬ ═══════");
        for (MasterRelation master : available) {
            PlayerProfile profile = playerManager.get(master.getMasterUuid());
            String realm = profile != null ? profile.getRealm().getDisplayName() : "?";
            
            Player mp = Bukkit.getPlayer(master.getMasterUuid());
            String online = (mp != null && mp.isOnline()) ? "§a●" : "§c○";
            
            player.sendMessage(online + " §6" + master.getMasterName() + 
                " §7[" + realm + "] §f" + master.getDiscipleCount() + "/" + master.getMaxDisciples());
        }
        player.sendMessage("§6═════════════════════════════════════════");
        player.sendMessage("§7Dùng §e/master request <tên> §7để xin bái sư.");
    }
    
    private void handleToggle(Player player) {
        String result = masterService.toggleAccepting(player);
        player.sendMessage(result);
    }
    
    private void handleTitle(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /master title <danh xưng>");
            return;
        }
        
        String title = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String result = masterService.setTitle(player, title);
        player.sendMessage(result);
    }
    
    private void handleDisciples(Player player) {
        UUID uuid = player.getUniqueId();
        MasterRelation masterRel = masterManager.getMaster(uuid);
        
        if (masterRel == null || masterRel.getDiscipleCount() == 0) {
            player.sendMessage("§7Bạn không có đệ tử nào.");
            return;
        }
        
        player.sendMessage("§6═══════ DANH SÁCH ĐỆ TỬ ═══════");
        for (UUID dUuid : masterRel.getDisciples()) {
            DiscipleInfo dInfo = masterManager.getDisciple(dUuid);
            if (dInfo == null) continue;
            
            Player dp = Bukkit.getPlayer(dUuid);
            String online = (dp != null && dp.isOnline()) ? "§a●" : "§c○";
            
            PlayerProfile profile = playerManager.get(dUuid);
            String realm = profile != null ? profile.getRealm().getDisplayName() : "?";
            
            player.sendMessage(online + " §f" + dInfo.getDiscipleName() + 
                " §7[" + realm + "] §eHiếu kính: " + dInfo.getContribution());
            
            if (!dInfo.getLearnedSkills().isEmpty()) {
                player.sendMessage("   §7Skills: " + String.join(", ", dInfo.getLearnedSkills()));
            }
        }
        player.sendMessage("§6═════════════════════════════════════");
    }
    
    private void handleCreateSkill(Player player) {
        if (skillCreatorListener == null) {
            player.sendMessage("§cTính năng chưa được khởi tạo!");
            return;
        }
        
        // SkillCreatorListener sẽ kiểm tra cảnh giới và mở GUI
        skillCreatorListener.startSession(player);
    }
    
    private void handleMySkills(Player player) {
        if (skillCreatorListener == null) {
            player.sendMessage("§cTính năng chưa được khởi tạo!");
            return;
        }
        
        // Truy cập SkillTemplateRegistry thông qua CoreContext
        // (Cần inject hoặc lấy từ listener)
        // Tạm thời thông báo
        player.sendMessage("§e[Công Pháp] Xem công pháp đã tạo trong GUI sắp có!");
        player.sendMessage("§7Dùng §e/master createskill §7để tạo công pháp mới.");
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6═══════ LỆNH BÁI SƯ ═══════");
        player.sendMessage("§e/master §7- Xem thông tin sư đồ");
        player.sendMessage("§e/master request <player> §7- Xin bái sư");
        player.sendMessage("§e/master accept <player> §7- Nhận đệ tử");
        player.sendMessage("§e/master deny <player> §7- Từ chối");
        player.sendMessage("§e/master invite <player> §7- Mời làm đệ tử");
        player.sendMessage("§e/master join / decline §7- Chấp nhận/từ chối lời mời");
        player.sendMessage("§e/master leave §7- Rời khỏi sư phụ");
        player.sendMessage("§e/master kick <player> §7- Đuổi đệ tử");
        player.sendMessage("§e/master teach <player> <skill> §7- Truyền skill");
        player.sendMessage("§e/master disciples §7- Danh sách đệ tử");
        player.sendMessage("§e/master list §7- Sư phụ đang nhận đệ tử");
        player.sendMessage("§e/master toggle §7- Bật/tắt nhận đệ tử");
        player.sendMessage("§e/master title <xưng> §7- Đặt danh xưng");
        player.sendMessage("§6─────── §fSÁNG TẠO CÔNG PHÁP §6───────");
        player.sendMessage("§e/master createskill §7- Mở GUI tạo công pháp");
        player.sendMessage("§e/master myskills §7- Xem công pháp đã tạo");
        player.sendMessage("§6═══════════════════════════════");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList(
                "request", "accept", "deny", "invite", "join", "decline",
                "leave", "kick", "teach", "list", "toggle", "title", "disciples",
                "createskill", "myskills", "help"
            ));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            
            if (sub.equals("request") || sub.equals("invite")) {
                // Suggest online players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.equals(sender)) {
                        suggestions.add(p.getName());
                    }
                }
            } else if (sub.equals("accept") || sub.equals("deny")) {
                // Suggest pending requests
                if (sender instanceof Player player) {
                    suggestions.addAll(masterService.getPendingRequestsFor(player.getUniqueId()));
                }
            } else if (sub.equals("kick") || sub.equals("teach")) {
                // Suggest disciples
                if (sender instanceof Player player) {
                    MasterRelation masterRel = masterManager.getMaster(player.getUniqueId());
                    if (masterRel != null) {
                        for (UUID dUuid : masterRel.getDisciples()) {
                            DiscipleInfo info = masterManager.getDisciple(dUuid);
                            if (info != null) {
                                suggestions.add(info.getDiscipleName());
                            }
                        }
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("teach")) {
            // Suggest skills the master knows
            if (sender instanceof Player player) {
                PlayerProfile profile = playerManager.get(player.getUniqueId());
                if (profile != null) {
                    suggestions.addAll(profile.getLearnedSkills());
                }
            }
        }
        
        String lastArg = args[args.length - 1].toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lastArg))
            .collect(Collectors.toList());
    }
}
