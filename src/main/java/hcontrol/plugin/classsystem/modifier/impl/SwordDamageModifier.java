package hcontrol.plugin.classsystem.modifier.impl;

import hcontrol.plugin.classsystem.modifier.ClassModifier;
import hcontrol.plugin.classsystem.modifier.ModifierContext;
import hcontrol.plugin.classsystem.modifier.ModifierType;
import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.item.EquipmentSlot;
import hcontrol.plugin.item.ItemTemplate;
import hcontrol.plugin.item.ItemType;
import hcontrol.plugin.model.LivingActor;
import hcontrol.plugin.player.PlayerProfile;

/**
 * PHASE 5 — SWORD DAMAGE MODIFIER
 * +15% damage khi dùng kiếm (melee weapon)
 */
public class SwordDamageModifier implements ClassModifier {
    
    @Override
    public ModifierType getType() {
        return ModifierType.DAMAGE;
    }
    
    @Override
    public double modify(LivingActor actor, ModifierContext ctx, double baseValue) {
        // Chỉ apply cho Player
        if (!(actor instanceof PlayerProfile playerProfile)) {
            return baseValue;
        }
        
        // Check đang cầm item ở HAND slot
        String handItemId = playerProfile.getItemAtSlot(EquipmentSlot.HAND);
        if (handItemId == null) {
            return baseValue;
        }
        
        // Check item type = WEAPON
        try {
            CoreContext coreCtx = CoreContext.getInstance();
            ItemTemplate itemTemplate = coreCtx.getItemContext().getRegistry().get(handItemId);
            if (itemTemplate == null || itemTemplate.getType() != ItemType.WEAPON) {
                return baseValue;
            }
        } catch (Exception e) {
            // Fallback: nếu không lấy được registry, chỉ check có item
            return baseValue * 1.15;
        }
        
        // +15% damage cho melee attacks với weapon
        return baseValue * 1.15;
    }
}
