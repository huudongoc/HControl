package hcontrol.plugin.classsystem;

import hcontrol.plugin.classsystem.modifier.ClassModifier;
import hcontrol.plugin.player.PlayerProfile;

import java.util.List;

/**
 * PHASE 5 — CLASS SERVICE
 * Service quản lý class system
 * Không giữ state, không Bukkit dependencies
 */
public class ClassService {
    
    private final ClassRegistry registry;
    
    public ClassService(ClassRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Get modifiers cho player
     * @return List modifiers (empty nếu không có class)
     */
    public List<ClassModifier> getModifiers(PlayerProfile player) {
        if (player == null) {
            return List.of();
        }
        
        ClassProfile classProfile = player.getClassProfile();
        if (classProfile == null) {
            return List.of();
        }
        
        return registry.getModifiers(classProfile.getType());
    }
}
