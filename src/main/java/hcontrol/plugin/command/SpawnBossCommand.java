package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.entity.EntityService;
import hcontrol.plugin.module.boss.BossEntity;
import hcontrol.plugin.module.boss.BossManager;
import hcontrol.plugin.module.boss.BossType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * DEBUG COMMAND - SPAWN BOSS
 * Test boss nameplate system
 * Usage: /spawnboss <name> [type]
 */
public class SpawnBossCommand implements CommandExecutor {
    
    private final BossManager bossManager;
    
    public SpawnBossCommand(BossManager bossManager) {
        this.bossManager = bossManager;
    }
    
    /**
     * Lấy EntityService từ CoreContext
     */
    private EntityService getEntityService() {
        CoreContext ctx = CoreContext.getInstance();
        if (ctx == null || ctx.getEntityContext() == null) {
            return null;
        }
        return ctx.getEntityContext().getEntityService();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChi player moi dung duoc lenh nay!");
            return true;
        }
        
        // Check permission: chi admin/op moi duoc spawn boss
        if (!player.hasPermission("hcontrol.admin")) {
            player.sendMessage("§cBạn không có quyền sử dụng lệnh này!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§eUsage: /spawnboss <ten> [type]");
            player.sendMessage("§7Type: FIELD, DUNGEON, WORLD, RAID");
            return true;
        }
        
        String bossName = args[0];
        BossType type = BossType.FIELD_BOSS; // mac dinh
        
        if (args.length >= 2) {
            try {
                type = BossType.valueOf(args[1].toUpperCase() + "_BOSS");
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cType khong hop le! Dung: FIELD, DUNGEON, WORLD, RAID");
                return true;
            }
        }
        
        // Lấy EntityService
        EntityService entityService = getEntityService();
        if (entityService == null) {
            player.sendMessage("§cLỗi: EntityService chưa được khởi tạo!");
            return true;
        }
        
        // Spawn zombie lam boss test
        LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(
            player.getLocation().add(0, 0, 3), 
            EntityType.ZOMBIE
        );
        
        // ✅ FIX: Randomize HP để boss có realm khác nhau (100-1000 HP)
        // HP cao hơn = realm cao hơn
        double randomHP = 100.0 + (Math.random() * 900.0);  // 100-1000 HP
        entity.setMaxHealth(randomHP);
        entity.setHealth(randomHP);
        
        // ✅ FIX: Gọi EntityService.spawnBoss() để tạo EntityProfile với realm và level đúng
        hcontrol.plugin.entity.EntityProfile bossProfile = entityService.spawnBoss(entity, bossName);
        
        // Tao boss entity
        BossEntity boss = new BossEntity(entity, bossName, type);
        bossManager.registerBoss(boss);
        
        // Hiển thị thông tin boss
        player.sendMessage("§aSpawn boss: " + bossName + " (Type: " + type.getDisplayName() + ")");
        player.sendMessage("§7Realm: " + bossProfile.getRealm().getDisplayName() + 
                          " | Level: " + bossProfile.getLevel() + 
                          " | HP: " + String.format("%.1f", bossProfile.getMaxHP()));
        player.sendMessage("§7Hit boss de test phase transition!");
        
        return true;
    }
}
