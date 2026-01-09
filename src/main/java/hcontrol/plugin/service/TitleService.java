package hcontrol.plugin.service;

import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.player.NameplateService;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * TITLE SERVICE - Logic danh hieu
 * Xu ly unlock, equip, unequip title
 */
public class TitleService {
    
    private NameplateService nameplateService;
    
    /**
     * Inject NameplateService (goi tu CoreContext)
     */
    public void setNameplateService(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    /**
     * Trang bi danh hieu
     * @return true neu thanh cong
     */
    public boolean equipTitle(PlayerProfile profile, Title title) {
        // Validate: phai mo khoa truoc
        if (!profile.hasTitle(title)) {
            return false;
        }
        
        // Execute
        profile.setActiveTitle(title);
        
        // Update nameplate
        if (nameplateService != null) {
            Player player = profile.getPlayer();
            if (player != null) {
                nameplateService.updateNameplate(player);
            }
        }
        
        return true;
    }
    
    /**
     * Go bo danh hieu
     */
    public void unequipTitle(PlayerProfile profile) {
        profile.setActiveTitle(Title.NONE);
        
        // Update nameplate
        if (nameplateService != null) {
            Player player = profile.getPlayer();
            if (player != null) {
                nameplateService.updateNameplate(player);
            }
        }
    }
    
    /**
     * Mo khoa danh hieu
     * @return true neu chua co (mo khoa thanh cong), false neu da co roi
     */
    public boolean unlockTitle(PlayerProfile profile, Title title) {
        if (profile.hasTitle(title)) {
            return false;  // da co roi
        }
        
        profile.unlockTitle(title);
        return true;
    }
    
    /**
     * Kiem tra da mo khoa chua
     */
    public boolean hasTitle(PlayerProfile profile, Title title) {
        return profile.hasTitle(title);
    }
    
    /**
     * Lay danh sach danh hieu da mo khoa
     */
    public List<Title> getUnlockedTitles(PlayerProfile profile) {
        return profile.getUnlockedTitles();
    }
    
    /**
     * Lay danh hieu dang trang bi
     */
    public Title getActiveTitle(PlayerProfile profile) {
        return profile.getActiveTitle();
    }
    
    // ========================================
    // AUTO-UNLOCK LOGIC (EVENT-BASED)
    // ========================================
    
    /**
     * Tu dong unlock title khi giet nguoi (PvP)
     * Goi tu CombatListener
     */
    public void checkPvPKillUnlock(PlayerProfile killer, int totalKills) {
        // FIRST_BLOOD - lan dau giet nguoi
        if (totalKills == 1) {
            if (unlockTitle(killer, Title.FIRST_BLOOD)) {
                Player player = killer.getPlayer();
                if (player != null) {
                    player.sendMessage("§6✦ Da mo khoa danh hieu: " + Title.FIRST_BLOOD.getFullDisplay());
                }
            }
        }
        
        // MASS_KILLER - giet 10 nguoi
        if (totalKills >= 10) {
            if (unlockTitle(killer, Title.MASS_KILLER)) {
                Player player = killer.getPlayer();
                if (player != null) {
                    player.sendMessage("§6✦ Da mo khoa danh hieu: " + Title.MASS_KILLER.getFullDisplay());
                }
            }
        }
        
        // IMMORTAL - giet 100 nguoi
        if (totalKills >= 100) {
            if (unlockTitle(killer, Title.IMMORTAL)) {
                Player player = killer.getPlayer();
                if (player != null) {
                    player.sendMessage("§6✦ Da mo khoa danh hieu: " + Title.IMMORTAL.getFullDisplay());
                }
            }
        }
    }
    
    /**
     * Tu dong unlock title khi breakthrough
     * Goi tu BreakthroughService
     */
    public void checkBreakthroughUnlock(PlayerProfile profile) {
        // HEAVENLY_TALENT - dot pha lan dau khong that bai
        // TODO: track breakthrough stats
        
        // Tam thoi unlock neu co dao heart cao
        if (profile.getDaoHeart() >= 95.0) {
            if (unlockTitle(profile, Title.HEAVENLY_TALENT)) {
                Player player = profile.getPlayer();
                if (player != null) {
                    player.sendMessage("§6✦ Da mo khoa danh hieu: " + Title.HEAVENLY_TALENT.getFullDisplay());
                }
            }
        }
    }
    
    /**
     * Tu dong unlock title khi join guild
     * Goi tu GuildService (PHASE 10)
     */
    public void checkGuildUnlock(PlayerProfile profile, String guildRole) {
        if ("LEADER".equals(guildRole)) {
            unlockTitle(profile, Title.GUILD_LEADER);
        } else if ("OFFICER".equals(guildRole)) {
            unlockTitle(profile, Title.GUILD_OFFICER);
        } else if ("ELITE".equals(guildRole)) {
            unlockTitle(profile, Title.GUILD_ELITE);
        }
    }
    
    /**
     * Tu dong unlock title theo rank
     * Goi tu RankingService (PHASE 10)
     */
    public void checkRankUnlock(PlayerProfile profile, int rank) {
        if (rank == 1) {
            unlockTitle(profile, Title.TOP_1);
        } else if (rank <= 10) {
            unlockTitle(profile, Title.TOP_10);
        } else if (rank <= 100) {
            unlockTitle(profile, Title.TOP_100);
        }
    }
}
