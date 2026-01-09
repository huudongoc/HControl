package hcontrol.plugin.service;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.LivingActor;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.player.NameplateService;

/**
 * PHASE 3 — Combat calculation service
 * Logic tinh damage theo TRIET LY TU TIEN
 * REFACTORED: Unified combat using LivingActor (Milestone 4)
 */
public class CombatService {
    
    private final Random random = new Random();
    private final PlayerManager playerManager;
    private final Plugin plugin;
    private final DamageEffectService effectService;
    private final EntityManager entityManager;
    private NameplateService nameplateService;  // inject sau tu CoreContext
    
    
    public CombatService(PlayerManager playerManager, Plugin plugin, 
                        DamageEffectService effectService, EntityManager entityManager) {
        this.playerManager = playerManager;
        this.plugin = plugin;
        this.effectService = effectService;
        this.entityManager = entityManager;
    }
    
    /**
     * Inject NameplateService (goi tu CoreContext)
     */
    public void setNameplateService(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    // ===== MILESTONE 4: UNIFIED COMBAT (LIVING ACTOR) =====
    
    /**
     * UNIFIED COMBAT - thong nhat PvP + PvE + Mob vs Player
     * Dung LivingActor interface
     * @param attacker ke tan cong (Player hoac Entity)
     * @param defender nguoi bi danh (Player hoac Entity)
     * @param techniqueModifier modifier cong phap (1.0 = pham phap)
     */
    public void handleCombat(LivingActor attacker, LivingActor defender, double techniqueModifier) {
        // ===== TINH DAMAGE =====
        
        // Base damage theo realm
        double baseDamage = attacker.getRealm().getBaseDamage();
        
        // Realm suppression (CỐT LÕI TU TIÊN)
        int realmDiff = attacker.getRealm().ordinal() - defender.getRealm().ordinal();
        double realmSuppression;
        if (realmDiff >= 1) {
            realmSuppression = 1.0 + (realmDiff * 0.5);  // ap che (cao danh thap)
        } else if (realmDiff < 0) {
            realmSuppression = 1.0 + (realmDiff * 0.7);  // phan phe (thap danh cao)
            realmSuppression = Math.max(0.1, realmSuppression);  // min 10%
        } else {
            realmSuppression = 1.0;  // bang nhau
        }
        
        // Defense mitigation
        double defense = defender.getDefense();
        double mitigation = defense / (defense + baseDamage * 3);
        mitigation = Math.min(0.8, mitigation);  // max giam 80%
        
        // Random dao factor (nho - khong phai crit)
        double daoFactor = 0.9 + (random.nextDouble() * 0.2);  // 0.9 - 1.1
        
        // FINAL DAMAGE
        double damage = baseDamage * realmSuppression * techniqueModifier * (1 - mitigation) * daoFactor;
        
        // ===== APPLY DAMAGE =====
        
        double newHP = Math.max(0, defender.getCurrentHP() - damage);
        defender.setCurrentHP(newHP);
        
        // ===== SYNC VANILLA HEALTH (NEU CO BUKKIT ENTITY) =====
        
        LivingEntity defenderEntity = defender.getEntity();
        if (defenderEntity != null) {
            // Neu la Player - dung PlayerHealthService de sync tablist
            if (defenderEntity instanceof Player defenderPlayer && defender instanceof PlayerProfile playerProfile) {
                var healthService = CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
                healthService.updateCurrentHealth(defenderPlayer, playerProfile);
            } else {
                // Neu la Entity - sync vanilla health percent
                double healthPercent = newHP / defender.getMaxHP();
                double vanillaHealth = defenderEntity.getMaxHealth() * healthPercent;
                defenderEntity.setHealth(Math.max(0.1, vanillaHealth));
            }
            
            // Check chet
            if (newHP <= 0) {
                defenderEntity.setHealth(0);
            }
        }
        
        // ===== HIEU UNG =====
        
        LivingEntity attackerEntity = attacker.getEntity();
        
        // Hieu ung bi danh (defender)
        if (defenderEntity != null) {
            boolean defenderVIP = (defenderEntity instanceof Player) && effectService.isVIP((Player) defenderEntity);
            effectService.playHitEffect(defenderEntity instanceof Player ? (Player) defenderEntity : null, 
                                       defender.getRealm(), damage, defenderVIP, defenderEntity.getLocation());
            
            // Floating damage
            String damageColor = getDamageColor(attacker.getRealm(), defender.getRealm());
            effectService.spawnFloatingDamage(defenderEntity.getLocation(), damage, damageColor, false);
        }
        
        // // ActionBar feedback cho attacker (neu la player)
        // if (attackerEntity instanceof Player attackerPlayer) {
        //     // CHI HIEN THI DAMAGE, KHONG HIEN THI HP HOAC TEN
        //     attackerPlayer.sendActionBar(String.format("§e⚔ %.1f", damage));
        // }
        
        // // ActionBar feedback cho defender (neu la player)
        // if (defenderEntity instanceof Player defenderPlayer) {
        //     // NOTE: CHI HIEN THI DAMAGE, KHONG HIEN THI REALM/LEVEL/HP
        //     defenderPlayer.sendActionBar(String.format("§c-%.1f", damage));
        // }
        
        // Update nameplate cho Entity (mob/boss) - KHONG update cho Player de tranh flash
        // NOTE: Entity nameplate update MỖI HIT de hien thi HP realtime
        if (defenderEntity != null && !(defenderEntity instanceof Player)) {
            // Entity nameplate - update voi force để hiển thị HP realtime
            var entityNameplateService = CoreContext.getInstance().getUIContext().getEntityNameplateService();
            if (entityNameplateService != null && defender instanceof EntityProfile entityProfile) {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    if (defenderEntity.isValid() && !defenderEntity.isDead()) {
                        entityNameplateService.updateNameplate(defenderEntity, entityProfile, true);
                    }
                });
            }
        }
        
        // Knockback (neu co attacker entity)
        if (attackerEntity != null && defenderEntity != null) {
            applyKnockback(attackerEntity.getLocation(), defenderEntity, damage);
        }
    }
    

    /**
     * Xu ly player danh player/mob (TU TIEN DAMAGE ONLY)
     * REFACTORED: Wrapper around handleCombat()
     */
    public void handlePlayerAttack(Player attacker, LivingEntity target, PlayerProfile attackerProfile) {
        // TODO: Technique modifier (kiem, phap bao...)
        double techniqueModifier = 1.0;
        
        // CASE 1: Player danh Player (PvP)
        if (target instanceof Player targetPlayer) {
            PlayerProfile targetProfile = playerManager.get(targetPlayer.getUniqueId());
            if (targetProfile == null) return;
            
            // Dung unified combat
            handleCombat(attackerProfile, targetProfile, techniqueModifier);
            return;
        }
        
        // CASE 2: Player danh Mob (PvE - DUNG ENTITY PROFILE)
        EntityProfile mobProfile = entityManager.getOrCreate(target);
        
        // Dung unified combat
        handleCombat(attackerProfile, mobProfile, techniqueModifier);
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
    private void applyKnockback(Location attackerLoc, LivingEntity target, double damage) {
        // tinh knockback strength
        double knockbackStrength = 0.3;
        
        // tinh huong bat lui
        Vector direction = target.getLocation().toVector()
            .subtract(attackerLoc.toVector());
        
        // check neu direction = 0 (cung vi tri) thi dung default direction
        if (direction.lengthSquared() < 0.0001) {
            // dung huong tu attackerLoc
            direction = attackerLoc.getDirection();
        } else {
            direction.normalize();
        }
        
        // apply knockback (ngang + len cao)
        direction.setY(0.2); // bat len cao mot chut
        direction.multiply(knockbackStrength);
        
        target.setVelocity(direction);
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
     * Hien thi floating damage text bay len tu dau target
     */
    private void showDamageIndicator(Player attacker, LivingEntity target, double damage, boolean isCrit) {
        String color = isCrit ? "§6§l" : "§c";
        String critText = isCrit ? " ✦" : "";
        String damageText = color + "⚔ " + String.format("%.1f", damage) + critText;
        
        // dung service de spawn floating text
        effectService.spawnFloatingDamage(target.getLocation(), damage, color, isCrit);
        
        // van giu ActionBar cho attacker biet damage
        attacker.sendActionBar(damageText);
    }
    
    /**
     * Xu ly mob danh player (DUNG ENTITY PROFILE)
     * REFACTORED: Wrapper around handleCombat()
     */
    public void handleMobAttackPlayer(LivingEntity mob, Player player, PlayerProfile playerProfile) {
        // Lay hoac tao mob profile
        EntityProfile mobProfile = entityManager.getOrCreate(mob);
        
        // TODO: Technique modifier
        double techniqueModifier = 1.0;
        
        // Dung unified combat
        handleCombat(mobProfile, playerProfile, techniqueModifier);
    }
    
    /**
     * Lay player profile
     */
    private PlayerProfile getPlayerProfile(Player player) {
        return playerManager.get(player.getUniqueId());
    }
}
