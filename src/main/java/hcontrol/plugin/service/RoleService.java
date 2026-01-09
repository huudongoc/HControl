package hcontrol.plugin.service;

import hcontrol.plugin.model.Title;
import hcontrol.plugin.player.PlayerManager;
import hcontrol.plugin.player.PlayerProfile;
import hcontrol.plugin.ui.player.NameplateService;

import org.bukkit.entity.Player;

/**
 * ROLE SERVICE - Xu ly logic set role (title) cho player
 * Service de admin set role/title cho player khac
 */
public class RoleService {
    
    private final TitleService titleService;
    private NameplateService nameplateService;
    
    public RoleService(TitleService titleService) {
        this.titleService = titleService;
    }
    
    /**
     * Inject NameplateService (goi tu CoreContext)
     */
    public void setNameplateService(NameplateService nameplateService) {
        this.nameplateService = nameplateService;
    }
    
    /**
     * Set role (title) cho player - chi admin dung
     * @param profile Player profile
     * @param title Title/role muon set
     * @return true neu thanh cong
     */
    public boolean setRole(PlayerProfile profile, Title title) {
        if (profile == null) {
            return false;
        }
        
        // Unlock title neu chua co
        if (!profile.hasTitle(title)) {
            titleService.unlockTitle(profile, title);
        }
        
        // Equip title
        boolean success = titleService.equipTitle(profile, title);
        
        // Update nameplate
        if (success && nameplateService != null) {
            Player player = profile.getPlayer();
            if (player != null && player.isOnline()) {
                nameplateService.updateNameplate(player, true);
            }
        }
        
        return success;
    }
    
    /**
     * Remove role (unequip title)
     */
    public void removeRole(PlayerProfile profile) {
        if (profile == null) {
            return;
        }
        
        titleService.unequipTitle(profile);
        
        // Update nameplate
        if (nameplateService != null) {
            Player player = profile.getPlayer();
            if (player != null && player.isOnline()) {
                nameplateService.updateNameplate(player, true);
            }
        }
    }
    
    /**
     * Get current role (active title)
     */
    public Title getRole(PlayerProfile profile) {
        if (profile == null) {
            return Title.NONE;
        }
        return profile.getActiveTitle();
    }
    
    /**
     * Set role cho chinh minh (chi set neu da so huu)
     * @param profile Player profile
     * @param title Title/role muon set
     * @return true neu thanh cong, false neu chua so huu title
     */
    public boolean setOwnRole(PlayerProfile profile, Title title) {
        if (profile == null) {
            return false;
        }
        
        // Chi set neu da so huu title
        if (!profile.hasTitle(title)) {
            return false;
        }
        
        // Equip title
        boolean success = titleService.equipTitle(profile, title);
        
        // Update nameplate
        if (success && nameplateService != null) {
            Player player = profile.getPlayer();
            if (player != null && player.isOnline()) {
                nameplateService.updateNameplate(player, true);
            }
        }
        
        return success;
    }
    
    /**
     * Xoa role cua tat ca players (admin only)
     */
    public void clearAllRoles(PlayerManager playerManager) {
        if (playerManager == null) {
            return;
        }
        
        for (PlayerProfile profile : playerManager.getAllOnline()) {
            removeRole(profile);
        }
    }
}
