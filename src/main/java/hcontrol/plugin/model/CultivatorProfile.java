package hcontrol.plugin.model;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.SpiritualRoot;
import hcontrol.plugin.model.RootQuality;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * CULTIVATOR PROFILE - TU SI PROFILE
 * Refactor tu PlayerProfile, day la linh hon tu si
 */
public class CultivatorProfile {
    
    private final UUID uuid;
    private String cultivatorName;  // ten tu si
    
    // ==== CANH GIOI & TU VI ====
    private CultivationRealm realm;
    private int realmLevel;             // level trong canh gioi (1 -> maxLevel)
    private long cultivation;           // tu vi hien tai
    private long cultivationToNext;     // tu vi can de len level ke tiep
    private long cultivationToBreakthrough; // tu vi can de dot pha len realm moi
    
    // ==== LINH CĂN ====
    private SpiritualRoot spiritualRoot;
    private RootQuality rootQuality;
    
    // ==== TRẠNG THÁI TU SI ====
    private double daoHeart;            // dao tam (0-100) - anh huong dot pha
    private double innerInjury;         // noi thuong (0-100) - giam hieu qua tu luyen
    private double tribulationPower;    // thien kiep luc tich luy
    private int karmaPoints;            // nhan qua / nghiep luc
    
    // ==== TU LUYỆN ====
    private long totalCultivationTime;  // tong thoi gian tu luyen (ticks)
    private boolean inSeclusion;        // dang be quan?
    private long seclusionStartTime;    // thoi diem bat dau be quan
    
    // ==== STATS (GIỮ TỪ CŨ) ====
    private final PlayerStats stats;
    
    // ==== COMBAT STATE ====
    private double currentHP;
    private double currentLingQi;
    
    public CultivatorProfile(UUID uuid) {
        this.uuid = uuid;
        this.realm = CultivationRealm.MORTAL;
        this.realmLevel = 1;  // bat dau level 1 trong realm
        this.cultivation = 0L;
        this.cultivationToNext = calculateCultivationToNextLevel();
        this.cultivationToBreakthrough = calculateCultivationToBreakthrough();
        
        // Random linh can - TODO: goi tu SpiritualRootService
        // Tam thoi de default, se set tu service
        this.spiritualRoot = SpiritualRoot.METAL;
        this.rootQuality = RootQuality.MORTAL;
        
        // Trang thai ban dau
        this.daoHeart = 100.0;
        this.innerInjury = 0.0;
        this.tribulationPower = 0.0;
        this.karmaPoints = 0;
        
        this.totalCultivationTime = 0L;
        this.inSeclusion = false;
        
        // Stats
        this.stats = new PlayerStats();
        this.stats.setLevel(1);  // tam thoi giu level cho tuong thich
        
        this.currentHP = stats.getMaxHP();
        this.currentLingQi = stats.getMaxLingQi();
    }
    
    /**
     * Random linh can theo ty le tu tien CHUAN
     * - Ngu linh can (5 loai): pho bien (98.9%)
     * - 4 linh can: 1/100 (TODO)
     * - 3 linh can: 1/1000 (TODO)
     * - Loi/Phong/Bang (bien di): 1/9999 (0.01%)
     * - Am Duong/Hon Don (huyen thoai): 1/99999 (0.001%)
     */
    private SpiritualRoot randomSpiritualRoot() {
        double roll = Math.random() * 99999;
        
        // 1/99999: Am Duong / Hon Don (HUYEN THOAI)
        if (roll < 1) {
            return Math.random() < 0.5 ? SpiritualRoot.YIN_YANG : SpiritualRoot.CHAOS;
        }
        
        // 1/9999: Loi/Phong/Bang (BIEN DI)
        if (roll < 10) {  // 10/99999 ≈ 1/9999
            SpiritualRoot[] mutations = {
                SpiritualRoot.THUNDER, 
                SpiritualRoot.WIND, 
                SpiritualRoot.ICE
            };
            return mutations[(int)(Math.random() * mutations.length)];
        }
        
        // 1/1000: 3 linh can (THIEN TAI) - TODO: implement multi-root
        if (roll < 100) {  // 100/99999 ≈ 1/1000
            // Tam thoi tra ve ngu hanh ngau nhien
            SpiritualRoot[] basics = {
                SpiritualRoot.METAL, SpiritualRoot.WOOD, 
                SpiritualRoot.WATER, SpiritualRoot.FIRE, SpiritualRoot.EARTH
            };
            return basics[(int)(Math.random() * basics.length)];
        }
        
        // 1/100: 4 linh can (THIEN PHAN) - TODO: implement multi-root
        if (roll < 1000) {  // 1000/99999 ≈ 1/100
            // Tam thoi tra ve ngu hanh ngau nhien
            SpiritualRoot[] basics = {
                SpiritualRoot.METAL, SpiritualRoot.WOOD, 
                SpiritualRoot.WATER, SpiritualRoot.FIRE, SpiritualRoot.EARTH
            };
            return basics[(int)(Math.random() * basics.length)];
        }
        
        // Con lai: Ngu linh can (PHO BIEN) - 98.9%
        SpiritualRoot[] basics = {
            SpiritualRoot.METAL,   // Kim
            SpiritualRoot.WOOD,    // Moc
            SpiritualRoot.WATER,   // Thuy
            SpiritualRoot.FIRE,    // Hoa
            SpiritualRoot.EARTH    // Tho
        };
        return basics[(int)(Math.random() * basics.length)];
    }
    
    /**
     * Random linh can pham chat
     */
    private RootQuality randomRootQuality() {
        double rand = Math.random();
        if (rand < 0.60) return RootQuality.MORTAL;
        if (rand < 0.90) return RootQuality.SPIRITUAL;
        if (rand < 0.98) return RootQuality.HEAVENLY;
        return RootQuality.IMMORTAL;
    }
    
    // ===== GETTERS & SETTERS =====
    
    public UUID getUuid() { return uuid; }
    public Player getPlayer() { return Bukkit.getPlayer(uuid); }
    
    public String getCultivatorName() { return cultivatorName; }
    public void setCultivatorName(String name) { this.cultivatorName = name; }
    
    // Canh gioi
    public CultivationRealm getRealm() { return realm; }
    public void setRealm(CultivationRealm realm) { 
        this.realm = realm;
        updateCultivationRequirement();
    }
    
    // Tu vi
    public long getCultivation() { return cultivation; }
    public void setCultivation(long cultivation) { this.cultivation = cultivation; }
    public void addCultivation(long amount) { this.cultivation += amount; }
    
    public long getCultivationToNext() { return cultivationToNext; }
    
    // Linh can
    public SpiritualRoot getSpiritualRoot() { return spiritualRoot; }
    public void setSpiritualRoot(SpiritualRoot root) { this.spiritualRoot = root; }
    
    public RootQuality getRootQuality() { return rootQuality; }
    public void setRootQuality(RootQuality quality) { this.rootQuality = quality; }
    
    /**
     * He so tu luyen tong hop
     */
    public double getCultivationMultiplier() {
        return spiritualRoot.getCultivationBonus() 
             * rootQuality.getQualityMultiplier()
             * (1.0 - innerInjury / 200.0); // noi thuong giam hieu suat
    }
    
    // Dao tam
    public double getDaoHeart() { return daoHeart; }
    public void setDaoHeart(double daoHeart) { 
        this.daoHeart = Math.max(0, Math.min(100, daoHeart)); 
    }
    public void addDaoHeart(double amount) { setDaoHeart(daoHeart + amount); }
    
    // Noi thuong
    public double getInnerInjury() { return innerInjury; }
    public void setInnerInjury(double injury) { 
        this.innerInjury = Math.max(0, Math.min(100, injury)); 
    }
    public void addInnerInjury(double amount) { setInnerInjury(innerInjury + amount); }
    
    // Thien kiep
    public double getTribulationPower() { return tribulationPower; }
    public void setTribulationPower(double power) { this.tribulationPower = power; }
    public void addTribulationPower(double amount) { this.tribulationPower += amount; }
    
    // Nhan qua
    public int getKarmaPoints() { return karmaPoints; }
    public void setKarmaPoints(int karma) { this.karmaPoints = karma; }
    public void addKarmaPoints(int amount) { this.karmaPoints += amount; }
    
    // Tu luyen
    public long getTotalCultivationTime() { return totalCultivationTime; }
    public void setTotalCultivationTime(long time) { this.totalCultivationTime = time; }
    public void addCultivationTime(long ticks) { this.totalCultivationTime += ticks; }
    
    public boolean isInSeclusion() { return inSeclusion; }
    public void setInSeclusion(boolean inSeclusion) { 
        this.inSeclusion = inSeclusion;
        if (inSeclusion) {
            this.seclusionStartTime = System.currentTimeMillis();
        }
    }
    
    public long getSeclusionStartTime() { return seclusionStartTime; }
    public void setSeclusionStartTime(long time) { this.seclusionStartTime = time; }
    
    // Stats (backward compatible)
    public PlayerStats getStats() { return stats; }
    
    // HP & Linh Khi
    public double getCurrentHP() { return currentHP; }
    public void setCurrentHP(double hp) { 
        this.currentHP = Math.max(0, Math.min(stats.getMaxHP(), hp)); 
    }
    
    public double getCurrentLingQi() { return currentLingQi; }
    public void setCurrentLingQi(double qi) { 
        this.currentLingQi = Math.max(0, Math.min(stats.getMaxLingQi(), qi)); 
    }
    
    public boolean hasEnoughLingQi(double amount) {
        return currentLingQi >= amount;
    }
    
    // Backward compatible voi code cu
    public int getLevel() { return stats.getLevel(); }
    public void setLevel(int level) { stats.setLevel(level); }
    
    public long getExp() { return cultivation; }  // map tu vi -> exp
    public void setExp(long exp) { this.cultivation = exp; }
    
    public int getStatPoints() { return stats.getLevel() * 5; }  // tam thoi
    public void removeStatPoints(int amount) { /* TODO */ }
    public void addStatPoints(int amount) { /* TODO */ }
    
    /**
     * Dot pha len canh gioi tiep theo
     */
    public boolean breakthrough() {
        CultivationRealm next = realm.getNext();
        if (next == null) return false;
        
        this.realm = next;
        this.realmLevel = 1;  // reset ve level 1 trong realm moi
        this.cultivation = 0;
        this.cultivationToNext = calculateCultivationToNextLevel();
        this.cultivationToBreakthrough = calculateCultivationToBreakthrough();
        
        return true;
    }
    
    /**
     * Len level trong cung canh gioi
     */
    public boolean levelUpInRealm() {
        if (realmLevel >= realm.getMaxLevelInRealm()) return false;
        if (cultivation < cultivationToNext) return false;
        
        realmLevel++;
        cultivation = 0;  // reset tu vi sau khi len level
        cultivationToNext = calculateCultivationToNextLevel();
        
        return true;
    }
    
    /**
     * Tinh tu vi can de len level ke tiep (trong cung realm)
     * Formula: base × realmMultiplier × level²
     */
    private long calculateCultivationToNextLevel() {
        if (realmLevel >= realm.getMaxLevelInRealm()) {
            return Long.MAX_VALUE; // da max level trong realm
        }
        
        long base = 100;
        double realmMultiplier = realm.ordinal() + 1;
        int nextLevel = realmLevel + 1;
        
        return (long)(base * realmMultiplier * nextLevel * nextLevel);
    }
    
    /**
     * Tinh tu vi can de dot pha len realm moi
     * Formula: 10x tu vi cua level cuoi trong realm
     * VD: Luyen Khi (maxLevel=9) = 100 × 9² × 2 × 10 = 162,000
     */
    private long calculateCultivationToBreakthrough() {
        if (realm.getNext() == null) return Long.MAX_VALUE;
        
        long base = 100;
        int realmMultiplier = realm.ordinal() + 1;
        int maxLevel = realm.getMaxLevelInRealm();
        
        // Breakthrough = 10x tu vi cua level cuoi
        return base * maxLevel * maxLevel * realmMultiplier * 10;
    }
    
    /**
     * Kiem tra co the dot pha khong
     */
    public boolean canBreakthrough() {
        if (realm.getNext() == null) return false;
        // Phai dat max level trong realm + du tu vi
        return realmLevel >= realm.getMaxLevelInRealm() && cultivation >= cultivationToBreakthrough;
    }
    
    /**
     * Getter/setter cho realmLevel
     */
    public int getRealmLevel() {
        return realmLevel;
    }
    
    public void setRealmLevel(int level) {
        this.realmLevel = Math.max(1, Math.min(realm.getMaxLevelInRealm(), level));
        this.cultivationToNext = calculateCultivationToNextLevel();
    }
    
    public long getCultivationToBreakthrough() {
        return cultivationToBreakthrough;
    }
    
    /**
     * Update cultivation requirement (goi khi thay doi realm/level)
     */
    private void updateCultivationRequirement() {
        this.cultivationToNext = calculateCultivationToNextLevel();
        this.cultivationToBreakthrough = calculateCultivationToBreakthrough();
    }
}
