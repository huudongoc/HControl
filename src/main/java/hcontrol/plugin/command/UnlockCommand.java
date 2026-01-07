package hcontrol.plugin.command;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command test unlock conditions
 * LEVEL UNLOCK:
 * /unlock quest - mo khoa level bang quest
 * /unlock kill <amount> - mo khoa level bang giet quai
 * /unlock achievement - mo khoa level bang thanh tuu
 * /unlock pill <name> - mo khoa level bang dan duoc
 * /unlock force - mo khoa level truc tiep
 * 
 * BREAKTHROUGH UNLOCK (kho hon):
 * /unlock breakthrough quest <name> - mo khoa dot pha bang nhiem vu dot pha
 * /unlock breakthrough boss <name> - mo khoa dot pha bang giet boss tinh anh
 * /unlock breakthrough tribulation - mo khoa dot pha bang vuot thien kiep
 * /unlock breakthrough force - mo khoa dot pha truc tiep (GM)
 */
public class UnlockCommand implements CommandExecutor {

    private final PlayerManager playerManager;

    public UnlockCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được!");
            return true;
        }

        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cLỗi: Không tìm thấy profile!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e§lHỆ THỐNG MỞ KHÓA");
            player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage("§6§lMỞ KHÓA LEVEL:");
            player.sendMessage("§7/unlock quest §f- Nhiệm vụ");
            player.sendMessage("§7/unlock kill <số> §f- Giết quái");
            player.sendMessage("§7/unlock achievement §f- Thành tựu");
            player.sendMessage("§7/unlock pill <tên> §f- Đan dược");
            player.sendMessage("§7Trạng thái level: " + (profile.isNextLevelUnlocked() ? "§a✔" : "§c✘"));
            player.sendMessage("");
            player.sendMessage("§6§lMỞ KHÓA ĐỘT PHÁ:");
            player.sendMessage("§7/unlock breakthrough quest <tên> §f- Nhiệm vụ đột phá");
            player.sendMessage("§7/unlock breakthrough boss <tên> §f- Boss tinh anh");
            player.sendMessage("§7/unlock breakthrough tribulation §f- Thiên kiếp");
            player.sendMessage("§7Trạng thái đột phá: " + (profile.isBreakthroughUnlocked() ? "§a✔" : "§c✘"));
            player.sendMessage("");
            player.sendMessage("§7/unlock force §f- GM unlock");
            player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return true;
        }

        String method = args[0].toLowerCase();

        switch (method) {
            case "quest" -> {
                profile.unlockNextLevelByQuest();
            }
            
            case "kill" -> {
                int killsRequired = 10; // mac dinh 10
                if (args.length > 1) {
                    try {
                        killsRequired = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cSố không hợp lệ!");
                        return true;
                    }
                }
                profile.unlockNextLevelByKill(killsRequired);
            }
            
            case "achievement" -> {
                profile.unlockNextLevelByAchievement();
            }
            
            case "pill" -> {
                String pillName = args.length > 1 ? args[1] : "Tụ Linh Đan";
                profile.unlockNextLevelByPill(pillName);
            }
            
            case "force" -> {
                if (!player.hasPermission("hcontrol.unlock.force")) {
                    player.sendMessage("§cKhông có quyền!");
                    return true;
                }
                profile.forceUnlockNextLevel();
            }
            
            case "breakthrough" -> {
                if (args.length < 2) {
                    player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    player.sendMessage("§e§lMỞ KHÓA ĐỘT PHÁ");
                    player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    player.sendMessage("§7/unlock breakthrough quest <tên>");
                    player.sendMessage("§7/unlock breakthrough boss <tên>");
                    player.sendMessage("§7/unlock breakthrough tribulation");
                    player.sendMessage("§7/unlock breakthrough force §c(GM only)");
                    player.sendMessage("");
                    player.sendMessage("§7Trạng thái: " + (profile.isBreakthroughUnlocked() ? "§a✔ Đã mở khóa" : "§c✘ Chưa mở khóa"));
                    player.sendMessage("§e§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    return true;
                }

                String btMethod = args[1].toLowerCase();
                switch (btMethod) {
                    case "quest" -> {
                        String questName = args.length > 2 ? args[2] : "Unknown Breakthrough Quest";
                        profile.unlockBreakthroughByQuest(questName);
                        player.sendMessage("§6★★★ ĐÃ MỞ KHÓA ĐỘT PHÁ! ★★★");
                        player.sendMessage("§eHoàn thành nhiệm vụ đột phá: §a" + questName);
                    }

                    case "boss" -> {
                        String bossName = args.length > 2 ? args[2] : "Unknown Elite Boss";
                        profile.unlockBreakthroughByEliteBoss(bossName);
                        player.sendMessage("§6★★★ ĐÃ MỞ KHÓA ĐỘT PHÁ! ★★★");
                        player.sendMessage("§eGiết boss tinh anh: §c" + bossName);
                    }

                    case "tribulation" -> {
                        profile.unlockBreakthroughByTribulation();
                        player.sendMessage("§6★★★ ĐÃ MỞ KHÓA ĐỘT PHÁ! ★★★");
                        player.sendMessage("§eVượt qua thiên kiếp thử thách!");
                    }

                    case "force" -> {
                        if (!player.hasPermission("hcontrol.unlock.breakthrough.force")) {
                            player.sendMessage("§cKhông có quyền sử dụng lệnh này!");
                            return true;
                        }
                        profile.forceUnlockBreakthrough();
                        player.sendMessage("§6✔ GM: Đã force unlock đột phá");
                    }

                    default -> {
                        player.sendMessage("§cPhương thức không hợp lệ!");
                        player.sendMessage("§7Sử dụng: quest/boss/tribulation/force");
                    }
                }
            }
            
            default -> {
                player.sendMessage("§cPhương thức không hợp lệ!");
                return true;
            }
        }

        return true;
    }
}
