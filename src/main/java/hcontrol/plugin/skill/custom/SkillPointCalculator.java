package hcontrol.plugin.skill.custom;

import hcontrol.plugin.model.CultivationRealm;

/**
 * SKILL POINT CALCULATOR
 * Tính toán điểm skill dựa trên stats
 * Dùng để balance và validation
 */
public class SkillPointCalculator {
    
    // ===== WEIGHTS =====
    private static final double POWER_WEIGHT = 1.0;         // 1 point per 1 power
    private static final double COOLDOWN_WEIGHT = 5.0;      // 5 points per -1s cooldown
    private static final double RANGE_WEIGHT = 2.0;         // 2 points per 1 block range
    private static final double AOE_WEIGHT = 8.0;           // 8 points per 1 block radius
    private static final double PROJECTILE_WEIGHT = 15.0;   // 15 points per extra projectile
    private static final double DURATION_WEIGHT = 3.0;      // 3 points per 1s duration
    private static final double EFFECT_WEIGHT = 20.0;       // 20 points per effect
    
    // Base cooldown (10s = 0 cost, lower = more cost)
    private static final double BASE_COOLDOWN = 10.0;
    
    /**
     * Tính tổng điểm skill
     */
    public static int calculatePoints(SkillTemplate template) {
        double points = 0;
        
        // 1. Power contribution
        points += template.getBasePower() * POWER_WEIGHT;
        
        // 2. Cooldown (shorter = more expensive)
        double cdDiff = BASE_COOLDOWN - template.getCooldown();
        if (cdDiff > 0) {
            points += cdDiff * COOLDOWN_WEIGHT;
        }
        
        // 3. Range (only for non-SELF)
        if (template.getTargetType() != TargetType.SELF) {
            points += template.getRange() * RANGE_WEIGHT;
        }
        
        // 4. AOE radius
        if (template.getAreaRadius() > 0) {
            points += template.getAreaRadius() * AOE_WEIGHT;
        }
        
        // 5. Projectile count
        if (template.getProjectileCount() > 1) {
            points += (template.getProjectileCount() - 1) * PROJECTILE_WEIGHT;
        }
        
        // 6. Duration (for BUFF/CONTROL)
        if (template.getDuration() > 0) {
            points += template.getDuration() * DURATION_WEIGHT;
        }
        
        // 7. Effects
        points += template.getEffects().size() * EFFECT_WEIGHT;
        
        // 8. Category multiplier
        points *= template.getCategory().getPointMultiplier();
        
        // 9. Target type multiplier
        points *= (1.0 + template.getTargetType().getPointMultiplier() * 0.2);
        
        // 10. Element bonus (có hệ = thêm giá trị)
        if (template.hasElement()) {
            points *= 1.1;
        }
        
        return (int) Math.ceil(points);
    }
    
    /**
     * Tính điểm từ session đang tạo (chưa build template)
     */
    public static int calculatePointsFromSession(
            SkillCategory category,
            Element element,
            TargetType targetType,
            double basePower,
            double cooldown,
            double range,
            double areaRadius,
            int projectileCount,
            double duration,
            int effectCount
    ) {
        double points = 0;
        
        // Power
        points += basePower * POWER_WEIGHT;
        
        // Cooldown
        double cdDiff = BASE_COOLDOWN - cooldown;
        if (cdDiff > 0) {
            points += cdDiff * COOLDOWN_WEIGHT;
        }
        
        // Range
        if (targetType != TargetType.SELF) {
            points += range * RANGE_WEIGHT;
        }
        
        // AOE
        if (areaRadius > 0) {
            points += areaRadius * AOE_WEIGHT;
        }
        
        // Projectiles
        if (projectileCount > 1) {
            points += (projectileCount - 1) * PROJECTILE_WEIGHT;
        }
        
        // Duration
        if (duration > 0) {
            points += duration * DURATION_WEIGHT;
        }
        
        // Effects
        points += effectCount * EFFECT_WEIGHT;
        
        // Multipliers
        points *= category.getPointMultiplier();
        points *= (1.0 + targetType.getPointMultiplier() * 0.2);
        
        if (element != null) {
            points *= 1.1;
        }
        
        return (int) Math.ceil(points);
    }
    
    /**
     * Lấy giới hạn điểm theo cảnh giới
     */
    public static int getPointCap(CultivationRealm realm) {
        return switch (realm) {
            case PHAMNHAN -> 80;
            case LUYENKHI -> 120;
            case TRUCCO -> 200;
            case KIMDAN -> 350;
            case NGUYENANH -> 550;
            case HOATHAN -> 800;
            case LUYENHON -> 1200;
            case HOPTHE -> 1800;
            case DAITHUA -> 2500;
            case DOKIEP -> 3500;
            case CHANTIEN -> 5000;
        };
    }
    
    /**
     * Lấy sát thương tối đa theo cảnh giới
     */
    public static double getMaxPower(CultivationRealm realm) {
        return realm.getBaseDamage() * 2.5;
    }
    
    /**
     * Lấy cooldown tối thiểu theo cảnh giới
     */
    public static double getMinCooldown(CultivationRealm realm) {
        return switch (realm) {
            case PHAMNHAN -> 5.0;
            case LUYENKHI -> 4.0;
            case TRUCCO -> 3.0;
            case KIMDAN -> 2.0;
            case NGUYENANH, HOATHAN -> 1.5;
            case LUYENHON, HOPTHE, DAITHUA -> 1.0;
            case DOKIEP, CHANTIEN -> 0.5;
        };
    }
    
    /**
     * Tính cost tạo skill (Linh Thạch)
     */
    public static long calculateCreationCost(int skillPoints) {
        // Base: 500 + 10 per point
        return 500 + (skillPoints * 10L);
    }
    
    /**
     * Hiển thị breakdown điểm
     */
    public static String getPointBreakdown(
            double basePower, double cooldown, double range,
            double areaRadius, int projectileCount, int effectCount
    ) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("§7Power: §e+").append((int)(basePower * POWER_WEIGHT)).append("\n");
        
        double cdDiff = BASE_COOLDOWN - cooldown;
        if (cdDiff > 0) {
            sb.append("§7Cooldown: §e+").append((int)(cdDiff * COOLDOWN_WEIGHT)).append("\n");
        }
        
        if (range > 0) {
            sb.append("§7Range: §e+").append((int)(range * RANGE_WEIGHT)).append("\n");
        }
        
        if (areaRadius > 0) {
            sb.append("§7AOE: §e+").append((int)(areaRadius * AOE_WEIGHT)).append("\n");
        }
        
        if (projectileCount > 1) {
            sb.append("§7Projectiles: §e+").append((int)((projectileCount - 1) * PROJECTILE_WEIGHT)).append("\n");
        }
        
        if (effectCount > 0) {
            sb.append("§7Effects: §e+").append((int)(effectCount * EFFECT_WEIGHT)).append("\n");
        }
        
        return sb.toString();
    }
}
