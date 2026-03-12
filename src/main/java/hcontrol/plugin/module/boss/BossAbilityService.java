package hcontrol.plugin.module.boss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import hcontrol.plugin.Main;

import java.util.Random;

/**
 * BOSS ABILITY SERVICE
 * Special abilities cho world boss (modifier-based design)
 * 
 * Abilities:
 * - AOE Slam (knockback + damage)
 * - Summon Minions (spawn adds)
 * - Heal (self-heal)
 * - Meteor Rain (ranged AOE)
 * 
 * Design: Modifier-based, không hard-code damage
 */
public class BossAbilityService {
    
    private final Main plugin;
    private final BossEntity boss;
    private final int bossAscensionLevel;
    private final Random random;
    
    private static final int AOE_SLAM_COOLDOWN = 15;  // 15 seconds
    private static final int SUMMON_MINIONS_COOLDOWN = 30;  // 30 seconds
    private static final int HEAL_COOLDOWN = 45;  // 45 seconds
    private static final int METEOR_RAIN_COOLDOWN = 20;  // 20 seconds
    
    private long lastAoeSlamTime = 0;
    private long lastSummonMinionsTime = 0;
    private long lastHealTime = 0;
    private long lastMeteorRainTime = 0;
    
    public BossAbilityService(Main plugin, BossEntity boss, int bossAscensionLevel) {
        this.plugin = plugin;
        this.boss = boss;
        this.bossAscensionLevel = bossAscensionLevel;
        this.random = new Random();
    }
    
    /**
     * Start ability scheduler
     * Randomly cast abilities based on phase
     */
    public void startAbilityScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead()) {
                    this.cancel();
                    return;
                }
                
                // Random cast ability based on phase
                int phase = boss.getCurrentPhase();
                tryRandomAbility(phase);
            }
        }.runTaskTimer(plugin, 20L * 5, 20L * 5);  // Check every 5 seconds
    }
    
    /**
     * Try cast random ability
     */
    private void tryRandomAbility(int phase) {
        long currentTime = System.currentTimeMillis() / 1000;
        
        // Phase 1: Chỉ AOE Slam
        // Phase 2: AOE Slam + Summon Minions
        // Phase 3: AOE Slam + Summon Minions + Meteor Rain
        // Phase 4: Tất cả abilities + Heal
        
        // AOE Slam (all phases)
        if (currentTime - lastAoeSlamTime >= AOE_SLAM_COOLDOWN && random.nextDouble() < 0.3) {
            castAoeSlam();
            lastAoeSlamTime = currentTime;
            return;
        }
        
        // Summon Minions (phase 2+)
        if (phase >= 2 && currentTime - lastSummonMinionsTime >= SUMMON_MINIONS_COOLDOWN && random.nextDouble() < 0.2) {
            castSummonMinions();
            lastSummonMinionsTime = currentTime;
            return;
        }
        
        // Meteor Rain (phase 3+)
        if (phase >= 3 && currentTime - lastMeteorRainTime >= METEOR_RAIN_COOLDOWN && random.nextDouble() < 0.25) {
            castMeteorRain();
            lastMeteorRainTime = currentTime;
            return;
        }
        
        // Heal (phase 4 only)
        if (phase >= 4 && currentTime - lastHealTime >= HEAL_COOLDOWN && random.nextDouble() < 0.15) {
            castHeal();
            lastHealTime = currentTime;
        }
    }
    
    /**
     * AOE Slam - knockback players gần boss
     */
    private void castAoeSlam() {
        LivingEntity entity = boss.getEntity();
        Location bossLoc = entity.getLocation();
        
        // Announce
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(bossLoc) < 50) {
                player.sendMessage("§c§l[BOSS] §e" + boss.getBossName() + " §7dùng §cAOE SLAM§7!");
            }
        }
        
        // Effect
        entity.getWorld().playSound(bossLoc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);
        
        // Knockback players trong range 10 blocks
        double range = 10.0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(bossLoc) <= range) {
                // Knockback
                Vector direction = player.getLocation().toVector()
                    .subtract(bossLoc.toVector())
                    .normalize()
                    .multiply(2.0);
                player.setVelocity(direction);
                
                // Damage (dùng modifier từ boss ascension level)
                double damage = 5.0 + (bossAscensionLevel * 2.0);
                player.damage(damage);
            }
        }
    }
    
    /**
     * Summon Minions - spawn thêm mobs
     */
    private void castSummonMinions() {
        LivingEntity entity = boss.getEntity();
        Location bossLoc = entity.getLocation();
        
        // Announce
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(bossLoc) < 50) {
                player.sendMessage("§c§l[BOSS] §e" + boss.getBossName() + " §7triệu hồi minions!");
            }
        }
        
        // Spawn minions (2-4 minions)
        int minionCount = 2 + random.nextInt(3);
        for (int i = 0; i < minionCount; i++) {
            // Random offset
            double offsetX = (random.nextDouble() - 0.5) * 10;
            double offsetZ = (random.nextDouble() - 0.5) * 10;
            Location spawnLoc = bossLoc.clone().add(offsetX, 0, offsetZ);
            
            // Spawn zombie minion
            entity.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ZOMBIE);
        }
    }
    
    /**
     * Meteor Rain - AOE damage tại random locations
     */
    private void castMeteorRain() {
        LivingEntity entity = boss.getEntity();
        Location bossLoc = entity.getLocation();
        
        // Announce
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(bossLoc) < 50) {
                player.sendMessage("§c§l[BOSS] §e" + boss.getBossName() + " §7triệu hồi §4METEOR RAIN§7!");
            }
        }
        
        // Spawn 5 meteors at random locations
        int meteorCount = 5;
        for (int i = 0; i < meteorCount; i++) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Random location around boss
                    double offsetX = (random.nextDouble() - 0.5) * 20;
                    double offsetZ = (random.nextDouble() - 0.5) * 20;
                    Location meteorLoc = bossLoc.clone().add(offsetX, 10, offsetZ);
                    
                    // Spawn fireball
                    entity.getWorld().spawnEntity(meteorLoc, org.bukkit.entity.EntityType.FIREBALL);
                    
                    // Effect
                    entity.getWorld().playSound(meteorLoc, org.bukkit.Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
                }
            }.runTaskLater(plugin, 20L * i);  // Stagger meteors
        }
    }
    
    /**
     * Heal - boss tự hồi máu
     */
    private void castHeal() {
        LivingEntity entity = boss.getEntity();
        
        // Announce
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(entity.getLocation()) < 50) {
                player.sendMessage("§c§l[BOSS] §e" + boss.getBossName() + " §7đang §ahồi máu§7!");
            }
        }
        
        // Heal 10% max HP
        double healAmount = entity.getMaxHealth() * 0.10;
        double newHealth = Math.min(entity.getHealth() + healAmount, entity.getMaxHealth());
        entity.setHealth(newHealth);
        
        // Effect
        entity.getWorld().playSound(entity.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.5f);
    }
}
