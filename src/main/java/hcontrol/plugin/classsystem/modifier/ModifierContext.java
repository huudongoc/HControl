package hcontrol.plugin.classsystem.modifier;

import hcontrol.plugin.model.LivingActor;

/**
 * PHASE 5 — MODIFIER CONTEXT
 * Context cho modifier calculations
 * Không chứa Bukkit dependencies
 */
public class ModifierContext {
    
    private final LivingActor attacker;
    private final LivingActor target;
    private final String skillId;  // nullable - skill đang dùng (nếu có)
    
    public ModifierContext(LivingActor attacker, LivingActor target) {
        this.attacker = attacker;
        this.target = target;
        this.skillId = null;
    }
    
    public ModifierContext(LivingActor attacker, LivingActor target, String skillId) {
        this.attacker = attacker;
        this.target = target;
        this.skillId = skillId;
    }
    
    public LivingActor getAttacker() {
        return attacker;
    }
    
    public LivingActor getTarget() {
        return target;
    }
    
    public String getSkillId() {
        return skillId;
    }
    
    public boolean hasSkill() {
        return skillId != null;
    }
}
