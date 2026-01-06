package hcontrol.plugin.player;

import org.bukkit.entity.Player;

public class PlayerLifecycle {

    private final PlayerManager playerManager;
    private final PlayerStorage playerStorage;

    public PlayerLifecycle(PlayerManager playerManager, PlayerStorage playerStorage) {
        this.playerManager = playerManager;
        this.playerStorage = playerStorage;
    }

    public void handleJoin(Player player) {
        PlayerProfile profile = playerStorage.load(player.getUniqueId());
        playerManager.add(profile);
        System.out.println("JOIN lifecycle: " + player.getName());

    }

    public void handleQuit(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile != null) {
            playerStorage.save(profile);
            playerManager.remove(player.getUniqueId());
        }
    }

    public void shutdown() {
        for (PlayerProfile profile : playerManager.getAllOnline()) {
            playerStorage.save(profile);
        }
        playerManager.clear();
    }
}
