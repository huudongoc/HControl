package hcontrol.plugin.classsystem.modifier;

import hcontrol.plugin.model.LivingActor;

/**
 * PHASE 5 — CLASS MODIFIER INTERFACE
 * Modifier chỉ sửa số, không có side effects
 */
public interface ClassModifier {
    
    /**
     * Get modifier type
     */
    ModifierType getType();
    
    /**
     * Modify value
     * @param actor Actor đang apply modifier
     * @param context Context của combat/skill
     * @param baseValue Giá trị gốc
     * @return Giá trị sau khi modify
     */
    double modify(LivingActor actor, ModifierContext context, double baseValue);
}
