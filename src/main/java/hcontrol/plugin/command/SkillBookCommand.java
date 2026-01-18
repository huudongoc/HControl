package hcontrol.plugin.command;

import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillRegistry;
import hcontrol.plugin.playerskill.SkillBookService;
import hcontrol.plugin.playerskill.SkillBookService.BookRarity;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PHASE 6 — SKILL BOOK COMMAND
 * /skillbook <skillId> [rarity] [player] - Tạo skill book
 * 
 * Permission: hcontrol.admin
 */
public class SkillBookCommand implements CommandExecutor, TabCompleter {
    
    private final SkillBookService skillBookService;
    private final PlayerSkillRegistry skillRegistry;
    
    public SkillBookCommand(SkillBookService skillBookService, PlayerSkillRegistry skillRegistry) {
        this.skillBookService = skillBookService;
        this.skillRegistry = skillRegistry;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("hcontrol.admin")) {
            sender.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }
        
        String skillId = args[0];
        
        // Check skill exists
        PlayerSkill skill = skillRegistry.getSkill(skillId);
        if (skill == null) {
            sender.sendMessage("§cSkill không tồn tại: " + skillId);
            sender.sendMessage("§7Danh sách skill:");
            skillRegistry.getAllSkills().forEach(s -> 
                sender.sendMessage("§7  - " + s.getSkillId())
            );
            return true;
        }
        
        // Parse rarity (optional)
        BookRarity rarity = null;
        if (args.length >= 2) {
            try {
                rarity = BookRarity.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cRarity không hợp lệ: " + args[1]);
                sender.sendMessage("§7Rarity hợp lệ: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC");
                return true;
            }
        }
        
        // Create skill book
        ItemStack book;
        if (rarity != null) {
            book = skillBookService.createSkillBook(skillId, rarity);
        } else {
            book = skillBookService.createSkillBook(skillId);
        }
        
        if (book == null) {
            sender.sendMessage("§cKhông thể tạo skill book!");
            return true;
        }
        
        // Target player
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage("§cKhông tìm thấy player: " + args[2]);
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cVui lòng chỉ định player khi dùng từ console!");
            return true;
        }
        
        // Give book to player
        target.getInventory().addItem(book);
        
        // Messages
        String rarityName = rarity != null ? rarity.getDisplayName() : "Tự động";
        sender.sendMessage("§a✓ Đã tạo Skill Book!");
        sender.sendMessage("§7  Skill: §e" + skill.getDisplayName());
        sender.sendMessage("§7  Rarity: §e" + rarityName);
        sender.sendMessage("§7  Người nhận: §e" + target.getName());
        
        if (target != sender) {
            target.sendMessage("§a✓ Bạn đã nhận được: §e【Bí Kíp】 " + skill.getDisplayName());
        }
        
        return true;
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§6§lSkill Book Command");
        sender.sendMessage("");
        sender.sendMessage("§e/skillbook <skillId> §7- Tạo skill book (rarity tự động)");
        sender.sendMessage("§e/skillbook <skillId> <rarity> §7- Tạo với rarity chỉ định");
        sender.sendMessage("§e/skillbook <skillId> <rarity> <player> §7- Cho player khác");
        sender.sendMessage("");
        sender.sendMessage("§7Rarity: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, MYTHIC");
        sender.sendMessage("§7§m━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Skill IDs
            skillRegistry.getAllSkills().forEach(s -> completions.add(s.getSkillId()));
        } else if (args.length == 2) {
            // Rarity
            Arrays.stream(BookRarity.values()).forEach(r -> completions.add(r.name()));
        } else if (args.length == 3) {
            // Online players
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }
        
        // Filter by prefix
        String prefix = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix))
                .toList();
    }
}
