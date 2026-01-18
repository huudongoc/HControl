package hcontrol.plugin.command;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.core.ItemContext;
import hcontrol.plugin.item.EquipmentSlot;
import hcontrol.plugin.item.ItemService;
import hcontrol.plugin.item.StatType;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * PHASE 8A — ITEM TEST COMMAND
 * Debug command để test equip/unequip items
 * Usage: /itemtest <equip|unequip|stats|list> [itemId] [slot]
 */
public class ItemTestCommand implements CommandExecutor {
    
    private final PlayerManager playerManager;
    
    public ItemTestCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }
    
    /**
     * Lazy load ItemService từ CoreContext
     */
    private ItemService getItemService() {
        CoreContext ctx = CoreContext.getInstance();
        ItemContext itemContext = ctx.getItemContext();
        if (itemContext == null) {
            return null;
        }
        return itemContext.getItemService();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cChỉ player mới dùng được command này!");
            return true;
        }
        
        if (!player.isOp()) {
            player.sendMessage("§cBạn không có quyền!");
            return true;
        }
        
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cProfile chưa load!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "equip":
                handleEquip(player, profile, args);
                break;
            case "unequip":
                handleUnequip(player, profile, args);
                break;
            case "stats":
                handleStats(player, profile);
                break;
            case "list":
                handleList(player);
                break;
            default:
                sendHelp(player);
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§e=== Item Test Command ===");
        player.sendMessage("§7/itemtest equip <itemId> <slot> - Equip item");
        player.sendMessage("§7/itemtest unequip <slot> - Unequip item");
        player.sendMessage("§7/itemtest stats - Xem stats từ items");
        player.sendMessage("§7/itemtest list - List tất cả items");
        player.sendMessage("§7Slots: HAND, HEAD, CHEST, LEGS, FEET");
    }
    
    private void handleEquip(Player player, PlayerProfile profile, String[] args) {
        ItemService itemService = getItemService();
        if (itemService == null) {
            player.sendMessage("§cItem System chưa được khởi tạo! Vui lòng chờ server load xong.");
            return;
        }
        
        if (args.length < 3) {
            player.sendMessage("§cUsage: /itemtest equip <itemId> <slot>");
            return;
        }
        
        String itemId = args[1];
        String slotStr = args[2].toUpperCase();
        
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(slotStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cSlot không hợp lệ! Dùng: HAND, HEAD, CHEST, LEGS, FEET");
            return;
        }
        
        boolean success = itemService.equipItem(profile, itemId, slot);
        
        if (success) {
            player.sendMessage("§a✓ Đã equip item: §f" + itemId + " §avào slot: §f" + slot);
        } else {
            player.sendMessage("§c✗ Không thể equip item: §f" + itemId);
            player.sendMessage("§7Kiểm tra: itemId tồn tại, requirements (realm/level), slot đúng");
        }
    }
    
    private void handleUnequip(Player player, PlayerProfile profile, String[] args) {
        ItemService itemService = getItemService();
        if (itemService == null) {
            player.sendMessage("§cItem System chưa được khởi tạo! Vui lòng chờ server load xong.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /itemtest unequip <slot>");
            return;
        }
        
        String slotStr = args[1].toUpperCase();
        
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(slotStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cSlot không hợp lệ! Dùng: HAND, HEAD, CHEST, LEGS, FEET");
            return;
        }
        
        String itemId = profile.getItemAtSlot(slot);
        if (itemId == null) {
            player.sendMessage("§cKhông có item ở slot: §f" + slot);
            return;
        }
        
        itemService.unequipItem(profile, slot);
        player.sendMessage("§a✓ Đã unequip item: §f" + itemId + " §atừ slot: §f" + slot);
    }
    
    private void handleStats(Player player, PlayerProfile profile) {
        ItemService itemService = getItemService();
        if (itemService == null) {
            player.sendMessage("§cItem System chưa được khởi tạo! Vui lòng chờ server load xong.");
            return;
        }
        
        Map<StatType, Double> stats = itemService.getTotalStats(profile);
        
        player.sendMessage("§e=== Item Stats ===");
        
        if (stats.isEmpty()) {
            player.sendMessage("§7Không có item nào được equip");
            return;
        }
        
        for (Map.Entry<StatType, Double> entry : stats.entrySet()) {
            StatType statType = entry.getKey();
            double value = entry.getValue();
            player.sendMessage("§7" + statType + ": §f" + String.format("%.1f", value));
        }
        
        // Show equipped items
        player.sendMessage("§e=== Equipped Items ===");
        Map<EquipmentSlot, String> equipped = profile.getEquippedItems();
        if (equipped.isEmpty()) {
            player.sendMessage("§7Không có item nào");
        } else {
            for (Map.Entry<EquipmentSlot, String> entry : equipped.entrySet()) {
                player.sendMessage("§7" + entry.getKey() + ": §f" + entry.getValue());
            }
        }
    }
    
    private void handleList(Player player) {
        CoreContext ctx = CoreContext.getInstance();
        var registry = ctx.getItemContext().getRegistry();
        
        player.sendMessage("§e=== Available Items ===");
        
        var allItems = registry.getAll();
        if (allItems.isEmpty()) {
            player.sendMessage("§7Không có item nào trong registry");
            return;
        }
        
        for (var item : allItems) {
            player.sendMessage("§7- §f" + item.getId() + " §7(" + item.getSlot() + ")");
        }
    }
}
