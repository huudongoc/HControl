package hcontrol.plugin.classsystem;

import hcontrol.plugin.classsystem.modifier.ClassModifier;
import hcontrol.plugin.classsystem.modifier.impl.SwordDamageModifier;

import java.util.*;

/**
 * PHASE 5 — CLASS REGISTRY
 * Quản lý modifiers cho mỗi class type
 */
public class ClassRegistry {
    
    private final Map<ClassType, List<ClassModifier>> modifierMap = new EnumMap<>(ClassType.class);
    
    public ClassRegistry() {
        registerDefaults();
    }
    
    /**
     * Register default modifiers cho các class
     */
    private void registerDefaults() {
        // SWORD_CULTIVATOR: +15% melee damage
        modifierMap.put(
            ClassType.SWORD_CULTIVATOR,
            List.of(new SwordDamageModifier())
        );
        
        // Các class khác sẽ thêm sau
        modifierMap.put(ClassType.SPELL_CULTIVATOR, new ArrayList<>());
        modifierMap.put(ClassType.BODY_REFINER, new ArrayList<>());
        modifierMap.put(ClassType.DEMON_PATH, new ArrayList<>());
        modifierMap.put(ClassType.MEDICAL_CULTIVATOR, new ArrayList<>());
    }
    
    /**
     * Get modifiers cho class type
     */
    public List<ClassModifier> getModifiers(ClassType type) {
        return modifierMap.getOrDefault(type, List.of());
    }
    
    /**
     * Register modifier cho class type (tương lai: load từ config)
     */
    public void registerModifier(ClassType type, ClassModifier modifier) {
        modifierMap.computeIfAbsent(type, k -> new ArrayList<>()).add(modifier);
    }
}
