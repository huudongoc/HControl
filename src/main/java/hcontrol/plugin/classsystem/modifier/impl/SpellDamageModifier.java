package hcontrol.plugin.classsystem.modifier.impl;

import hcontrol.plugin.classsystem.modifier.ClassModifier;
import hcontrol.plugin.classsystem.modifier.ModifierContext;
import hcontrol.plugin.classsystem.modifier.ModifierType;
import hcontrol.plugin.model.LivingActor;

/**
 * PHASE 5 — SPELL DAMAGE MODIFIER
 * Buff damage khi dùng skill, scale theo % Linh Khí
 * 
 * Logic:
 * - Chỉ buff khi dùng skill (không buff đánh thường)
 * - Scale: 0% linh khí → +0%, 100% linh khí → +30%
 * - Không có linh khí → không buff
 */
public class SpellDamageModifier implements ClassModifier {

    @Override
    public ModifierType getType() {
        return ModifierType.DAMAGE;
    }

    @Override
    public double modify(LivingActor actor, ModifierContext ctx, double base) {
        // ❌ Không dùng skill → không buff
        if (!ctx.hasSkill()) {
            return base;
        }

        // ❌ Không có linh khí → thôi
        double maxLingQi = actor.getMaxLingQi();
        if (maxLingQi <= 0) {
            return base;
        }

        double current = actor.getCurrentLingQi();
        double ratio = current / maxLingQi;

        // Scale: 0% → +0%, 100% → +30%
        double bonus = 1.0 + (0.3 * ratio);

        return base * bonus;
    }
}
