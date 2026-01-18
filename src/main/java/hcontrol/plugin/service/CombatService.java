package hcontrol.plugin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import hcontrol.plugin.core.CoreContext;
import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.LivingActor;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
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
    private hcontrol.plugin.item.ItemService itemService;  // PHASE 8A: inject sau tu ItemContext
    private hcontrol.plugin.classsystem.ClassService classService;  // PHASE 5: inject sau tu ClassContext
    
    // Track last attacker cho moi player (de hien thi trong death message)
    // Key: victim UUID, Value: attacker info (UUID + timestamp + weapon)
    private final Map<UUID, AttackerInfo> lastAttackers = new HashMap<>();
    
    /**
     * Thong tin ve attacker
     */
    public static class AttackerInfo {
        private final UUID attackerUUID;
        private final boolean isPlayer;
        private final long timestamp;
        private final ItemStack weapon; // null neu khong co weapon dac biet
        
        public AttackerInfo(UUID attackerUUID, boolean isPlayer, ItemStack weapon) {
            this.attackerUUID = attackerUUID;
            this.isPlayer = isPlayer;
            this.timestamp = System.currentTimeMillis();
            this.weapon = weapon;
        }
        
        public UUID getAttackerUUID() { return attackerUUID; }
        public boolean isPlayer() { return isPlayer; }
        public long getTimestamp() { return timestamp; }
        public ItemStack getWeapon() { return weapon; }
        
        public boolean isExpired(long timeoutMs) {
            return System.currentTimeMillis() - timestamp > timeoutMs;
        }
    }
    
    public CombatService(PlayerManager playerManager, Plugin plugin, 
                        DamageEffectService effectService, EntityManager entityManager) {
        this.playerManager = playerManager;
        this.plugin = plugin;
        this.effectService = effectService;
        this.entityManager = entityManager;
        
        // Cleanup expired attackers moi 30 giay
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredAttackers();
            }
        }.runTaskTimer(plugin, 600L, 600L); // moi 30 giay
    }
    
    /**
     * Cleanup expired attackers (qua 5 phut)
     */
    private void cleanupExpiredAttackers() {
        long timeout = 5 * 60 * 1000; // 5 phut
        lastAttackers.entrySet().removeIf(entry -> entry.getValue().isExpired(timeout));
    }
    
    /**
     * Lay last attacker info cho player
     */
    public AttackerInfo getLastAttacker(UUID victimUUID) {
        AttackerInfo info = lastAttackers.get(victimUUID);
        if (info != null && info.isExpired(5 * 60 * 1000)) {
            lastAttackers.remove(victimUUID);
            return null;
        }
        return info;
    }
    
    /**
     * Xoa last attacker info (sau khi chet)
     */
    public void clearLastAttacker(UUID victimUUID) {
        lastAttackers.remove(victimUUID);
    }
    
    /**
     * PHASE 8A: Inject ItemService (goi tu ItemContext)
     */
    public void setItemService(hcontrol.plugin.item.ItemService itemService) {
        this.itemService = itemService;
    }
    
    /**
     * PHASE 5: Inject ClassService (goi tu ClassContext)
     */
    public void setClassService(hcontrol.plugin.classsystem.ClassService classService) {
        this.classService = classService;
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
        handleCombat(attacker, defender, techniqueModifier, null);
    }
    
    /**
     * PHASE 5A — Handle combat với skill support
     * @param skillId Skill ID đang được dùng (null nếu đánh thường)
     */
    public void handleCombat(LivingActor attacker, LivingActor defender, double techniqueModifier, String skillId) {
        // CHECK: Defender da chet - khong cho tan cong
        if (defender.getCurrentHP() <= 0) {
            return; // Defender da chet, khong xu ly damage
        }
        
        // CHECK: Defender entity da chet (double check)
        LivingEntity defenderEntity = defender.getEntity();
        if (defenderEntity != null && defenderEntity.isDead()) {
            return; // Entity da chet trong game, khong xu ly damage
        }
        
        // ===== TRACK LAST ATTACKER (neu defender la Player) =====
        if (defenderEntity instanceof Player defenderPlayer) {
            LivingEntity attackerEntity = attacker.getEntity();
            if (attackerEntity != null) {
                UUID attackerUUID = attackerEntity.getUniqueId();
                boolean isPlayer = attackerEntity instanceof Player;
                
                // Lay weapon neu attacker la Player
                ItemStack weapon = null;
                if (isPlayer) {
                    Player attackerPlayer = (Player) attackerEntity;
                    ItemStack item = attackerPlayer.getInventory().getItemInMainHand();
                    if (item != null && item.getType() != Material.AIR) {
                        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            boolean hasLore = meta.hasLore();
                            java.util.List<String> lore = hasLore ? meta.getLore() : null;
                            if (meta.hasDisplayName() || (hasLore && lore != null && !lore.isEmpty())) {
                                weapon = item.clone(); // Clone de tranh thay doi
                            }
                        }
                    }
                }
                
                // Luu last attacker
                lastAttackers.put(defenderPlayer.getUniqueId(), new AttackerInfo(attackerUUID, isPlayer, weapon));
                
                // DEBUG LOG
                String attackerName = isPlayer ? ((Player) attackerEntity).getName() : attackerEntity.getType().name();
                String weaponInfo = weapon != null ? " với vũ khí đặc biệt" : "";
                //System.out.println("[DEBUG] Lưu attacker: " + attackerName + " -> " + defenderPlayer.getName() + weaponInfo);
            }
        }
        
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
        
        // 🔥 Spiritual Root damage bonus (nếu attacker là player)
        double rootDamageBonus = 1.0;
        if (attacker instanceof PlayerProfile attackerProfile) {
            hcontrol.plugin.core.CoreContext ctx = hcontrol.plugin.core.CoreContext.getInstance();
            if (ctx != null && ctx.getSpiritualRootService() != null) {
                hcontrol.plugin.service.SpiritualRootService rootService = ctx.getSpiritualRootService();
                double bonus = rootService.getDamageBonus(attackerProfile.getSpiritualRoot());
                rootDamageBonus = 1.0 + (bonus / 100.0);  // Convert +50% -> 1.5x
            }
        }
        
        // FINAL DAMAGE (tính base damage trước)
        double damage = baseDamage * realmSuppression * techniqueModifier * (1 - mitigation) * daoFactor * rootDamageBonus;
        
        // Đảm bảo damage tối thiểu > 0 (ít nhất 1% baseDamage để tránh damage = 0)
        double minDamage = Math.max(0.1, baseDamage * 0.01);
        damage = Math.max(minDamage, damage);
        
        // PHASE 8A: Apply item stat bonuses vào final damage (sau tất cả modifiers)
        // Item bonus được scale theo các modifiers để giữ balance
        if (attacker instanceof PlayerProfile attackerProfile && itemService != null) {
            double itemAttackBonus = itemService.getStat(attackerProfile, hcontrol.plugin.item.StatType.ATTACK);
            if (itemAttackBonus > 0) {
                // Apply item bonus với cùng modifiers như base damage
                double itemDamageBonus = itemAttackBonus * realmSuppression * techniqueModifier * (1 - mitigation) * daoFactor;
                damage += itemDamageBonus;
                
                // Debug log đã tắt
                // plugin.getLogger().info("[DEBUG] Player " + attackerProfile.getName() + 
                //     " có item ATTACK bonus: " + itemAttackBonus + 
                //     " → thêm damage: " + String.format("%.2f", itemDamageBonus) + 
                //     " (total: " + String.format("%.2f", damage) + ")");
            }
        }
        
        // PHASE 5: Apply class modifiers (sau item bonus)
        if (attacker instanceof PlayerProfile attackerProfile && classService != null) {
            hcontrol.plugin.classsystem.ClassProfile classProfile = attackerProfile.getClassProfile();
            
            // Debug: Check class profile
            if (classProfile == null) {
                // Player chưa chọn class - không có modifiers
                // plugin.getLogger().info("[DEBUG] Player " + attackerProfile.getName() + " chưa có class profile");
            } else {
                hcontrol.plugin.classsystem.modifier.ModifierContext modifierCtx = 
                    new hcontrol.plugin.classsystem.modifier.ModifierContext(attacker, defender, skillId);
                
                var modifiers = classService.getModifiers(attackerProfile);
                // Debug: Check modifiers
                // plugin.getLogger().info("[DEBUG] Player " + attackerProfile.getName() + 
                //     " class=" + classProfile.getType() + " có " + modifiers.size() + " modifiers");
                
                for (hcontrol.plugin.classsystem.modifier.ClassModifier modifier : modifiers) {
                    if (modifier.getType() == hcontrol.plugin.classsystem.modifier.ModifierType.DAMAGE) {
                        double oldDamage = damage;
                        damage = modifier.modify(attacker, modifierCtx, damage);
                        if (damage != oldDamage) {
                            // Debug log đã tắt
                            // plugin.getLogger().info("[DEBUG] Class modifier applied: " + 
                            //     classProfile.getType() + 
                            //     " (skill=" + (skillId != null ? skillId : "none") + ")" +
                            //     " → damage: " + String.format("%.2f", oldDamage) + 
                            //     " → " + String.format("%.2f", damage));
                        }
                    }
                }
            }
        }
        
        // ASCENSION SYSTEM - ENDGAME: Apply ascension power multiplier (sau class modifiers, trước apply damage)
        if (attacker instanceof PlayerProfile attackerProfile) {
            hcontrol.plugin.model.AscensionProfile ascension = attackerProfile.getAscensionProfile();
            if (ascension != null && ascension.isAscended()) {
                double ascensionPower = ascension.getAscensionPower();
                damage *= ascensionPower;
                
                // Debug log đã tắt
                // plugin.getLogger().info("[DEBUG] Ascension power applied: " + 
                //     attackerProfile.getName() + 
                //     " (level " + ascension.getAscensionLevel() + 
                //     ", power " + String.format("%.2f", ascensionPower) + "x)" +
                //     " → damage: " + String.format("%.2f", damage));
            }
        }
        
        // ===== APPLY DAMAGE =====
        
        // Debug log đã tắt
        // if (attacker instanceof PlayerProfile attackerProfile) {
        //     plugin.getLogger().info("[COMBAT] " + attackerProfile.getName() + 
        //         " đánh " + (defender instanceof PlayerProfile ? ((PlayerProfile)defender).getName() : defender.getEntity().getType().name()) +
        //         " | baseDamage=" + String.format("%.1f", baseDamage) +
        //         " | realmSuppression=" + String.format("%.2f", realmSuppression) +
        //         " | mitigation=" + String.format("%.2f", mitigation) +
        //         " | damage=" + String.format("%.1f", damage) +
        //         " | defenderHP=" + String.format("%.1f", defender.getCurrentHP()) + "/" + String.format("%.1f", defender.getMaxHP()));
        // }
        
        double newHP = Math.max(0, defender.getCurrentHP() - damage);
        defender.setCurrentHP(newHP);
        
        // Debug log đã tắt
        // if (attacker instanceof PlayerProfile attackerProfile) {
        //     plugin.getLogger().info("[COMBAT] Sau khi apply: defenderHP=" + String.format("%.1f", newHP) + "/" + String.format("%.1f", defender.getMaxHP()));
        // }
        
        // WORLD BOSS PARTICIPATION TRACKING - Track damage khi player đánh world boss
        if (attacker instanceof PlayerProfile attackerProfile && 
            defender instanceof EntityProfile defenderProfile) {
            // Check xem defender có phải world boss không
            hcontrol.plugin.core.CoreContext ctx = hcontrol.plugin.core.CoreContext.getInstance();
            if (ctx != null && ctx.getWorldBossManager() != null) {
                // Check xem entity có phải world boss không
                UUID defenderUUID = defenderProfile.getUUID();
                hcontrol.plugin.module.boss.BossEntity boss = 
                    ctx.getBossManager().getBoss(defenderUUID);
                if (boss != null && boss.getType() == hcontrol.plugin.module.boss.BossType.WORLD_BOSS) {
                    // Track participation
                    ctx.getWorldBossManager().trackDamage(
                        attackerProfile.getUuid(),
                        defenderUUID,
                        damage
                    );
                }
            }
        }
        
        // ===== SYNC VANILLA HEALTH (NEU CO BUKKIT ENTITY) =====
        
        // defenderEntity da duoc khai bao o tren
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
            showDamageIndicator(attackerEntity instanceof Player ? (Player) attackerEntity : null, defenderEntity, damage, false);
            //effectService.spawnFloatingDamage(defenderEntity.getLocation(), damage, damageColor, false);
        }
        
        // Update nameplate cho Entity (mob/boss) - KHONG update cho Player de tranh flash
        // NOTE: Entity nameplate update MỖI HIT de hien thi HP realtime
        // SAFETY: Double check defender KHÔNG phải Player trước khi update entity nameplate

        
        // if (defenderEntity != null && !(defenderEntity instanceof Player)) {
        //     // CHỈ update entity nameplate nếu defender là EntityProfile (mob/boss)
        //     if (defender instanceof EntityProfile entityProfile) {
        //         var entityNameplateService = CoreContext.getInstance().getUIContext().getEntityNameplateService();
        //         if (entityNameplateService != null) {
        //             org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
        //                 // Safety check lại: KHÔNG update nameplate cho Player
        //                 if (defenderEntity.isValid() && !defenderEntity.isDead() && !(defenderEntity instanceof Player)) {
        //                     entityNameplateService.updateNameplate(defenderEntity, entityProfile, true);
        //                 }
        //             });
        //         }
        //     }
        // }
        
        // // Update nameplate cho Player (hien thi HP sau combat)
        // // NOTE: Player nameplate update de hien thi HP realtime (giong Entity)
        // if (defenderEntity instanceof Player playerDefender && defender instanceof PlayerProfile) {
        //     org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
        //         // Safety check: player van con song va online
        //         if (playerDefender.isValid() && !playerDefender.isDead() && playerDefender.isOnline()) {
        //             var nameplateService = CoreContext.getInstance().getUIContext().getNameplateService();
        //             if (nameplateService != null) {
        //                 // Update nameplate de hien thi HP moi (force = false de tranh spam, co cooldown 1s)
        //                 nameplateService.updateNameplate(playerDefender);
        //             }
        //         }
                
        //         // Reset custom name nếu có (có thể do plugin khác hoặc lỗi)
        //         if (playerDefender.getCustomName() != null && !playerDefender.getCustomName().equals(playerDefender.getName())) {
        //             playerDefender.setCustomName(null);
        //             playerDefender.setCustomNameVisible(false);
        //         }
        //     });
        // }
        
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
        handlePlayerAttack(attacker, target, attackerProfile, null);
    }
    
    /**
     * PHASE 5A — Handle player attack với skill support
     * @param skillId Skill ID đang được dùng (null nếu đánh thường)
     */
    public void handlePlayerAttack(Player attacker, LivingEntity target, PlayerProfile attackerProfile, String skillId) {
        // CHECK: Target da chet - khong xu ly damage
        if (target.isDead()) {
            return; // Target da chet, khong xu ly damage
        }
        
        // TODO: Technique modifier (kiem, phap bao...)
        double techniqueModifier = 1.0;
        
        // CASE 1: Player danh Player (PvP)
        if (target instanceof Player targetPlayer) {
            PlayerProfile targetProfile = playerManager.get(targetPlayer.getUniqueId());
            if (targetProfile == null) return;
            
            // CHECK: Target player HP = 0 - da chet
            if (targetProfile.getCurrentHP() <= 0) {
                return; // Target player da chet, khong xu ly damage
            }
            
            // Dung unified combat
            handleCombat(attackerProfile, targetProfile, techniqueModifier, skillId);
            return;
        }
        
        // CASE 2: Player danh Mob (PvE - DUNG ENTITY PROFILE)
        EntityProfile mobProfile = entityManager.getOrCreate(target);
        
        // CHECK: Mob HP = 0 - da chet
        if (mobProfile.getCurrentHP() <= 0) {
            return; // Mob da chet, khong xu ly damage
        }
        
        // Dung unified combat
        handleCombat(attackerProfile, mobProfile, techniqueModifier, skillId);
    }
    
    // LOAI BO calculateDamage / checkCrit / checkDodge
    // He thong tu tien KHONG CO crit/dodge kieu RPG
    // Chi co Realm Suppression va Dao Factor trong DamageFormula
    
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
        
        // ko giu ActionBar cho attacker biet damage
        //attacker.sendActionBar(damageText);
    }
    
    /**
     * Xu ly mob danh player (DUNG ENTITY PROFILE)
     * REFACTORED: Wrapper around handleCombat()
     */
    public void handleMobAttackPlayer(LivingEntity mob, Player player, PlayerProfile playerProfile) {
        // CHECK: Player da chet - khong cho mob tan cong
        if (player.isDead() || playerProfile.getCurrentHP() <= 0) {
            return; // Player da chet, khong xu ly damage
        }
        
        // CHECK: Mob da chet - khong cho tan cong
        if (mob.isDead()) {
            return; // Mob da chet, khong xu ly damage
        }
        
        // Lay hoac tao mob profile
        EntityProfile mobProfile = entityManager.getOrCreate(mob);
        
        // CHECK: Mob profile HP = 0 - da chet
        if (mobProfile.getCurrentHP() <= 0) {
            return; // Mob da chet trong profile, khong xu ly damage
        }
        
        // TODO: Technique modifier
        double techniqueModifier = 1.0;
        
        // Dung unified combat
        handleCombat(mobProfile, playerProfile, techniqueModifier);
    }
    
    /**
     * Handle environmental damage (fall, fire, drown, void, etc.)
     * Damage được tính từ vanilla damage và apply vào tu tiên HP
     * 
     * @param player Player bị damage
     * @param profile PlayerProfile của player
     * @param vanillaDamage Damage từ vanilla event (fall, fire, etc.)
     * @param cause Nguyên nhân damage (để hiển thị message)
     * @return Damage message để hiển thị action bar
     */
    public String handleEnvironmentalDamage(Player player, PlayerProfile profile, double vanillaDamage, org.bukkit.event.entity.EntityDamageEvent.DamageCause cause) {
        // Apply damage vao tu tien HP
        // -50% damage của vanilla (tu tien HP) -> 100% damage của tu tien HP
        double damage = vanillaDamage * 0.5;
        double currentHP = profile.getCurrentHP();
        double newHP = Math.max(0, currentHP - damage);
        profile.setCurrentHP(newHP);
        
        // Sync vanilla health
        var healthService = CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
        healthService.updateCurrentHealth(player, profile);
        
        // Check chet
        if (newHP <= 0) {
            player.setHealth(0);
        }
        
        // Format message (dung profile.getMaxHP() de apply realm multiplier)
        String causeMsg = getDamageCauseMessage(cause);
        return String.format("§c-%.1f HP §7| %s §7| §c%.0f§7/§e%.0f", 
            vanillaDamage, causeMsg, newHP, profile.getMaxHP());
    }
    
    /**
     * Get message theo damage cause
     */
    private String getDamageCauseMessage(org.bukkit.event.entity.EntityDamageEvent.DamageCause cause) {
        return switch(cause) {
            case FALL -> "§cRơi từ cao";
            case FIRE, FIRE_TICK, LAVA -> "§6Lửa";
            case DROWNING -> "§9Đuối nước";
            case SUFFOCATION -> "§7Ngạt";
            case VOID -> "§5Hư không";
            default -> "§cSát thương";
        };
    }
}
