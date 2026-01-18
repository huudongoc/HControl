package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.service.AutoSpawnService;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * AUTO SPAWN COMMAND
 * Quản lý hệ thống spawn tự động
 * Chỉ admin mới có thể set khu vực spawn
 */
public class AutoSpawnCommand implements CommandExecutor, TabCompleter {
    
    private static final String PERMISSION = "hcontrol.admin";
    
    // Lưu vị trí đang chọn (để set khu vực)
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            sender.sendMessage("§c[AutoSpawn] Không có quyền!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        CoreContext ctx = CoreContext.getInstance();
        if (ctx == null || ctx.getAutoSpawnService() == null) {
            sender.sendMessage("§c[AutoSpawn] Hệ thống chưa được khởi tạo!");
            return true;
        }
        
        AutoSpawnService service = ctx.getAutoSpawnService();
        
        switch (subCommand) {
            case "start" -> {
                service.start();
                sender.sendMessage("§a[AutoSpawn] Đã bắt đầu hệ thống spawn tự động");
            }
            case "stop" -> {
                service.stop();
                sender.sendMessage("§a[AutoSpawn] Đã dừng hệ thống spawn tự động");
            }
            case "pos1" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c[AutoSpawn] Chỉ player mới có thể dùng lệnh này!");
                    return true;
                }
                Location loc = player.getLocation();
                pos1Map.put(player.getUniqueId(), loc);
                sender.sendMessage("§a[AutoSpawn] Đã set vị trí 1: §e" + formatLocation(loc));
            }
            case "pos2" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c[AutoSpawn] Chỉ player mới có thể dùng lệnh này!");
                    return true;
                }
                Location loc = player.getLocation();
                pos2Map.put(player.getUniqueId(), loc);
                sender.sendMessage("§a[AutoSpawn] Đã set vị trí 2: §e" + formatLocation(loc));
            }
            case "setarea" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§c[AutoSpawn] Chỉ player mới có thể dùng lệnh này!");
                    return true;
                }
                UUID uuid = player.getUniqueId();
                Location pos1 = pos1Map.get(uuid);
                Location pos2 = pos2Map.get(uuid);
                
                if (pos1 == null || pos2 == null) {
                    sender.sendMessage("§c[AutoSpawn] Cần set cả 2 vị trí (pos1 và pos2) trước!");
                    sender.sendMessage("§7Dùng: §e/autospawn pos1 §7và §e/autospawn pos2");
                    return true;
                }
                
                if (pos1.getWorld() != pos2.getWorld()) {
                    sender.sendMessage("§c[AutoSpawn] Hai vị trí phải cùng một world!");
                    return true;
                }
                
                AutoSpawnService.SpawnArea area = new AutoSpawnService.SpawnArea(pos1, pos2);
                service.setSpawnArea(area);
                sender.sendMessage("§a[AutoSpawn] Đã set khu vực spawn:");
                sender.sendMessage("§7  World: §e" + pos1.getWorld().getName());
                sender.sendMessage("§7  X: §e" + area.getMinX() + " §7→ §e" + area.getMaxX());
                sender.sendMessage("§7  Z: §e" + area.getMinZ() + " §7→ §e" + area.getMaxZ());
                sender.sendMessage("§7  Y: §e" + area.getMinY() + " §7→ §e" + area.getMaxY());
            }
            case "cleararea" -> {
                service.clearSpawnArea();
                sender.sendMessage("§a[AutoSpawn] Đã xóa khu vực spawn (sẽ spawn quanh player)");
            }
            case "info" -> {
                AutoSpawnService.SpawnArea area = service.getSpawnArea();
                if (area != null) {
                    sender.sendMessage("§6[AutoSpawn] Thông tin khu vực spawn:");
                    sender.sendMessage("§7  World: §e" + area.getWorld().getName());
                    sender.sendMessage("§7  X: §e" + area.getMinX() + " §7→ §e" + area.getMaxX());
                    sender.sendMessage("§7  Z: §e" + area.getMinZ() + " §7→ §e" + area.getMaxZ());
                    sender.sendMessage("§7  Y: §e" + area.getMinY() + " §7→ §e" + area.getMaxY());
                } else {
                    sender.sendMessage("§6[AutoSpawn] Chưa set khu vực (đang spawn quanh player)");
                }
            }
            case "interval" -> {
                if (args.length < 2) {
                    sender.sendMessage("§c[AutoSpawn] Cách dùng: /autospawn interval <ticks>");
                    sender.sendMessage("§7Ví dụ: §e/autospawn interval 100 §7(spawn mỗi 5 giây)");
                    return true;
                }
                try {
                    int ticks = Integer.parseInt(args[1]);
                    if (ticks < 20) {
                        sender.sendMessage("§c[AutoSpawn] Interval phải >= 20 ticks (1 giây)!");
                        return true;
                    }
                    service.setSpawnInterval(ticks);
                    sender.sendMessage("§a[AutoSpawn] Đã set interval: §e" + ticks + " ticks");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c[AutoSpawn] Interval không hợp lệ!");
                }
            }
            case "radius" -> {
                if (args.length < 2) {
                    sender.sendMessage("§c[AutoSpawn] Cách dùng: /autospawn radius <blocks>");
                    return true;
                }
                try {
                    int radius = Integer.parseInt(args[1]);
                    if (radius < 10 || radius > 200) {
                        sender.sendMessage("§c[AutoSpawn] Radius phải từ 10-200 blocks!");
                        return true;
                    }
                    service.setSpawnRadius(radius);
                    sender.sendMessage("§a[AutoSpawn] Đã set radius: §e" + radius + " blocks");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c[AutoSpawn] Radius không hợp lệ!");
                }
            }
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l[AutoSpawn] Hệ thống spawn tự động");
        sender.sendMessage("§7/autospawn start §7- Bắt đầu spawn tự động");
        sender.sendMessage("§7/autospawn stop §7- Dừng spawn tự động");
        sender.sendMessage("§7/autospawn pos1 §7- Set vị trí 1 (đứng tại vị trí)");
        sender.sendMessage("§7/autospawn pos2 §7- Set vị trí 2 (đứng tại vị trí)");
        sender.sendMessage("§7/autospawn setarea §7- Set khu vực spawn (cần pos1 và pos2)");
        sender.sendMessage("§7/autospawn cleararea §7- Xóa khu vực (spawn quanh player)");
        sender.sendMessage("§7/autospawn info §7- Xem thông tin khu vực");
        sender.sendMessage("§7/autospawn interval <ticks> §7- Set interval spawn");
        sender.sendMessage("§7/autospawn radius <blocks> §7- Set bán kính spawn quanh player");
    }
    
    private String formatLocation(Location loc) {
        return String.format("X:%.1f Y:%.1f Z:%.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "pos1", "pos2", "setarea", "cleararea", "info", "interval", "radius");
        }
        
        return Collections.emptyList();
    }
}
