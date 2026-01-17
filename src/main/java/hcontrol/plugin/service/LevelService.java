package hcontrol.plugin.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hcontrol.plugin.event.PlayerLevelUpEvent;
import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.player.PlayerProfile;

public class LevelService {

    private final LevelUpEffectService effectService;

    public LevelService(LevelUpEffectService effectService) {
        this.effectService = effectService;
    }

    /**
     * LEVEL UP CONDITIONS:
     * 1. Du tu vi (cultivation >= required)
     * 2. Mo khoa dieu kien (quest/kill/achievement/pill/item)
     */
    
    /**
     * Lay max level cho realm hien tai
     * Tat ca realm co max level = 10
     */
    public int getMaxLevelForRealm(CultivationRealm realm) {
        return realm.getMaxLevelInRealm();  // tat ca realm = 10
    }

    public void sendLevelInfo(Player player, PlayerProfile profile) {
        long currentCultivation = profile.getCultivation();
        long requiredCultivation = getRequiredCultivation(profile.getLevel() + 1, profile.getRealm());
        long cultivationProgress = requiredCultivation > 0 ? currentCultivation * 100 / requiredCultivation : 100;
        
        int maxLevel = getMaxLevelForRealm(profile.getRealm());
        boolean realmCapped = profile.getLevel() >= maxLevel;
        
        boolean unlocked = profile.isNextLevelUnlocked();
        
        player.sendMessage("§7§m--------------------");
        player.sendMessage("§e§lTHÔNG TIN TU TIÊN");
        
        // Hien thi canh gioi + tieu canh (KHONG hien thi level so)
        String fullName = getFullRealmName(profile.getRealm(), profile.getLevel());
        player.sendMessage("§7Cảnh giới: " + profile.getRealm().getColor() + fullName);
        
        // Debug: chi tu than thay level so
        player.sendMessage("§8[Debug: Tầng " + profile.getLevel() + "/" + maxLevel + "]");
        
        player.sendMessage("§7  ├─ §dTu vi: §e" + currentCultivation + "§7/§e" + requiredCultivation + " §7(§e" + cultivationProgress + "%§7)");
        player.sendMessage("§7  ├─ §aĐiểm thuộc tính: §e" + profile.getStatPoints());
        
        // Dieu kien mo khoa
        if (currentCultivation >= requiredCultivation) {
            if (unlocked) {
                player.sendMessage("§7  └─ §a✔ Đã mở khóa: Có thể thăng cấp!");
            } else {
                player.sendMessage("§7  └─ §c✘ Cần mở khóa: Làm nhiệm vụ/giết quái/ăn đan dược");
            }
        } else {
            player.sendMessage("§7  └─ §c✘ Chưa đủ tu vi để thăng cấp");
        }
        
        if (realmCapped) {
            player.sendMessage("§c⚠ Cần đột phá để tiếp tục thăng cấp! /breakthrough");
        }
        
        player.sendMessage("§7§m--------------------");
    }
    
    /**
     * Ten tieu canh (ha/trung/thuong/dinh)
     * 4 TIER MAPPING (chot chan)
     * Ha: 1-3, Trung: 4-6, Thuong: 7-9, Dinh: 10
     * 🔥 Sử dụng DisplayFormatService.getTierName() để thống nhất logic
     */
    private String getSubRealmName(int level) {
        if (level <= 0) return null;
        // Sử dụng DisplayFormatService để thống nhất logic tier name
        hcontrol.plugin.service.DisplayFormatService formatService = 
            hcontrol.plugin.service.DisplayFormatService.getInstance();
        String tierName = formatService.getTierName(level);
        // Remove color codes để dùng cho internal logic (không có màu)
        return tierName.replaceAll("§[0-9a-fk-or]", "");
    }
    
    // Check xem co dang o tier boundary khong (3, 6, 9)
    private boolean isAtTierBoundary(int currentLevel) {
        return currentLevel == 3 || currentLevel == 6 || currentLevel == 9;
    }
    
    /**
     * LAY TIER NUMBER (1=Ha, 2=Trung, 3=Thuong, 4=Dinh)
     */
    public int getCurrentTier(int level) {
        if (level <= 3) return 1;  // Ha
        if (level <= 6) return 2;  // Trung
        if (level <= 9) return 3;  // Thuong
        return 4;  // Dinh
    }
    
    /**
     * LAY LEVEL MAX CUA TIER
     */
    public int getMaxLevelOfTier(int tier) {
        return switch(tier) {
            case 1 -> 3;   // Ha -> level 3
            case 2 -> 6;   // Trung -> level 6
            case 3 -> 9;   // Thuong -> level 9
            case 4 -> 10;  // Dinh -> level 10
            default -> 3;
        };
    }
    
    /**
     * Lay ten day du (VD: Luyen Khi Ha)
     */
    public String getFullRealmName(CultivationRealm realm, int level) {
        String subRealm = getSubRealmName(level);
        if (subRealm == null) return realm.getDisplayName();
        return realm.getDisplayName() + " " + subRealm;
    }

    /**
     * CONG TU VI - AUTO LEVEL UP TRONG TIER, CHECKPOINT O TIER BOUNDARY
     * Auto: 1->2->3, 4->5->6, 7->8->9
     * Checkpoint: 3->4, 6->7, 9->10 (can /unlock)
     * 
     * NEU DA MAX LEVEL REALM (level 10) - KHONG NHAN TU VI
     * 🔥 Áp dụng cultivation multiplier từ spiritual root
     */
    public void addCultivation(PlayerProfile profile, long amount) {
        Player player = profile.getPlayer();
        int maxLevel = getMaxLevelForRealm(profile.getRealm());
        
        // 🔥 Áp dụng cultivation multiplier từ spiritual root
        hcontrol.plugin.core.CoreContext ctx = hcontrol.plugin.core.CoreContext.getInstance();
        if (ctx != null && ctx.getSpiritualRootService() != null) {
            hcontrol.plugin.service.SpiritualRootService rootService = ctx.getSpiritualRootService();
            double multiplier = rootService.getCultivationMultiplier(
                profile.getSpiritualRoot(),
                profile.getRootQuality(),
                profile.getInnerInjury()
            );
            amount = (long)(amount * multiplier);
        }
        
        // CONG TU VI (cho phep cong ngay ca khi max level)
        long newCultivation = profile.getCultivation() + amount;
        profile.setCultivation(newCultivation);
        
        // AUTO LEVEL UP trong cung tier
        while (newCultivation >= getRequiredCultivation(profile.getLevel() + 1, profile.getRealm())) {
            int nextLevel = profile.getLevel() + 1;
            
            // Check xem co vuot tier boundary khong
            if (isAtTierBoundary(profile.getLevel())) {
                // Can unlock de vuot tier
                String nextTier = getSubRealmName(nextLevel);
                if (player != null && player.isOnline()) {
                    player.sendMessage("§e§l⚡ Đủ tu vi để lên " + nextTier + "!");
                    player.sendMessage("§7Dùng §6/unlock §7để vượt checkpoint lên " + nextTier);
                }
                break; // DUNG LAI, cho user /unlock
            }
            
            // AUTO LEVEL UP trong cung tier
            if (nextLevel > maxLevel) break;
            
            long requiredCult = getRequiredCultivation(nextLevel, profile.getRealm());
            newCultivation -= requiredCult;
            profile.setCultivation(newCultivation);
            profile.setLevel(nextLevel);
            profile.addStatPoints(5);
            
            if (player != null && player.isOnline()) {
                String tierName = getSubRealmName(nextLevel);
                player.sendMessage("§a⚜ Đã lên tầng " + nextLevel + " (" + tierName + ")");
                
                // Hieu ung level up
                effectService.playLevelUpEffect(player, nextLevel, tierName);
                
                // Sync vanilla health (maxHP tang theo level)
                var healthService = hcontrol.plugin.core.CoreContext.getInstance().getPlayerContext().getPlayerHealthService();
                healthService.syncHealth(player, profile);
            }
        }
    }


    
    /**
     * TU VI CAN DE LEN LEVEL (TRONG CUNG CANH GIOI)
     * Formula: base × level² × realmMultiplier × peakMultiplier
     * - Level 1-9: base = 50
     * - Level 10: base = 150 (gap 3 lan)
     */
    public long getRequiredCultivation(int level, CultivationRealm realm) {
        long base = (level == 10) ? 150 : 50;
        int realmMultiplier = realm.ordinal() + 1;
        return base * level * level * realmMultiplier;
    }
    
    /**
     * UNLOCK TIER TIEP THEO - VUOT CHECKPOINT (3->4, 6->7, 9->10)
     * Chi can du tu vi la vuot duoc, khong can dieu kien khac
     */
    public boolean unlockNextTier(PlayerProfile profile) {
        Player player = profile.getPlayer();
        int currentLevel = profile.getLevel();
        
        // Check xem co dang o tier boundary khong
        if (!isAtTierBoundary(currentLevel)) {
            if (player != null && player.isOnline()) {
                player.sendMessage("§c✘ Chưa đến checkpoint! Tầng hiện tại: " + currentLevel);
                player.sendMessage("§7Checkpoint: tầng 3, 6, 9");
            }
            return false;
        }
        
        // Check da o dinh chua
        if (currentLevel >= 10) {
            if (player != null && player.isOnline()) {
                player.sendMessage("§c✘ Đã ở Đỉnh! Dùng /dokiep để lên đại cảnh giới");
            }
            return false;
        }
        
        // Tinh level target (3->4, 6->7, 9->10)
        int targetLevel = currentLevel + 1;
        long requiredCult = getRequiredCultivation(targetLevel, profile.getRealm());
        
        // Check du tu vi chua
        if (profile.getCultivation() < requiredCult) {
            if (player != null && player.isOnline()) {
                player.sendMessage("§c✘ Chưa đủ tu vi để vượt checkpoint!");
                player.sendMessage("§7Cần: §e" + requiredCult + " §7| Hiện tại: §e" + profile.getCultivation());
            }
            return false;
        }
        
        // Unlock tier
        String oldTier = getSubRealmName(currentLevel);
        profile.setLevel(targetLevel);
        profile.setCultivation(profile.getCultivation() - requiredCult);
        profile.addStatPoints(5);
        String newTier = getSubRealmName(targetLevel);
        
        // Thong bao
        if (player != null && player.isOnline()) {
            player.sendMessage("§6§l★ VƯỢT CHECKPOINT - LÊN " + newTier.toUpperCase() + "! ★");
            player.sendMessage("§eTầng " + targetLevel + " | +5 điểm stat");
            
            // Hieu ung vuot checkpoint
            effectService.playTierUnlockEffect(player, targetLevel, newTier);
            
            // Update UI - su dung UIContext helper method
            var uiContext = hcontrol.plugin.core.CoreContext.getInstance().getUIContext();
            if (uiContext != null) {
                uiContext.updateAllUI(player);
            }
        }
        
        return true;
    }
    
    private void levelUp(PlayerProfile profile) {
        int oldLevel = profile.getLevel();
        int newLevel = oldLevel + 1;
        long requiredCultivation = getRequiredCultivation(newLevel, profile.getRealm());
        
        profile.setLevel(newLevel);
        
        // TIEU HAO TU VI (tu vi bi tieu hao khi len level)
        profile.setCultivation(profile.getCultivation() - requiredCultivation);
        
        // cong 5 stat point
        profile.addStatPoints(5);
        
        // thong bao level up
        Player player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            String tierName = getSubRealmName(newLevel);
            String fullName = profile.getRealm().getDisplayName() + " " + tierName;
            
            player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("§e§lĐỘT PHÁ THÀNH CÔNG! ⚡");
            player.sendMessage("§7Tu vi: " + profile.getRealm().getColor() + fullName);
            player.sendMessage("§7+§a5 §7Điểm thuộc tính");
            player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            // sound da co trong LevelUpEffectService.playRealmBreakthrough()
        }
        
        // fire event
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(profile, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        
        // Update UI - su dung UIContext helper method
        if (player != null && player.isOnline()) {
            var uiContext = hcontrol.plugin.core.CoreContext.getInstance().getUIContext();
            if (uiContext != null) {
                uiContext.updateAllUI(player);
            }
        }
    }
}
