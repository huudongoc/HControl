package hcontrol.plugin.command;

import hcontrol.plugin.entity.ZoneManager;
import hcontrol.plugin.entity.ZoneSpawnConfig;
import hcontrol.plugin.model.CultivationRealm;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * ZONE COMMAND
 * Admin command để set zone spawn config
 * /zone create <name> - Tạo zone tại vị trí hiện tại
 * /zone set realm <name> <realm> - Set realm cho zone
 * /zone set mobs <name> <mob1,mob2,...> - Set mob types cho zone
 * /zone set level <name> <min> <max> - Set level range
 * /zone list - Liệt kê tất cả zones
 * /zone delete <name> - Xóa zone
 */
public class ZoneCommand implements CommandExecutor {
    
    private final ZoneManager zoneManager;
    
    public ZoneCommand(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được lệnh này!");
            return true;
        }
        
        if (!player.hasPermission("hcontrol.admin")) {
            player.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "set":
                handleSet(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "delete":
                handleDelete(player, args);
                break;
            default:
                sendUsage(player);
                break;
        }
        
        return true;
    }
    
    private void sendUsage(Player player) {
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l    ZONE SPAWN CONFIG");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§7/zone create <name> §7- Tạo zone tại vị trí hiện tại");
        player.sendMessage("§7/zone set realm <name> <realm> §7- Set realm cho zone");
        player.sendMessage("§7/zone set mobs <name> <mob1,mob2,...> §7- Set mob types");
        player.sendMessage("§7/zone set level <name> <min> <max> §7- Set level range");
        player.sendMessage("§7/zone list §7- Liệt kê tất cả zones");
        player.sendMessage("§7/zone delete <name> §7- Xóa zone");
    }
    
    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /zone create <name>");
            return;
        }
        
        String zoneName = args[1];
        if (zoneManager.hasZone(zoneName)) {
            player.sendMessage("§cZone §e" + zoneName + " §cđã tồn tại!");
            return;
        }
        
        Location loc = player.getLocation();
        Location minCorner = loc.clone().subtract(25, 5, 25);
        Location maxCorner = loc.clone().add(25, 5, 25);
        
        ZoneSpawnConfig config = new ZoneSpawnConfig(zoneName, loc.getWorld(), minCorner, maxCorner);
        zoneManager.addZone(config);
        
        player.sendMessage("§a✓ Đã tạo zone §e" + zoneName);
        player.sendMessage("§7Khu vực: §f" + minCorner.getBlockX() + "," + minCorner.getBlockY() + "," + minCorner.getBlockZ() + 
                       " §7→ §f" + maxCorner.getBlockX() + "," + maxCorner.getBlockY() + "," + maxCorner.getBlockZ());
    }
    
    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cCách dùng: /zone set <realm|mobs|level> <name> <value>");
            return;
        }
        
        String setType = args[1].toLowerCase();
        String zoneName = args[2];
        
        ZoneSpawnConfig config = zoneManager.getZone(zoneName);
        if (config == null) {
            player.sendMessage("§cZone §e" + zoneName + " §ckhông tồn tại!");
            return;
        }
        
        switch (setType) {
            case "realm":
                if (args.length < 4) {
                    player.sendMessage("§cCách dùng: /zone set realm <name> <realm>");
                    return;
                }
                try {
                    CultivationRealm realm = CultivationRealm.valueOf(args[3].toUpperCase());
                    config.setDefaultRealm(realm);
                    player.sendMessage("§a✓ Đã set realm §e" + realm.getDisplayName() + " §acho zone §e" + zoneName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cRealm không hợp lệ! Các realm: LUYENKHI, TRUCCO, KIMDAN, NGUYENANH, HOATHAN...");
                }
                break;
                
            case "mobs":
                if (args.length < 4) {
                    player.sendMessage("§cCách dùng: /zone set mobs <name> <mob1,mob2,...>");
                    return;
                }
                String[] mobNames = args[3].split(",");
                List<EntityType> mobTypes = new ArrayList<>();
                for (String mobName : mobNames) {
                    try {
                        EntityType type = EntityType.valueOf(mobName.trim().toUpperCase());
                        mobTypes.add(type);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("§cMob type không hợp lệ: §e" + mobName);
                    }
                }
                if (!mobTypes.isEmpty()) {
                    config.setAllowedMobTypes(mobTypes);
                    player.sendMessage("§a✓ Đã set §e" + mobTypes.size() + " §amob types cho zone §e" + zoneName);
                }
                break;
                
            case "level":
                if (args.length < 5) {
                    player.sendMessage("§cCách dùng: /zone set level <name> <min> <max>");
                    return;
                }
                try {
                    int min = Integer.parseInt(args[3]);
                    int max = Integer.parseInt(args[4]);
                    if (min < 1 || max > 10 || min > max) {
                        player.sendMessage("§cLevel phải từ 1-10 và min <= max!");
                        return;
                    }
                    config.setMinLevel(min);
                    config.setMaxLevel(max);
                    player.sendMessage("§a✓ Đã set level range §e" + min + "-" + max + " §acho zone §e" + zoneName);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cLevel phải là số!");
                }
                break;
                
            default:
                player.sendMessage("§cLoại set không hợp lệ: §e" + setType);
                break;
        }
    }
    
    private void handleList(Player player) {
        if (zoneManager.getAllZones().isEmpty()) {
            player.sendMessage("§7Không có zone nào!");
            return;
        }
        
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§l    DANH SÁCH ZONES");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        for (ZoneSpawnConfig zone : zoneManager.getAllZones()) {
            player.sendMessage("§7- §e" + zone.getZoneName());
            player.sendMessage("§7  Realm: §f" + zone.getDefaultRealm().getDisplayName());
            player.sendMessage("§7  Level: §f" + zone.getMinLevel() + "-" + zone.getMaxLevel());
            player.sendMessage("§7  Mobs: §f" + zone.getAllowedMobTypes().size() + " types");
        }
    }
    
    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cCách dùng: /zone delete <name>");
            return;
        }
        
        String zoneName = args[1];
        if (!zoneManager.hasZone(zoneName)) {
            player.sendMessage("§cZone §e" + zoneName + " §ckhông tồn tại!");
            return;
        }
        
        zoneManager.removeZone(zoneName);
        player.sendMessage("§a✓ Đã xóa zone §e" + zoneName);
    }
}
