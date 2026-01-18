package hcontrol.plugin.command;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hcontrol.plugin.model.AscensionProfile;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.AscensionService;

/**
 * ASCENSION COMMAND - ENDGAME
 * Thăng cấp ascension sau CHANTIEN level 10
 * 
 * Usage: /ascend [info]
 */
public class AscensionCommand implements CommandExecutor {
    
    private final PlayerManager playerManager;
    private final AscensionService ascensionService;
    
    public AscensionCommand(PlayerManager playerManager, AscensionService ascensionService) {
        this.playerManager = playerManager;
        this.ascensionService = ascensionService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cChưa load profile!");
            return true;
        }
        
        // Check command args
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            ascensionService.sendAscensionInfo(player, profile);
            return true;
        }
        
        // Thực hiện ascension
        CultivationRealm currentRealm = profile.getRealm();
        int currentLevel = profile.getLevel();
        
        // Check điều kiện: CHANTIEN level 10
        if (currentRealm != CultivationRealm.CHANTIEN) {
            player.sendMessage("§c✘ Chưa đủ điều kiện ascension!");
            player.sendMessage("§7Yêu cầu: §eCHÂN TIÊN §7level §e10");
            return true;
        }
        
        if (currentLevel < currentRealm.getMaxLevelInRealm()) {
            player.sendMessage("§c✘ Chưa đủ điều kiện ascension!");
            player.sendMessage("§7Yêu cầu: §eCHÂN TIÊN §7level §e10");
            player.sendMessage("§7Hiện tại: §e" + currentRealm.getDisplayName() + " §7level §e" + currentLevel);
            return true;
        }
        
        // Check đủ cultivation
        long requiredCultivation = ascensionService.getRequiredCultivation(profile);
        long currentCultivation = profile.getCultivation();
        
        if (currentCultivation < requiredCultivation) {
            long needed = requiredCultivation - currentCultivation;
            player.sendMessage("§c✘ Chưa đủ cultivation!");
            player.sendMessage("§7Cần: §e" + requiredCultivation);
            player.sendMessage("§7Hiện có: §e" + currentCultivation);
            player.sendMessage("§7Còn thiếu: §c" + needed);
            return true;
        }
        
        // Thực hiện ascension
        boolean success = ascensionService.ascend(profile);
        
        if (success) {
            AscensionProfile ascension = profile.getAscensionProfile();
            
            // Effect
            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.TOTEM, loc, 50, 0.5, 1.0, 0.5, 0.1);
            player.getWorld().spawnParticle(Particle.END_ROD, loc, 30, 0.5, 1.0, 0.5, 0.05);
            player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
            player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5f, 1.2f);
            
            // Message
            player.sendMessage("");
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e§l    ⚡ ASCENSION THÀNH CÔNG!");
            player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage("§7Ascension Level: §e" + ascension.getAscensionLevel());
            player.sendMessage("§7Ascension Power: §e" + String.format("%.1f", ascension.getAscensionPower()) + "x");
            player.sendMessage("§7Cultivation còn lại: §e" + profile.getCultivation());
            player.sendMessage("");
            player.sendMessage("§a✔ Sức mạnh của bạn đã tăng lên!");
            player.sendMessage("");
            
            // Broadcast (optional)
            for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                onlinePlayer.sendMessage("§6§l[ASCENSION] §e" + player.getName() + 
                    " §7đã đạt Ascension Level §e" + ascension.getAscensionLevel() + "§7!");
            }
        } else {
            player.sendMessage("§c✘ Ascension thất bại! Vui lòng thử lại.");
        }
        
        return true;
    }
}
