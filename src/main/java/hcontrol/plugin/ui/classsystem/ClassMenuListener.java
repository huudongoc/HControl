package hcontrol.plugin.ui.classsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.classsystem.ClassProfile;
import hcontrol.plugin.classsystem.ClassType;
import hcontrol.plugin.core.CoreContext;

/**
 * Listener to handle clicks in the ClassMenuGUI.
 */
public class ClassMenuListener implements Listener {
    private final PlayerManager playerManager;

    public ClassMenuListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals("Choose Class")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        Player clicker = (Player) event.getWhoClicked();
        String name = meta.getDisplayName();
        if (name == null) return;

        // name format is §eCLASS_NAME
        String className = name.replace("§e", "");
        try {
            ClassType type = ClassType.valueOf(className);
            PlayerProfile profile = playerManager.get(clicker.getUniqueId());
            if (profile != null) {
                ClassProfile cp = new ClassProfile(type);
                profile.setClassProfile(cp);

                // persist immediately
                var ctx = CoreContext.getInstance();
                if (ctx != null && ctx.getPlayerContext() != null && ctx.getPlayerContext().getPlayerStorage() != null) {
                    ctx.getPlayerContext().getPlayerStorage().save(profile);
                }

                clicker.sendMessage("§a✓ Bạn đã chọn class: §e" + type.name());
            }
        } catch (IllegalArgumentException ex) {
            // ignore invalid
        }

        clicker.closeInventory();
    }
}
