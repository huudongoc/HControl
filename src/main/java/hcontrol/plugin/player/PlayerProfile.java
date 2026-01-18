package hcontrol.plugin.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import hcontrol.plugin.model.CultivationRealm;
import hcontrol.plugin.model.LivingActor;
import hcontrol.plugin.model.PlayerStats;
import hcontrol.plugin.model.RootQuality;
import hcontrol.plugin.model.SpiritualRoot;
import hcontrol.plugin.model.Title;

public class PlayerProfile implements LivingActor {

    private final UUID uuid;
    private String playerName;
    private int level;
    private int statPoints;
    
    // TU TIEN - Cultivation Realm
    private CultivationRealm realm;
    private int realmLevel;  // level trong canh gioi (1 -> maxLevelInRealm)
    private long cultivation;  // tu vi (exp de level up trong realm)
    
    // TU TIEN - Spiritual Root
    private SpiritualRoot spiritualRoot;
    private RootQuality rootQuality;
    
    // TU TIEN - Dao Heart & State
    private double daoHeart;  // 0-100 (dao tam)
    private double innerInjury;  // 0-100 (noi thuong)
    private double tribulationPower;  // suc manh thien kiep tich luy
    private int karmaPoints;  // nghiep luc (am/duong)
    
    // TU TIEN - Cultivation Time
    private long totalCultivationTime;  // tong thoi gian tu luyen (tick)
    private boolean inSeclusion;  // dang bi quan
    private long seclusionStartTime;  // thoi diem bat dau bi quan
    
    // TU TIEN - Breakthrough Cooldown
    private long breakthroughCooldownEnd;  // thoi diem het cooldown dot pha (0 = khong cooldown)
    
    // LEVEL UNLOCK SYSTEM
    private boolean nextLevelUnlocked;  // da mo khoa dieu kien len level tiep theo
    private int questCompleted;  // so nhiem vu hoan thanh
    private int monstersKilled;  // so quai vat da giet
    private int achievementsUnlocked;  // so thanh tuu da dat duoc
    private int pillsConsumed;  // so dan duoc da dung
    
    // BREAKTHROUGH UNLOCK SYSTEM (kho hon level unlock)
    private boolean breakthroughUnlocked;  // da mo khoa dieu kien dot pha len realm moi
    private int breakthroughQuestsCompleted;  // nhiem vu dot pha (kho hon nhiem vu thuong)
    private int eliteBossKilled;  // boss tinh anh da giet
    private int tribulationSurvived;  // thien kiep da vuot qua
    
    // TITLE SYSTEM - Danh hieu
    private Title activeTitle;  // danh hieu dang trang bi
    private final List<Title> unlockedTitles;  // danh hieu da mo khoa
    
    // SKILL SYSTEM - PHASE 6
    private final java.util.Set<String> learnedSkills;  // skills da hoc
    private final java.util.Map<Integer, String> skillHotbar;  // slot 1-9 -> skillId
    
    // ITEM SYSTEM - PHASE 8A
    private final java.util.Map<hcontrol.plugin.item.EquipmentSlot, String> equippedItems;  // slot -> itemId
    
    // IDENTITY SYSTEM - PHASE 5
    private hcontrol.plugin.identity.PlayerIdentity identity;  // identity layer
    
    // CLASS SYSTEM - PHASE 5
    private hcontrol.plugin.classsystem.ClassProfile classProfile;  // nullable - chưa chọn class
    
    // ASCENSION SYSTEM - ENDGAME
    private hcontrol.plugin.model.AscensionProfile ascensionProfile;  // ascension layer (sau CHANTIEN 10)
    
    // LINK STATS
    private final PlayerStats stats;
    
    // COMBAT STATE
    private double currentHP;
    private double currentLingQi;  // Linh Khi thay Mana

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        Player player = Bukkit.getPlayer(uuid);
        this.playerName = player != null ? player.getName() : "Unknown";
        this.level = 1;
        this.statPoints = 0;
        this.realm = CultivationRealm.PHAMNHAN;  // bat dau tu Pham Nhan
        this.realmLevel = 1;  // level 1 trong canh gioi
        this.cultivation = 0L;  // chua co tu vi
        
        // khoi tao spiritual root (random) - sử dụng service nếu có
        hcontrol.plugin.core.CoreContext ctx = hcontrol.plugin.core.CoreContext.getInstance();
        if (ctx != null && ctx.getPlayerContext() != null && ctx.getPlayerContext().getSpiritualRootService() != null) {
            hcontrol.plugin.service.SpiritualRootService rootService = ctx.getPlayerContext().getSpiritualRootService();
            this.spiritualRoot = rootService.randomSpiritualRoot();
            this.rootQuality = rootService.randomRootQuality();
        } else {
            // Fallback nếu context chưa sẵn sàng
            this.spiritualRoot = SpiritualRoot.randomSpiritualRoot();
            this.rootQuality = RootQuality.randomQuality();
        }
            
        // khoi tao dao heart & state
        this.daoHeart = 100.0;  // dao tam hoan hao
        this.innerInjury = 0.0;  // khong noi thuong
        this.tribulationPower = 0.0;  // chua tich luy
        this.karmaPoints = 0;  // trung lap
        
        // khoi tao cultivation time
        this.totalCultivationTime = 0L;
        this.inSeclusion = false;
        this.seclusionStartTime = 0L;
        
        // khoi tao breakthrough cooldown
        this.breakthroughCooldownEnd = 0L;  // khong cooldown
        
        // khoi tao unlock system
        this.nextLevelUnlocked = true;  // level 1 mac dinh da mo khoa
        this.questCompleted = 0;
        this.monstersKilled = 0;
        this.achievementsUnlocked = 0;
        this.pillsConsumed = 0;
        
        // khoi tao breakthrough unlock system
        this.breakthroughUnlocked = false;  // dot pha phai mo khoa
        this.breakthroughQuestsCompleted = 0;
        this.eliteBossKilled = 0;
        this.tribulationSurvived = 0;
        
        // khoi tao title system
        this.activeTitle = Title.NONE;  // khong danh hieu
        this.unlockedTitles = new ArrayList<>();
        this.unlockedTitles.add(Title.NONE);  // mac dinh mo khoa NONE
        
        // khoi tao skill system - PHASE 6
        this.learnedSkills = new java.util.HashSet<>();
        this.skillHotbar = new java.util.HashMap<>();
        
        // khoi tao item system - PHASE 8A
        this.equippedItems = new java.util.HashMap<>();
        
        // khoi tao identity system - PHASE 5
        this.identity = new hcontrol.plugin.identity.PlayerIdentity();  // default identity
        
        // khoi tao class system - PHASE 5
        this.classProfile = null;  // chua chon class
        
        // khoi tao ascension system - ENDGAME
        this.ascensionProfile = new hcontrol.plugin.model.AscensionProfile();  // bat dau tu level 0
        
        // khoi tao stats
        this.stats = new PlayerStats();
        this.stats.setLevel(this.level);
        
        // khoi tao HP/Linh Khi = max
        this.currentHP = stats.getMaxHP();
        this.currentLingQi = stats.getMaxLingQi();
    }

    // === BASIC ===

    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return playerName;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    // === LEVEL ===

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.realmLevel = level;  // sync realmLevel với level
        this.stats.setLevel(level); // sync level vao stats
    }

    public void addLevel(int amount) {
        setLevel(this.level + amount);
    }

    // === STAT POINTS ===

    public int getStatPoints() {
        return statPoints;
    }

    public void setStatPoints(int statPoints) {
        this.statPoints = statPoints;
    }

    public void addStatPoints(int amount) {
        this.statPoints += amount;
    }

    public void removeStatPoints(int amount) {
        this.statPoints = Math.max(0, this.statPoints - amount);
    }
    
    // === STATS ===
    
    public PlayerStats getStats() {
        return stats;
    }
    
    // === CULTIVATION REALM ===
    
    public CultivationRealm getRealm() {
        return realm;
    }
    
    public void setRealm(CultivationRealm realm) {
        this.realm = realm;
        // Ensure stats level is synced (can affect maxHP calculation)
        this.stats.setLevel(this.level);
    }
    
    public boolean canBreakthrough() {
        CultivationRealm nextRealm = realm.getNext();
        if (nextRealm == null) {
            return false;  // da max realm
        }
        
        // BAT BUOC LEVEL 10 (Dinh) moi duoc do kiep
        if (level < realm.getMaxLevelInRealm()) {
            return false;  // chua max level
        }
        
        // Check tu vi du (can du cultivation de dot pha)
        if (cultivation < nextRealm.getRequiredCultivation()) {
            return false;
        }
        
        // Check da mo khoa dot pha chua
        return breakthroughUnlocked;
    }
    
    public void breakthrough() {
        CultivationRealm next = realm.getNext();
        if (next != null && canBreakthrough()) {
            this.realm = next;
            this.level = 1;  // reset level ve 1
            this.realmLevel = 1;  // reset ve level 1 trong canh gioi moi
            this.cultivation = 0L;  // reset tu vi
            this.stats.setLevel(1);  // sync level vao stats
        }
    }
    
    // === REALM LEVEL ===
    
    public int getRealmLevel() {
        return realmLevel;
    }
    
    public void setRealmLevel(int realmLevel) {
        this.realmLevel = realmLevel;
    }
    
    // === CULTIVATION ===
    
    public long getCultivation() {
        return cultivation;
    }
    
    public void setCultivation(long cultivation) {
        this.cultivation = cultivation;
    }
    
    public void addCultivation(long amount) {
        this.cultivation += amount;
    }
    
    // === SPIRITUAL ROOT ===
    
    public SpiritualRoot getSpiritualRoot() {
        return spiritualRoot;
    }
    
    public void setSpiritualRoot(SpiritualRoot root) {
        this.spiritualRoot = root;
    }
    
    public RootQuality getRootQuality() {
        return rootQuality;
    }
    
    public void setRootQuality(RootQuality quality) {
        this.rootQuality = quality;
    }
    
    // === DAO HEART & STATE ===
    
    public double getDaoHeart() {
        return daoHeart;
    }
    
    public void setDaoHeart(double daoHeart) {
        this.daoHeart = Math.max(0, Math.min(100, daoHeart));
    }
    
    public void addDaoHeart(double amount) {
        setDaoHeart(daoHeart + amount);
    }
    
    public void removeDaoHeart(double amount) {
        setDaoHeart(daoHeart - amount);
    }
    
    public double getInnerInjury() {
        return innerInjury;
    }
    
    public void setInnerInjury(double injury) {
        this.innerInjury = Math.max(0, Math.min(100, injury));
    }
    
    public void addInnerInjury(double amount) {
        setInnerInjury(innerInjury + amount);
    }
    
    public void removeInnerInjury(double amount) {
        setInnerInjury(innerInjury - amount);
    }
    
    public double getTribulationPower() {
        return tribulationPower;
    }
    
    public void setTribulationPower(double power) {
        this.tribulationPower = Math.max(0, power);
    }
    
    public void addTribulationPower(double amount) {
        this.tribulationPower += amount;
    }
    
    public int getKarmaPoints() {
        return karmaPoints;
    }
    
    public void setKarmaPoints(int karma) {
        this.karmaPoints = karma;
    }
    
    public void addKarma(int amount) {
        this.karmaPoints += amount;
    }
    
    public void addKarmaPoints(int amount) {
        this.karmaPoints += amount;  // alias for compatibility
    }
    
    // === CULTIVATION TIME ===
    
    public long getTotalCultivationTime() {
        return totalCultivationTime;
    }
    
    public void setTotalCultivationTime(long time) {
        this.totalCultivationTime = time;
    }
    
    public void addCultivationTime(long ticks) {
        this.totalCultivationTime += ticks;
    }
    
    public boolean isInSeclusion() {
        return inSeclusion;
    }
    
    public void setInSeclusion(boolean inSeclusion) {
        this.inSeclusion = inSeclusion;
    }
    
    public long getSeclusionStartTime() {
        return seclusionStartTime;
    }
    
    public void setSeclusionStartTime(long time) {
        this.seclusionStartTime = time;
    }
    
    // === BREAKTHROUGH COOLDOWN ===
    
    public long getBreakthroughCooldownEnd() {
        return breakthroughCooldownEnd;
    }
    
    public void setBreakthroughCooldownEnd(long timestamp) {
        this.breakthroughCooldownEnd = timestamp;
    }
    
    public boolean isBreakthroughOnCooldown() {
        return System.currentTimeMillis() < breakthroughCooldownEnd;
    }
    
    // === TITLE SYSTEM ===
    
    public Title getActiveTitle() {
        return activeTitle;
    }
    
    public void setActiveTitle(Title title) {
        // chi set duoc neu da mo khoa
        if (unlockedTitles.contains(title)) {
            this.activeTitle = title;
        }
    }
    
    public List<Title> getUnlockedTitles() {
        return new ArrayList<>(unlockedTitles);  // return copy
    }
    
    public void unlockTitle(Title title) {
        if (!unlockedTitles.contains(title)) {
            unlockedTitles.add(title);
        }
    }
    
    public boolean hasTitle(Title title) {
        return unlockedTitles.contains(title);
    }
    
    public void removeTitle(Title title) {
        if (activeTitle == title) {
            activeTitle = Title.NONE;
        }
        unlockedTitles.remove(title);
    }
    
    // === HP / LINH KHI ===
    
    public double getCurrentHP() {
        return currentHP;
    }
    
    public void setCurrentHP(double hp) {
        this.currentHP = Math.max(0, Math.min(hp, stats.getMaxHP()));
    }
    
    public void addHP(double amount) {
        setCurrentHP(currentHP + amount);
    }
    
    public void removeHP(double amount) {
        setCurrentHP(currentHP - amount);
    }
    
    public boolean isDead() {
        return currentHP <= 0;
    }
    
    public double getCurrentLingQi() {
        return currentLingQi;
    }
    
    public void setCurrentLingQi(double qi) {
        this.currentLingQi = Math.max(0, Math.min(qi, stats.getMaxLingQi()));
    }
    
    public void addLingQi(double amount) {
        setCurrentLingQi(currentLingQi + amount);
    }
    
    public void removeLingQi(double amount) {
        setCurrentLingQi(currentLingQi - amount);
    }
    
    // backward compatible
    public double getCurrentMana() {
        return currentLingQi;
    }
    
    public void setCurrentMana(double mana) {
        setCurrentLingQi(mana);
    }
    
    public void addMana(double amount) {
        addLingQi(amount);
    }
    
    public void removeMana(double amount) {
        removeLingQi(amount);
    }
    
    public boolean hasEnoughMana(double cost) {
        return currentLingQi >= cost;
    }
    
    public boolean hasEnoughLingQi(double cost) {
        return currentLingQi >= cost;
    }
    
    // === UNLOCK SYSTEM ===
    
    public boolean isNextLevelUnlocked() {
        return nextLevelUnlocked;
    }
    
    public void setNextLevelUnlocked(boolean unlocked) {
        this.nextLevelUnlocked = unlocked;
    }
    
    /**
     * Mo khoa level tiep theo (quest completed)
     */
    public void unlockNextLevelByQuest() {
        this.questCompleted++;
        this.nextLevelUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§a✔ Đã mở khóa thăng cấp tiếp theo! (Nhiệm vụ hoàn thành)");
            player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
        }
    }
    
    /**
     * Mo khoa level tiep theo (monster killed)
     */
    public void unlockNextLevelByKill(int killsRequired) {
        this.monstersKilled++;
        
        Player player = getPlayer();
        if (this.monstersKilled >= killsRequired) {
            this.nextLevelUnlocked = true;
            if (player != null && player.isOnline()) {
                player.sendMessage("§a✔ Đã mở khóa thăng cấp tiếp theo! (Giết " + killsRequired + " quái vật)");
                player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
            }
        } else {
            if (player != null && player.isOnline()) {
                player.sendActionBar("§7Giết quái: §e" + this.monstersKilled + "§7/§e" + killsRequired);
            }
        }
    }
    
    /**
     * Mo khoa level tiep theo (achievement unlocked)
     */
    public void unlockNextLevelByAchievement() {
        this.achievementsUnlocked++;
        this.nextLevelUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§a✔ Đã mở khóa thăng cấp tiếp theo! (Thành tựu đạt được)");
            player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
        }
    }
    
    /**
     * Mo khoa level tiep theo (pill consumed)
     */
    public void unlockNextLevelByPill(String pillName) {
        this.pillsConsumed++;
        this.nextLevelUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§a✔ Đã mở khóa thăng cấp tiếp theo! (Dùng " + pillName + ")");
            player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
        }
    }
    
    /**
     * Mo khoa truc tiep (GM command / item dac biet)
     */
    public void forceUnlockNextLevel() {
        this.nextLevelUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§a✔ Đã mở khóa thăng cấp tiếp theo!");
        }
    }
    
    public int getQuestCompleted() {
        return questCompleted;
    }
    
    public int getMonstersKilled() {
        return monstersKilled;
    }
    
    public int getAchievementsUnlocked() {
        return achievementsUnlocked;
    }
    
    public int getPillsConsumed() {
        return pillsConsumed;
    }
    
    // === BREAKTHROUGH UNLOCK SYSTEM ===
    
    public boolean isBreakthroughUnlocked() {
        return breakthroughUnlocked;
    }
    
    public void setBreakthroughUnlocked(boolean unlocked) {
        this.breakthroughUnlocked = unlocked;
    }
    
    /**
     * Mo khoa dot pha bang nhiem vu dot pha (kho hon nhiem vu thuong)
     */
    public void unlockBreakthroughByQuest(String questName) {
        this.breakthroughQuestsCompleted++;
        this.breakthroughUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§6★★★ Đã mở khóa đột phá! ★★★");
            player.sendMessage("§eHoàn thành: " + questName);
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 0.5f);
        }
    }
    
    /**
     * Mo khoa dot pha bang giet boss tinh anh
     */
    public void unlockBreakthroughByEliteBoss(String bossName) {
        this.eliteBossKilled++;
        this.breakthroughUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§6★★★ Đã mở khóa đột phá! ★★★");
            player.sendMessage("§eGiết: " + bossName);
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 0.5f);
        }
    }
    
    /**
     * Mo khoa dot pha bang vuot qua thien kiep
     */
    public void unlockBreakthroughByTribulation() {
        this.tribulationSurvived++;
        this.breakthroughUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§6★★★ Đã mở khóa đột phá! ★★★");
            player.sendMessage("§eVượt qua thiên kiếp!");
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 0.5f);
        }
    }
    
    /**
     * Mo khoa truc tiep (GM / item dac biet)
     */
    public void forceUnlockBreakthrough() {
        this.breakthroughUnlocked = true;
        
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§6★ Đã mở khóa đột phá!");
        }
    }
    
    public int getBreakthroughQuestsCompleted() {
        return breakthroughQuestsCompleted;
    }
    
    public int getEliteBossKilled() {
        return eliteBossKilled;
    }
    
    public int getTribulationSurvived() {
        return tribulationSurvived;
    }
    
    // ========== LIVING ACTOR IMPLEMENTATION ==========
    // Cac method da co roi, chi can them @Override cho cac method chua co
    
    @Override
    public UUID getUUID() {
        return uuid;
    }
    
    @Override
    public String getDisplayName() {
        return playerName;
    }
    
    // getRealm(), getLevel(), getCurrentHP(), setCurrentHP(), getCurrentLingQi(), setCurrentLingQi()
    // DA CO ROI (không cần duplicate)
    
    @Override
    public double getMaxHP() {
        return stats.getMaxHP();
    }
    
    @Override
    public double getAttack() {
        // Tu tien damage DEN TU REALM, khong phai stat
        // Tra ve base attack de CombatService tinh theo realm
        int root = stats.getRoot();  // Can Cot
        return root * 1.5;  // base attack tu can cot
    }
    
    @Override
    public double getDefense() {
        return stats.getDefense();
    }
    
    @Override
    public double getMaxLingQi() {
        return stats.getMaxLingQi();
    }
    
    // getCurrentLingQi(), setCurrentLingQi() DA CO ROI
    
    @Override
    public org.bukkit.entity.LivingEntity getEntity() {
        return Bukkit.getPlayer(uuid);
    }
    
    // ========== SKILL SYSTEM - PHASE 6 ==========
    
    /**
     * Learn skill
     */
    public void learnSkill(String skillId) {
        learnedSkills.add(skillId);
    }
    
    /**
     * Check if player has learned skill
     */
    public boolean hasLearnedSkill(String skillId) {
        return learnedSkills.contains(skillId);
    }
    
    /**
     * Get all learned skills
     */
    public java.util.Set<String> getLearnedSkills() {
        return new java.util.HashSet<>(learnedSkills);
    }
    
    /**
     * Bind skill to hotbar slot (1-9)
     */
    public void bindSkill(int slot, String skillId) {
        if (slot < 1 || slot > 9) return;
        skillHotbar.put(slot, skillId);
    }
    
    /**
     * Get skill at hotbar slot
     */
    public String getSkillAtSlot(int slot) {
        return skillHotbar.get(slot);
    }
    
    /**
     * Unbind skill from slot
     */
    public void unbindSkill(int slot) {
        skillHotbar.remove(slot);
    }
    
    // ========== ITEM SYSTEM - PHASE 8A ==========
    
    /**
     * Equip item vào slot
     */
    public void equipItem(hcontrol.plugin.item.EquipmentSlot slot, String itemId) {
        equippedItems.put(slot, itemId);
    }
    
    /**
     * Unequip item từ slot
     */
    public void unequipItem(hcontrol.plugin.item.EquipmentSlot slot) {
        equippedItems.remove(slot);
    }
    
    /**
     * Get item ở slot
     */
    public String getItemAtSlot(hcontrol.plugin.item.EquipmentSlot slot) {
        return equippedItems.get(slot);
    }
    
    /**
     * Get tất cả equipped items
     */
    public java.util.Map<hcontrol.plugin.item.EquipmentSlot, String> getEquippedItems() {
        return new java.util.HashMap<>(equippedItems);
    }
    
    // ========== CLASS SYSTEM - PHASE 5 ==========
    
    /**
     * Get class profile
     */
    public hcontrol.plugin.classsystem.ClassProfile getClassProfile() {
        return classProfile;
    }
    
    /**
     * Set class profile
     */
    public void setClassProfile(hcontrol.plugin.classsystem.ClassProfile classProfile) {
        this.classProfile = classProfile;
    }
    
    // ========== ASCENSION SYSTEM - ENDGAME ==========
    
    /**
     * Get ascension profile
     */
    public hcontrol.plugin.model.AscensionProfile getAscensionProfile() {
        return ascensionProfile;
    }
    
    /**
     * Check có thể ascension không
     * Chỉ mở khi: realm == CHANTIEN && level == 10
     */
    public boolean canAscend() {
        return realm == CultivationRealm.CHANTIEN && level >= realm.getMaxLevelInRealm();
    }
    
    /**
     * Check if player has class
     */
    public boolean hasClass() {
        return classProfile != null;
    }
    
    /**
     * Get all hotbar bindings
     */
    public java.util.Map<Integer, String> getSkillHotbar() {
        return new java.util.HashMap<>(skillHotbar);
    }
    
    // ========== IDENTITY SYSTEM - PHASE 5 ==========
    
    /**
     * Get player identity (read-only)
     */
    public hcontrol.plugin.identity.PlayerIdentity getIdentity() {
        return identity;
    }
}


