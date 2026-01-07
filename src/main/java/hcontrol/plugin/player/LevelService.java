package hcontrol.plugin.player;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.event.PlayerLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LevelService {

    /**
     * LEVEL UP CONDITIONS:
     * 1. Du tu vi (cultivation >= required)
     * 2. Mo khoa dieu kien (quest/kill/achievement/pill/item)
     */
    
    /**
     * Lay max level cho realm hien tai (khong breakthrough khong len cap)
     */
    public int getMaxLevelForRealm(CultivationRealm realm) {
        CultivationRealm nextRealm = realm.getNext();
        if (nextRealm == null) {
            return Integer.MAX_VALUE; // realm cuoi khong gioi han
        }
        return nextRealm.getRequiredLevel() - 1; // VD: Qi Refining yeu cau lv10, Mortal max lv9
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
        player.sendMessage(String.format(
            "§7Cảnh giới: %s §7[Lv%d]", 
            profile.getRealm().toString(),
            profile.getLevel()
        ));
        
        // Hien thi tieu canh (ha/trung/cao/dinh)
        String subRealm = getSubRealmName(profile.getLevel());
        if (subRealm != null) {
            player.sendMessage("§7  ├─ Tiểu cảnh: §f" + subRealm);
        }
        
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
     * Ten tieu canh (ha/trung/cao/dinh cap)
     */
    private String getSubRealmName(int level) {
        if (level <= 0) return null;
        if (level <= 3) return "Hạ cấp";
        if (level <= 6) return "Trung cấp";
        if (level <= 9) return "Cao cấp";
        if (level == 10) return "Đỉnh cấp";
        return null;
    }

    /**
     * Tu luyen tu dong (passive cultivation)
     * Tang tu vi theo thoi gian/trang thai
     */
    public void addCultivation(PlayerProfile profile, long amount) {
        Player player = profile.getPlayer();
        
        // check realm cap
        int maxLevel = getMaxLevelForRealm(profile.getRealm());
        if (profile.getLevel() >= maxLevel) {
            // da max level cua realm, khong nhan tu vi nua
            if (player != null && player.isOnline()) {
                player.sendActionBar("§c⚠ " + profile.getRealm().getDisplayName() + " đã đạt giới hạn! Cần đột phá /breakthrough");
            }
            return; // khong nhan cultivation
        }
        
        long newCultivation = profile.getCultivation() + amount;
        profile.setCultivation(newCultivation);
        
        // Check level up (nhung khong vuot qua realm cap)
        while (canLevelUp(profile)) {
            if (profile.getLevel() >= maxLevel) {
                // sap vuot realm cap, dung lai
                if (player != null && player.isOnline()) {
                    player.sendMessage("§e§l⚡ Ban da dat dinh cao " + profile.getRealm().getDisplayName() + "!");
                    player.sendMessage("§7Su dung §e/breakthrough §7de dot pha len canh gioi tiep theo");
                }
                break;
            }
            levelUp(profile);
        }
    }

    /**
     * CHECK LEN LEVEL
     * 1. Du tu vi (dieu kien can)
     * 2. Mo khoa dieu kien (quest/kill/achievement/pill/item)
     */
    private boolean canLevelUp(PlayerProfile profile) {
        long requiredCultivation = getRequiredCultivation(profile.getLevel() + 1, profile.getRealm());
        
        // PHAI CO CA HAI:
        // 1. Tu vi du
        // 2. Da mo khoa (thong qua quest/kill/achievement/pill/item)
        return profile.getCultivation() >= requiredCultivation 
            && profile.isNextLevelUnlocked();
    }
    
    /**
     * TU VI CAN DE LEN LEVEL (TRONG CUNG CANH GIOI)
     * Formula: 50 × level² × (realm + 1)
     * VD: Pham Nhan (realm 0) level 1→2 = 50 × 2² × 1 = 200
     */
    public long getRequiredCultivation(int level, CultivationRealm realm) {
        long base = 50;
        int realmMultiplier = realm.ordinal() + 1;
        return base * level * level * realmMultiplier;
    }

    private void levelUp(PlayerProfile profile) {
        int oldLevel = profile.getLevel();
        int newLevel = oldLevel + 1;
        long requiredCultivation = getRequiredCultivation(newLevel, profile.getRealm());
        
        profile.setLevel(newLevel);
        
        // TIEU HAO TU VI (tu vi bi tieu hao khi len level)
        profile.setCultivation(profile.getCultivation() - requiredCultivation);
        
        // RESET UNLOCK FLAG (can mo khoa lai cho level tiep theo)
        profile.setNextLevelUnlocked(false);
        
        // cong 5 stat point
        profile.addStatPoints(5);
        
        // thong bao level up
        Player player = profile.getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.sendMessage("§e§lTHĂNG CẤP! ⚡");
            player.sendMessage("§7Bạn đã đạt §fLevel " + newLevel);
            player.sendMessage("§7+§a5 §7Điểm thuộc tính");
            player.sendMessage("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
        }
        
        // fire event
        PlayerLevelUpEvent event = new PlayerLevelUpEvent(profile, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        
        // update scoreboard ngay lap tuc
        if (player != null && player.isOnline()) {
            var scoreboardService = hcontrol.plugin.core.CoreContext.getInstance().getScoreboardService();
            if (scoreboardService != null) {
                scoreboardService.updateScoreboard(player);
            }
            
            // update nameplate (realm/level thay doi)
            var nameplateService = hcontrol.plugin.core.CoreContext.getInstance().getNameplateService();
            if (nameplateService != null) {
                nameplateService.updateNameplate(player);
            }
        }
    }
}
