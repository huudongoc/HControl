package hcontrol.plugin.ui.classsystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import hcontrol.plugin.classsystem.ClassType;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.player.PlayerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Class selection GUI for Phase 5.
 * Shows ClassType values and lets player click to set class.
 */
public class ClassMenuGUI {
    private final PlayerManager playerManager;

    public ClassMenuGUI(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Choose Class");

        int i = 0;
        for (ClassType type : ClassType.values()) {
            ItemStack item = createClassItem(type);
            inv.setItem(i++, item);
        }

        player.openInventory(inv);
    }

    private ItemStack createClassItem(ClassType type) {
        org.bukkit.Material mat = org.bukkit.Material.PAPER;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + type.name());
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to choose this class");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
