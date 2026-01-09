package hcontrol.plugin.ui.player;

import org.bukkit.entity.Player;

public interface PlayerStatusProvider {

    PlayerStatusSnapshot getStatus(Player player);

}
