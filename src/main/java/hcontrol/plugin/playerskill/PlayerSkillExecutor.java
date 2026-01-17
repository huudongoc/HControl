package hcontrol.plugin.playerskill;

import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CombatService;
import hcontrol.plugin.skill.SkillType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * PHASE 6 — PLAYER SKILL EXECUTOR
 * Execute skill effects (damage, particles, sounds)
 */
public class PlayerSkillExecutor {
    
    private final CombatService combatService;
    
    public PlayerSkillExecutor(CombatService combatService) {
        this.combatService = combatService;
    }
    
    /**
     * Execute skill
     * @return true nếu thành công
     */
    public boolean execute(Player player, PlayerProfile profile, PlayerSkill skill) {
        SkillType type = skill.getType();
        
        return switch (type) {
            case MELEE -> executeMelee(player, profile, skill);
            case RANGED -> executeRanged(player, profile, skill);
            case AOE -> executeAOE(player, profile, skill);
            case BUFF -> executeBuff(player, profile, skill);
            case DEBUFF -> executeDebuff(player, profile, skill);
            case HEAL -> executeHeal(player, profile, skill);
            case TELEPORT -> executeTeleport(player, profile, skill);
            case SUMMON -> executeSummon(player, profile, skill);
        };
    }
    
    // ========== MELEE ==========
    
    private boolean executeMelee(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Find nearest target in range
        LivingEntity target = findNearestTarget(player, skill.getRange());
        
        if (target == null) {
            player.sendMessage("§7Không có mục tiêu trong tầm đánh!");
            return false;
        }
        
        // Calculate damage (from REALM, not stat)
        double baseDamage = profile.getRealm().getBaseDamage();
        double finalDamage = baseDamage * skill.getDamageMultiplier();
        
        // Deal damage manually (CombatService.dealDamage is private)
        dealSkillDamage(target, finalDamage);
        
        // Apply effects
        applyEffects(target, skill);
        
        // Visual effects
        playMeleeEffects(player, target, skill);
        
        // Notify
        player.sendMessage("§a⚔ " + skill.getDisplayName() + " §a→ " + getEntityName(target) + " §7(" + (int) finalDamage + " damage)");
        
        return true;
    }
    
    // ========== RANGED ==========
    
    private boolean executeRanged(Player player, PlayerProfile profile, PlayerSkill skill) {
        int projectileCount = skill.getProjectileCount();
        
        for (int i = 0; i < projectileCount; i++) {
            // Launch projectile with spread if multiple
            Vector direction = player.getLocation().getDirection();
            
            if (projectileCount > 1) {
                // Add spread for multiple projectiles
                double spread = 0.1 * (i - projectileCount / 2.0);
                direction.rotateAroundY(spread);
            }
            
            // Spawn fireball (or arrow depending on skill)
            launchProjectile(player, profile, skill, direction);
        }
        
        // Visual effects
        playRangedEffects(player, skill);
        
        // Notify
        player.sendMessage("§c🔥 " + skill.getDisplayName() + " §c→ Đã bắn!");
        
        return true;
    }
    
    private void launchProjectile(Player player, PlayerProfile profile, PlayerSkill skill, Vector direction) {
        Location spawnLoc = player.getEyeLocation().add(direction.clone().multiply(1.5));
        
        // Tạo fireball
        Fireball fireball = player.getWorld().spawn(spawnLoc, Fireball.class);
        fireball.setDirection(direction.normalize().multiply(1.5));
        fireball.setShooter(player);
        fireball.setYield(0); // No explosion damage (custom damage)
        fireball.setIsIncendiary(false);
        
        // Store skill data trong metadata hoặc custom tag
        // TODO: Handle projectile hit via listener để apply damage
        
        // Particle trail (optional)
        player.getWorld().spawnParticle(Particle.FLAME, spawnLoc, 5, 0.1, 0.1, 0.1, 0.01);
    }
    
    // ========== AOE ==========
    
    private boolean executeAOE(Player player, PlayerProfile profile, PlayerSkill skill) {
        Location center = player.getLocation();
        double radius = skill.getAreaRadius();
        
        // Find all targets in radius
        List<LivingEntity> targets = player.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> !(e instanceof Player p && p.equals(player))) // Exclude self
                .map(e -> (LivingEntity) e)
                .toList();
        
        if (targets.isEmpty()) {
            player.sendMessage("§7Không có mục tiêu trong phạm vi!");
            return false;
        }
        
        // Calculate damage (from REALM, not stat)
        double baseDamage = profile.getRealm().getBaseDamage();
        double finalDamage = baseDamage * skill.getDamageMultiplier();
        
        // Deal damage to all targets
        int hitCount = 0;
        for (LivingEntity target : targets) {
            dealSkillDamage(target, finalDamage);
            applyEffects(target, skill);
            hitCount++;
        }
        
        // Visual effects
        playAOEEffects(player, center, radius, skill);
        
        // Notify
        player.sendMessage("§d💥 " + skill.getDisplayName() + " §d→ Đánh trúng " + hitCount + " mục tiêu!");
        
        return true;
    }
    
    // ========== BUFF ==========
    
    private boolean executeBuff(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Apply buff effects to self
        for (PlayerSkill.SkillEffect effect : skill.getEffects()) {
            PotionEffect potionEffect = new PotionEffect(
                    effect.getEffectType(),
                    effect.getDuration(),
                    effect.getAmplifier()
            );
            player.addPotionEffect(potionEffect);
        }
        
        // Visual effects
        playBuffEffects(player, skill);
        
        // Notify
        player.sendMessage("§a✨ " + skill.getDisplayName() + " §a→ Đã kích hoạt!");
        
        return true;
    }
    
    // ========== DEBUFF ==========
    
    private boolean executeDebuff(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Find target
        LivingEntity target = findNearestTarget(player, skill.getRange());
        
        if (target == null) {
            player.sendMessage("§7Không có mục tiêu trong tầm!");
            return false;
        }
        
        // Apply debuff effects to target
        applyEffects(target, skill);
        
        // Visual effects
        playDebuffEffects(player, target, skill);
        
        // Notify
        player.sendMessage("§5☠ " + skill.getDisplayName() + " §5→ " + getEntityName(target));
        
        return true;
    }
    
    // ========== HEAL ==========
    
    private boolean executeHeal(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Calculate heal amount (30% of max HP by default)
        double maxHP = profile.getMaxHP();
        double healAmount = maxHP * 0.3; // TODO: Configurable trong skill
        
        // Apply heal
        double currentHP = profile.getCurrentHP();
        double newHP = Math.min(currentHP + healAmount, maxHP);
        profile.setCurrentHP(newHP);
        
        // Sync with Bukkit health
        double healthPercent = newHP / maxHP;
        double bukkitMaxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(healthPercent * bukkitMaxHealth, bukkitMaxHealth));
        
        // Apply buff effects if any
        for (PlayerSkill.SkillEffect effect : skill.getEffects()) {
            PotionEffect potionEffect = new PotionEffect(
                    effect.getEffectType(),
                    effect.getDuration(),
                    effect.getAmplifier()
            );
            player.addPotionEffect(potionEffect);
        }
        
        // Visual effects
        playHealEffects(player, skill);
        
        // Notify
        player.sendMessage("§a❤ " + skill.getDisplayName() + " §a→ Hồi " + (int) healAmount + " HP!");
        
        return true;
    }
    
    // ========== TELEPORT ==========
    
    private boolean executeTeleport(Player player, PlayerProfile profile, PlayerSkill skill) {
        // Get teleport destination (forward by range)
        Location current = player.getLocation();
        Vector direction = current.getDirection().normalize();
        double distance = skill.getRange();
        
        Location destination = current.clone().add(direction.multiply(distance));
        
        // Safety check - ensure solid ground
        destination = findSafeLocation(destination);
        
        if (destination == null) {
            player.sendMessage("§cKhông thể dịch chuyển đến vị trí này!");
            return false;
        }
        
        // Teleport
        player.teleport(destination);
        
        // Visual effects
        playTeleportEffects(current, destination, skill);
        
        // Notify
        player.sendMessage("§b✦ " + skill.getDisplayName() + " §b→ Đã dịch chuyển!");
        
        return true;
    }
    
    // ========== SUMMON (PLACEHOLDER) ==========
    
    private boolean executeSummon(Player player, PlayerProfile profile, PlayerSkill skill) {
        // TODO PHASE 8+: Implement summon system
        player.sendMessage("§7Summon skill chưa được implement!");
        return false;
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Find nearest living entity in range
     */
    private LivingEntity findNearestTarget(Player player, double range) {
        Location loc = player.getLocation();
        
        return player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> !(e instanceof Player)) // Don't target players for now
                .map(e -> (LivingEntity) e)
                .min((a, b) -> Double.compare(
                        a.getLocation().distanceSquared(loc),
                        b.getLocation().distanceSquared(loc)
                ))
                .orElse(null);
    }
    
    /**
     * Apply skill effects to target
     */
    private void applyEffects(LivingEntity target, PlayerSkill skill) {
        for (PlayerSkill.SkillEffect effect : skill.getEffects()) {
            PotionEffect potionEffect = new PotionEffect(
                    effect.getEffectType(),
                    effect.getDuration(),
                    effect.getAmplifier()
            );
            target.addPotionEffect(potionEffect);
        }
    }
    
    /**
     * Find safe teleport location
     */
    private Location findSafeLocation(Location loc) {
        // Check if destination is safe
        if (loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable()) {
            // Find ground
            while (loc.getY() > 0 && loc.clone().add(0, -1, 0).getBlock().isPassable()) {
                loc.add(0, -1, 0);
            }
            return loc;
        }
        return null;
    }
    
    /**
     * Deal skill damage to entity (simple damage without realm suppression)
     */
    private void dealSkillDamage(LivingEntity target, double damage) {
        double newHealth = Math.max(0, target.getHealth() - damage);
        target.setHealth(newHealth);
    }
    
    /**
     * Get entity display name
     */
    private String getEntityName(Entity entity) {
        if (entity instanceof Player p) {
            return p.getName();
        }
        return entity.getType().name();
    }
    
    // ========== VISUAL EFFECTS ==========
    
    private void playMeleeEffects(Player player, LivingEntity target, PlayerSkill skill) {
        Location loc = target.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.CRIT, loc, 15, 0.3, 0.3, 0.3, 0.1);
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
    }
    
    private void playRangedEffects(Player player, PlayerSkill skill) {
        Location loc = player.getLocation();
        player.getWorld().spawnParticle(Particle.FLAME, loc.add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.05);
        player.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
    }
    
    private void playAOEEffects(Player player, Location center, double radius, PlayerSkill skill) {
        // Circle particle
        for (double angle = 0; angle < 2 * Math.PI; angle += 0.3) {
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particleLoc = center.clone().add(x, 0.5, z);
            player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, particleLoc, 1, 0, 0, 0, 0);
        }
        player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
    
    private void playBuffEffects(Player player, PlayerSkill skill) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 20, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }
    
    private void playDebuffEffects(Player player, LivingEntity target, PlayerSkill skill) {
        Location loc = target.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 15, 0.3, 0.3, 0.3, 0.05);
        player.getWorld().playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.5f, 0.5f);
    }
    
    private void playHealEffects(Player player, PlayerSkill skill) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.HEART, loc, 10, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
    }
    
    private void playTeleportEffects(Location from, Location to, PlayerSkill skill) {
        from.getWorld().spawnParticle(Particle.PORTAL, from.add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
        to.getWorld().spawnParticle(Particle.END_ROD, to.add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        to.getWorld().playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
}
