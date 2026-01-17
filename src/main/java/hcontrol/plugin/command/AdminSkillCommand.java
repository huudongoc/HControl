package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.skill.custom.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ADMIN SKILL COMMAND
 * Backdoor commands cho admin quản lý skill system
 * 
 * /askill list                          - List tất cả templates
 * /askill info <template_id>            - Xem chi tiết template
 * /askill give <player> <template_id>   - Give skill cho player
 * /askill remove <player> <template_id> - Xóa skill của player
 * /askill setmastery <player> <id> <value> - Set mastery
 * /askill setrefinement <player> <id> <value> - Set refinement
 * /askill create <id> <name> <category> - Tạo template nhanh
 * /askill delete <template_id>          - Xóa template
 * /askill reload                        - Reload data
 * /askill bypass <player>               - Mở GUI bypass realm check
 */
public class AdminSkillCommand implements CommandExecutor, TabCompleter {
    
    private static final String PERMISSION = "hcontrol.admin.skill";
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            sender.sendMessage("§c[Admin] Bạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "list" -> handleList(sender, args);
            case "info" -> handleInfo(sender, args);
            case "give" -> handleGive(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "setmastery" -> handleSetMastery(sender, args);
            case "setrefinement", "setrefine" -> handleSetRefinement(sender, args);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "reload" -> handleReload(sender);
            case "bypass" -> handleBypass(sender, args);
            case "instances" -> handleInstances(sender, args);
            case "stats" -> handleStats(sender);
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    // ===== SUB COMMANDS =====
    
    private void handleList(CommandSender sender, String[] args) {
        SkillTemplateRegistry registry = getTemplateRegistry();
        if (registry == null) {
            sender.sendMessage("§c[Admin] Registry chưa được khởi tạo!");
            return;
        }
        
        Collection<SkillTemplate> templates = registry.getAllTemplates();
        
        if (templates.isEmpty()) {
            sender.sendMessage("§7[Admin] Không có template nào.");
            return;
        }
        
        sender.sendMessage("§6═══════ SKILL TEMPLATES (" + templates.size() + ") ═══════");
        
        for (SkillTemplate t : templates) {
            String elementStr = t.getElement() != null ? t.getElement().getColoredName() : "§7None";
            int learners = registry.getLearnerCount(t.getId());
            
            sender.sendMessage("§e" + t.getId());
            sender.sendMessage("  §7Name: §f" + t.getName() + " §7| " + t.getCategory().getColoredName() + " §7| " + elementStr);
            sender.sendMessage("  §7Power: §c" + (int)t.getBasePower() + " §7| CD: §b" + t.getCooldown() + "s §7| Learners: §a" + learners);
        }
        
        sender.sendMessage("§6═══════════════════════════════════════");
    }
    
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /askill info <template_id>");
            return;
        }
        
        SkillTemplateRegistry registry = getTemplateRegistry();
        if (registry == null) return;
        
        SkillTemplate t = registry.getTemplate(args[1]);
        if (t == null) {
            sender.sendMessage("§cKhông tìm thấy template: " + args[1]);
            return;
        }
        
        sender.sendMessage("§6═══════ TEMPLATE INFO ═══════");
        sender.sendMessage("§eID: §f" + t.getId());
        sender.sendMessage("§eName: §f" + t.getColoredName());
        sender.sendMessage("§eCreator: §f" + (t.getCreatorName() != null ? t.getCreatorName() : "System"));
        sender.sendMessage("§eCategory: " + t.getCategory().getColoredName());
        sender.sendMessage("§eElement: " + (t.getElement() != null ? t.getElement().getColoredName() : "§7None"));
        sender.sendMessage("§eTarget: " + t.getTargetType().getColoredName());
        sender.sendMessage("§7─────────────────────────────");
        sender.sendMessage("§ePower: §c" + (int)t.getBasePower());
        sender.sendMessage("§eCooldown: §b" + t.getCooldown() + "s");
        sender.sendMessage("§eMana Cost: §d" + (int)t.getManaCost());
        sender.sendMessage("§eRange: §a" + t.getRange() + " blocks");
        sender.sendMessage("§eAOE Radius: §6" + t.getAreaRadius() + " blocks");
        sender.sendMessage("§eProjectiles: §f" + t.getProjectileCount());
        sender.sendMessage("§eDuration: §f" + t.getDuration() + "s");
        sender.sendMessage("§7─────────────────────────────");
        sender.sendMessage("§eRequired Realm: " + t.getRequiredRealm().getColor() + t.getRequiredRealm().getDisplayName());
        sender.sendMessage("§eSkill Point Cost: §e" + t.getSkillPointCost());
        sender.sendMessage("§eMax Learners: §f" + t.getMaxLearners());
        sender.sendMessage("§eTransferable: " + (t.isTransferable() ? "§aYes" : "§cNo"));
        sender.sendMessage("§eLearners: §a" + registry.getLearnerCount(t.getId()));
        sender.sendMessage("§6═══════════════════════════════");
    }
    
    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /askill give <player> <template_id>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player: " + args[1]);
            return;
        }
        
        SkillTemplateRegistry registry = getTemplateRegistry();
        SkillInstanceManager manager = getInstanceManager();
        if (registry == null || manager == null) return;
        
        SkillTemplate template = registry.getTemplate(args[2]);
        if (template == null) {
            sender.sendMessage("§cKhông tìm thấy template: " + args[2]);
            return;
        }
        
        if (manager.hasLearned(target.getUniqueId(), args[2])) {
            sender.sendMessage("§c" + target.getName() + " đã học skill này rồi!");
            return;
        }
        
        SkillInstance instance = manager.learnSkill(target.getUniqueId(), target.getName(), args[2]);
        if (instance != null) {
            sender.sendMessage("§a[Admin] Đã give skill §e" + template.getName() + " §acho §f" + target.getName());
            target.sendMessage("§a[System] Bạn đã nhận được skill: " + template.getColoredName());
        } else {
            sender.sendMessage("§cLỗi khi give skill!");
        }
    }
    
    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cCách dùng: /askill remove <player> <template_id>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        UUID targetUuid;
        String targetName;
        
        if (target != null) {
            targetUuid = target.getUniqueId();
            targetName = target.getName();
        } else {
            // Try offline player
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage("§cKhông tìm thấy player: " + args[1]);
                return;
            }
            targetUuid = offlinePlayer.getUniqueId();
            targetName = args[1];
        }
        
        SkillInstanceManager manager = getInstanceManager();
        if (manager == null) return;
        
        if (!manager.hasLearned(targetUuid, args[2])) {
            sender.sendMessage("§c" + targetName + " chưa học skill này!");
            return;
        }
        
        boolean removed = manager.forgetSkill(targetUuid, args[2]);
        if (removed) {
            sender.sendMessage("§a[Admin] Đã xóa skill §e" + args[2] + " §akhỏi §f" + targetName);
            if (target != null && target.isOnline()) {
                target.sendMessage("§c[System] Skill §e" + args[2] + " §cđã bị xóa!");
            }
        } else {
            sender.sendMessage("§cLỗi khi xóa skill!");
        }
    }
    
    private void handleSetMastery(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cCách dùng: /askill setmastery <player> <template_id> <0-100>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer phải online!");
            return;
        }
        
        int value;
        try {
            value = Integer.parseInt(args[3]);
            if (value < 0 || value > 100) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage("§cMastery phải từ 0-100!");
            return;
        }
        
        SkillInstanceManager manager = getInstanceManager();
        if (manager == null) return;
        
        SkillInstance instance = manager.getSkillInstance(target.getUniqueId(), args[2]);
        if (instance == null) {
            sender.sendMessage("§c" + target.getName() + " chưa học skill " + args[2]);
            return;
        }
        
        instance.setMastery(value);
        manager.saveData();
        
        sender.sendMessage("§a[Admin] Set mastery của §f" + target.getName() + " §askill §e" + args[2] + " §a= §f" + value + "%");
    }
    
    private void handleSetRefinement(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cCách dùng: /askill setrefinement <player> <template_id> <1-9>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer phải online!");
            return;
        }
        
        int value;
        try {
            value = Integer.parseInt(args[3]);
            if (value < 1 || value > 9) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage("§cRefinement phải từ 1-9!");
            return;
        }
        
        SkillInstanceManager manager = getInstanceManager();
        if (manager == null) return;
        
        SkillInstance instance = manager.getSkillInstance(target.getUniqueId(), args[2]);
        if (instance == null) {
            sender.sendMessage("§c" + target.getName() + " chưa học skill " + args[2]);
            return;
        }
        
        instance.setRefinement(value);
        manager.saveData();
        
        sender.sendMessage("§a[Admin] Set refinement của §f" + target.getName() + " §askill §e" + args[2] + " §a= §f" + value + " (" + instance.getRefinementName() + ")");
    }
    
    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§cCách dùng: /askill create <id> <name> <category>");
            sender.sendMessage("§7Categories: ATTACK, DEFENSE, CONTROL, BUFF, HEAL, MOVEMENT");
            return;
        }
        
        String id = args[1].toLowerCase();
        String name = args[2];
        String categoryStr = args[3].toUpperCase();
        
        SkillCategory category;
        try {
            category = SkillCategory.valueOf(categoryStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cCategory không hợp lệ: " + categoryStr);
            sender.sendMessage("§7Categories: ATTACK, DEFENSE, CONTROL, BUFF, HEAL, MOVEMENT");
            return;
        }
        
        SkillTemplateRegistry registry = getTemplateRegistry();
        if (registry == null) return;
        
        if (registry.exists(id)) {
            sender.sendMessage("§cID đã tồn tại: " + id);
            return;
        }
        
        // Tạo template mặc định
        SkillTemplate template = new SkillTemplate.Builder(id)
            .name(name)
            .creator(null, "Admin")
            .category(category)
            .targetType(TargetType.SINGLE)
            .basePower(50)
            .cooldown(5)
            .manaCost(30)
            .range(10)
            .requiredRealm(CultivationRealm.PHAMNHAN)
            .transferable(true)
            .maxLearners(999)
            .build();
        
        registry.registerTemplate(template);
        
        sender.sendMessage("§a[Admin] Đã tạo template: §e" + id);
        sender.sendMessage("§7Dùng §e/askill info " + id + " §7để xem chi tiết");
    }
    
    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /askill delete <template_id>");
            return;
        }
        
        SkillTemplateRegistry registry = getTemplateRegistry();
        if (registry == null) return;
        
        if (!registry.exists(args[1])) {
            sender.sendMessage("§cKhông tìm thấy template: " + args[1]);
            return;
        }
        
        boolean removed = registry.removeTemplate(args[1]);
        if (removed) {
            sender.sendMessage("§a[Admin] Đã xóa template: §e" + args[1]);
        } else {
            sender.sendMessage("§cLỗi khi xóa template!");
        }
    }
    
    private void handleReload(CommandSender sender) {
        SkillTemplateRegistry registry = getTemplateRegistry();
        SkillInstanceManager manager = getInstanceManager();
        
        if (registry != null) {
            registry.reload();
            sender.sendMessage("§a[Admin] Đã reload SkillTemplateRegistry: §f" + registry.getTemplateCount() + " templates");
        }
        
        if (manager != null) {
            manager.reload();
            sender.sendMessage("§a[Admin] Đã reload SkillInstanceManager");
        }
    }
    
    private void handleBypass(CommandSender sender, String[] args) {
        Player target;
        
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cKhông tìm thấy player: " + args[1]);
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cCần chỉ định player: /askill bypass <player>");
            return;
        }
        
        // Mở GUI với bypass
        var listener = CoreContext.getInstance().getSkillCreatorListener();
        if (listener != null) {
            // Force start session với fake realm cao
            sender.sendMessage("§a[Admin] Mở Skill Creator GUI cho §f" + target.getName() + " §a(bypass realm check)");
            listener.startSession(target);
        } else {
            sender.sendMessage("§cSkillCreatorListener chưa được khởi tạo!");
        }
    }
    
    private void handleInstances(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cCách dùng: /askill instances <player>");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cKhông tìm thấy player: " + args[1]);
            return;
        }
        
        SkillInstanceManager manager = getInstanceManager();
        if (manager == null) return;
        
        var instances = manager.getPlayerSkillsWithTemplates(target.getUniqueId());
        
        if (instances.isEmpty()) {
            sender.sendMessage("§7" + target.getName() + " chưa học skill nào.");
            return;
        }
        
        sender.sendMessage("§6═══════ SKILLS OF " + target.getName().toUpperCase() + " ═══════");
        
        for (var entry : instances) {
            SkillInstance inst = entry.getInstance();
            SkillTemplate tmpl = entry.getTemplate();
            
            sender.sendMessage("§e" + tmpl.getId() + " §7(" + tmpl.getName() + ")");
            sender.sendMessage("  §7Mastery: §a" + inst.getMastery() + "% §7| Refinement: §e" + inst.getRefinement() + " (" + inst.getRefinementName() + ")");
            sender.sendMessage("  §7Uses: §f" + inst.getUseCount() + " §7| Final Dmg: §c" + String.format("%.1f", entry.getFinalDamage()));
        }
        
        sender.sendMessage("§6═══════════════════════════════════════");
    }
    
    private void handleStats(CommandSender sender) {
        SkillTemplateRegistry registry = getTemplateRegistry();
        SkillInstanceManager manager = getInstanceManager();
        
        sender.sendMessage("§6═══════ SKILL SYSTEM STATS ═══════");
        
        if (registry != null) {
            sender.sendMessage("§eTemplates: §f" + registry.getTemplateCount());
            
            // Count by category
            Map<SkillCategory, Integer> byCat = new HashMap<>();
            for (SkillTemplate t : registry.getAllTemplates()) {
                byCat.merge(t.getCategory(), 1, Integer::sum);
            }
            for (var e : byCat.entrySet()) {
                sender.sendMessage("  " + e.getKey().getColoredName() + "§7: §f" + e.getValue());
            }
        }
        
        sender.sendMessage("§6═══════════════════════════════════");
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6═══════ ADMIN SKILL COMMANDS ═══════");
        sender.sendMessage("§e/askill list §7- List tất cả templates");
        sender.sendMessage("§e/askill info <id> §7- Xem chi tiết template");
        sender.sendMessage("§e/askill give <player> <id> §7- Give skill");
        sender.sendMessage("§e/askill remove <player> <id> §7- Xóa skill");
        sender.sendMessage("§e/askill setmastery <player> <id> <0-100> §7- Set mastery");
        sender.sendMessage("§e/askill setrefinement <player> <id> <1-9> §7- Set refinement");
        sender.sendMessage("§e/askill create <id> <name> <category> §7- Tạo template");
        sender.sendMessage("§e/askill delete <id> §7- Xóa template");
        sender.sendMessage("§e/askill instances <player> §7- Xem skills của player");
        sender.sendMessage("§e/askill bypass [player] §7- Mở GUI (bypass realm)");
        sender.sendMessage("§e/askill reload §7- Reload data");
        sender.sendMessage("§e/askill stats §7- Thống kê hệ thống");
        sender.sendMessage("§6═══════════════════════════════════");
    }
    
    // ===== HELPERS =====
    
    private SkillTemplateRegistry getTemplateRegistry() {
        try {
            return CoreContext.getInstance().getSkillTemplateRegistry();
        } catch (Exception e) {
            return null;
        }
    }
    
    private SkillInstanceManager getInstanceManager() {
        try {
            return CoreContext.getInstance().getSkillInstanceManager();
        } catch (Exception e) {
            return null;
        }
    }
    
    // ===== TAB COMPLETE =====
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            return Collections.emptyList();
        }
        
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList(
                "list", "info", "give", "remove", "setmastery", "setrefinement",
                "create", "delete", "instances", "bypass", "reload", "stats"
            ));
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            
            if (sub.equals("give") || sub.equals("remove") || sub.equals("setmastery") 
                || sub.equals("setrefinement") || sub.equals("instances") || sub.equals("bypass")) {
                // Suggest players
                for (Player p : Bukkit.getOnlinePlayers()) {
                    suggestions.add(p.getName());
                }
            } else if (sub.equals("info") || sub.equals("delete")) {
                // Suggest template IDs
                SkillTemplateRegistry registry = getTemplateRegistry();
                if (registry != null) {
                    for (SkillTemplate t : registry.getAllTemplates()) {
                        suggestions.add(t.getId());
                    }
                }
            } else if (sub.equals("create")) {
                suggestions.add("<id>");
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            
            if (sub.equals("give") || sub.equals("remove") || sub.equals("setmastery") || sub.equals("setrefinement")) {
                // Suggest template IDs
                SkillTemplateRegistry registry = getTemplateRegistry();
                if (registry != null) {
                    for (SkillTemplate t : registry.getAllTemplates()) {
                        suggestions.add(t.getId());
                    }
                }
            } else if (sub.equals("create")) {
                suggestions.add("<name>");
            }
        } else if (args.length == 4) {
            String sub = args[0].toLowerCase();
            
            if (sub.equals("setmastery")) {
                suggestions.addAll(Arrays.asList("0", "25", "50", "75", "100"));
            } else if (sub.equals("setrefinement")) {
                suggestions.addAll(Arrays.asList("1", "3", "5", "7", "9"));
            } else if (sub.equals("create")) {
                for (SkillCategory cat : SkillCategory.values()) {
                    suggestions.add(cat.name());
                }
            }
        }
        
        String lastArg = args[args.length - 1].toLowerCase();
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(lastArg))
            .collect(Collectors.toList());
    }
}
