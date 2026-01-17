package hcontrol.plugin.listener;

import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.playerskill.PlayerSkill;
import hcontrol.plugin.playerskill.PlayerSkillRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * PHASE 6 — PLAYER SKILL PROJECTILE LISTENER
 * Xử lý khi projectile từ player skill trúng mục tiêu
 */
public class PlayerSkillProjectileListener implements Listener {
    
    private final Plugin plugin;
    private final PlayerManager playerManager;
    private final PlayerSkillRegistry skillRegistry;
    private final NamespacedKey skillIdKey;
    
    public PlayerSkillProjectileListener(Plugin plugin, PlayerManager playerManager, 
                                         PlayerSkillRegistry skillRegistry) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.skillRegistry = skillRegistry;
        this.skillIdKey = new NamespacedKey(plugin, "player_skill_id");
    }
    
    /**
     * Lấy NamespacedKey để dùng trong executor
     */
    public NamespacedKey getSkillIdKey() {
        return skillIdKey;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        
        // Check có phải player skill projectile không
        if (!projectile.getPersistentDataContainer().has(skillIdKey, PersistentDataType.STRING)) {
            return;
        }
        
        // Lấy skill ID
        String skillId = projectile.getPersistentDataContainer().get(skillIdKey, PersistentDataType.STRING);
        if (skillId == null) return;
        
        // Lấy skill info
        PlayerSkill skill = skillRegistry.getSkill(skillId);
        if (skill == null) return;
        
        // Check shooter là player
        if (!(projectile.getShooter() instanceof Player shooter)) {
            return;
        }
        
        // Lấy profile của shooter
        PlayerProfile shooterProfile = playerManager.get(shooter.getUniqueId());
        if (shooterProfile == null) return;
        
        // Check hit entity
        if (event.getHitEntity() instanceof LivingEntity target) {
            // Không damage player (PvP sẽ xử lý riêng nếu cần)
            if (target instanceof Player) {
                return;
            }
            
            // Calculate damage
            double baseDamage = shooterProfile.getRealm().getBaseDamage();
            double finalDamage = baseDamage * skill.getDamageMultiplier();
            
            // Deal damage
            double newHealth = Math.max(0, target.getHealth() - finalDamage);
            target.setHealth(newHealth);
            
            // Apply skill effects
            for (PlayerSkill.SkillEffect effect : skill.getEffects()) {
                target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    effect.getEffectType(),
                    effect.getDuration(),
                    effect.getAmplifier()
                ));
            }
            
            // Visual feedback
            shooter.sendMessage("§c⚔ " + skill.getDisplayName() + " §c→ " + 
                target.getType().name() + " §7(" + (int) finalDamage + " damage)");
            
            // Particles at hit location
            target.getWorld().spawnParticle(
                org.bukkit.Particle.FLAME,
                target.getLocation().add(0, 1, 0),
                15, 0.3, 0.3, 0.3, 0.05
            );
        }
        
        // Remove projectile if it's a fireball (to prevent explosion)
        if (projectile instanceof Fireball) {
            projectile.remove();
        }
    }
}
