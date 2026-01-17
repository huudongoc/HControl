package hcontrol.plugin.event;

import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event được bắn khi player có thay đổi trạng thái
 * Các hệ thống khác (Nameplate, Chat, TabList...) lắng nghe event này
 */
public class PlayerStateChangeEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final PlayerProfile profile;
    private final PlayerStateChangeType type;
    private final Object data; // Optional: thêm data nếu cần (ví dụ: Sect cũ, Realm cũ...)
    
    public PlayerStateChangeEvent(Player player, PlayerProfile profile, PlayerStateChangeType type) {
        this(player, profile, type, null);
    }
    
    public PlayerStateChangeEvent(Player player, PlayerProfile profile, PlayerStateChangeType type, Object data) {
        this.player = player;
        this.profile = profile;
        this.type = type;
        this.data = data;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public PlayerProfile getProfile() {
        return profile;
    }
    
    public PlayerStateChangeType getType() {
        return type;
    }
    
    /**
     * Lấy data kèm theo (có thể null)
     * Cần cast về type phù hợp:
     * - REALM_CHANGE: CultivationRealm (realm cũ)
     * - SECT_JOIN/LEAVE: Sect (sect object)
     * - TITLE_CHANGE: Title (title cũ)
     * - MASTER_RELATION_CHANGE: String (master name hoặc disciple name)
     */
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
