package hcontrol.plugin.model;

import hcontrol.plugin.player.PlayerProfile;

/**
 * DEATH CONTEXT
 * Chua toan bo thong tin ve cai chet
 * Domain model - khong chua logic format/presentation
 */
public class DeathContext {
    
    private final PlayerProfile victim;
    private final DeathType type;
    private final String killerName;      // boss / player / mob name (null neu khong co)
    private final String locationName;    // battlefield / secret realm name (null neu khong co)
    private final String weaponName;      // ten weapon/item dac biet (null neu khong co)
    private final PlayerProfile killerProfile; // profile cua killer neu la player (null neu khong phai player)
    
    public DeathContext(PlayerProfile victim, DeathType type, String killerName, String locationName) {
        this(victim, type, killerName, locationName, null, null);
    }
    
    public DeathContext(PlayerProfile victim, DeathType type, String killerName, String locationName, String weaponName) {
        this(victim, type, killerName, locationName, weaponName, null);
    }
    
    public DeathContext(PlayerProfile victim, DeathType type, String killerName, String locationName, String weaponName, PlayerProfile killerProfile) {
        this.victim = victim;
        this.type = type;
        this.killerName = killerName;
        this.locationName = locationName;
        this.weaponName = weaponName;
        this.killerProfile = killerProfile;
    }
    
    // ========== GETTERS ==========
    
    public PlayerProfile getVictim() {
        return victim;
    }
    
    public DeathType getType() {
        return type;
    }
    
    public String getKillerName() {
        return killerName;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public String getWeaponName() {
        return weaponName;
    }
    
    public PlayerProfile getKillerProfile() {
        return killerProfile;
    }
    
    // ========== HELPER GETTERS ==========
    
    /**
     * Lay ten nguoi chet (de format message)
     */
    public String getVictimName() {
        return victim.getName();
    }
    
    /**
     * Lay realm cua nguoi chet (de format message)
     */
    public CultivationRealm getRealm() {
        return victim.getRealm();
    }
    
    /**
     * Lay level cua nguoi chet (de format message)
     */
    public int getLevel() {
        return victim.getLevel();
    }
}