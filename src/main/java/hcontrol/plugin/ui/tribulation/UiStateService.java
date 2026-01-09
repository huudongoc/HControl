package hcontrol.plugin.ui.tribulation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UI STATE SERVICE
 * Quan ly trang thai UI cua tung player
 * KHONG dung static - inject qua DI
 */
public class UiStateService {

    private final Map<UUID, UiState> states = new HashMap<>();

    public UiState getState(UUID id) {
        return states.getOrDefault(id, UiState.NONE);
    }

    public void setState(UUID id, UiState state) {
        states.put(id, state);
    }

    public void clear(UUID id) {
        states.remove(id);
    }
    
    /**
     * Check xem player co dang trong UI nao khong
     */
    public boolean isInUI(UUID id) {
        return getState(id) != UiState.NONE;
    }
}
