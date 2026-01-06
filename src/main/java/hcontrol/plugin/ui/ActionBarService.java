package hcontrol.plugin.ui;

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
                .append(Component.text("Lv. " + status.getLevel(), NamedTextColor.GOLD))
                .build();

        player.sendActionBar(bar);
    }
}
