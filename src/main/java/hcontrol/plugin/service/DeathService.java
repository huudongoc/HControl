package hcontrol.plugin.service;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import hcontrol.plugin.entity.EntityManager;
import hcontrol.plugin.entity.EntityProfile;
import hcontrol.plugin.model.DeathContext;
import hcontrol.plugin.model.DeathType;
import hcontrol.plugin.module.boss.BossManager;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.service.CombatService.AttackerInfo;

/**
 * DEATH SERVICE
 * Build DeathContext tu player va event
 * Domain logic - xac dinh nguyen nhan chet
 */
public class DeathService {
    
    private final EntityManager entityManager;
    private final BossManager bossManager;
    private final PlayerManager playerManager;
    private final CombatService combatService;
    
    public DeathService(EntityManager entityManager, BossManager bossManager, PlayerManager playerManager, CombatService combatService) {
        this.entityManager = entityManager;
        this.bossManager = bossManager;
        this.playerManager = playerManager;
        this.combatService = combatService;
    }
    
    /**
     * Build DeathContext tu player va death event
     * Xac dinh nguyen nhan chet (poison, boss, battlefield, secret realm, tribulation, normal)
     */
    public DeathContext buildContext(Player player, PlayerProfile profile, EntityDamageEvent.DamageCause cause) {
        return buildContext(player, profile, cause, null);
    }
    
    /**
     * Build DeathContext tu player va death event (co event de lay thong tin killer + weapon)
     */
    public DeathContext buildContext(Player player, PlayerProfile profile, EntityDamageEvent.DamageCause cause, PlayerDeathEvent event) {
        // Lay thong tin killer va weapon neu co
        String killerName = null;
        String weaponName = null;
        
        // UU TIEN 1: Lay tu CombatService (tracking trong combat system)
        UUID victimUUID = player.getUniqueId();
        AttackerInfo attackerInfo = combatService.getLastAttacker(victimUUID);
        
        // // DEBUG LOG
        // System.out.println("[DEBUG] Player chết: " + player.getName());
        // System.out.println("[DEBUG] AttackerInfo từ CombatService: " + (attackerInfo == null ? "null" : "found"));
        
        PlayerProfile killerProfile = null; // Luu killer profile de format nameplate
        
        if (attackerInfo != null) {
            UUID attackerUUID = attackerInfo.getAttackerUUID();
            
            if (attackerInfo.isPlayer()) {
                // Attacker la Player
                Player attackerPlayer = org.bukkit.Bukkit.getPlayer(attackerUUID);
                if (attackerPlayer != null) {
                    killerProfile = playerManager.get(attackerUUID);
                    if (killerProfile != null) {
                        killerName = killerProfile.getName();
                    } else {
                        killerName = attackerPlayer.getName();
                    }
                    
                    // Lay weapon tu attackerInfo hoac tu player
                    if (attackerInfo.getWeapon() != null) {
                        weaponName = getWeaponNameFromItem(attackerInfo.getWeapon());
                    } else {
                        weaponName = getWeaponName(attackerPlayer);
                    }
                }
            } else {
                // Attacker la LivingEntity (mob/boss)
                org.bukkit.entity.Entity attackerEntity = null;
                for (org.bukkit.entity.Entity entity : player.getWorld().getEntities()) {
                    if (entity.getUniqueId().equals(attackerUUID) && entity instanceof LivingEntity) {
                        attackerEntity = entity;
                        break;
                    }
                }
                
                if (attackerEntity instanceof LivingEntity killerEntity) {
                    // Kiem tra co phai boss khong
                    if (bossManager != null && bossManager.isBoss(killerEntity)) {
                        var boss = bossManager.getBoss(killerEntity.getUniqueId());
                        if (boss != null) {
                            killerName = boss.getBossName();
                        }
                    } else {
                        // Kiem tra EntityProfile
                        EntityProfile entityProfile = entityManager.get(killerEntity.getUniqueId());
                        if (entityProfile != null) {
                            killerName = entityProfile.getDisplayName();
                        } else {
                            killerName = formatMobName(killerEntity.getType().name());
                        }
                    }
                }
            }
        }
        
        // UU TIEN 2: Neu chua co, thu lay tu event (fallback)
        if (killerName == null && event != null) {
            // Lay killer tu event - uu tien getKiller() truoc
            Player killerPlayer = player.getKiller();
            if (killerPlayer != null) {
                // Bi giet boi player
                killerProfile = playerManager.get(killerPlayer.getUniqueId());
                if (killerProfile != null) {
                    killerName = killerProfile.getName();
                } else {
                    killerName = killerPlayer.getName();
                }
                
                // Lay weapon tu killer
                if (weaponName == null) {
                    weaponName = getWeaponName(killerPlayer);
                }
            } else {
                // Kiem tra last damage cause - co the la entity khac
                EntityDamageEvent lastDamage = player.getLastDamageCause();
                if (lastDamage != null) {
                    // Kiem tra EntityDamageByEntityEvent de lay damager
                    if (lastDamage instanceof org.bukkit.event.entity.EntityDamageByEntityEvent damageByEntity) {
                        org.bukkit.entity.Entity damager = damageByEntity.getDamager();
                        
                        // Neu damager la Player
                        if (damager instanceof Player damagerPlayer) {
                            killerProfile = playerManager.get(damagerPlayer.getUniqueId());
                            if (killerProfile != null) {
                                killerName = killerProfile.getName();
                            } else {
                                killerName = damagerPlayer.getName();
                            }
                            if (weaponName == null) {
                                weaponName = getWeaponName(damagerPlayer);
                            }
                        }
                        // Neu damager la LivingEntity (mob/boss)
                        else if (damager instanceof LivingEntity killerEntity) {
                            // Kiem tra co phai boss khong
                            if (bossManager != null && bossManager.isBoss(killerEntity)) {
                                var boss = bossManager.getBoss(killerEntity.getUniqueId());
                                if (boss != null) {
                                    killerName = boss.getBossName();
                                }
                            } else {
                                // Kiem tra EntityProfile
                                EntityProfile entityProfile = entityManager.get(killerEntity.getUniqueId());
                                if (entityProfile != null) {
                                    killerName = entityProfile.getDisplayName();
                                } else {
                                    // Lay ten mob default - format cho dep hon
                                    killerName = formatMobName(killerEntity.getType().name());
                                }
                            }
                        }
                    }
                    // Neu khong phai EntityDamageByEntityEvent, kiem tra entity trong lastDamage
                    else if (lastDamage.getEntity() instanceof LivingEntity && lastDamage.getEntity() != player) {
                        LivingEntity killerEntity = (LivingEntity) lastDamage.getEntity();
                        
                        // Kiem tra co phai boss khong
                        if (bossManager != null && bossManager.isBoss(killerEntity)) {
                            var boss = bossManager.getBoss(killerEntity.getUniqueId());
                            if (boss != null) {
                                killerName = boss.getBossName();
                            }
                        } else {
                            // Kiem tra EntityProfile
                            EntityProfile entityProfile = entityManager.get(killerEntity.getUniqueId());
                            if (entityProfile != null) {
                                killerName = entityProfile.getDisplayName();
                            } else {
                                killerName = formatMobName(killerEntity.getType().name());
                            }
                        }
                    }
                }
            }
        }
        
        // Xoa last attacker sau khi da lay thong tin
        combatService.clearLastAttacker(victimUUID);
        
        // DEBUG LOG - Ket qua cuoi cung
        // System.out.println("[DEBUG] KillerName cuoi cùng: " + (killerName == null ? "null" : killerName));
        // System.out.println("[DEBUG] WeaponName cuoi cùng: " + (weaponName == null ? "null" : weaponName));
        // System.out.println("[DEBUG] Cause: " + cause);
        
        // 1. Kiem tra POISON (uu tien cao nhat - neu chet vi doc)
        if (isPoisonDeath(player, cause)) {
            return new DeathContext(profile, DeathType.POISON, killerName, null, weaponName, killerProfile);
        }
        
        // 2. Kiem tra TRIBULATION (chet trong thien kiep)
        if (isTribulationDeath(player, profile)) {
            return new DeathContext(profile, DeathType.TRIBULATION, killerName, null, weaponName, killerProfile);
        }
        
        // 3. Kiem tra BOSS (chet vi boss)
        if (killerName != null && isBossKiller(player, killerName)) {
            return new DeathContext(profile, DeathType.BOSS, killerName, null, weaponName, killerProfile);
        }
        
        // Kiem tra boss neu chua co killerName
        if (killerName == null) {
            String bossName = getBossKiller(player);
            if (bossName != null) {
                return new DeathContext(profile, DeathType.BOSS, bossName, null, weaponName, killerProfile);
            }
        }
        
        // 4. Kiem tra BATTLEFIELD (chet o chien truong ngoai vuc)
        String battlefieldName = getBattlefieldLocation(player);
        if (battlefieldName != null) {
            return new DeathContext(profile, DeathType.BATTLEFIELD, killerName, battlefieldName, weaponName, killerProfile);
        }
        
        // 5. Kiem tra SECRET_REALM (chet trong bi canh)
        String secretRealmName = getSecretRealmLocation(player);
        if (secretRealmName != null) {
            return new DeathContext(profile, DeathType.SECRET_REALM, killerName, secretRealmName, weaponName, killerProfile);
        }
        
        // 6. Neu co killer thi la PVP hoac PVE
        if (killerName != null) {
            // Kiem tra lai co phai boss khong
            if (isBossKiller(player, killerName)) {
                return new DeathContext(profile, DeathType.BOSS, killerName, null, weaponName, killerProfile);
            }
            // Neu khong phai boss thi la normal death nhung co killer
            return new DeathContext(profile, DeathType.NORMAL, killerName, null, weaponName, killerProfile);
        }
        
        // 7. FALLBACK: NORMAL death
        return new DeathContext(profile, DeathType.NORMAL, null, null, weaponName, killerProfile);
    }
    /**
     * Lay ten weapon tu ItemStack
     */
    private String getWeaponNameFromItem(ItemStack item) {
        if (item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        // Neu co custom name thi lay custom name
        if (meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        
        // Neu co lore (item dac biet) thi lay ten item + type
        if (meta.hasLore()) {
            java.util.List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                String itemType = item.getType().name();
                return formatItemName(itemType);
            }
        }
        
        return null;
    }
    
    /**
     * Format mob name tu EntityType name
     */
    private String formatMobName(String entityTypeName) {
        // Convert ZOMBIE -> "Zombie", SKELETON -> "Skeleton"
        String[] parts = entityTypeName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
        }
        return result.toString();
    }
    /**
     * Lay ten weapon/item neu la item dac biet (co custom name hoac lore)
     */
    private String getWeaponName(Player killer) {
        ItemStack item = killer.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        
        // Neu co custom name thi lay custom name
        if (meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        
        // Neu co lore (item dac biet) thi lay ten item + type
        if (meta.hasLore()) {
            java.util.List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                // Lay ten item type
                String itemType = item.getType().name();
                // Format: "Kiếm Sắt" -> "Kiếm Sắt"
                return formatItemName(itemType);
            }
        }
        
        return null;
    }
    
    /**
     * Format item name tu Material name
     */
    private String formatItemName(String materialName) {
        // Convert DIAMOND_SWORD -> "Kiếm Kim Cương"
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i];
            if (part.equals("sword")) {
                result.insert(0, "Kiếm ");
            } else if (part.equals("axe")) {
                result.insert(0, "Rìu ");
            } else if (part.equals("bow")) {
                result.insert(0, "Cung ");
            } else if (part.equals("trident")) {
                result.insert(0, "Đinh Ba ");
            } else {
                // Format word: diamond -> "Kim Cương"
                String formatted = part.substring(0, 1).toUpperCase() + part.substring(1);
                result.insert(0, formatted + " ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Kiem tra killer co phai boss khong
     */
    private boolean isBossKiller(Player player, String killerName) {
        // Kiem tra qua BossManager
        if (bossManager != null) {
            var nearbyEntities = player.getNearbyEntities(10, 10, 10);
            for (var entity : nearbyEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    if (bossManager.isBoss(livingEntity)) {
                        var boss = bossManager.getBoss(livingEntity.getUniqueId());
                        if (boss != null && boss.getBossName().equals(killerName)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        // Kiem tra qua EntityProfile
        var nearbyEntities = player.getNearbyEntities(10, 10, 10);
        for (var entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity) {
                EntityProfile entityProfile = entityManager.get(entity.getUniqueId());
                if (entityProfile != null && entityProfile.isBoss() && entityProfile.getDisplayName().equals(killerName)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Kiem tra chet vi doc
     */
    private boolean isPoisonDeath(Player player, EntityDamageEvent.DamageCause cause) {
        // Kiem tra co poison effect khong
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            return true;
        }
        
        // Kiem tra cause co phai poison khong
        return cause == EntityDamageEvent.DamageCause.POISON || 
               cause == EntityDamageEvent.DamageCause.MAGIC; // magic damage co the la doc
    }
    
    /**
     * Kiem tra chet trong thien kiep
     * Check qua UiStateService xem player co dang trong tribulation khong
     */
    private boolean isTribulationDeath(Player player, PlayerProfile profile) {
        try {
            var ctx = hcontrol.plugin.core.CoreContext.getInstance();
            if (ctx != null && ctx.getUIContext() != null) {
                var uiStateService = ctx.getUIContext().getUiStateService();
                if (uiStateService != null) {
                    var state = uiStateService.getState(player.getUniqueId());
                    // Check xem player co dang trong tribulation khong
                    return state == hcontrol.plugin.ui.tribulation.UiState.TRIBULATION_IN_PROGRESS;
                }
            }
        } catch (Exception e) {
            // Ignore - service chua san sang
        }
        return false;
    }
    
    /**
     * Lay ten boss neu chet vi boss
     * @return boss name hoac null
     */
    private String getBossKiller(Player player) {
        // Check entity trong pham vi gan nhat
        var nearbyEntities = player.getNearbyEntities(10, 10, 10);
        for (var entity : nearbyEntities) {
            if (entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
                // Check qua BossManager
                if (bossManager != null && bossManager.isBoss(livingEntity)) {
                    var boss = bossManager.getBoss(livingEntity.getUniqueId());
                    if (boss != null) {
                        return boss.getBossName();
                    }
                }
                
                // Check qua EntityProfile
                EntityProfile entityProfile = entityManager.get(entity.getUniqueId());
                if (entityProfile != null && entityProfile.isBoss()) {
                    return entityProfile.getDisplayName();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Kiem tra co phai chien truong ngoai vuc khong
     * @return battlefield name hoac null
     */
    private String getBattlefieldLocation(Player player) {
        String worldName = player.getWorld().getName().toLowerCase();
        
        // Check world name co chua "battlefield", "chien_truong", "ngoai_vuc"
        if (worldName.contains("battlefield") || 
            worldName.contains("chien_truong") || 
            worldName.contains("ngoai_vuc") ||
            worldName.contains("outer")) {
            return "Chiến Trường Ngoại Vực";
        }
        
        return null;
    }
    
    /**
     * Kiem tra co phai bi canh khong
     * @return secret realm name hoac null
     */
    private String getSecretRealmLocation(Player player) {
        String worldName = player.getWorld().getName().toLowerCase();
        
        // Check world name co chua "secret", "bi_canh", "realm"
        if (worldName.contains("secret") || 
            worldName.contains("bi_canh") || 
            worldName.contains("realm") ||
            worldName.contains("dungeon")) {
            return "Bí Cảnh";
        }
        
        return null;
    }
}