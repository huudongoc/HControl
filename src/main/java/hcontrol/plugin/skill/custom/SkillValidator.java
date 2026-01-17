package hcontrol.plugin.skill.custom;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * SKILL VALIDATOR
 * Kiểm tra công pháp có hợp lệ theo đạo tắc không
 */
public class SkillValidator {
    
    /**
     * Kết quả validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        private ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, List.of(), List.of());
        }
        
        public static ValidationResult success(List<String> warnings) {
            return new ValidationResult(true, List.of(), warnings);
        }
        
        public static ValidationResult fail(List<String> errors) {
            return new ValidationResult(false, errors, List.of());
        }
        
        public static ValidationResult fail(List<String> errors, List<String> warnings) {
            return new ValidationResult(false, errors, warnings);
        }
        
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        
        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
    }
    
    /**
     * Validate skill template cho creator
     */
    public static ValidationResult validate(SkillTemplate template, PlayerProfile creator) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        CultivationRealm realm = creator.getRealm();
        
        // 1. Kiểm tra cảnh giới tối thiểu để tạo skill
        if (realm.ordinal() < CultivationRealm.TRUCCO.ordinal()) {
            errors.add("§cCần đạt Trúc Cơ để sáng tạo công pháp!");
            return ValidationResult.fail(errors);
        }
        
        // 2. Kiểm tra tổng điểm skill
        int points = SkillPointCalculator.calculatePoints(template);
        int cap = SkillPointCalculator.getPointCap(realm);
        
        if (points > cap) {
            errors.add("§cVượt giới hạn đạo tắc! " + points + "/" + cap + " điểm");
            errors.add("§7Cần nâng cảnh giới để tạo công pháp mạnh hơn");
        }
        
        // 3. Kiểm tra power tối đa
        double maxPower = SkillPointCalculator.getMaxPower(realm);
        if (template.getBasePower() > maxPower) {
            errors.add("§cSát thương vượt quá giới hạn cảnh giới!");
            errors.add("§7Max: " + (int) maxPower + ", hiện tại: " + (int) template.getBasePower());
        }
        
        // 4. Kiểm tra cooldown tối thiểu
        double minCd = SkillPointCalculator.getMinCooldown(realm);
        if (template.getCooldown() < minCd) {
            errors.add("§cHồi chiêu quá nhanh!");
            errors.add("§7Min: " + minCd + "s với cảnh giới hiện tại");
        }
        
        // 5. Kiểm tra AOE không quá lớn
        if (template.getAreaRadius() > 15) {
            errors.add("§cBán kính AOE tối đa 15 blocks!");
        }
        
        // 6. Kiểm tra projectile không quá nhiều
        if (template.getProjectileCount() > 10) {
            errors.add("§cSố phi tiêu tối đa 10!");
        }
        
        // 7. Warnings (không chặn, chỉ cảnh báo)
        if (points > cap * 0.8) {
            warnings.add("§eĐang sử dụng " + (points * 100 / cap) + "% giới hạn đạo tắc");
        }
        
        if (template.getManaCost() < 10) {
            warnings.add("§eMana cost thấp có thể gây mất cân bằng");
        }
        
        if (template.getCooldown() <= 2 && template.getCategory() == SkillCategory.ATTACK) {
            warnings.add("§eCooldown ngắn cho skill tấn công!");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success(warnings);
        }
        
        return ValidationResult.fail(errors, warnings);
    }
    
    /**
     * Validate từ session (chưa build template)
     */
    public static ValidationResult validateSession(
            PlayerProfile creator,
            SkillCategory category,
            Element element,
            TargetType targetType,
            double basePower,
            double cooldown,
            double manaCost,
            double range,
            double areaRadius,
            int projectileCount,
            double duration,
            int effectCount
    ) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        CultivationRealm realm = creator.getRealm();
        
        // 1. Cảnh giới
        if (realm.ordinal() < CultivationRealm.TRUCCO.ordinal()) {
            errors.add("§cCần đạt Trúc Cơ để sáng tạo công pháp!");
            return ValidationResult.fail(errors);
        }
        
        // 2. Tổng điểm
        int points = SkillPointCalculator.calculatePointsFromSession(
            category, element, targetType, basePower, cooldown, range,
            areaRadius, projectileCount, duration, effectCount
        );
        int cap = SkillPointCalculator.getPointCap(realm);
        
        if (points > cap) {
            errors.add("§cVượt giới hạn! " + points + "/" + cap + " điểm");
        }
        
        // 3. Power
        double maxPower = SkillPointCalculator.getMaxPower(realm);
        if (basePower > maxPower) {
            errors.add("§cSát thương vượt max: " + (int) maxPower);
        }
        
        // 4. Cooldown
        double minCd = SkillPointCalculator.getMinCooldown(realm);
        if (cooldown < minCd) {
            errors.add("§cCooldown min: " + minCd + "s");
        }
        
        // 5. Limits
        if (areaRadius > 15) {
            errors.add("§cAOE max 15 blocks!");
        }
        if (projectileCount > 10) {
            errors.add("§cProjectile max 10!");
        }
        
        // Warnings
        if (points > cap * 0.8 && points <= cap) {
            warnings.add("§eSử dụng " + (points * 100 / cap) + "% đạo tắc");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success(warnings);
        }
        
        return ValidationResult.fail(errors, warnings);
    }
    
    /**
     * Kiểm tra player có thể học skill không
     * 
     * Logic:
     * - Nếu player realm > skill required realm → OK (cảnh giới cao hơn học skill thấp hơn)
     * - Nếu player realm == skill required realm → check level trong realm
     * - Nếu player realm < skill required realm → FAIL
     */
    public static ValidationResult canLearn(SkillTemplate template, PlayerProfile learner) {
        List<String> errors = new ArrayList<>();
        
        int playerRealmOrdinal = learner.getRealm().ordinal();
        int skillRealmOrdinal = template.getRequiredRealm().ordinal();
        
        // 1. Kiểm tra cảnh giới
        if (playerRealmOrdinal < skillRealmOrdinal) {
            // Player cảnh giới thấp hơn yêu cầu
            errors.add("§cCần đạt " + template.getRequiredRealm().getDisplayName() + " để học!");
        } else if (playerRealmOrdinal == skillRealmOrdinal) {
            // Player CÙNG cảnh giới với skill → check level
            if (learner.getRealmLevel() < template.getRequiredLevel()) {
                errors.add("§cCần level " + template.getRequiredLevel() + " trong " + 
                    template.getRequiredRealm().getDisplayName() + "!");
            }
        }
        // Nếu playerRealmOrdinal > skillRealmOrdinal → OK, không cần check level
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        }
        
        return ValidationResult.fail(errors);
    }
}
