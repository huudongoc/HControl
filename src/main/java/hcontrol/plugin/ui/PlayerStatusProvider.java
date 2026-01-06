package hcontrol.plugin.ui;

import org.bukkit.entity.Player;

public interface PlayerStatusProvider {

    PlayerStatusSnapshot getStatus(Player player);

}
