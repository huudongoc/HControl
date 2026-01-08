package hcontrol.plugin.service;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * SOUND SERVICE
 * Quan ly tat ca am thanh trong game
 */
public class SoundService {

    // ===== LEVEL UP SOUNDS =====
    
    /**
     * Am thanh khi len tang (auto level up)
     */
    public void playLevelUpSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
    }
    
    /**
     * Am thanh khi vuot checkpoint (tier unlock)
     */
    public void playTierUnlockSound(Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
    }
    
    // ===== BREAKTHROUGH SOUNDS =====
    
    /**
     * Am thanh dot pha thanh cong
     */
    public void playBreakthroughSuccessSound(Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.5f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
        player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
    }
    
    /**
     * Am thanh dot pha that bai
     */
    public void playBreakthroughFailureSound(Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
    }
    
    /**
     * Am thanh noi thuong nang
     */
    public void playSevereInjurySound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
    }
    
    /**
     * Am thanh tan phe (crippled)
     */
    public void playCrippledSound(Player player) {
        Location loc = player.getLocation();
        player.getWorld().createExplosion(loc, 0F, false, false);
        player.playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
    }
    
    /**
     * Am thanh chet
     */
    public void playDeathSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);
    }
    
    // ===== TRIBULATION SOUNDS =====
    
    /**
     * Am thanh bat dau thien kiep
     */
    public void playTribulationStartSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
    }
    
    /**
     * Am thanh thien loi (storm phase)
     */
    public void playThunderSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 0.8f);
    }
    
    /**
     * Am thanh set danh (lightning phase)
     */
    public void playLightningStrikeSound(Location loc) {
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);
    }
    
    /**
     * Am thanh thien kiep that bai
     */
    public void playTribulationFailureSound(Player player) {
        Location loc = player.getLocation();
        player.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        player.playSound(loc, Sound.ENTITY_WITHER_HURT, 0.8f, 0.7f);
    }
    
    // ===== COMBAT SOUNDS =====
    
    /**
     * Am thanh danh trung (hit)
     */
    public void playHitSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.7f, 1.0f);
    }
    
    /**
     * Am thanh chi mang (critical hit)
     */
    public void playCriticalHitSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
    }
    
    /**
     * Am thanh ne tranh (dodge)
     */
    public void playDodgeSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.8f, 1.5f);
    }
    
    /**
     * Am thanh bi sat thuong
     */
    public void playHurtSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.0f);
    }
    
    // ===== SKILL SOUNDS =====
    
    /**
     * Am thanh su dung skill
     */
    public void playSkillCastSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.8f, 1.2f);
    }
    
    /**
     * Am thanh skill thanh cong
     */
    public void playSkillSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
    }
    
    /**
     * Am thanh skill that bai (khong du linh luc)
     */
    public void playSkillFailSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
    }
    
    // ===== UI SOUNDS =====
    
    /**
     * Am thanh mo GUI
     */
    public void playUIOpenSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.2f);
    }
    
    /**
     * Am thanh dong GUI
     */
    public void playUICloseSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
    }
    
    /**
     * Am thanh click button
     */
    public void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    /**
     * Am thanh loi (error)
     */
    public void playErrorSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 0.8f);
    }
    
    /**
     * Am thanh thanh cong
     */
    public void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
    }
    
    // ===== CULTIVATION SOUNDS =====
    
    /**
     * Am thanh tu luyen (meditation)
     */
    public void playMeditationSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.4f, 1.5f);
    }
    
    /**
     * Am thanh thu thap linh khi
     */
    public void playGatherEnergySound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.3f);
    }
    
    // ===== CUSTOM SOUNDS =====
    
    /**
     * Play custom sound voi tham so tuy chinh
     */
    public void playCustomSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
    
    /**
     * Play custom sound tai vi tri cu the
     */
    public void playCustomSound(Location location, Sound sound, float volume, float pitch) {
        location.getWorld().playSound(location, sound, volume, pitch);
    }
}
