package hcontrol.plugin.command;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillService;
import hcontrol.plugin.ui.skill.SkillMenuGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PHASE 6 — SKILL COMMAND
 * /skill - Mở Skill Menu GUI
 * /skill gui - Mở Skill Menu GUI
 * /skill list - Xem skills đã học
 * /skill available - Xem skills có thể học
 * /skill learn <id> - Học skill
 * /skill cast <id> - Dùng skill
 * /skill bind <id> <slot> - Gán skill vào hotbar
 * /skill unbind <slot> - Gỡ skill khỏi hotbar
 * /skill hotbar - Xem hotbar
 * /skill info <id> - Xem thông tin skill
 */
public class SkillCommand implements CommandExecutor, TabCompleter {
    
    private final PlayerManager playerManager;
    private final PlayerSkillService skillService;
    private SkillMenuGUI menuGUI; // Inject sau
    
    public SkillCommand(PlayerManager playerManager, PlayerSkillService skillService) {
        this.playerManager = playerManager;
        this.skillService = skillService;
    }
    
    /**
     * Inject SkillMenuGUI (gọi sau khi tạo)
     */
    public void setMenuGUI(SkillMenuGUI menuGUI) {
        this.menuGUI = menuGUI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được command này!");
            return true;
        }
        
        if (args.length == 0) {
            // Mở GUI nếu không có args
            handleGUI(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "gui", "menu" -> handleGUI(player);
            case "list" -> handleList(player);
            case "available" -> handleAvailable(player);
            case "learn" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /skill learn <skillId>");
                    return true;
                }
                handleLearn(player, args[1]);
            }
            case "cast" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /skill cast <skillId>");
                    return true;
                }
                handleCast(player, args[1]);
            }
            case "bind" -> {
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /skill bind <skillId> <slot 1-9>");
                    return true;
                }
                handleBind(player, args[1], args[2]);
            }
            case "unbind" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /skill unbind <slot 1-9>");
                    return true;
                }
                handleUnbind(player, args[1]);
            }
            case "hotbar" -> handleHotbar(player);
            case "info" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /skill info <skillId>");
                    return true;
                }
                handleInfo(player, args[1]);
            }
            default -> sendUsage(player);
        }
        
        return true;
    }
    
    // ========== HANDLERS ==========
    
    private void handleGUI(Player player) {
        if (menuGUI == null) {
            player.sendMessage("§cSkill Menu chưa sẵn sàng!");
            return;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return;
        }
        
        menuGUI.openMenu(player, profile);
    }
    
    private void handleList(Player player) {
        List<PlayerSkill> learnedSkills = skillService.getLearnedSkills(player);
        
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lSkills Đã Học §7(" + learnedSkills.size() + ")");
        player.sendMessage("");
        
        if (learnedSkills.isEmpty()) {
            player.sendMessage("§7Bạn chưa học skill nào!");
            player.sendMessage("§7Dùng /skill available để xem skills có thể học");
        } else {
            for (PlayerSkill skill : learnedSkills) {
                String cooldownInfo = "";
                if (skillService.isOnCooldown(player.getUniqueId(), skill.getSkillId())) {
                    long remaining = skillService.getRemainingCooldown(player.getUniqueId(), skill.getSkillId());
                    cooldownInfo = " §c(CD: " + (remaining / 1000) + "s)";
                }
                
                player.sendMessage("§e▸ " + skill.getDisplayName() + " §7[" + skill.getSkillId() + "]" + cooldownInfo);
                player.sendMessage("  §7Cost: §b" + (int) skill.getCost().getLingQi() + " LQ §7| CD: §e" + skill.getCooldown() + "s");
            }
        }
        
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void handleAvailable(Player player) {
        List<PlayerSkill> availableSkills = skillService.getAvailableSkills(player);
        
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§a§lSkills Có Thể Học §7(" + availableSkills.size() + ")");
        player.sendMessage("");
        
        if (availableSkills.isEmpty()) {
            player.sendMessage("§7Không có skill nào có thể học lúc này!");
            player.sendMessage("§7Nâng cảnh giới để mở khóa skills mới");
        } else {
            for (PlayerSkill skill : availableSkills) {
                player.sendMessage("§a▸ " + skill.getDisplayName() + " §7[" + skill.getSkillId() + "]");
                player.sendMessage("  §7Yêu cầu: §e" + skill.getMinRealm().getDisplayName() + " Lv" + skill.getMinLevel());
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§7Dùng: /skill learn <skillId>");
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void handleLearn(Player player, String skillId) {
        skillService.learnSkill(player, skillId);
    }
    
    private void handleCast(Player player, String skillId) {
        skillService.castSkill(player, skillId);
    }
    
    private void handleBind(Player player, String skillId, String slotStr) {
        try {
            int slot = Integer.parseInt(slotStr);
            skillService.bindSkill(player, skillId, slot);
        } catch (NumberFormatException e) {
            player.sendMessage("§cSlot phải là số từ 1-9!");
        }
    }
    
    private void handleUnbind(Player player, String slotStr) {
        try {
            int slot = Integer.parseInt(slotStr);
            skillService.unbindSkill(player, slot);
        } catch (NumberFormatException e) {
            player.sendMessage("§cSlot phải là số từ 1-9!");
        }
    }
    
    private void handleHotbar(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return;
        }
        
        Map<Integer, String> hotbar = profile.getSkillHotbar();
        
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lSkill Hotbar");
        player.sendMessage("");
        
        for (int i = 1; i <= 9; i++) {
            String skillId = hotbar.get(i);
            if (skillId != null) {
                PlayerSkill skill = skillService.getRegistry().getSkill(skillId);
                String skillName = skill != null ? skill.getDisplayName() : "§c[Invalid]";
                player.sendMessage("§e[" + i + "] §7→ " + skillName);
            } else {
                player.sendMessage("§7[" + i + "] → (trống)");
            }
        }
        
        player.sendMessage("");
        player.sendMessage("§7Dùng: /skill bind <id> <slot>");
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void handleInfo(Player player, String skillId) {
        PlayerSkill skill = skillService.getRegistry().getSkill(skillId);
        
        if (skill == null) {
            player.sendMessage("§cSkill không tồn tại: " + skillId);
            return;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        boolean learned = profile != null && profile.hasLearnedSkill(skillId);
        
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(skill.getDisplayName() + (learned ? " §a✓" : " §7✗"));
        player.sendMessage("");
        
        // Description
        for (String line : skill.getDescription()) {
            player.sendMessage("§7" + line);
        }
        
        player.sendMessage("");
        player.sendMessage("§eType: §f" + skill.getType().name());
        player.sendMessage("§eCost: §b" + (int) skill.getCost().getLingQi() + " Linh Khí");
        player.sendMessage("§eCooldown: §f" + skill.getCooldown() + "s");
        player.sendMessage("§eDamage: §c" + String.format("%.0f%%", skill.getDamageMultiplier() * 100));
        player.sendMessage("§eRange: §f" + String.format("%.1f", skill.getRange()) + " blocks");
        player.sendMessage("");
        player.sendMessage("§eYêu cầu: §f" + skill.getMinRealm().getDisplayName() + " Lv" + skill.getMinLevel());
        
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void sendUsage(Player player) {
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6§lSkill Commands");
        player.sendMessage("");
        player.sendMessage("§e/skill §7- Mở Skill Menu GUI");
        player.sendMessage("§e/skill gui §7- Mở Skill Menu GUI");
        player.sendMessage("§e/skill list §7- Xem skills đã học");
        player.sendMessage("§e/skill available §7- Xem skills có thể học");
        player.sendMessage("§e/skill learn <id> §7- Học skill");
        player.sendMessage("§e/skill cast <id> §7- Dùng skill");
        player.sendMessage("§e/skill bind <id> <slot> §7- Gán vào hotbar");
        player.sendMessage("§e/skill unbind <slot> §7- Gỡ khỏi hotbar");
        player.sendMessage("§e/skill hotbar §7- Xem hotbar");
        player.sendMessage("§e/skill info <id> §7- Xem thông tin skill");
        player.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    // ========== TAB COMPLETER ==========
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(List.of("gui", "list", "available", "learn", "cast", "bind", "unbind", "hotbar", "info"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "learn", "info" -> {
                    // Show all skills
                    skillService.getRegistry().getAllSkills().forEach(s -> completions.add(s.getSkillId()));
                }
                case "cast", "bind" -> {
                    // Show learned skills
                    PlayerProfile profile = playerManager.get(player.getUniqueId());
                    if (profile != null) {
                        completions.addAll(profile.getLearnedSkills());
                    }
                }
                case "unbind" -> {
                    // Show slots 1-9
                    for (int i = 1; i <= 9; i++) {
                        completions.add(String.valueOf(i));
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("bind")) {
            // Show slots 1-9
            for (int i = 1; i <= 9; i++) {
                completions.add(String.valueOf(i));
            }
        }
        
        // Filter by prefix
        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix))
                .toList();
    }
}
