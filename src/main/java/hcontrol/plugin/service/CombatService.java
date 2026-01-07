package hcontrol.plugin.service;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.util.DamageFormula;
import hcontrol.plugin.model.CultivatorProfile;
import hcontrol.plugin.model.PlayerStats;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.NameplateService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * PHASE 3 — Combat calculation service
 * Logic tinh damage theo TRIET LY TU TIEN
 */
public class CombatService {
    
    private final Random random = new Random();
    private final PlayerManager playerManager;
    private final Plugin plugin;
    private NameplateService nameplateService;  // inject sau tu CoreContext
    
    public CombatService(PlayerManager playerManager, Plugin plugin) {
        this.playerManager = playerManager;
        this.plugin = plugin;
    }
    
    /**
     * Inject NameplateService (goi tu CoreContext)
     */
    public void setNameplateService(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    /**
     * Xu ly player danh player/mob (TU TIEN DAMAGE ONLY)
     * CHI DUNG DamageFormula - LOAI BO CACH TINH CU
     */
    public void handlePlayerAttack(Player attacker, LivingEntity target, PlayerProfile attackerProfile) {
        // LOAI BO he thong RPG cu
        // Damage tu REALM, khong tu stat
        
        double finalDamage;
        double techniqueModifier = DamageFormula.TECHNIQUE_MORTAL; // mac dinh Pham Phap
        
        // Lay realm damage
        CultivationRealm attackerRealm = attackerProfile.getRealm();
        double baseDamage = attackerRealm.getBaseDamage();
        
        // Neu target la player -> ap dung realm suppression
        if (target instanceof Player targetPlayer) {
            PlayerProfile targetProfile = getPlayerProfile(targetPlayer);
            if (targetProfile != null) {
                // Tinh realm suppression
                CultivationRealm defenderRealm = targetProfile.getRealm();
                int diff = attackerRealm.ordinal() - defenderRealm.ordinal();
                
                double realmSuppression;
                if (diff >= 1) {
                    realmSuppression = 1.0 + (diff * 0.5);  // ap che
                } else if (diff < 0) {
                    realmSuppression = 1.0 + (diff * 0.7);  // phan phe
                    realmSuppression = Math.max(0.1, realmSuppression);
                } else {
                    realmSuppression = 1.0;
                }
                
                // Defense mitigation
                double defense = targetProfile.getStats().getDefense();
                double mitigation = defense / (defense + baseDamage * 3);
                mitigation = Math.min(0.8, mitigation);
                
                // Final damage
                finalDamage = baseDamage * realmSuppression * (1 - mitigation);
                
                // Check noi thuong neu vuot cap danh
                if (diff < -1) {
                    double injuryChance = Math.abs(diff) * 5.0;
                    if (random.nextDouble() * 100 < injuryChance) {
                        double injury = Math.abs(diff) * 2.0;
                        attackerProfile.addInnerInjury(injury);
                        attacker.sendMessage(String.format("§c⚠ Bi noi thuong +%.1f%% (vuot cap!)", injury));
                    }
                }
            } else {
                finalDamage = baseDamage * 0.5;
            }
        } else {
            // Danh mob -> base damage
            finalDamage = baseDamage;
        }
        
        // deal damage
        dealDamage(target, finalDamage);
        
        // hieu ung knockback (bat lui)
        applyKnockback(attacker, target, finalDamage, false);
        
        // hieu ung particle
        spawnHitParticles(target, false);
        
        // sound effect
        playHitSound(attacker, target, false);
        
        // hien thi damage indicator
        showDamageIndicator(attacker, target, finalDamage, false);
    }
    
    // LOAI BO calculateDamage / checkCrit / checkDodge
    // He thong tu tien KHONG CO crit/dodge kieu RPG
    // Chi co Realm Suppression va Dao Factor trong DamageFormula
    
    /**
     * Deal damage to entity
     */
    private void dealDamage(LivingEntity target, double damage) {
        double newHealth = Math.max(0, target.getHealth() - damage);
        target.setHealth(newHealth);
    }
    
    /**
     * Apply knockback effect (bat lui)
     */
    private void applyKnockback(Player attacker, LivingEntity target, double damage, boolean isCrit) {
        // tinh knockback strength (crit manh hon)
        double knockbackStrength = isCrit ? 0.5 : 0.3;
        
        // tinh huong bat lui
        Vector direction = target.getLocation().toVector()
            .subtract(attacker.getLocation().toVector());
        
        // check neu direction = 0 (cung vi tri) thi dung default direction
        if (direction.lengthSquared() < 0.0001) {
            // dung huong attacker dang nhin
            direction = attacker.getLocation().getDirection();
        } else {
            direction.normalize();
        }
        
        // apply knockback (ngang + len cao)
        direction.setY(0.2); // bat len cao mot chut
        direction.multiply(knockbackStrength);
        
        target.setVelocity(direction);
    }
    
    /**
     * Spawn hit particles
     */
    private void spawnHitParticles(LivingEntity target, boolean isCrit) {
        Location loc = target.getLocation().add(0, 1, 0); // giua nguoi
        
        if (isCrit) {
            // crit: particle vang nhieu hon
            target.getWorld().spawnParticle(
                Particle.CRIT, 
                loc, 
                15,           // so luong
                0.3, 0.5, 0.3, // spread (x, y, z)
                0.1           // speed
            );
            target.getWorld().spawnParticle(
                Particle.CRIT_MAGIC, 
                loc, 
                10, 
                0.3, 0.5, 0.3,
                0.1
            );
        } else {
            // hit binh thuong: particle trang
            target.getWorld().spawnParticle(
                Particle.CRIT, 
                loc, 
                5, 
                0.2, 0.3, 0.2,
                0.05
            );
        }
    }
    
    /**
     * Play hit sound
     */
    private void playHitSound(Player attacker, LivingEntity target, boolean isCrit) {
        if (isCrit) {
            // crit sound
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5f, 1.0f);
        } else {
            // normal hit sound
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
        }
        
        // hurt sound (vanilla)
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.0f);
    }
    
    // ========== TU TIEN COMBAT (NEW) ==========
    
    /**
     * Xu ly combat TU TIEN giua 2 cultivator
     * Dung DamageFormula 4 tang nhan
     */
    public void handleCultivatorCombat(Player attackerPlayer, Player defenderPlayer, 
                                      CultivatorProfile attacker, CultivatorProfile defender,
                                      double techniqueModifier) {
        
        // Tinh damage theo cong thuc tu tien
        double damage = DamageFormula.calculateCultivationDamage(attacker, defender, techniqueModifier);
        
        // Kiem tra noi thuong (neu vuot cap danh)
        double innerInjury = DamageFormula.calculateInnerInjuryFromAttack(attacker, defender);
        if (innerInjury > 0) {
            attacker.addInnerInjury(innerInjury);
            attackerPlayer.sendMessage(String.format("§c⚠ Bi noi thuong +%.1f%% (danh vuot cap!)", innerInjury));
        }
        
        // Apply damage
        double newHP = defender.getCurrentHP() - damage;
        defender.setCurrentHP(newHP);
        
        // Update nameplate de hien % HP moi
        if (nameplateService != null) {
            // Convert CultivatorProfile -> Player
            Player defenderPlayerForUpdate = defender.getPlayer();
            if (defenderPlayerForUpdate != null) {
                nameplateService.updateNameplate(defenderPlayerForUpdate);
            }
        }
        
        // Hieu ung combat
        String damageColor = getDamageColor(attacker.getRealm(), defender.getRealm());
        spawnFloatingText(defenderPlayer, damageColor + String.format("%.0f", damage));
        playTuTienHitSound(attackerPlayer, defenderPlayer, techniqueModifier);
        spawnTuTienHitParticles(defenderPlayer.getLocation(), techniqueModifier);
        
        // ActionBar feedback
        attackerPlayer.sendActionBar(String.format("§e⚡ %.1f §7(%s)", damage, getTechniqueRank(techniqueModifier)));
        defenderPlayer.sendActionBar(String.format("§c-%.1f HP §7| §4%s", damage, attacker.getRealm().getDisplayName()));
        
        // Check chet
        if (newHP <= 0) {
            defenderPlayer.setHealth(0);
        }
    }
    
    /**
     * Mau sac damage theo realm suppression
     */
    private String getDamageColor(CultivationRealm attackerRealm, 
                                  CultivationRealm defenderRealm) {
        int diff = attackerRealm.ordinal() - defenderRealm.ordinal();
        
        if (diff >= 2) return "§4";      // do dam (ap che manh)
        if (diff == 1) return "§c";      // do (ap che)
        if (diff == 0) return "§e";      // vang (bang nhau)
        if (diff == -1) return "§7";     // xam (bi phan phe)
        return "§8";                     // xam dam (bi phan phe nang)
    }
    
    /**
     * Ten cong phap
     */
    private String getTechniqueRank(double modifier) {
        if (modifier >= 4.0) return "Cam Thuat";
        if (modifier >= 2.5) return "Thien Cap";
        if (modifier >= 1.7) return "Dia Cap";
        if (modifier >= 1.3) return "Linh Cap";
        return "Pham Phap";
    }
    
    /**
     * Sound hieu ung tu tien
     */
    private void playTuTienHitSound(Player attacker, Player defender, double techniqueModifier) {
        if (techniqueModifier >= 2.5) {
            // Thien cap / Cam thuat
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
            defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.2f);
        } else if (techniqueModifier >= 1.3) {
            // Linh cap / Dia cap
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
            defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 0.9f);
        } else {
            // Pham phap
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
        }
    }
    
    /**
     * Particle hieu ung tu tien
     */
    private void spawnTuTienHitParticles(Location loc, double techniqueModifier) {
        loc = loc.clone().add(0, 1, 0);
        
        if (techniqueModifier >= 4.0) {
            // Cam thuat - explosion + lightning
            loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 20, 0.5, 0.5, 0.5, 0.1);
        } else if (techniqueModifier >= 2.5) {
            // Thien cap - flash + flame
            loc.getWorld().spawnParticle(Particle.FLASH, loc, 3, 0.3, 0.3, 0.3, 0);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 15, 0.4, 0.4, 0.4, 0.05);
        } else if (techniqueModifier >= 1.7) {
            // Dia cap - soul fire flame
            loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 20, 0.5, 0.5, 0.5, 0.01);
        } else if (techniqueModifier >= 1.3) {
            // Linh cap - cloud
            loc.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.3, 0.3, 0.3, 0.05);
        } else {
            // Pham phap - crit
            loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0.2, 0.3, 0.2, 0.05);
        }
    }
    
    /**
     * Hien thi floating damage text bay len tu dau target
     */
    private void showDamageIndicator(Player attacker, LivingEntity target, double damage, boolean isCrit) {
        String color = isCrit ? "§6§l" : "§c";
        String critText = isCrit ? " ✦" : "";
        String damageText = color + "⚔ " + String.format("%.1f", damage) + critText;
        
        spawnFloatingText(target, damageText);
        
        // van giu ActionBar cho attacker biet damage
        attacker.sendActionBar(damageText);
    }
    
    /**
     * Spawn floating text bay len tu entity
     */
    private void spawnFloatingText(LivingEntity entity, String text) {
        // vi tri spawn: tren dau entity + random offset
        Location loc = entity.getLocation().add(
            (random.nextDouble() - 0.5) * 0.5,  // random x (-0.25 to 0.25)
            entity.getHeight() + 0.5,            // tren dau
            (random.nextDouble() - 0.5) * 0.5   // random z
        );
        
        // spawn invisible armor stand (CHI hien custom name)
        ArmorStand armorStand = (ArmorStand) entity.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);           // an body
        armorStand.setGravity(false);           // khong roi
        armorStand.setCustomName(text);         // damage text
        armorStand.setCustomNameVisible(true);  // hien name
        armorStand.setMarker(true);             // khong collision, an base plate
        armorStand.setInvulnerable(true);       // khong bi danh
        armorStand.setSmall(true);              // nho lai (optional)
        armorStand.setBasePlate(false);         // an base plate (gia do)
        armorStand.setArms(false);              // an tay
        
        // animate bay len (0.1 block moi tick, 20 tick = 1 giay)
        final int[] tickCount = {0};
        final int duration = 20; // 1 second
        
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            tickCount[0]++;
            
            // bay len
            Location currentLoc = armorStand.getLocation();
            currentLoc.add(0, 0.05, 0); // bay len 0.05 block/tick
            armorStand.teleport(currentLoc);
            
            // fade out (giam alpha - lam mo dan)
            if (tickCount[0] > duration * 0.7) {
                // sau 70% thoi gian, bat dau fade
                armorStand.setCustomNameVisible(tickCount[0] % 2 == 0); // nhap nhay
            }
            
            // remove sau duration
            if (tickCount[0] >= duration) {
                armorStand.remove();
                Bukkit.getScheduler().cancelTask(tickCount[0]);
            }
        }, 0L, 1L); // chay moi tick
        
        // fallback: remove sau 2 giay (phong truong hop task loi)
        Bukkit.getScheduler().runTaskLater(plugin, armorStand::remove, duration + 20L);
    }
    
    /**
     * Xu ly mob danh player (MOB DAMAGE THEO REALM NEU CO)
     */
    public void handleMobAttackPlayer(LivingEntity mob, Player player, PlayerProfile playerProfile) {
        // tinh base damage (mob damage)
        double baseDamage = mob.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE).getValue();
        
        // LOAI BO dodge - tu tien khong co dodge
        // Ap dung defense formula tu tien
        double defense = playerProfile.getStats().getDefense();
        double mitigation = defense / (defense + baseDamage * 3);
        mitigation = Math.min(0.8, mitigation);
        double finalDamage = baseDamage * (1 - mitigation);
        
        // deal damage
        dealDamage(player, finalDamage);
        
        // hieu ung nhe hon (khong bat lui player qua manh)
        spawnHitParticles(player, false);
        
        // floating damage text
        spawnFloatingText(player, "§c♥ -" + String.format("%.1f", finalDamage));
    }
    
    /**
     * Lay player profile
     */
    private PlayerProfile getPlayerProfile(Player player) {
        return playerManager.get(player.getUniqueId());
    }
}
