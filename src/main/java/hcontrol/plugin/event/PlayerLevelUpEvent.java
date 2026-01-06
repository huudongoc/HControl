package hcontrol.plugin.event;

import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLevelUpEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final PlayerProfile profile;
    private final int oldLevel;
    private final int newLevel;

    public PlayerLevelUpEvent(PlayerProfile profile, int oldLevel, int newLevel) {
        this.profile = profile;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
