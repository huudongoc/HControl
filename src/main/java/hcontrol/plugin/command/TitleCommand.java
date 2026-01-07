package hcontrol.plugin.command;

import hcontrol.plugin.model.Title;
import hcontrol.plugin.model.TitleRarity;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.TitleService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * TITLE COMMAND - Quan ly danh hieu
 * /title list - xem danh sach danh hieu da mo khoa
 * /title equip <title> - trang bi danh hieu
 * /title unequip - go bo danh hieu
 * /title all - xem tat ca danh hieu (admin)
 */
public class TitleCommand implements CommandExecutor {
    
    private final PlayerManager playerManager;
    private final TitleService titleService;
    
    public TitleCommand(PlayerManager playerManager, TitleService titleService) {
        this.playerManager = playerManager;
        this.titleService = titleService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChi player moi dung duoc lenh nay!");
            return true;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhong tim thay profile!");
            return true;
        }
        
        // /title (list)
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            showUnlockedTitles(player, profile);
            return true;
        }
        
        // /title equip <title>
        if (args[0].equalsIgnoreCase("equip")) {
            if (args.length < 2) {
                player.sendMessage("§cCu phap: /title equip <ten_danh_hieu>");
                return true;
            }
            
            String titleName = args[1].toUpperCase();
            try {
                Title title = Title.valueOf(titleName);
                equipTitle(player, profile, title);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cKhong tim thay danh hieu: " + args[1]);
                player.sendMessage("§7Dung /title list de xem danh hieu da mo khoa");
            }
            return true;
        }
        
        // /title unequip
        if (args[0].equalsIgnoreCase("unequip") || args[0].equalsIgnoreCase("remove")) {
            titleService.unequipTitle(profile);
            player.sendMessage("§aDa go bo danh hieu!");
            return true;
        }
        
        // /title all (admin)
        if (args[0].equalsIgnoreCase("all")) {
            if (!player.hasPermission("hcontrol.admin")) {
                player.sendMessage("§cKhong co quyen!");
                return true;
            }
            showAllTitles(player);
            return true;
        }
        
        // /title unlock <title> (admin)
        if (args[0].equalsIgnoreCase("unlock")) {
            if (!player.hasPermission("hcontrol.admin")) {
                player.sendMessage("§cKhong co quyen!");
                return true;
            }
            
            if (args.length < 2) {
                player.sendMessage("§cCu phap: /title unlock <ten_danh_hieu>");
                return true;
            }
            
            String titleName = args[1].toUpperCase();
            try {
                Title title = Title.valueOf(titleName);
                boolean success = titleService.unlockTitle(profile, title);
                if (success) {
                    player.sendMessage("§aDa mo khoa danh hieu: " + title.getFullDisplay());
                } else {
                    player.sendMessage("§eDa co danh hieu nay roi!");
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cKhong tim thay danh hieu: " + args[1]);
            }
            return true;
        }
        
        player.sendMessage("§cCu phap:");
        player.sendMessage("§7/title list §f- xem danh hieu da mo khoa");
        player.sendMessage("§7/title equip <ten> §f- trang bi danh hieu");
        player.sendMessage("§7/title unequip §f- go bo danh hieu");
        return true;
    }
    
    /**
     * Hien thi danh hieu da mo khoa
     */
    private void showUnlockedTitles(Player player, PlayerProfile profile) {
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━ §e⚡ DANH HIEU §6§l━━━━━━━━━━━━");
        player.sendMessage("");
        
        Title activeTitle = profile.getActiveTitle();
        player.sendMessage("§7Dang trang bi: " + (activeTitle == Title.NONE ? "§7Khong" : activeTitle.getFullDisplay()));
        player.sendMessage("");
        
        player.sendMessage("§e§lDanh hieu da mo khoa:");
        
        for (Title title : profile.getUnlockedTitles()) {
            if (title == Title.NONE) continue;
            
            String active = title == activeTitle ? " §a✓" : "";
            player.sendMessage("  §7► " + title.getFullDisplay() + " §8[" + title.getRarity().getDisplayName() + "]" + active);
        }
        
        if (profile.getUnlockedTitles().size() <= 1) {
            player.sendMessage("  §7Chua co danh hieu nao!");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Su dung: §f/title equip <ten>");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
    }
    
    /**
     * Goi service xu ly logic trang bi danh hieu
     */
    private void equipTitle(Player player, PlayerProfile profile, Title title) {
        boolean success = titleService.equipTitle(profile, title);
        
        if (!success) {
            player.sendMessage("§cBan chua mo khoa danh hieu nay!");
            return;
        }
        
        player.sendMessage("§aDa trang bi danh hieu: " + title.getFullDisplay());
    }
    
    /**
     * Hien thi tat ca danh hieu (admin)
     */
    private void showAllTitles(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━ §e⚡ TAT CA DANH HIEU §6§l━━━━━━━━");
        player.sendMessage("");
        
        // Group by rarity
        for (TitleRarity rarity : TitleRarity.values()) {
            player.sendMessage("§e" + rarity.getDisplayName() + ":");
            
            for (Title title : Title.values()) {
                if (title.getRarity() == rarity && title != Title.NONE) {
                    player.sendMessage("  §7► " + title.getFullDisplay() + " §8(" + title.name() + ")");
                }
            }
            player.sendMessage("");
        }
        
        player.sendMessage("§7Su dung: §f/title unlock <ten>");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
    }
}
