package hcontrol.plugin.ui.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ActionBarService {

    private final PlayerStatusProvider statusProvider;

    public ActionBarService(PlayerStatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }

    public void render(Player player) {
        PlayerStatusSnapshot status = statusProvider.getStatus(player);

        Component bar = Component.text()
                .append(Component.text("❤ ", NamedTextColor.RED))
                .append(Component.text(
                        (int) status.getCurrentHp() + "/" + (int) status.getMaxHp(),
                        NamedTextColor.WHITE
                ))
                .append(Component.text("   ✦   ", NamedTextColor.DARK_GRAY))
                .append(Component.text("✦ Mana: ", NamedTextColor.AQUA))
                .append(Component.text(
                        (int) status.getCurrentMana() + "/" + (int) status.getMaxMana(),
                        NamedTextColor.WHITE
                ))
                .append(Component.text("   ✦   ", NamedTextColor.DARK_GRAY))
                .append(Component.text(getTierName(status.getLevel()), NamedTextColor.GOLD))
                .build();

        player.sendActionBar(bar);
    }
    
    private String getTierName(int level) {
        if (level <= 3) return "§7Hạ Phẩm";
        if (level <= 6) return "§eTrung Phẩm";
        if (level <= 9) return "§6Thượng Phẩm";
        return "§cĐỉnh Phẩm";
    }
}
