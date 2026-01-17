package hcontrol.plugin.playerskill;

import hcontrol.plugin.identity.IdentityRuleService;
import hcontrol.plugin.identity.PlayerIdentity;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PHASE 6 — PLAYER SKILL SERVICE
 * Main service cho player skills
 * - Learn skills
 * - Cast skills
 * - Manage cooldowns
 * - Validate permissions
 */
public class PlayerSkillService {
    
    private final PlayerManager playerManager;
    private final PlayerSkillRegistry registry;
    private final IdentityRuleService identityRules;
    private final PlayerSkillExecutor executor;
    
    // Cooldown tracking: UUID -> SkillId -> CooldownEndTime
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    
    public PlayerSkillService(
            PlayerManager playerManager, 
            PlayerSkillRegistry registry, 
            IdentityRuleService identityRules,
            PlayerSkillExecutor executor) {
        this.playerManager = playerManager;
        this.registry = registry;
        this.identityRules = identityRules;
        this.executor = executor;
    }
    
    // ========== LEARN SKILL ==========
    
    /**
     * Player học skill
     * @return true nếu học thành công
     */
    public boolean learnSkill(Player player, String skillId) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return false;
        }
        
        // 1. Check skill exists
        PlayerSkill skill = registry.getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§cSkill không tồn tại: " + skillId);
            return false;
        }
        
        // 2. Check already learned
        if (profile.hasLearnedSkill(skillId)) {
            player.sendMessage("§eBạn đã học skill này rồi!");
            return false;
        }
        
        // 3. Check realm requirement
        if (!canLearnAtRealm(profile, skill)) {
            player.sendMessage("§cCần đạt cảnh giới " + skill.getMinRealm().getDisplayName() + " để học skill này!");
            return false;
        }
        
        // 4. Check level requirement (CHỈ KHI CÙNG CẢNH GIỚI)
        // Nếu player ở cảnh giới CAO HƠN skill yêu cầu → bỏ qua check level
        if (profile.getRealm().ordinal() == skill.getMinRealm().ordinal() 
            && profile.getRealmLevel() < skill.getMinLevel()) {
            player.sendMessage("§cCần đạt level " + skill.getMinLevel() + " trong " + 
                skill.getMinRealm().getDisplayName() + " để học!");
            return false;
        }
        
        // 5. Check identity rules
        PlayerIdentity identity = profile.getIdentity();
        if (!identityRules.canUseSkill(identity, skillId)) {
            player.sendMessage("§cThân phận không phù hợp để học skill này!");
            return false;
        }
        
        // 6. Learn skill
        profile.learnSkill(skillId);
        
        // 7. Success message
        player.sendMessage("§a✦ Đã học skill: " + skill.getDisplayName());
        player.sendMessage("§7" + String.join("\n§7", skill.getDescription()));
        
        return true;
    }
    
    // ========== CAST SKILL ==========
    
    /**
     * Player sử dụng skill
     * @return true nếu cast thành công
     */
    public boolean castSkill(Player player, String skillId) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return false;
        }
        
        // 1. Check skill exists
        PlayerSkill skill = registry.getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§cSkill không tồn tại!");
            return false;
        }
        
        // 2. Check learned
        if (!profile.hasLearnedSkill(skillId)) {
            player.sendMessage("§cBạn chưa học skill này!");
            return false;
        }
        
        // 3. Check identity rules
        PlayerIdentity identity = profile.getIdentity();
        if (!identityRules.canUseSkill(identity, skillId)) {
            player.sendMessage("§cThân phận không cho phép dùng skill này!");
            return false;
        }
        
        // 4. Check cooldown
        if (isOnCooldown(player.getUniqueId(), skillId)) {
            long remaining = getRemainingCooldown(player.getUniqueId(), skillId);
            player.sendMessage("§cSkill đang hồi chiêu! Còn " + formatTime(remaining));
            return false;
        }
        
        // 5. Check cost
        SkillCost cost = skill.getCost();
        if (!cost.canAfford(profile)) {
            player.sendMessage("§cKhông đủ Linh Khí! Cần: " + (int) cost.getLingQi());
            return false;
        }
        
        // 6. Deduct cost
        cost.deduct(profile);
        
        // 7. Set cooldown
        setCooldown(player.getUniqueId(), skillId, skill.getCooldown());
        
        // 8. Execute skill
        boolean success = executor.execute(player, profile, skill);
        
        if (!success) {
            // Refund cost if execution failed
            profile.setCurrentLingQi(profile.getCurrentLingQi() + cost.getLingQi());
            clearCooldown(player.getUniqueId(), skillId);
            player.sendMessage("§cKhông thể sử dụng skill lúc này!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Cast skill từ hotbar slot (1-9)
     */
    public boolean castSkillFromSlot(Player player, int slot) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return false;
        
        String skillId = profile.getSkillAtSlot(slot);
        if (skillId == null) {
            player.sendMessage("§7Slot " + slot + " chưa gán skill!");
            return false;
        }
        
        return castSkill(player, skillId);
    }
    
    // ========== BIND SKILL ==========
    
    /**
     * Gán skill vào hotbar slot
     */
    public boolean bindSkill(Player player, String skillId, int slot) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) {
            player.sendMessage("§cKhông tìm thấy profile!");
            return false;
        }
        
        // Check skill exists
        PlayerSkill skill = registry.getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§cSkill không tồn tại!");
            return false;
        }
        
        // Check learned
        if (!profile.hasLearnedSkill(skillId)) {
            player.sendMessage("§cBạn chưa học skill này!");
            return false;
        }
        
        // Validate slot
        if (slot < 1 || slot > 9) {
            player.sendMessage("§cSlot phải từ 1-9!");
            return false;
        }
        
        // Bind
        profile.bindSkill(slot, skillId);
        player.sendMessage("§aĐã gán " + skill.getDisplayName() + " §avào slot " + slot);
        
        return true;
    }
    
    /**
     * Gỡ skill khỏi hotbar slot
     */
    public boolean unbindSkill(Player player, int slot) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return false;
        
        String skillId = profile.getSkillAtSlot(slot);
        if (skillId == null) {
            player.sendMessage("§7Slot " + slot + " không có skill!");
            return false;
        }
        
        profile.unbindSkill(slot);
        player.sendMessage("§aĐã gỡ skill khỏi slot " + slot);
        return true;
    }
    
    // ========== COOLDOWN MANAGEMENT ==========
    
    /**
     * Set cooldown cho skill
     */
    private void setCooldown(UUID playerId, String skillId, int seconds) {
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
                 .put(skillId, System.currentTimeMillis() + (seconds * 1000L));
    }
    
    /**
     * Clear cooldown
     */
    private void clearCooldown(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns != null) {
            playerCooldowns.remove(skillId);
        }
    }
    
    /**
     * Check if skill is on cooldown
     */
    public boolean isOnCooldown(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return false;
        
        Long endTime = playerCooldowns.get(skillId);
        if (endTime == null) return false;
        
        return System.currentTimeMillis() < endTime;
    }
    
    /**
     * Get remaining cooldown in milliseconds
     */
    public long getRemainingCooldown(UUID playerId, String skillId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;
        
        Long endTime = playerCooldowns.get(skillId);
        if (endTime == null) return 0;
        
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Clear all cooldowns for player (logout/death)
     */
    public void clearAllCooldowns(UUID playerId) {
        cooldowns.remove(playerId);
    }
    
    // ========== QUERY ==========
    
    /**
     * Get available skills for player to learn
     */
    public List<PlayerSkill> getAvailableSkills(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return List.of();
        
        return registry.getSkillsByRealm(profile.getRealm()).stream()
                .filter(s -> !profile.hasLearnedSkill(s.getSkillId()))
                .filter(s -> profile.getRealmLevel() >= s.getMinLevel())
                .filter(s -> identityRules.canUseSkill(profile.getIdentity(), s.getSkillId()))
                .toList();
    }
    
    /**
     * Get learned skills for player
     */
    public List<PlayerSkill> getLearnedSkills(Player player) {
        PlayerProfile profile = playerManager.get(player.getUniqueId());
        if (profile == null) return List.of();
        
        return profile.getLearnedSkills().stream()
                .map(registry::getSkill)
                .filter(s -> s != null)
                .toList();
    }
    
    // ========== HELPERS ==========
    
    /**
     * Check if player's realm allows learning this skill
     */
    private boolean canLearnAtRealm(PlayerProfile profile, PlayerSkill skill) {
        return profile.getRealm().ordinal() >= skill.getMinRealm().ordinal();
    }
    
    /**
     * Format time (ms -> "Xs" or "Xm Ys")
     */
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "m " + remainingSeconds + "s";
    }
    
    // ========== GETTERS ==========
    
    public PlayerSkillRegistry getRegistry() {
        return registry;
    }
}
