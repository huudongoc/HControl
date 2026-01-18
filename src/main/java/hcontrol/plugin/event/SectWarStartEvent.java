package hcontrol.plugin.event;

import hcontrol.plugin.sect.Sect;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

/**
 * Event được bắn khi Sect War bắt đầu
 * Trigger từ SectWarStartEvent
 * Không chạy thường xuyên
 */
public class SectWarStartEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Sect sect1;
    private final Sect sect2;
    private final List<UUID> sect1Members;
    private final List<UUID> sect2Members;
    private final int durationSeconds; // Thời gian war (giây)
    
    public SectWarStartEvent(Sect sect1, Sect sect2, 
                            List<UUID> sect1Members, 
                            List<UUID> sect2Members,
                            int durationSeconds) {
        this.sect1 = sect1;
        this.sect2 = sect2;
        this.sect1Members = sect1Members;
        this.sect2Members = sect2Members;
        this.durationSeconds = durationSeconds;
    }
    
    public Sect getSect1() {
        return sect1;
    }
    
    public Sect getSect2() {
        return sect2;
    }
    
    public List<UUID> getSect1Members() {
        return sect1Members;
    }
    
    public List<UUID> getSect2Members() {
        return sect2Members;
    }
    
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
